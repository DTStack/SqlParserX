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
package com.dtstack.sqlparserx.core.datasource.doris;

import com.dtstack.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableRelation;

import com.alibaba.druid.DbType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @description  doris特殊表血缘解析
 * @author changlu
 * @date 2026/1/31 18:59
 */
public class DorisTableLineageTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.doris);
    private String sqlTxtName = "DorisTableLineageTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/datasource/doris/";
    }

    /**
     * 支持CTE_FORWARD_REFERENCE 特性，cte表引用向前表追溯
     */
    @Test
    public void test_1_withas_cte_forward() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        TableLineageResult result = sqlParser.parseTableLineages(sql);
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals(2, tableLineages.size());
        assertTrue(result.getRelations().contains(new TableRelation("demo_table2", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("demo_table", "new_table")));
    }

}
