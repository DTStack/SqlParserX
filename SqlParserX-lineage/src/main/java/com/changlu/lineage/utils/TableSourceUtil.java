package com.changlu.lineage.utils;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.changlu.lineage.domain.Table;

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
    public static String extractTableName(SQLExprTableSource tableSource) {
        return Table.TableTransfer.transfer(extractTable(tableSource));
    }

    /**
     * 获取表对象信息
     *  1、极端场景 select * from ttt.catalog.db.table as tmp
     *  2、全包含场景 select * from catalog.db.table as tmp
     *  3、仅仅有db、table场景 select * from table as tmp
     *  4、仅仅有table场景 select * table as tmp
     * @param tableSource
     * @return
     */
    public static Table extractTable(SQLExprTableSource tableSource) {
        if (tableSource == null) return null;
        // 别名
        String aliasTableName = null;

        // 获取到catalog、db、table
        SQLExpr expr = tableSource.getExpr();
        Table table = extractTable(expr);

        // 初始化别名
        aliasTableName = tableSource.getAlias() == null ? table.getName() : tableSource.getAlias();
        table.setAliasTableName(aliasTableName);
        return table;
    }

    /**
     * 获取到table对象信息，其中别名字段在expr中获取不到
     * @param expr 表达式 无as
     * @return table信息
     */
    public static Table extractTable(SQLExpr expr) {
        String catalog = null;
        String db = null;
        String tableName = null;
        if (expr instanceof SQLIdentifierExpr) {
            tableName = ((SQLIdentifierExpr) expr).getName();
        } else if (expr instanceof SQLPropertyExpr) {
            SQLPropertyExpr hasTableNamePropertyExpr = (SQLPropertyExpr) expr;
            tableName = hasTableNamePropertyExpr.getName();
            SQLExpr hasDbNamePropertyExprOwner = hasTableNamePropertyExpr.getOwner();

            // 尝试去获取db
            if (hasDbNamePropertyExprOwner instanceof SQLIdentifierExpr) {
                db = ((SQLIdentifierExpr) hasDbNamePropertyExprOwner).getName();
            } else if ( hasDbNamePropertyExprOwner instanceof SQLPropertyExpr) {
                db = ((SQLPropertyExpr) hasDbNamePropertyExprOwner).getName();

                // 尝试去获取catalog
                SQLExpr hasCataLogPropertyExprOwner = ((SQLPropertyExpr) hasDbNamePropertyExprOwner).getOwner();
                if (hasCataLogPropertyExprOwner instanceof SQLIdentifierExpr) {
                    catalog = ((SQLIdentifierExpr) hasCataLogPropertyExprOwner).getName();
                }else if (hasCataLogPropertyExprOwner instanceof SQLPropertyExpr) {
                    catalog = ((SQLPropertyExpr) hasCataLogPropertyExprOwner).getName();
                }
            }
        }
        return new Table(catalog, db, tableName, null);
    }


    public static void main(String[] args) {
        String sql = "select * from table as tmp";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.hive);
        SQLStatement stmt = parser.parseStatement();
        SQLSelectStatement selectStatement = (SQLSelectStatement) stmt;
        SQLSelect select = selectStatement.getSelect();
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        SQLTableSource from = queryBlock.getFrom();
        Table table = extractTable((SQLExprTableSource) from);
        System.out.println(table);
    }


}
