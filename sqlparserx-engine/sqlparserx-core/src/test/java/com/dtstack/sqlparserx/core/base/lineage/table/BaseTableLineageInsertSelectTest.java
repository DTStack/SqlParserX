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
package com.dtstack.sqlparserx.core.base.lineage.table;

import com.dtstack.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableRelation;

import com.alibaba.druid.DbType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseTableLineageInsertSelectTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseTableLineageInsertSelectTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/lineage/table/";
    }

    /**
     * insert OVERWRITE结构
     */
    @Test
    public void test_01_select_allcolumn_from() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("dws_jyjs0504", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("dwd_jszt_jyjs0504"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("dwd_jszt_jyjs0504", "dws_jyjs0504")));
    }

    @Test
    public void test_02_select_allcolumn_from() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("dws_jyjs0504", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("dwd_jszt_jyjs0504"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("dwd_jszt_jyjs0504", "dws_jyjs0504")));
    }

    /**
     * insert into结构
     */
    @Test
    public void test_03_insert_into() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("dws_jyjs0504", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("dwd_jszt_jyjs0504"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("dwd_jszt_jyjs0504", "dws_jyjs0504")));
    }

}
