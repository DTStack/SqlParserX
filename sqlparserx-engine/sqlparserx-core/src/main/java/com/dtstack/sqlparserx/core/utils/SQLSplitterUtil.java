/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.core.utils;

import com.dtstack.sqlparserx.syntax.SqlSyntaxParserFactory;

import com.alibaba.druid.DbType;

import java.util.List;

/**
 * sql切分工具类库
 */
public class SQLSplitterUtil {
    public static void main(String[] args) {
        // 1. 输入包含多条 SQL 的字符串
//        String sql = "CREATE OR REPLACE TRIGGER kaifa_transactions_trg\n" +
//                     "BEFORE INSERT ON kaifa_transactions\n" +
//                     "FOR EACH ROW\n" +
//                     "BEGIN\n" +
//                     "SELECT kaifa_transactions_seq.NEXTVAL\n" +
//                     "INTO :NEW.id\n" +
//                     "FROM dual;\n" +
//                     "END;\n" +
//                     "SELECT id, name FROM student WHERE id = 1 AND name = 'changlu';";
        String sql = "SELECT id, name FROM student WHERE id = 1 AND name = 'changlu';" +
                "CREATE OR REPLACE TRIGGER kaifa_transactions_trg\n" +
                "-- 测试; /\n" +
                "BEFORE INSERT ON kaifa_transactions\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "SELECT kaifa_transactions_seq.NEXTVAL\n" +
                "INTO :NEW.id\n" +
                "FROM dual;\n" +
                "END;\n" +
                "SELECT id, name FROM student WHERE id = 1 AND name = 'changlu';";
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
//        List<String> splitSQLs = splitSQL(sql, DbType.oracle);
        List<String> splitSQLs = SqlSyntaxParserFactory.getParser(DbType.oracle).splitByDruid(sql);
        for (String splitSQL : splitSQLs) {
            System.out.println(splitSQL);
        }
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
        return SqlSyntaxParserFactory.getParser(dbType).split(sql);
    }

    /**
     * 最稳的切割sql，解析语法树，然后转sql
     * @param sql String
     * @param dbType DbType
     * @return List<String>
     */
    public static List<String> splitSQL2(String sql, DbType dbType) {
        return SqlSyntaxParserFactory.getParser(dbType).splitAndFormat(sql);
    }

    /**
     * 基于官方druid提供的切分sql 【有问题例如：oracle的
     * CREATE OR REPLACE FUNCTION add_numbers(
     *     p_num1 IN NUMBER,
     *     p_num2 IN NUMBER
     * ) RETURN NUMBER IS
     * BEGIN
     *     RETURN p_num1 + p_num2;
     * END add_numbers;
     *
     * select * from WWV_FLOW_FILE_OBJECTS$;
     * 】
     * @param sql String
     * @param dbType DbType
     * @return List<String>
     */
    public static List<String> splitSQL3(String sql, DbType dbType) {
        return SqlSyntaxParserFactory.getParser(dbType).splitByDruid(sql);
    }

}
