package com.dtstack.sqlparser.sqlparserx.core.select.column;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.dtstack.sqlparser.sqlparserx.core.domain.Pair;
import com.dtstack.sqlparser.sqlparserx.core.enums.ColumnTypeEnum;
import com.dtstack.sqlparser.sqlparserx.core.enums.TableNameMatchStrategy;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparser.sqlparserx.core.config.Constant;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.table.Table;
import com.dtstack.sqlparser.sqlparserx.core.select.table.SelectTable;
import com.dtstack.sqlparser.sqlparserx.core.utils.TableSourceUtil;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SelectColumnSpecialLineageService {

    private Logger log = LoggerFactory.getLogger(SelectColumnSpecialLineageService.class);

    // 真实表&字段关联关系（用于外部查询元数据）
    private final Map<String, List<Column>> tableColumnsMap;
    // 策略选择控制
    private TableNameMatchStrategy tableNameStrategy = TableNameMatchStrategy.CHECK_COLUMN_EXISTS;

    public SelectColumnSpecialLineageService() {
        this.tableColumnsMap = new HashMap<>();
        this.tableNameStrategy = TableNameMatchStrategy.DEFAULT_FROM_TABLE;
    }

    public SelectColumnSpecialLineageService(Map<String, List<Column>> tableColumnsMap) {
        this.tableColumnsMap = tableColumnsMap;
    }

    /**
     * 查询到SQLSelect中所有涉及字段 匹配 真实表所关联的信息
     * @param sqlSelect SQLSelect
     * @return List<SelectColumn> 每一个SelectColumn中包含字段来源表名 & 字段名
     */
    public List<SelectColumn> queryColumnsBySqlSelect(SQLSelect sqlSelect) {
        if (sqlSelect == null) {
            return new ArrayList<>();
        }
        // 表查询
        SQLSelectQuery sqlSelectQuery = sqlSelect.getQuery();
        return queryColumnsBySqlSelectQuery(sqlSelectQuery);
    }

    /**
     * 查询选择字段
     * @param sqlSelectQuery
     * @return
     */
    public List<SelectColumn> queryColumnsBySqlSelectQuery(SQLSelectQuery sqlSelectQuery) {
        List<SelectColumn> selectColumns = new ArrayList<>();
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            return queryColumnsBySqlSelectQueryBlock((SQLSelectQueryBlock) sqlSelectQuery);
        }else if (sqlSelectQuery instanceof SQLUnionQuery) {
            // #feat4：select union 场景特殊化处理含合并逻辑
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            List<SQLSelectQuery> relations = sqlUnionQuery.getRelations();
            if (relations == null || relations.isEmpty()) return selectColumns;
            // 遍历所有union 查询
            List<SelectColumn> preSelectColumnList = queryColumnsBySqlSelectQuery(relations.get(0));
            int firstSelectColumnSize = preSelectColumnList.size();
            for (int i = 1; i < relations.size(); i++) {
                List<SelectColumn> curSelectColumns = queryColumnsBySqlSelectQuery(relations.get(i));
                if (curSelectColumns != null && curSelectColumns.size() != firstSelectColumnSize) {
                    log.error("检测到select union查询匹配的字段不一致，请检查sql写法是否有问题！");
                }
                // 实现合并字段逻辑
                preSelectColumnList = unionMergeSelectColumns(preSelectColumnList, curSelectColumns);
            }
            selectColumns = preSelectColumnList;
        }
        return selectColumns;
    }

    /**
     * 针对union场景，合并n组字段列表，仅仅涉及到真实字段、表名合并，别名字段采用preSelectColumnList中的
     * 示范：
     * 真实字段   别名字段      真实表名
     *   col1      cc1       table1
     *   col2      cc2       table1
     *
     *   col1 		 tt_col3    table4
     *   col2		 tt_col4    table5
     *
     * 合并为：
     *    真实字段         别名字段        真实表名
     *   col1,col1         cc1       table1, table4
     *   col2,col2         cc2       table1, table5
     *
     *  额外处理：针对union all 场景中前后字段数量不一致时，我们依旧支持匹配处理
     *
     * @param preSelectColumnList
     * @param curSelectColumns
     * @return
     */
    public List<SelectColumn> unionMergeSelectColumns(List<SelectColumn> preSelectColumnList, List<SelectColumn> curSelectColumns){
        int minSize = Math.min(preSelectColumnList.size(), curSelectColumns.size());
        int maxSize = Math.max(preSelectColumnList.size(), curSelectColumns.size());
        List<SelectColumn> unionSelectColumns = new ArrayList<>();
        // 遍历前minSize个字段
        for (int i = 0; i < minSize; i++) {
            String tableName = preSelectColumnList.get(i).getTableInfo() + "," + curSelectColumns.get(i).getTableInfo();
            String columnName = preSelectColumnList.get(i).getColumnName() + "," + curSelectColumns.get(i).getColumnName();
            String aliasName = preSelectColumnList.get(i).getAliasName();
            SelectColumn selectColumn = new SelectColumn(tableName, columnName, aliasName);
            // 过滤处理，针对别名相同情况，真实表名 & 真实字段名 相同情况只保留一个
            selectColumn = SelectColumn.filterDuplicateRealColumns(selectColumn);
            unionSelectColumns.add(selectColumn);
        }
        // 处理额外情况：针对union all 场景中前后字段数量不一致时，我们依旧支持匹配处理
        if (maxSize != minSize) {
            // 识别到最多字段的一组
            List<SelectColumn> moreSelectColumns = preSelectColumnList.size() > curSelectColumns.size() ? preSelectColumnList : curSelectColumns;
            // 举例：min=2、max=3，
            // 0     1    2
            // id   name  age
            // id1  name1
            for (int i = minSize; i < maxSize; i++) {
                SelectColumn curSelectColumn = moreSelectColumns.get(i);
                SelectColumn selectColumn = SelectColumn.filterDuplicateRealColumns(curSelectColumn);
                unionSelectColumns.add(selectColumn);
            }
        }
        return unionSelectColumns;
    }


    /**
     * 获取到SQLSelect中所有的字段【不关注别名】
     * @param selectQueryBlock select语句
     * @return 关联到的所有字段(只关注获取到字段来源表名 & 字段名)
     */
    public List<SelectColumn> queryColumnsBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        if (selectQueryBlock == null) return null;
        // 遍历SELECT项并处理
        List<SelectColumn> selectColumns = buildSelectColumnsBySelectItems(selectQueryBlock.getSelectList());

        // 表来源拆分
        SQLTableSource fromTable = selectQueryBlock.getFrom();
        // 基于from、select选项完成构造表字段记录逻辑
        selectColumns = queryFromTableByColumns(fromTable, selectColumns);
        return selectColumns;
    }


    /**
     * select & from表各类场景匹配规则逻辑处理
     * @param fromTable 来源表数据源类型
     * @param selectColumns select字段集合封装
     * @return select字段集合转换封装结果
     */
    public List<SelectColumn> queryFromTableByColumns(SQLTableSource fromTable, List<SelectColumn> selectColumns) {
        // 不同场景
        // 场景1：from 普通表场景（SQLExprTableSource） 如：from table1  或者 from table1 as t1
        if (fromTable instanceof SQLExprTableSource) {
            // #feat2：结合select & from 结合转换字段处理
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) fromTable;
            // 真实表名 表别名
            String sourceTableName = TableSourceUtil.extractTableName((SQLExprTableSource)fromTable);
            String aliasSourceTableName = sqlExprTableSource.getAlias() == null ? sourceTableName : sqlExprTableSource.getAlias();
            // 完成真实表名的替换
            for (SelectColumn selectColumn : selectColumns) {
                // #feat3：基于#feat2的特殊情况（单列多字段场景）
                // 跟随真实字段数量匹配多个tableName场景走
                //                      真实字段       别名字段    真实表名
                // 场景1：                 id          sum        o
                // 场景2:                tt1,tt2       tmp       n,n2
                // 场景3（无表名情况）:     tt2,tt3      tmp2
                String tableInfo = selectColumn.getTableInfo();
                String columnName = selectColumn.getColumnName();
                // -1参数场景："," => ["",""]
                String[] tableNames = tableInfo.split(",", -1);
                String[] columnNames = columnName.split(",", -1);
                // 用于组装tables集合
                List<String> tableNameList = new ArrayList<>();
                for (int i = 0; i < tableNames.length; i++) {
                    String curColumnTableName = tableNames[i];
                    // 1、字段中真实表名为空情况
                    if (curColumnTableName == null || curColumnTableName.isEmpty()) {
                        // 2、若字段中真实表名不存在情况
                        //   可进行开关设置：默认就是from的表名 或者 根据外界（表元数据）传入的表字段来进行判断是否有来匹配
                        // 这里直接默认from的表名
                        // 根据当前选择策略
                        if (tableNameStrategy == TableNameMatchStrategy.DEFAULT_FROM_TABLE) {
                            // 策略1：默认就是from对应的表名
                            tableNameList.add(sourceTableName);
                        }else {
                            // 策略2：根据外界（表元数据）传入的表字段来进行判断是否有来匹配
                            if (Constant.ALL.equals(columnNames[i]) || checkColumnRealExists(columnNames[i], sourceTableName)) {
                                tableNameList.add(sourceTableName);
                            }else {
                                // 无来源表情况
                                tableNameList.add(Constant.NO_RELATION_TABLE);
                            }
                        }
                    }else {
                        // 2、真实表不为空情况
                        // 如果字段的表名与当前from表的别名一致，则设置当前from的真实表名
                        if (curColumnTableName.equals(aliasSourceTableName)) {
                            tableNameList.add(sourceTableName);
                        }else {
                            // 如果不一致，表明当前并非使用的是别名，直接使用当前该字段的表名
                            tableNameList.add(curColumnTableName);
                        }
                    }
                }
                // 替换真实表名
                selectColumn.setTableInfo(String.join(",", tableNameList));
            }

            // 针对参数中 * 场景替换处理
            if (tableNameStrategy == TableNameMatchStrategy.CHECK_COLUMN_EXISTS) {

                // 提前check是否有需要替换操作
                boolean isMatchHasAllColumns = false;
                for (SelectColumn selectColumn : selectColumns) {
                    if (selectColumn.getColumnName().contains(Constant.ALL)) {
                        isMatchHasAllColumns = true;
                        break;
                    }
                }

                // 是否有匹配到元数据表填充字段
                List<Column> metaTableColumns = this.tableColumnsMap.get(sourceTableName);
                if (isMatchHasAllColumns && metaTableColumns != null && !metaTableColumns.isEmpty()) {
                    List<SelectColumn> newBuildSelectColumns = new ArrayList<>();
                    // 真实表替换后 => 针对字段 * 构建逻辑
                    //                      真实字段       别名字段    真实表名
                    // 场景4（*情况）：           *           *       table1       =>  *场景，需要补充多个selectColumn，真实字段与别名字段一致，真实表名复制一份
                    // 场景5（字段中带有*情况）  *,id          tmp      table1, o   =>  一个字段中带有*，在同一个selectColumn扩展字段
                    // 普通from 查询中的带有*字段处理
                    //   举例：select * from (select * from new_table) as t; 内层的*
                    for (SelectColumn selectColumn : selectColumns) {
                        String tableInfo = selectColumn.getTableInfo();
                        String columnName = selectColumn.getColumnName();
                        // 场景4情况
                        if (Constant.ALL.equals(columnName)) {
                            if (sourceTableName.equals(tableInfo)) {
                                // 场景4 *，扩建多个selectColumn
                                // *           *       table1
                                // 转为
                                // name       name     table1
                                // age        age      table1
                                for (Column metaTableColumn : metaTableColumns) {
                                    String metaTableColumnColumnName = metaTableColumn.getColumnName();
                                    newBuildSelectColumns.add(new SelectColumn(sourceTableName, metaTableColumnColumnName, metaTableColumnColumnName));
                                }
                            }else {
                                newBuildSelectColumns.add(selectColumn);
                            }
                        }else {
                            String[] columnNameArr = columnName.split(",", -1);
                            String[] tableNameArr = tableInfo.split(",", -1);
                            List<String> columnNameList = new ArrayList<>();
                            List<String> tableNameList = new ArrayList<>();
                            for (int i = 0; i < columnNameArr.length; i++) {
                                if (Constant.ALL.equals(columnNameArr[i]) && sourceTableName.equals(tableNameArr[i])) {
                                    // 在一个selectColumn中扩展多个情况
                                    // *,id          tmp      table1, o   =>  一个字段中带有*，在同一个selectColumn扩展字段
                                    // 填充
                                    // name,age,id   tmp      table1,table1,o
                                    for (Column metaTableColumn : metaTableColumns) {
                                        columnNameList.add(metaTableColumn.getColumnName());
                                        tableNameList.add(sourceTableName);
                                    }
                                }else{
                                    columnNameList.add(columnNameArr[i]);
                                    tableNameList.add(tableNameArr[i]);
                                }
                            }
                            selectColumn.setColumnName(String.join(",", columnNameList));
                            selectColumn.setTableInfo(String.join(",", tableNameList));
                            newBuildSelectColumns.add(selectColumn);
                        }
                    }
                    selectColumns = newBuildSelectColumns;
                }
            }

        }// 场景2：子查询表（SQLSubqueryTableSource） 如：from (select xxx, xxx from table 1)
        else if (fromTable instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) fromTable;
            SQLSelect select = sqlSubqueryTableSource.getSelect();
            // 查询到子查询中的字段集合
            List<SelectColumn> subquerySelectColumns = queryColumnsBySqlSelect(select);

            // subquerySelectColumns中 外层属性* 切带有子查询情况
            //   举例：select * from (select * from new_table) as t; 外层的* 字段取决于子查询中的所有字段
            selectColumns = dealSubQuerySelectColumnsByAllColumns(selectColumns, subquerySelectColumns);

            // selectColumns & subquerySelectColumns 进行match匹配操作
            selectColumns = matchSubQuerySelectColumns(selectColumns, subquerySelectColumns);
        } // 场景3：join表 如：from table1 left|inner|right join table2 on table1.id = table2.id
        else if (fromTable instanceof SQLJoinTableSource){
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) fromTable;
            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
            // 左表匹配扫描
            selectColumns = queryFromTableByColumns(left, selectColumns);
            // 右表匹配扫描
            selectColumns = queryFromTableByColumns(right, selectColumns);
        }

        return selectColumns;
    }

    /**
     *  场景 select * from (select * from new_table) as t; 外层的* 字段取决于子查询中的所有字段
     *  selectColumns如下：
     *  字段名      别名     真实表名
     *  *           *
     *
     *  subquerySelectColumns如下： 这里必然会拿到*匹配的所有情况，因为在内部select * from xxx 中已经做过一次匹配了，无需再考虑会有*情况（除非是没有提供元数据相应表字段属性）
     *  字段名           别名         真实表名
     *   gh             gh      dwd_jszt_jyjs0504
     *   xm             xm      dwd_jszt_jyjs0504
     *   dqszdwmc   dqszdwmc    dwd_jszt_jyjs0504
     *   zdxsjsjlmc zdxsjsjlmc  dwd_jszt_jyjs0504
     *
     *  效果： * => 构建多个selectColumns，拷贝subquerySelectColumns
     *
     * @param selectColumns 当前select xxx 中的xxx作为一组selectColumns
     * @param subquerySelectColumns from (select xxx from table1) 中的子查询查询字段列表集合
     * @return selectColumns
     */
    private List<SelectColumn> dealSubQuerySelectColumnsByAllColumns(List<SelectColumn> selectColumns, List<SelectColumn> subquerySelectColumns) {
        List<SelectColumn> buildNewSelectColumns = new ArrayList<>();
        for (SelectColumn selectColumn : selectColumns) {
            if (Constant.ALL.equals(selectColumn.getColumnName())) {
                for (SelectColumn subquerySelectColumn : subquerySelectColumns) {
                    buildNewSelectColumns.add(subquerySelectColumn.cloneOwn());
                }
            }else {
                buildNewSelectColumns.add(selectColumn);
            }
        }
        return buildNewSelectColumns;
    }

    /**
     * select id from table1 场景，判断id是否真实存在table1中
     *
     * 兼容场景为：
     *  this.tableColumnsMap中的key可能会出现db.table或者.table场景
     *      若是map中的key为table1    匹配table1 应当可以匹配到
     *      若是map中的key为.table1   匹配table1 应当可以匹配到
     *      若是map中的key为db.table2 匹配table2 应当可以匹配到
     *
     * @param columnName sql中的字段
     * @param sourceTableName 测试验证的表名
     * @return true：存在，false：不存在
     */
    private boolean checkColumnRealExists(String columnName, String sourceTableName) {
        // 先尝试直接匹配 避免全扫描一遍
        if (this.tableColumnsMap.containsKey(sourceTableName)) {
            return checkColumns(this.tableColumnsMap.get(sourceTableName), columnName);
        }

        // 再尝试匹配带数据库前缀的情况
        for (Map.Entry<String, List<Column>> entry : this.tableColumnsMap.entrySet()) {
            String tableKey = entry.getKey();
            // 兼容.table情况，所以这里去匹配后缀
            if (tableKey.endsWith(sourceTableName)) {
                return checkColumns(entry.getValue(), columnName);
            }
        }

        return false;
    }

    // 辅助方法，检查列是否存在
    private boolean checkColumns(List<Column> columns, String columnName) {
        for (Column column : columns) {
            if (column.getColumnName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 场景2：from查询表（SQLSubqueryTableSource）核心匹配逻辑
     * @param selectColumns select字段集合
     * @param subquerySelectColumns 子查询select集合
     * @return
     */
    private List<SelectColumn> matchSubQuerySelectColumns(List<SelectColumn> selectColumns, List<SelectColumn> subquerySelectColumns) {
        if (subquerySelectColumns == null || subquerySelectColumns.isEmpty()) return selectColumns;

        List<SelectColumn> res = new ArrayList<>();

        Map<String, SelectColumn> subQuerySelectMap = subquerySelectColumns.stream()
                .collect(Collectors.toMap(
                        SelectColumn::getAliasName,  // Key mapper
                        column -> column,           // Value mapper
                        (existing, replacement) -> {  // 处理重复key的情况
                            // 这里可以根据业务需求决定保留哪个
                            // 例如记录日志或抛出异常
                            System.out.println("Duplicate aliasName found: " + existing.getAliasName());
                            return existing;  // 默认保留已存在的
                        }
                ));

        // 1、遍历select所有字段尝试去进行匹配
        for (SelectColumn selectColumn : selectColumns) {
            // 初始化一个
            SelectColumn curSelectColumn = new SelectColumn();

            // 别名字段
            String aliasName = selectColumn.getAliasName();
            //2、拿select某条字段中的【真实字段】尝试去匹配子查询全部字段的【别名字段】
            String columnName = selectColumn.getColumnName();

            // 使用真实字段去匹配表中的别名字段
            // 注：其中的真实字段里可能有多条字段场景
            String[] columnNames = columnName.split(",", -1);
            String[] columnTableNames = selectColumn.getTableInfo().split(",", -1);
            List<String> filterColumnNames = new ArrayList<>();
            List<String> filterColumnTableNames = new ArrayList<>();
            for (int i = 0; i < columnNames.length; i++) {
                String curColumnName = columnNames[i];
                String curColumnTableName = columnTableNames[i];
                // core match alias name
                if (subQuerySelectMap.containsKey(curColumnName)) {
                    SelectColumn subQuerySelectColumn = subQuerySelectMap.get(curColumnName);
                    filterColumnNames.add(subQuerySelectColumn.getColumnName());
                    filterColumnTableNames.add(subQuerySelectColumn.getTableInfo());
                }else {
                    filterColumnNames.add(curColumnName);
                    filterColumnTableNames.add(curColumnTableName);
                }
            }

            // 设置字段、table以及别名
            curSelectColumn.setColumnName(String.join(",", filterColumnNames));
            curSelectColumn.setTableInfo(String.join(",", filterColumnTableNames));
            curSelectColumn.setAliasName(aliasName);

            res.add(curSelectColumn);
        }
        return res;
    }

    /**
     * insert into values(xxx, xxx)，其中(xxx, xxx)场景就是一组selectExprList
     * @param sqlExprs sqlExprs
     * @return 查询到一组column字段信息集合
     */
    public List<SelectColumnItem> buildSelectColumnsBySQLExprs(List<SQLExpr> sqlExprs) {
        List<SelectColumnItem> selectColumnItems = new ArrayList<>();
        for (SQLExpr sqlExpr : sqlExprs) {
            SelectColumnItem selectColumnItem = buildSelectColumnItem(sqlExpr);
            selectColumnItems.add(selectColumnItem);
        }
        return selectColumnItems;
    }

    /**
     * select xxx, xxx, xxx 场景，xxx, xxx, xxx作为一组 List<SQLSelectItem>
     * @param selectList selectList
     * @return  List<SelectColumn>（完整带别名情况）
     */
    public List<SelectColumn> buildSelectColumnsBySelectItems(List<SQLSelectItem> selectList) {
        List<SelectColumn> selectColumns = new ArrayList<>();
        for (SQLSelectItem sqlSelectItem : selectList) {
            SelectColumn selectColumn = buildSelectColumnBySelectItem(sqlSelectItem);
            if (selectColumn != null) selectColumns.add(selectColumn);
        }
        return selectColumns;
    }

    // #feat1：筛选select字段场景（带别名情况）
    private SelectColumn buildSelectColumnBySelectItem(SQLSelectItem selectItem) {
        SQLExpr expr = selectItem.getExpr();
        SelectColumn selectColumn = new SelectColumn();
        // 将expr表达式 => SelectColumn（无别名）
        SelectColumnItem selectColumnItem = buildSelectColumnItem(expr);
        if (selectColumn == null) return null;

        String aliasColumnName = selectItem.getAlias();
        String tableInfo = selectColumnItem.getTableInfo();
        String columnName = selectColumnItem.getColumnName();

        // 别名为空情况设置
        if (aliasColumnName == null) {
            // 别名为空情况无法直接设置columnName，保证aliasColumnName在SelectColumn中只有一个
            aliasColumnName = columnName;
            // 举例：select concat(t.id, '_', t.name), t.num + 1 from table，这种情况别名只能够是当前selectItem的字符串情况
//            aliasColumnName = selectItem.toString();
        }

        selectColumn.setColumnName(columnName);
        selectColumn.setTableInfo(tableInfo);
        selectColumn.setAliasName(aliasColumnName);
        return selectColumn;
    }

    /**
     * 单独去获取到select中的一个字段选项 可以是如下场景
     * select
     *   sum(o.id) as sum,
     *   CONCAT(n.tt1, n2.tt2) as tmp => 函数场景 SQLAggregateExpr、SQLMethodInvokeExpr
     *   t5.level + 1  => SQLBinaryOpExpr
     *
     *   真实字段    别名字段    真实表名
     *    id         sum         o
     *  tt1,tt2     tmp        n,n2
     *   level     level        t5
     * @param expr
     * @return
     */
    public SelectColumnItem buildSelectColumnItem(SQLExpr expr) {
        if (expr == null) return null;
        ColumnTypeEnum columnTypeEnum = ColumnTypeEnum.BELONGS_TO_FIELD;
        String columnName = "";
        String tableInfo = "";
        if (expr instanceof SQLIdentifierExpr) {
            // 如：select id 中的id
            columnName = ((SQLIdentifierExpr) expr).getName();
        } else if (expr instanceof SQLPropertyExpr) {
            // 如：select catalog.db.table.id 中的id
            columnName = ((SQLPropertyExpr) expr).getName();
            // 如：select catalog.db.table.id 中的catalog.db.table
            SQLExpr owner = ((SQLPropertyExpr) expr).getOwner();
            tableInfo = Table.TableTransfer.transfer(TableSourceUtil.extractTable(owner));
        }else if (expr instanceof SQLAggregateExpr || expr instanceof SQLMethodInvokeExpr) {
            // SQLAggregateExpr 聚合表达式场景：如sum、count、avg、concat场景。
            // SQLMethodInvokeExpr：now()、concat(id, name)
            SQLMethodInvokeExpr aggregateExpr = (SQLMethodInvokeExpr) expr;
            // 函数中的参数
            List<SQLExpr> arguments = aggregateExpr.getArguments();
            // 如concat函数可能带有多个字段场景 如concat(o.id, '_', t.id)
            if (!arguments.isEmpty()) {
                // 原始写法
//            List<String> columnNameItems = new ArrayList<>();
//            List<String> tableNameItems = new ArrayList<>();
//            for (SQLExpr argumentExpr : arguments) {
//                // 递归逻辑获取
//                SelectColumnItem selectColumnItem = buildSelectColumnItem(argumentExpr);
//                if (selectColumnItem == null) continue;
//                columnNameItems.add(selectColumnItem.getColumnName());
//                tableNameItems.add(selectColumnItem.getTableInfo());
//            }
                // 复用写法
                Pair<String, String> columnNameAndTableInfo = getColumnAndTablePairBySQLExpr(arguments);
                columnName = columnNameAndTableInfo.getFirst();
                tableInfo = columnNameAndTableInfo.getSecond();
            }else {
                // 函数中无参数场景，如now()
                columnName = expr.toString();
                tableInfo = Constant.NO_RELATION_TABLE;
                columnTypeEnum = ColumnTypeEnum.LITERAL_VALUE;
            }
        }else if (expr instanceof SQLBinaryOpExpr) {
            // SELECT t5.level + 1 二元表达式场景
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr)expr;
            // 左右两边表达式情况
            List<SQLExpr> sqlBinaryOpExprList = Arrays.asList(
                    sqlBinaryOpExpr.getLeft(),
                    sqlBinaryOpExpr.getRight()
            );
            Pair<String, String> columnNameAndTableInfo = getColumnAndTablePairBySQLExpr(sqlBinaryOpExprList);
            columnName = columnNameAndTableInfo.getFirst();
            tableInfo = columnNameAndTableInfo.getSecond();
        }else if (expr instanceof SQLAllColumnExpr) {
            // select db.t1.* from xxx，其中db.t1.*就是一个SQLAllColumnExpr
            SQLAllColumnExpr sqlAllColumnExpr = (SQLAllColumnExpr) expr;
            columnName = Constant.ALL;
            tableInfo = Table.TableTransfer.transfer(TableSourceUtil.extractTable(sqlAllColumnExpr.getOwner()));
        } else if (expr instanceof SQLQueryExpr) {
            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) expr;
            // select (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count
            SQLSelect sqlSelect = sqlQueryExpr.getSubQuery();
            List<SelectColumn> selectColumns = this.queryColumnsBySqlSelect(sqlSelect);
            if (!selectColumns.isEmpty()) {
                // 若是匹配到多个字段，则取第一个
//                SelectColumn selectColumn = selectColumns.get(0);
                // 若是匹配到多个字段，则完成合并
                Pair<String, String> columnAndTablePairBySelectColumns = getColumnAndTablePairBySelectColumns(selectColumns);
                columnName = columnAndTablePairBySelectColumns.getFirst();
                tableInfo = columnAndTablePairBySelectColumns.getSecond();
            }
        } else if (expr instanceof SQLCaseExpr){
            /**
             * 示例：
             * CASE
             * 	WHEN salary > 10000 THEN 'High'
             * 	WHEN salary BETWEEN 5000 AND 10000 THEN 'Medium'
             * 	WHEN salary < 5000 THEN 'Low'
             * 	ELSE 'Unknown'
             * END
             */
            SQLCaseExpr sqlCaseExpr = (SQLCaseExpr) expr;
            // when 集合
            List<SQLCaseExpr.Item> items = sqlCaseExpr.getItems();
            List<SQLExpr> sqlExprs = new ArrayList<>();
            // case when xxx else xxx end中的else部分
            if (sqlCaseExpr.getElseExpr() != null) {
                sqlExprs.add(sqlCaseExpr.getElseExpr());
            }
            for (SQLCaseExpr.Item item : items) {
                sqlExprs.add(item.getConditionExpr());
                sqlExprs.add(item.getValueExpr());
            }
            // 走通用方法获取到目标字段集合即可
            Pair<String, String> columnNameAndTableInfo = this.getColumnAndTablePairBySQLExpr(sqlExprs);
            columnName = columnNameAndTableInfo.getFirst();
            tableInfo = columnNameAndTableInfo.getSecond();
        }else if (expr instanceof SQLCastExpr) {
            // 场景：select CAST(ROUND(ATTACHNO) AS char)
            SQLCastExpr sqlCastExpr = (SQLCastExpr) expr;
            Pair<String, String> columnNameAndTableInfo = this.getColumnAndTablePairBySQLExpr(Arrays.asList(sqlCastExpr.getExpr()));
            columnName = columnNameAndTableInfo.getFirst();
            tableInfo = columnNameAndTableInfo.getSecond();
        } else if (expr instanceof SQLInListExpr) {
            // 场景：select sum(case when t.RATINGCHANGE in(0,2) then 1 else 0 end) as tg_num
            //      其中t.RATINGCHANGE in(0,2)为一个SQLInListExpr
            SQLInListExpr sqlInListExpr = (SQLInListExpr) expr;
            List<SQLExpr> sqlExprs = new ArrayList<>();
            sqlExprs.add(sqlInListExpr.getExpr());
            if (sqlInListExpr.getTargetList() != null) {
                for (SQLExpr sqlExpr : sqlInListExpr.getTargetList()) {
                    sqlExprs.add(sqlExpr);
                }
            }
            Pair<String, String> columnNameAndTableInfo = this.getColumnAndTablePairBySQLExpr(sqlExprs);
            columnName = columnNameAndTableInfo.getFirst();
            tableInfo = columnNameAndTableInfo.getSecond();
        } else {
            // 其他场景暂无匹配逻辑
            // 目前如字符串类型expr忽略，如 select 'test' 中的'test'，得到的expr类型为：SQLCharExpr
//            return null;
            // 引入字段匹配table机制之后，可以返回相应的字面量值
            columnName = expr.toString();
            tableInfo = Constant.NO_RELATION_TABLE;
            columnTypeEnum = ColumnTypeEnum.LITERAL_VALUE;
        }
        return new SelectColumnItem(tableInfo, columnName, columnTypeEnum);
    }

    // 优化多个SQLExpr场景需要递归处理场景（通常是聚合函数、表达式情况）
    private Pair<String, String> getColumnAndTablePairBySQLExpr(List<SQLExpr> sqlExprs) {
        Set<String> checkColumnNames = new HashSet<>();

        List<String> columnNameItems = new ArrayList<>();
        List<String> tableNameItems = new ArrayList<>();
        String columnName = "";
        String tableInfo = "";
        for (SQLExpr sqlExpr : sqlExprs) {
            // 递归逻辑获取
            SelectColumnItem selectColumnItem = buildSelectColumnItem(sqlExpr);
            // 若是在当前聚合函数、表达式情况，过滤掉其中的字面量值
            if (selectColumnItem == null || !ColumnTypeEnum.BELONGS_TO_FIELD.getFieldName().equals(selectColumnItem.getColumnType())) continue;
            // 复杂场景情况去除重复字段
            String[] selectColumns = selectColumnItem.getColumnName().split(",", -1);
            String[] selectColumnTables = selectColumnItem.getTableInfo().split(",", -1);
            List<String> filterSelectColumns = new ArrayList<>();
            List<String> filterSelectColumnTables = new ArrayList<>();
            for (int i = 0; i < selectColumns.length; i++) {
                // 去除掉重复字段的情况
                if (checkColumnNames.contains(selectColumns[i])) continue;
                filterSelectColumns.add(selectColumns[i]);
                filterSelectColumnTables.add(selectColumnTables[i]);

                // 用于check 同名的情况
                checkColumnNames.add(selectColumns[i]);
            }
            // 去除之后只有搜集到的元素>0才可被添加
            if (!filterSelectColumns.isEmpty()) {
                columnNameItems.add(String.join(",", filterSelectColumns));
                tableNameItems.add(String.join(",", filterSelectColumnTables));
            }
        }
        columnName = String.join(",", columnNameItems);
        tableInfo = String.join(",", tableNameItems);
        return Pair.of(columnName, tableInfo);  // 返回不可变的 Pair
    }

    /**
     * 多个SelectColumn时，来进行合并为一个SelectColumn的columnName & tableInfo （无需关心别名）
     * 该种场景为：
     *      select (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count
     *      select (SELECT concat(id,name) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count
     *     不可能出现 select (SELECT id, name FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count，若是出现这种sql后续走db也会失败
     * 举例如下：
     *  真实字段   真实表    别名
     *  id        table    id
     *  name      table    name
     * @param selectColumns
     * @return
     */
    private Pair<String, String> getColumnAndTablePairBySelectColumns(List<SelectColumn> selectColumns) {
        List<String> columnNameItems = new ArrayList<>();
        List<String> tableNameItems = new ArrayList<>();
        String columnName = "";
        String tableInfo = "";
        for (SelectColumn selectColumn : selectColumns) {
            columnNameItems.add(selectColumn.getColumnName());
            tableNameItems.add(selectColumn.getTableInfo());
        }
        columnName = String.join(",", columnNameItems);
        tableInfo = String.join(",", tableNameItems);
        return Pair.of(columnName, tableInfo);  // 返回不可变的 Pair
    }

    /**
     * 构建with map
     *    key：with as别名   value：List<SelectColumn>
     * @param with SQLWithSubqueryClause
     * @return Map<String, List<SelectColumn>>
     */
    public Map<String, List<SelectColumn>> buildColumnInfoByTableMap(SQLWithSubqueryClause with) {
        Map<String, List<SelectColumn>> columnInfoByTableMap = new HashMap<>();
        if (with != null) {
            List<SQLWithSubqueryClause.Entry> entries = with.getEntries();
            if (!entries.isEmpty()) {
                // 遍历每一组with as结构
                for (SQLWithSubqueryClause.Entry entry : entries) {
                    // as 的别名
                    String aliasTableName = entry.getAlias();
                    // select 主体过滤得到的字段记录含追溯真实表记录
                    List<SelectColumn> withAsTableSelectColumns = this.queryColumnsBySqlSelect(entry.getSubQuery());
                    List<SelectColumn> filterSelectColumns = new ArrayList<>();
                    // with as 递归CTE场景 去除tableInfo中相对应匹配别名table相对应的字段记录信息
                    for (SelectColumn selectColumn : withAsTableSelectColumns) {
                        List<String> selectColumnNames = new ArrayList<>();
                        List<String> seletTableNames = new ArrayList<>();
                        String[] selectColumnNameArr = selectColumn.getColumnName().split(",");
                        String[] selectTableNameArr = selectColumn.getTableInfo().split(",");
                        for (int i = 0; i < selectColumnNameArr.length; i++) {
                            // 核心过滤掉递归CTE场景的字段依赖别名情况
                            if (aliasTableName.equals(selectTableNameArr[i])) continue;
                            selectColumnNames.add(selectColumnNameArr[i]);
                            seletTableNames.add(selectTableNameArr[i]);
                        }
                        // 若是去除引入别名字段依赖情况后，数组数量为空情况，则跳过（一般不会出现该问题）
                        if (selectColumnNames.isEmpty()) continue;
                        selectColumn.setColumnName(String.join(",", selectColumnNames));
                        selectColumn.setTableInfo(String.join(",", seletTableNames));
                        filterSelectColumns.add(selectColumn);
                    }

                    columnInfoByTableMap.put(aliasTableName, filterSelectColumns);
                }
            }
        }
        return columnInfoByTableMap;
    }

    public Map<String, List<Column>> getTableColumnsMap() {
        return tableColumnsMap;
    }

    public TableNameMatchStrategy getTableNameStrategy() {
        return tableNameStrategy;
    }

    public void setTableNameStrategy(TableNameMatchStrategy tableNameStrategy) {
        this.tableNameStrategy = tableNameStrategy;
    }
}



