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
package com.dtstack.sqlparserx.core.parser.base.table.domain;

import com.dtstack.sqlparserx.core.parser.base.lineage.table.Table;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description  解析表结果
 * @author changlu
 * @date 2025/4/30 17:16
 */
public class ParseTableResult {
    private Table mainTable;
    private Set<Table> allTables;

    public ParseTableResult() {
    }

    public ParseTableResult(Table mainTable, Set<Table> allTables) {
        this.mainTable = mainTable;
        this.allTables = allTables;;
    }

    public Table getMainTable() {
        return mainTable;
    }

    public void setMainTable(Table mainTable) {
        this.mainTable = mainTable;
    }

    public Set<Table> getAllTables() {
        return allTables;
    }

    public void setAllTables(Set<Table> allTables) {
        this.allTables = allTables;
    }

    // 对外暴露
    public Set<String> getAllTableName() {
        Set<String> tables = this.allTables.stream().map((table -> Table.TableTransfer.transfer(table))).collect(
                Collectors.toSet());
        return tables;
    }

    @Override
    public String toString() {
        return "ParseTableResult{" +
                "mainTable=" + mainTable +
                ", allTables=" + allTables +
                '}';
    }
}