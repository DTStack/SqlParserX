package com.changlu.utils;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.changlu.lineage.table.Table;
import com.changlu.visitor.ParseTableVisitor;

import java.util.*;

/**
 * 收集table表名 key
 */
public class TableSourceUtil implements SQLASTVisitor {

    /**
     * 解析真实表
     */
    public static List<String> parseTables(String sql, DbType dbType) {
        ParseTableVisitor parseTableVisitor = new ParseTableVisitor();
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement stmt = parser.parseStatement();
        stmt.accept(parseTableVisitor);
        return new ArrayList<>(parseTableVisitor.getAllTableName());
    }

    /**
     * 解析真实表
     */
    public static ParseTableVisitor.ParseTableResult parseTables(SQLStatement stmt) {
        ParseTableVisitor parseTableVisitor = new ParseTableVisitor();
        stmt.accept(parseTableVisitor);
        return parseTableVisitor.getParseTableResult();
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
        // 针对解析得到的catalog、db、tableName 进行`` or ''去除
        catalog = removeQuotes(catalog);
        db = removeQuotes(db);
        tableName = removeQuotes(tableName);
        return new Table(catalog, db, tableName, null);
    }

    /**
     * 去除字符串中的反引号(`)和单引号(')
     * @param input 输入字符串
     * @return 处理后的字符串，如果输入为null则返回null
     */
    public static String removeQuotes(String input) {
        if (input == null) {
            return null;
        }
        // 去除字符串开头和结尾的反引号或单引号
        return input.replaceAll("`", "").replaceAll("'", "");
    }


}
