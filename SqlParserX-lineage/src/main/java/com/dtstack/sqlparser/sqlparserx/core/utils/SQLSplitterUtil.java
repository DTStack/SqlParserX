package com.dtstack.sqlparser.sqlparserx.core.utils;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * sql切分工具类库
 */
public class SQLSplitterUtil {
    public static void main(String[] args) {
        // 1. 输入包含多条 SQL 的字符串
        String sql = "CREATE OR REPLACE TRIGGER kaifa_transactions_trg\n" +
                     "BEFORE INSERT ON kaifa_transactions\n" +
                     "FOR EACH ROW\n" +
                     "BEGIN\n" +
                     "SELECT kaifa_transactions_seq.NEXTVAL\n" +
                     "INTO :NEW.id\n" +
                     "FROM dual;\n" +
                     "END;\n" +
                     "SELECT id, name FROM student WHERE id = 1 AND name = 'changlu';";
//        String sql = "SELECT id, name FROM student WHERE id = 1 AND name = 'changlu';" +
//                "CREATE OR REPLACE TRIGGER kaifa_transactions_trg\n" +
//                "-- 测试; /\n" +
//                "BEFORE INSERT ON kaifa_transactions\n" +
//                "FOR EACH ROW\n" +
//                "BEGIN\n" +
//                "SELECT kaifa_transactions_seq.NEXTVAL\n" +
//                "INTO :NEW.id\n" +
//                "FROM dual;\n" +
//                "END;\n" +
//                "SELECT id, name FROM student WHERE id = 1 AND name = 'changlu';";
//        String sql = "CREATE OR REPLACE FUNCTION add_numbers(\n" +
//                "    p_num1 IN NUMBER,\n" +
//                "    p_num2 IN NUMBER \n" +
//                ") RETURN NUMBER IS\n" +
//                "BEGIN\n" +
//                "    RETURN p_num1 + p_num2;\n" +
//                "END add_numbers;" +
//                "select * from WWV_FLOW_FILE_OBJECTS$ ;";
//        String sql = "CREATE OR REPLACE FUNCTION add_numbers(\n" +
//                "p_num1 IN NUMBER,\n" +
//                "p_num2 IN NUMBER \n" +
//                ") RETURN NUMBER IS\n" +
//                "BEGIN\n" +
//                "RETURN p_num1 + p_num2;\n" +
//                "END add_numbers;\n" +
//                "\n" +
//                "select * from WWV_FLOW_FILE_OBJECTS$;";

//        System.setProperty("druid_sql_parser_end_token_checking_disabled", "true");

        // 2. 调用切分方法
//        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, DbType.oracle);
//        System.out.println(stmtList);
        List<String> splitSQLs = splitSQL(sql, DbType.oracle);
//
//        // 3. 输出切分结果
//        System.out.println("Splitted SQL Statements:");
//        for (int i = 0; i < splitSQLs.size(); i++) {
//            System.out.println("SQL " + (i + 1) + ":");
//            System.out.println(splitSQLs.get(i));
//            System.out.println();
//        }
    }

    /**
     * 切分 SQL 并保留原始内容
     * 说明：parser.parseStatement() 极其不稳定，有问题
     *
     * @param sql 原始 SQL 字符串
     * @return 切分后的 SQL 列表
     */
    public static List<String> splitSQL(String sql, DbType dbType) {
        List<String> result = new ArrayList<>();

        // 使用 Druid 的 SQL 解析器
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
//        MySqlStatementParser parser = new MySqlStatementParser(sql);
        // 获取 Lexer
        Lexer lexer = parser.getLexer();

        // 遍历 SQL，逐条解析
        int startIndex = 0;
        while (true) {
            // 尝试解析一条 SQL 语句
            SQLStatement stmt = parser.parseStatement();
            if (stmt == null) {
                break; // 如果没有更多 SQL 语句，退出循环
            }
            int backLen = lexer.token().name != null ? lexer.token().name.length() : 0;
            int endIndex = lexer.pos() - backLen; // 当前 SQL 的结束位置

            // 提取当前 SQL 的原始内容
            String originalSQL = sql.substring(startIndex, endIndex).trim();
            result.add(originalSQL);

            startIndex = endIndex;

            // 检查是否到达末尾
            if (lexer.token() == Token.EOF) {
                break;
            }
        }

        return result;
    }

    public static List<String> splitSQL2(String sql, DbType dbType) {
        List<String> result = new ArrayList<>();
        // 使用 Druid 的 SQL 解析器
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);

        List<SQLStatement> statements = parser.parseStatementList();
        statements.forEach(statement -> {
            result.add(statement.toString());
        });
        return result;
    }

}
