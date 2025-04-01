package com.changlu.lineage.table;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import java.util.HashSet;
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
        // 处理create table as select
        if (x.getSelect() != null) {
            // visitor模式放下该select 所有的tables
            x.getSelect().accept(new SQLASTVisitorAdapter() {
                @Override
                public boolean visit(SQLExprTableSource tableSource) {
                    sourceTables.add(extractTableName(tableSource));
                    return true;
                }
            });
        }
        return true;
    }

    // 提取表名（处理带schema和别名的情况）
    private String extractTableName(SQLExprTableSource tableSource) {
        // 针对有别名场景，返回当前的表名
//        if (tableSource.getAlias() != null) {
//            return tableSource.getTableName();
//        }
        // 下面就是重写tableSource.getTableName()的逻辑实现
        SQLExpr expr = tableSource.getExpr();
        if (expr instanceof SQLIdentifierExpr) { // 单独一个字面量情况 select * from table，其中table就是SQLIdentifierExpr
            return ((SQLIdentifierExpr) expr).getName();
        } else if (expr instanceof SQLPropertyExpr) { // 属性情况 select * from table.column，其中table.column就是SQLPropertyExpr， owner就是table，name为column
            SQLPropertyExpr propExpr = (SQLPropertyExpr) expr;
            return propExpr.getOwner() + "." + propExpr.getName();
        }
        return tableSource.toString();
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
