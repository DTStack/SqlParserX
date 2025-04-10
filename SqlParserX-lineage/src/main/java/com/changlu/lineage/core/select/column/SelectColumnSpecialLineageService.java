package com.changlu.lineage.core.select.column;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.lineage.domain.Pair;
import com.changlu.lineage.domain.Table;
import com.changlu.lineage.utils.TableSourceUtil;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.stream.Collectors;

public class SelectColumnSpecialLineageService {

    /**
     * 查询到SQLSelect中所有涉及字段 匹配 真实表所关联的信息
     * @param sqlSelect SQLSelect
     * @return List<SelectColumn> 每一个SelectColumn中包含字段来源表名 & 字段名
     */
    public static List<SelectColumn> queryColumnsBySqlSelect(SQLSelect sqlSelect) {
        // 表查询
        SQLSelectQuery sqlSelectQuery = sqlSelect.getQuery();
        return queryColumnsBySqlSelectQuery(sqlSelectQuery);
    }

    /**
     * 查询选择字段
     * @param sqlSelectQuery
     * @return
     */
    public static List<SelectColumn> queryColumnsBySqlSelectQuery(SQLSelectQuery sqlSelectQuery) {
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
    public static List<SelectColumn> unionMergeSelectColumns(List<SelectColumn> preSelectColumnList, List<SelectColumn> curSelectColumns){
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
    public static List<SelectColumn> queryColumnsBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
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
    public static List<SelectColumn> queryFromTableByColumns(SQLTableSource fromTable, List<SelectColumn> selectColumns) {
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
                //                  真实字段       别名字段    真实表名
                // 场景1：              id          sum        o
                // 场景2:             tt1,tt2       tmp       n,n2
                // 场景3（无表名情况）:  tt2,tt3      tmp2      无表名
                // select item中的真实表名
                String tableInfo = selectColumn.getTableInfo();
                // 1、字段中真实表名存在情况
                if (!"".equals(tableInfo)) {
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

                }else {
                    // 兼容真实表名是否有多个,场景
                    // 真实字段带有,场景，根据，分隔数量来构造表名的数量
                    int num = 1;
                    if (selectColumn.getColumnName().contains(",")) {
                        num = selectColumn.getColumnName().split(",").length;
                    }
                    String[] tableNameArr = new String[num];
                    for (int i = 0; i < num; i++) {
                        // 2、若字段中真实表名不存在情况
                        //   可进行开关设置：默认就是from的表名 或者 根据外界（表元数据）传入的表字段来进行判断是否有来匹配
                        // 这里直接默认from的表名
                        tableNameArr[i] = sourceTableName;
                    }
                    sourceTableName = String.join(",", tableNameArr);
                    selectColumn.setTableInfo(sourceTableName);
                }
            }
        }// 场景2：子查询表（SQLSubqueryTableSource） 如：from (select xxx, xxx from table 1)
        else if (fromTable instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) fromTable;
            SQLSelect select = sqlSubqueryTableSource.getSelect();
            // 查询到子查询中的字段集合
            List<SelectColumn> subquerySelectColumns = queryColumnsBySqlSelect(select);

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
     * 场景2：from查询表（SQLSubqueryTableSource）核心匹配逻辑
     * @param selectColumns select字段集合
     * @param subquerySelectColumns 子查询select集合
     * @return
     */
    private static List<SelectColumn> matchSubQuerySelectColumns(List<SelectColumn> selectColumns, List<SelectColumn> subquerySelectColumns) {
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
     * @return 查询到一组column字段信息集合，包含真实所属表信息字段
     */
    public static List<SelectColumn> buildSelectColumnsBySQLExprs(List<SQLExpr> sqlExprs) {
        List<SelectColumn> selectColumns = new ArrayList<>();
        for (SQLExpr sqlExpr : sqlExprs) {
            SelectColumn selectColumn = buildSelectColumnBySelectExpr(sqlExpr);
            if (selectColumn != null) selectColumns.add(selectColumn);
        }
        return selectColumns;
    }

    /**
     * select xxx, xxx, xxx 场景，xxx, xxx, xxx作为一组 List<SQLSelectItem>
     * @param selectList selectList
     * @return  List<SelectColumn>（完整带别名情况）
     */
    public static List<SelectColumn> buildSelectColumnsBySelectItems(List<SQLSelectItem> selectList) {
        List<SelectColumn> selectColumns = new ArrayList<>();
        for (SQLSelectItem sqlSelectItem : selectList) {
            SelectColumn selectColumn = buildSelectColumnBySelectItem(sqlSelectItem);
            if (selectColumn != null) selectColumns.add(selectColumn);
        }
        return selectColumns;
    }

    // #feat1：筛选select字段场景（带别名情况）
    private static SelectColumn buildSelectColumnBySelectItem(SQLSelectItem selectItem) {
        SQLExpr expr = selectItem.getExpr();
        // 将expr表达式 => SelectColumn（无别名）
        SelectColumn selectColumn = buildSelectColumnBySelectExpr(expr);
        if (selectColumn == null) return null;

        String aliasColumnName = selectItem.getAlias();
        String tableInfo = selectColumn.getTableInfo();
        String columnName = selectColumn.getColumnName();

        // 别名为空情况设置
        if (aliasColumnName == null) {
            aliasColumnName = columnName;
        }

        selectColumn.setColumnName(columnName);
        selectColumn.setTableInfo(tableInfo);
        selectColumn.setAliasName(aliasColumnName);
        return selectColumn;
    }

    /**
     * SQLExpr => SelectColumn (提取字段信息，无别名)
     * @param sqlExpr sqlExpr
     * @return SelectColumn无中无别名
     */
    private static SelectColumn buildSelectColumnBySelectExpr(SQLExpr sqlExpr) {
        // 初始化封装表字段
        SelectColumn selectColumn = new SelectColumn();
        String columnName = "";
        String tableInfo = "";

        // 特定封装处理字段细节函数，包含
        SelectColumnItem selectColumnItem = buildSelectColumnItem(sqlExpr);
        if (selectColumnItem == null) return null;
        columnName = selectColumnItem.getColumnName();
        tableInfo = selectColumnItem.getTableInfo();

        // 初始化字段信息、表信息
        selectColumn.setColumnName(columnName);
        selectColumn.setTableInfo(tableInfo);
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
    private static SelectColumnItem buildSelectColumnItem(SQLExpr expr) {
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
        } else {
            // 其他场景暂无匹配逻辑
            // 目前如字符串类型expr忽略，如 select 'test' 中的'test'，得到的expr类型为：SQLCharExpr
            return null;
        }
        return new SelectColumnItem(tableInfo, columnName);
    }

    // 优化多个SQLExpr场景需要递归处理场景
    private static Pair<String, String> getColumnAndTablePairBySQLExpr(List<SQLExpr> sqlExprs) {
        List<String> columnNameItems = new ArrayList<>();
        List<String> tableNameItems = new ArrayList<>();
        String columnName = "";
        String tableInfo = "";
        for (SQLExpr sqlExpr : sqlExprs) {
            // 递归逻辑获取
            SelectColumnItem selectColumnItem = buildSelectColumnItem(sqlExpr);
            if (selectColumnItem == null) continue;
            columnNameItems.add(selectColumnItem.getColumnName());
            tableNameItems.add(selectColumnItem.getTableInfo());
        }
        columnName = String.join(",", columnNameItems);
        tableInfo = String.join(",", tableNameItems);
        return Pair.of(columnName, tableInfo);  // 返回不可变的 Pair
    }


}



