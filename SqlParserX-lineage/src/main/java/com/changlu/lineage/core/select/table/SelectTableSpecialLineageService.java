package com.changlu.lineage.core.select.table;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.utils.TableSourceUtil;

import java.util.ArrayList;
import java.util.List;

public class SelectTableSpecialLineageService {

    public List<SelectTable> queryTablesBySqlSelect(SQLSelect sqlSelect) {
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

    private List<SelectTable> queryTableBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
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
    private List<SelectTable> queryTableBySQLSelectItems(List<SQLSelectItem> selectList) {
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

    private List<SelectTable> queryFromTable(SQLTableSource fromTable) {
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
            // 合并为一条表记录
            //      真实表          表别名
            // table3, table4        t34
            List<String> tableGroup = new ArrayList<>();
            for (SelectTable subQuerySelectTable : subQuerySelectTables) {
                tableGroup.add(subQuerySelectTable.getTableInfo());
            }
            tables.add(new SelectTable(String.join(",", tableGroup), aliasTableName));
        } // 场景3：join表 如：from table1 left|inner|right join table2 on table1.id = table2.id
        else if (fromTable instanceof SQLJoinTableSource){
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) fromTable;
            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
//            // 左表匹配扫描
            tables.addAll(queryFromTable(left));
//            // 右表匹配扫描
            tables.addAll(queryFromTable(right));
        }
        return tables;
    }

}
