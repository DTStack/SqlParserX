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
package com.dtstack.sqlparserx.core.parser.base.table;

import com.dtstack.sqlparserx.core.parser.IParseTableClient;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.sql.ast.SQLStatement;

/**
 * @description  解析表基类client
 *   目前支持支持解析真实表-parseTables
 *   解析create table类型-parseCreateTable
 * @author changlu
 * @date 2025/8/4 11:30
 */
public class BaseParseTableClient implements IParseTableClient {

    @Override
    public ParseCreateTableResult parseCreateTable(SQLStatement stmt) {
        return getParseCreateTableClient().parseCreateTable(stmt);
    }

    @Override
    public ParseTableResult parseTables(SQLStatement stmt) {
        return getParseTableClient().parseTables(stmt);
    }

    protected ParseTablesClient getParseTableClient() {
        return new ParseTablesClient();
    }

    protected ParseCreateTableClient getParseCreateTableClient() {
        return new ParseCreateTableClient();
    }
}
