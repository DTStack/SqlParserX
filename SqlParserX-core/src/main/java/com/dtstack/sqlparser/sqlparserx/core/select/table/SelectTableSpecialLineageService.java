package com.dtstack.sqlparser.sqlparserx.core.select.table;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.dtstack.sqlparser.sqlparserx.core.utils.TableSourceUtil;

import java.util.*;
import java.util.stream.Collectors;

public class SelectTableSpecialLineageService {

    public List<SelectTable> queryTablesBySqlSelect(SQLSelect sqlSelect) {
        if (sqlSelect == null) {
            return new ArrayList<>();
        }
        SQLSelectQuery sqlSelectQuery = sqlSelect.getQuery();
        return this.queryTablesBySqlSelectQuery(sqlSelectQuery);
    }

    public List<SelectTable> queryTablesBySqlSelectQuery(SQLSelectQuery sqlSelectQuery) {
        List<SelectTable> selectTables = new ArrayList<>();
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            return this.queryTableBySqlSelectQueryBlock((SQLSelectQueryBlock) sqlSelectQuery);
        }else if (sqlSelectQuery instanceof SQLUnionQuery) {
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            List<SQLSelectQuery> relations = sqlUnionQuery.getRelations();
            if (relations == null || relations.isEmpty()) return selectTables;
            for (int i = 0; i < relations.size(); i++) {
                List<SelectTable> curSelectTables = queryTablesBySqlSelectQuery(relations.get(i));
                selectTables.addAll(curSelectTables);
            }
        }
        return selectTables;
    }

    protected List<SelectTable> queryTableBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        if (selectQueryBlock == null) return null;
        // 从表字段中筛选得到表
        List<SelectTable> tables = this.queryTableBySQLSelectItems(selectQueryBlock.getSelectList());
        // 表来源拆分
        SQLTableSource fromTable = selectQueryBlock.getFrom();
        // 基于from、select选项完成构造表字段记录逻辑
        tables.addAll(queryFromTable(fromTable));
        return tables;
    }

    /**
     * 查询出来一组字段中的表
     */
    protected List<SelectTable> queryTableBySQLSelectItems(List<SQLSelectItem> selectList) {
        List<SelectTable> selectTables = new ArrayList<>();
        for (SQLSelectItem sqlSelectItem : selectList) {
            SQLExpr expr = sqlSelectItem.getExpr();
            selectTables.addAll(this.queryFromTableBySQLExpr(expr));
        }
        return selectTables;
    }

    /**
     * 查询字段表达式中是否带有真实表场景
     */
    private List<SelectTable> queryFromTableBySQLExpr(SQLExpr expr) {
        List<SelectTable> selectTables = new ArrayList<>();
        if (expr instanceof SQLQueryExpr) {
            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) expr;
            // select (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count
            SQLSelect sqlSelect = sqlQueryExpr.getSubQuery();
            selectTables = this.queryTablesBySqlSelect(sqlSelect);
        }
        return selectTables;
    }

    protected List<SelectTable> queryFromTable(SQLTableSource fromTable) {
        List<SelectTable> tables = new ArrayList<>();
        // 不同场景
        // 场景1：from 普通表场景（SQLExprTableSource） 如：from table1  或者 from table1 as t1
        if (fromTable instanceof SQLExprTableSource) {
            // #feat2：结合select & from 结合转换字段处理
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) fromTable;
            // 真实表名 表别名
            String sourceTableName = TableSourceUtil.extractTableName((SQLExprTableSource)fromTable);
            String aliasTableName = sqlExprTableSource.getAlias() == null ? sourceTableName : sqlExprTableSource.getAlias();
            tables.add(new SelectTable(sourceTableName, aliasTableName));
        }// 场景2：子查询表（SQLSubqueryTableSource） 如：from (select xxx, xxx from table 1)
        else if (fromTable instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) fromTable;
            SQLSelect select = sqlSubqueryTableSource.getSelect();
            String aliasTableName = sqlSubqueryTableSource.getAlias();
            // 查询到主体select中的多个表
            // 如：select * from table3 as tt3 left join table4 as tt4 on tt3.id = tt4.id
            // 得到多条表记录
            //    真实表      表别名
            //    table3      tt3
            //    table4      tt4
            List<SelectTable> subQuerySelectTables = this.queryTablesBySqlSelect(select);

            SelectTable selectTable = this.doMergeSubqueryTables(subQuerySelectTables, aliasTableName);
            tables.add(selectTable);
        } // 场景3：join表 如：from table1 left|inner|right join table2 on table1.id = table2.id
        else if (fromTable instanceof SQLJoinTableSource){
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) fromTable;
            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
//            // 左表匹配扫描
            tables.addAll(queryFromTable(left));
//            // 右表匹配扫描
            tables.addAll(queryFromTable(right));
        } // 场景5：子查询+union结构（SQLUnionQueryTableSource） 与场景2基本一致
        else if (fromTable instanceof SQLUnionQueryTableSource) {
            SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) fromTable;
            SQLUnionQuery sqlUnionQuery = sqlUnionQueryTableSource.getUnion();
            String aliasTableName = sqlUnionQueryTableSource.getAlias();

            List<SelectTable> subQuerySelectTables = this.queryTablesBySqlSelectQuery(sqlUnionQuery);

            SelectTable selectTable = this.doMergeSubqueryTables(subQuerySelectTables, aliasTableName);
            tables.add(selectTable);
        }
        return tables;
    }

    /**
     * 将子查询中的一组表 合并 为一个SelectTable
     * @param subQuerySelectTables List<SelectTable>
     * @return SelectTable
     */
    public SelectTable doMergeSubqueryTables(List<SelectTable> subQuerySelectTables, String aliasTableName) {
        // 合并为一条表记录
        //      真实表          表别名
        // table3, table4        t34
        List<String> tableGroup = new ArrayList<>();
        for (SelectTable subQuerySelectTable : subQuerySelectTables) {
            tableGroup.add(subQuerySelectTable.getTableInfo());
        }
        return new SelectTable(String.join(",", tableGroup), aliasTableName);
    }

    /**
     * 构建with map
     *    key：with as别名   value：List<SelectTable>
     * @param with SQLWithSubqueryClause
     * @return Map<String,List<SelectTable>>
     */
    public Map<String, List<SelectTable>> buildTableInfoByTableMap(SQLWithSubqueryClause with) {
        Map<String, List<SelectTable>> tableInfoByTableMap = new HashMap<>();
        if (with != null) {
            List<SQLWithSubqueryClause.Entry> entries = with.getEntries();
            if (!entries.isEmpty()) {
                // 遍历每一组with as结构
                for (SQLWithSubqueryClause.Entry entry : entries) {
                    // as 的别名
                    String aliasTableName = entry.getAlias();
                    SQLSelect withSubquerySQLSelect = entry.getSubQuery();
                    List<SelectTable> withSubqueryTables = this.queryTablesBySqlSelect(withSubquerySQLSelect);
                    tableInfoByTableMap.put(aliasTableName, withSubqueryTables);
                }
            }
        }
        return tableInfoByTableMap;
    }

    /**
     * with as 场景处理：当前只考虑完成selectTables中具有的别名替换
     *  注意：这块逻辑表血缘 与 表解析有一些小区别（表解析应当将with as中的所有表字段全部解析拿到的）
     */
    public List<SelectTable> matchWithAsAndSelectTables(List<SelectTable> selectTables, Map<String, List<SelectTable>> tableInfoByTableMap) {
        /**
         * filter逻辑：针对with as递归cte场景单独处理
         * t5 -> [{tableInfo:"t5, table1"}, {tableInfo:"table2"}]
         *      => 只过滤得到最终为
         *      t5->table1,table2
         */
        Map<String, String> aliasNameBySourceTables = tableInfoByTableMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(SelectTable::getTableInfo) // 获取每个 TableInfo
                                .flatMap(tableInfo -> Arrays.stream(tableInfo.split(",", -1))) // 将每个 TableInfo 按逗号分割为多个 tableName
                                .map(String::trim) // 去除多余的空格
                                .filter(tableName -> !tableName.equals(entry.getKey())) // 过滤掉与 entryKey 相同的 tableName
                                .collect(Collectors.joining(",")) // 将剩余的 tableName 拼接为字符串
                ));

        // 重新构建匹配替换select主体引用的别名表
        List<SelectTable> newSelectTables = new ArrayList<>();
        for (SelectTable selectTable : selectTables) {
            List<String> tableGroup = new ArrayList<>();
            String[] selectTableArr = selectTable.getTableInfo().split(",");
            for (String tableName : selectTableArr) {
                tableGroup.add(aliasNameBySourceTables.getOrDefault(tableName, tableName));
            }
            newSelectTables.add(new SelectTable(String.join(",", tableGroup), selectTable.getAliasName()));
        }
        return newSelectTables;
    }

}
