package com.changlu.lineage.core.select.table;

import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.lineage.domain.Table;

import java.util.List;

public class SelectTableSpecialLineageService {

    public List<Table> queryTablesBySqlSelect(SQLSelect sqlSelect) {
        SQLSelectQuery sqlSelectQuery = sqlSelect.getQuery();
        return this.queryTablesBySqlSelectQuery(sqlSelectQuery);
    }

    private List<Table> queryTablesBySqlSelectQuery(SQLSelectQuery sqlSelectQuery) {
//        List<SelectTable> selectTables = new ArrayList<>();
//        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
//            return this.queryTableBySqlSelectQueryBlock((SQLSelectQueryBlock) sqlSelectQuery);
//        }else if (sqlSelectQuery instanceof SQLUnionQuery) {
//            =
//        }
//        return selectColumns;
        return null;
    }

    private List<Table> queryTableBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        if (selectQueryBlock == null) return null;
        // 表来源拆分
        SQLTableSource fromTable = selectQueryBlock.getFrom();
        // 基于from、select选项完成构造表字段记录逻辑
        List<Table> tables = queryFromTable(fromTable);
        return tables;
    }

    private List<Table> queryFromTable(SQLTableSource fromTable) {
//        // 不同场景
//        // 场景1：from 普通表场景（SQLExprTableSource） 如：from table1  或者 from table1 as t1
//        if (fromTable instanceof SQLExprTableSource) {
//            // #feat2：结合select & from 结合转换字段处理
//            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) fromTable;
//            // 真实表名 表别名
//            String sourceTableName = TableSourceUtil.extractTableName((SQLExprTableSource)fromTable);
//            String aliasSourceTableName = sqlExprTableSource.getAlias() == null ? sourceTableName : sqlExprTableSource.getAlias();
//
//        }// 场景2：子查询表（SQLSubqueryTableSource） 如：from (select xxx, xxx from table 1)
//        else if (fromTable instanceof SQLSubqueryTableSource) {
//            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) fromTable;
//            SQLSelect select = sqlSubqueryTableSource.getSelect();
//            // 查询到子查询中的字段集合
//            List<SelectColumn> subquerySelectColumns = this.queryColumnsBySqlSelect(select);
//
//            // selectColumns & subquerySelectColumns 进行match匹配操作
//            selectColumns = matchSubQuerySelectColumns(selectColumns, subquerySelectColumns);
//        } // 场景3：join表 如：from table1 left|inner|right join table2 on table1.id = table2.id
//        else if (fromTable instanceof SQLJoinTableSource){
//            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) fromTable;
//            SQLTableSource left = sqlJoinTableSource.getLeft();
//            SQLTableSource right = sqlJoinTableSource.getRight();
//            // 左表匹配扫描
//            selectColumns = queryFromTableByColumns(left, selectColumns);
//            // 右表匹配扫描
//            selectColumns = queryFromTableByColumns(right, selectColumns);
//        }

        return null;
    }

}
