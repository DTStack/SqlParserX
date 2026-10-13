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
package com.dtstack.sqlparserx.core.parser;

import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;

import com.alibaba.druid.DbType;
import org.junit.Assert;
import org.junit.Test;

public class SqlParserFactoryTest {

    @Test
    public void shouldCreateEveryRegisteredParser() {
        for (SqlParserDataSourceType dataSourceType : SqlParserDataSourceType.values()) {
            ISqlParser parser = SqlParserFactory.getSqlParser(dataSourceType);
            Assert.assertNotNull(parser);
            Assert.assertEquals(dataSourceType.getDbType(), parser.getDbType());
            Assert.assertTrue(SqlParserFactory.supports(dataSourceType));
        }
    }

    @Test
    public void shouldExposeCoreTypesAsSupportedTypes() {
        Assert.assertEquals(SqlParserDataSourceType.values().length,
                SqlParserFactory.getSupportedDataSourceTypes().size());
        Assert.assertEquals(SqlParserDataSourceType.MYSQL,
                SqlParserDataSourceType.fromDbType(DbType.mysql));
        Assert.assertNull(SqlParserDataSourceType.fromDbType(DbType.db2));
    }

    @Test
    public void shouldReturnSameParserInstanceForDataSourceType() {
        ISqlParser first = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);
        ISqlParser second = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);

        Assert.assertSame(first, second);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullDataSourceType() {
        SqlParserFactory.getSqlParser((SqlParserDataSourceType) null);
    }
}
