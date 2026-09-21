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
package com.dtstack.sqlparserx.core.parser.mysql.table;

import com.dtstack.sqlparserx.core.parser.base.lineage.table.Table;
import com.dtstack.sqlparserx.core.parser.base.table.ParseTablesClient;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description  针对mysql数据源提供的解析表client
 * @author changlu
 * @date 2025/4/27 12:01
 */
public class MysqlParseTablesClient extends ParseTablesClient {

    @Override
    public ParseTableResult parseTables(SQLStatement stmt) {
        if (stmt instanceof MySqlRenameTableStatement) {
            this.parseMySqlRenameTable((MySqlRenameTableStatement) stmt);
        }
        return super.parseTables(stmt);
    }

    public void parseMySqlRenameTable(MySqlRenameTableStatement mySqlRenameTableStatement) {
        List<MySqlRenameTableStatement.Item> items = mySqlRenameTableStatement.getItems();
        List<String> tableNames = items.stream().map(item -> item.getName().getSimpleName()).collect(Collectors
                .toList());
        if (!tableNames.isEmpty()) {
            this.mainTable = Table.TableTransfer.parse(tableNames.get(0));
            this.allTables = tableNames.stream().map(Table.TableTransfer::parse).collect(Collectors.toSet());
        }
    }

}
