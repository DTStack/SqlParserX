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
package com.dtstack.sqlparserx.core.parser.mysql;

import com.dtstack.sqlparserx.core.parser.IParseParameterClient;
import com.dtstack.sqlparserx.core.parser.IParseTableClient;
import com.dtstack.sqlparserx.core.parser.base.BaseSqlParser;
import com.dtstack.sqlparserx.core.parser.mysql.parameter.MysqlParseParameterClient;
import com.dtstack.sqlparserx.core.parser.mysql.table.MysqlParseTablesClient;

import com.alibaba.druid.DbType;

/**
 * @description  MySqlParser 提供各类处理器
 * @author changlu
 * @date 2025/4/27 12:07
 */
public class MySqlParser extends BaseSqlParser {

    @Override
    public IParseParameterClient getParseParameterClient() {
        return new MysqlParseParameterClient();
    }

    @Override
    public IParseTableClient getParseTableClient() {
        return new MysqlParseTablesClient();
    }

    @Override
    public DbType getDbType() {
        return DbType.mysql;
    }
}
