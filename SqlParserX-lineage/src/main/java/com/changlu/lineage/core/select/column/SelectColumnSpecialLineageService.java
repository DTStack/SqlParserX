package com.changlu.lineage.core.select.column;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.enums.ColumnTypeEnum;
import com.changlu.enums.TableNameMatchStrategy;
import com.changlu.lineage.column.Column;
import com.changlu.config.Constant;
import com.changlu.domain.Pair;
import com.changlu.lineage.table.Table;
import com.changlu.utils.TableSourceUtil;

import java.util.*;
import java.util.stream.Collectors;

public class SelectColumnSpecialLineageService {

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
                    throw new RuntimeException("select union查询匹配的字段不一致，请检查sql");
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
     * @param preSelectColumnList
     * @param curSelectColumns
     * @return
     */
    public List<SelectColumn> unionMergeSelectColumns(List<SelectColumn> preSelectColumnList, List<SelectColumn> curSelectColumns){
        int size = preSelectColumnList.size();
        List<SelectColumn> unionSelectColumns = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String tableName = preSelectColumnList.get(i).getTableInfo() + "," + curSelectColumns.get(i).getTableInfo();
            String columnName = preSelectColumnList.get(i).getColumnName() + "," + curSelectColumns.get(i).getColumnName();
            String aliasName = preSelectColumnList.get(i).getAliasName();
            SelectColumn selectColumn = new SelectColumn(tableName, columnName, aliasName);
            // 过滤处理，针对别名相同情况，真实表名 & 真实字段名 相同情况只保留一个
            selectColumn = SelectColumn.filterDuplicateRealColumns(selectColumn);
            unionSelectColumns.add(selectColumn);
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
                // 1、字段中真实表名为空情况
                if (tableInfo == null || tableInfo.isEmpty()) {
                    // 真实表为空情况 需要去匹配根据当前表去匹配
                    String[] columnNameArr = columnName.split(",");
                    // 兼容真实表名是否有多个,场景
                    // 真实字段带有,场景，根据，分隔数量来构造表名的数量
                    List<String> tableNameList = new ArrayList<>();
                    for (int i = 0; i < columnNameArr.length; i++) {
                        // 2、若字段中真实表名不存在情况
                        //   可进行开关设置：默认就是from的表名 或者 根据外界（表元数据）传入的表字段来进行判断是否有来匹配
                        // 这里直接默认from的表名
                        // 根据当前选择策略
                        if (tableNameStrategy == TableNameMatchStrategy.DEFAULT_FROM_TABLE) {
                            // 策略1：默认就是from对应的表名
                            tableNameList.add(sourceTableName);
                        }else {
                            // 策略2：根据外界（表元数据）传入的表字段来进行判断是否有来匹配
                            if (Constant.ALL.equals(columnNameArr[i]) || checkColumnRealExists(columnNameArr[i], sourceTableName)) {
                                tableNameList.add(sourceTableName);
                            }else {
                                // 无来源表情况
                                tableNameList.add(Constant.NO_RELATION_TABLE);
                            }
                        }
                    }
                    sourceTableName = String.join(",", tableNameList);
                    selectColumn.setTableInfo(sourceTableName);
                }else {
                    // 2、真实表不为空情况
                    // 兼容真实表名是否有多个,场景
                    if (tableInfo.contains(",")) {
                        String[] tableNameArr = tableInfo.split(",");
                        for (int i = 0; i < tableNameArr.length; i++) {
                            if (tableNameArr[i].equals(aliasSourceTableName)) {
                                tableNameArr[i] = sourceTableName;
                            }
                        }
                        selectColumn.setTableInfo(String.join(",", tableNameArr));
                    }else {
                        // 单独tableName
                        if (tableInfo.equals(aliasSourceTableName)) {
                            selectColumn.setTableInfo(sourceTableName);
                        }
                    }
                }
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
                            String[] columnNameArr = columnName.split(",");
                            String[] tableNameArr = tableInfo.split(",");
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
     * @param columnName sql中的字段
     * @param sourceTableName 测试验证的表名
     * @return true：存在，false：不存在
     */
    private boolean checkColumnRealExists(String columnName, String sourceTableName) {
        if (this.tableColumnsMap.containsKey(sourceTableName)) {
            List<Column> columns = this.tableColumnsMap.get(sourceTableName);
            for (Column column : columns) {
                if (column.getColumnName().equals(columnName)) {
                    return true;
                }
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
            if (subQuerySelectMap.containsKey(columnName)) {
                SelectColumn subQuerySelectColumn = subQuerySelectMap.get(columnName);
                // 保留原始的真实字段、真实表名
                curSelectColumn.setColumnName(subQuerySelectColumn.getColumnName());
                curSelectColumn.setTableInfo(subQuerySelectColumn.getTableInfo());
                // 设置当前select字段中的别名替换替换
                curSelectColumn.setAliasName(aliasName);
            }else {
                curSelectColumn = selectColumn;
            }

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
            // SQLMethodInvokeExpr：如concat函数可能带有多个字段场景 如concat(o.id, '_', t.id)
            SQLMethodInvokeExpr aggregateExpr = (SQLMethodInvokeExpr) expr;
            List<SQLExpr> arguments = aggregateExpr.getArguments();
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
        } else {
            // 其他场景暂无匹配逻辑
            // 目前如字符串类型expr忽略，如 select 'test' 中的'test'，得到的expr类型为：SQLCharExpr
//            return null;
            // 引入字段匹配table机制之后，可以返回相应的字面量值
            columnName = expr.toString();
            return new SelectColumnItem(tableInfo, columnName, ColumnTypeEnum.LITERAL_VALUE);
        }
        return new SelectColumnItem(tableInfo, columnName);
    }

    // 优化多个SQLExpr场景需要递归处理场景（通常是聚合函数、表达式情况）
    private Pair<String, String> getColumnAndTablePairBySQLExpr(List<SQLExpr> sqlExprs) {
        List<String> columnNameItems = new ArrayList<>();
        List<String> tableNameItems = new ArrayList<>();
        String columnName = "";
        String tableInfo = "";
        for (SQLExpr sqlExpr : sqlExprs) {
            // 递归逻辑获取
            SelectColumnItem selectColumnItem = buildSelectColumnItem(sqlExpr);
            // 若是在当前聚合函数、表达式情况，过滤掉其中的字面量值
            if (selectColumnItem == null || !ColumnTypeEnum.BELONGS_TO_FIELD.getFieldName().equals(selectColumnItem.getColumnType()))
                continue;
            columnNameItems.add(selectColumnItem.getColumnName());
            tableNameItems.add(selectColumnItem.getTableInfo());
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



