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
package com.dtstack.sqlparserx.core.base.function;

import com.dtstack.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;

import com.alibaba.druid.DbType;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * 测试BaseSqlParser中的parseFunction 解析函数
 */
public class BaseParseFunctionTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseParseFunctionTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/function/";
    }

    @Test
    public void test_01() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.trino);
        Set<String> functions = sqlParser.parseFunction(sql);
        assertEquals(3, functions.size());
        assertEquals(true, functions.contains("sum"));
        assertEquals(true, functions.contains("row_number"));
        assertEquals(true, functions.contains("substr"));
    }

}
