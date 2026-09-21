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
package com.dtstack.sqlparserx.core.base.tables;

import com.dtstack.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.Table;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import com.dtstack.sqlparserx.core.utils.TableSourceUtil;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @description  数据源工具类单测
 * @author changlu
 * @date 2025/4/22 19:08
 */
public class BaseParseTablesTest extends AbstractSqlParserTest {

    private String sqlTxtName = "BaseParseTablesTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/tables/";
    }

    @Test
    public void test_01_parseTables() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        List<String> tables = TableSourceUtil.parseTables(sql, DbType.mysql);
        assertEquals(9, tables.size());
        assertEquals(true, tables.contains("table1"));
        assertEquals(true, tables.contains("table2"));
        assertEquals(true, tables.contains("table3"));
        assertEquals(true, tables.contains("table4"));
        assertEquals(true, tables.contains("table5"));
        assertEquals(true, tables.contains("table6"));
        assertEquals(true, tables.contains("table7"));
        assertEquals(true, tables.contains("table8"));
        assertEquals(true, tables.contains("new_table"));
    }

    @Test
    public void test_02_parseTables() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        List<String> tables = TableSourceUtil.parseTables(sql, DbType.hive);
        assertEquals(3, tables.size());
        assertEquals(true, tables.contains("table1"));
        assertEquals(true, tables.contains("table2"));
        assertEquals(true, tables.contains("new_table"));
    }

    @Test
    public void test_03_parseTables_comment_on_column() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.greenplum);
        SQLStatement stmt = parser.parseStatement();

        ParseTableResult parseTableResult = TableSourceUtil.parseTables(stmt);
        Table mainTable = parseTableResult.getMainTable();
        assertEquals(true, mainTable.getDb().contains("batch_601"));
        assertEquals(true, mainTable.getName().contains("employees2"));
    }

    @Test
    public void test_04_parseTables_select_in() {
        String sql = readSqlByIndex(sqlTxtName, 4);
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLStatement stmt = parser.parseStatement();
        ParseTableResult parseTableResult = TableSourceUtil.parseTables(stmt);
        Table mainTable = parseTableResult.getMainTable();
        Set<String> tableNames = parseTableResult.getAllTableName();
        assertEquals(4, tableNames.size());
        assertEquals(true, mainTable.getName().contains("test_2a"));
        assertEquals(true, tableNames.contains("test_2a"));
        assertEquals(true, tableNames.contains("Customers"));
        assertEquals(true, tableNames.contains("Orders"));
        assertEquals(true, tableNames.contains("OrderItems"));
    }

    @Test
    public void test_04_parseTables_select_in_and() {
        String sql = readSqlByIndex(sqlTxtName, 5);
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLStatement stmt = parser.parseStatement();
        ParseTableResult parseTableResult = TableSourceUtil.parseTables(stmt);
        Table mainTable = parseTableResult.getMainTable();
        Set<String> tableNames = parseTableResult.getAllTableName();
        assertEquals(3, tableNames.size());
        assertEquals(true, tableNames.contains("tz"));
        assertEquals(true, tableNames.contains("tz2"));
        assertEquals(true, tableNames.contains("tz3"));
    }

    @Test
    public void test_05_parseTables_select_subquery_union() {
        String sql = readSqlByIndex(sqlTxtName, 6);
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLStatement stmt = parser.parseStatement();
        ParseTableResult parseTableResult = TableSourceUtil.parseTables(stmt);
        Set<Table> allTables = parseTableResult.getAllTables();
        assertEquals(2, allTables.size());
        assertEquals(true, allTables.contains(new Table(null, null, "table3", null)));
        assertEquals(true, allTables.contains(new Table(null, null, "table2", null)));
    }

    @Test
    public void test_06_parseTables_select_subquery_union() {
        String sql = readSqlByIndex(sqlTxtName, 7);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
        ParseTableResult parseTableResult = sqlParser.parseTables(sql);
        Set<Table> allTables = parseTableResult.getAllTables();
        assertEquals(2, allTables.size());
        assertEquals(true, allTables.contains(new Table(null, "ods", "ods_task_condition_invite_hf", null)));
        assertEquals(true, allTables.contains(new Table(null, "ods", "ods_task_rule_condition_hf", null)));
    }

    @Test
    public void test_07_parseTables_noTable() {
        String sql = readSqlByIndex(sqlTxtName, 8);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
        ParseTableResult parseTableResult = sqlParser.parseTables(sql);
        Set<Table> allTables = parseTableResult.getAllTables();
        assertEquals(0, allTables.size());
    }

    @Test
    public void test_08_parseTables_withas() {
        String sql = readSqlByIndex(sqlTxtName, 9);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
        ParseTableResult parseTableResult = sqlParser.parseTables(sql);
        Set<Table> allTables = parseTableResult.getAllTables();
        assertEquals(2, allTables.size());
        assertEquals(true, allTables.contains(new Table(null, null, "new_t", "new_t")));
        assertEquals(true, allTables.contains(new Table(null, null, "t1", null)));
    }

}
