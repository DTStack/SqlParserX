package com.changlu.lineage.utils;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * 收集table表名 key
 */
public class TableSourceUtil implements SQLASTVisitor {

    /**
     * 获取到对应sqlStatement 所有的tables记录
     * @param selectQuery selectQuery
     * @return tables  key：alias,value：tableName
     */
    public static Map<String, String> getAllTables(SQLSelectQuery selectQuery) {
        Map<String, String> tables = new HashMap<>();
        selectQuery.accept(new SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLExprTableSource tableSource) {
                String alias = tableSource.getAlias();
                String tableName = extractTableName(tableSource);
                if (alias != null) {
                    tables.put(alias, tableName);
                }else {
                    tables.put(tableName, tableName);
                }
                return true;
            }
        });
        return tables;
    }

    /**
     * 获取表名
     * @param tableSource 表名source
     * @return 表名
     */
    public static String extractTableName(SQLTableSource tableSource) {
        if (tableSource == null) return null;
        if (tableSource instanceof SQLExprTableSource) {
            SQLExpr expr = ((SQLExprTableSource) tableSource).getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                return ((SQLIdentifierExpr) expr).getName();
            } else if (expr instanceof SQLPropertyExpr) {
                return ((SQLPropertyExpr) expr).getName();
            }
        }
        return null;
    }

}
