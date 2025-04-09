package com.changlu.lineage.core.table;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.domain.Table;

import java.util.ArrayList;
import java.util.List;

public class TableService {

    public static List<Table> parseTables(String sql, DbType dbType) {
        List<Table> tables = new ArrayList<>();
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement sqlStatement = parser.parseStatement();
        return parseTables(sqlStatement);
    }

    public static List<Table> parseTables(SQLStatement sqlStatement) {
        List<Table> tables = new ArrayList<>();
        sqlStatement.accept(new OracleASTVisitor() {
            @Override
            public boolean visit(SQLExprTableSource tableSource) {
                tables.add(parseTable(tableSource));
                return true;
            }

            @Override
            public boolean visit(OracleSelectTableReference tableSource) {
                tables.add(parseTable(tableSource));
                return true;
            }
        });
        return tables;
    }

    /**
     * 解析SQLExprTableSource为一个表对象
     * @param tableSource SQLExprTableSource
     * @return 表对象
     */
    public static Table parseTable(SQLExprTableSource tableSource) {
        String aliasTableName = tableSource.getAlias();
        // 重写tableSource.getCatalog()
//                String cataLog = tableSource.getCatalog();
        String catalog = getCatalog(tableSource);
        String schema = tableSource.getSchema();
        String tableName = tableSource.getTableName();
        return new Table(catalog, schema, tableName, aliasTableName);
    }

    // 重写情况 SQLExprTableSource#getCatalog
    private static String getCatalog(SQLExprTableSource tableSource) {
        SQLExpr expr = tableSource.getExpr();
        if (expr instanceof SQLPropertyExpr) {
            SQLExpr owner = ((SQLPropertyExpr)expr).getOwner();
            if (owner instanceof SQLPropertyExpr) {
                SQLExpr catalogExpr = ((SQLPropertyExpr)owner).getOwner();
                if (catalogExpr instanceof SQLIdentifierExpr) {
                    return ((SQLIdentifierExpr)catalogExpr).getName();
                }
                // 兼容 ttt.catalog.schema.table 场景 依旧能够读取到倒数第三个catalog
                else if (catalogExpr instanceof SQLPropertyExpr) {
                    return ((SQLPropertyExpr)catalogExpr).getName();
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        List<Table> tables = TableService.parseTables("select * from ttt.catalog.schema.table as tmp", DbType.mysql);
        System.out.println(tables);
    }

}
