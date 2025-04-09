package com.changlu.lineage.table;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.changlu.lineage.core.table.TableService;
import com.changlu.lineage.domain.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableLineageVisitor extends SQLASTVisitorAdapter {

    private String targetTable;
    private Set<String> sourceTables = new HashSet<>();
    private Set<TableRelation> relations = new HashSet<>();

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public boolean visit(SQLCreateTableStatement x) {
        targetTable = x.getName().getSimpleName();
        SQLExprTableSource mainTableSource = x.getTableSource();
        // 主表
        Table mainTable = TableService.parseTable(mainTableSource);
        this.targetTable = mainTable.getName();
        // 匹配到所有的表
        List<Table> tables = TableService.parseTables(x);
        tables.remove(mainTable);
        for (Table table : tables) {
            sourceTables.add(table.getName());
        }
        return true;
    }

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public boolean visit(SQLInsertStatement x) {
        SQLExprTableSource mainTableSource = x.getTableSource();
        // 主表
        Table mainTable = TableService.parseTable(mainTableSource);
        this.targetTable = mainTable.getName();

        // check是否是with as结构
        if (x.getWith() != null) {
            SQLWithSubqueryClause with = x.getWith();
            List<SQLWithSubqueryClause.Entry> entries = with.getEntries();
            if (!entries.isEmpty()) {
                // 遍历每一组with as结构
                for (SQLWithSubqueryClause.Entry entry : entries) {
                    // as 的别名
                    String aliasTableName = entry.getAlias();
                }
            }
        }

        // 匹配到所有的表
        List<Table> tables = TableService.parseTables(x);
        tables.remove(mainTable);
        for (Table table : tables) {
            sourceTables.add(table.getName());
        }
        return true;
    }

    /**
     * 获取到血缘解析及结果
     * @return 表血缘结果
     */
    public TableLineageResult getResult() {
        // 初始化血缘解析结果
        if (!sourceTables.isEmpty()) {
            for (String sourceTable : sourceTables) {
                relations.add(new TableRelation(sourceTable, targetTable));
            }
        }
        return new TableLineageResult(targetTable, sourceTables, relations);
    }

}
