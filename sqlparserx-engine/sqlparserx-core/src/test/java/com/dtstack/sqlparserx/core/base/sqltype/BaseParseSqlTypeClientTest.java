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
package com.dtstack.sqlparserx.core.base.sqltype;

import com.dtstack.sqlparserx.core.enums.SqlType;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.mysql.MySqlParser;
import com.dtstack.sqlparserx.core.parser.oracle.OracleSqlParser;
import com.dtstack.sqlparserx.core.parser.postgresql.PostgresqlSqlParser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * SQL 语句类型解析回归测试。
 *
 * <p>验证基于 Druid AST 的语句分类结果，覆盖查询、DML、DDL、SHOW/DESCRIBE/EXPLAIN、方言特有语句、
 * 文本与 AST 两种入口及未分类语句兜底，并校验 MySQL、Oracle、PostgreSQL 的公共类型和方言差异。
 */
public class BaseParseSqlTypeClientTest {

    private final ISqlParser mysql = new MySqlParser();
    private final ISqlParser oracle = new OracleSqlParser();
    private final ISqlParser postgres = new PostgresqlSqlParser();

    @Test
    public void shouldDetectCommonMysqlTypes() {
        assertEquals(SqlType.QUERY, mysql.parseSqlType("select * from t1"));
        assertEquals(SqlType.QUERY_NO_FROM, mysql.parseSqlType("select 1"));
        assertEquals(SqlType.WITH_QUERY, mysql.parseSqlType("with c as (select 1) select * from c"));
        assertEquals(SqlType.INSERT, mysql.parseSqlType("insert into t1(id) values (1)"));
        assertEquals(SqlType.UPDATE, mysql.parseSqlType("update t1 set name = 'a' where id = 1"));
        assertEquals(SqlType.DELETE, mysql.parseSqlType("delete from t1 where id = 1"));
        assertEquals(SqlType.TRUNCATE, mysql.parseSqlType("truncate table t1"));
    }

    @Test
    public void shouldDetectDdlTypes() {
        assertEquals(SqlType.CREATE, mysql.parseSqlType("create table t1(id int)"));
        assertEquals(SqlType.CREATE_AS, mysql.parseSqlType("create table t2 as select * from t1"));
        assertEquals(SqlType.CREATE_LIKE, mysql.parseSqlType("create table t2 like t1"));
        assertEquals(SqlType.CREATE_TEMP, mysql.parseSqlType("create temporary table t2(id int)"));
        assertEquals(SqlType.DROP, mysql.parseSqlType("drop table t1"));
        assertEquals(SqlType.ALTERTABLE_ADDCOLS, mysql.parseSqlType("alter table t1 add column c int"));
        assertEquals(SqlType.ALTERTABLE_RENAMECOL, mysql.parseSqlType("alter table t1 rename column a to b"));
        assertEquals(SqlType.ALTER, mysql.parseSqlType("alter table t1 engine = innodb"));
    }

    @Test
    public void shouldDetectShowDescribeExplain() {
        assertEquals(SqlType.SHOW_TABLES, mysql.parseSqlType("show tables"));
        assertEquals(SqlType.SHOW_CREATETABLE, mysql.parseSqlType("show create table t1"));
        assertEquals(SqlType.EXPLAIN, mysql.parseSqlType("explain select * from t1"));
        // MySQL 中 desc 等价于 explain
        assertEquals(SqlType.EXPLAIN, mysql.parseSqlType("desc t1"));
        // describe 在 PG / Spark 等方言下为独立描述语句
        assertEquals(SqlType.DESC_TABLE, postgres.parseSqlType("describe t1"));
    }

    @Test
    public void shouldDetectDialectSpecificTypes() {
        assertEquals(SqlType.COMMENT_ON, oracle.parseSqlType("comment on table t1 is 'x'"));
        assertEquals(SqlType.CREATE_AS, oracle.parseSqlType("create view v1 as select * from t1"));
        assertEquals(SqlType.MERGE_INTO,
                postgres.parseSqlType("merge into t1 using t2 on t1.id = t2.id when matched then update set x = 1"));
    }

    @Test
    public void shouldDetectByStatementOverload() {
        assertEquals(SqlType.QUERY,
                mysql.parseSqlType(SQLUtils.parseSingleStatement("select * from t1", DbType.mysql)));
    }

    @Test
    public void shouldFallbackToOtherForUnsupported() {
        assertEquals(SqlType.OTHER, mysql.parseSqlType("grant select on t1 to u1"));
    }
}
