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
package com.dtstack.sqlparserx.core.parser.base.lineage.table;

import java.util.Objects;

public class TableRelation {
    private final String sourceTable;
    private final String targetTable;

    public TableRelation(String sourceTable, String targetTable) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public Table getSourceTableObj() {
        return Table.TableTransfer.parse(this.sourceTable);
    }

    public Table getTargetTableObj() {
        return Table.TableTransfer.parse(this.targetTable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TableRelation that = (TableRelation) o;
        return Objects.equals(sourceTable, that.sourceTable) && Objects.equals(targetTable, that.targetTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTable, targetTable);
    }
}