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
package com.dtstack.sqlparserx.core.parser.base.table.createtable.domain;

import com.dtstack.sqlparserx.core.parser.base.table.createtable.enums.TablePartitionEnum;

import java.util.List;

public class TablePartition {

    /**
     * 分区类型
     */
    private TablePartitionEnum tablePartitionEnum;

    /**
     * 分区字段
     */
    private List<String> columnName;

    /**
     * 分区元素： 只有 LIST和 RANG 有
     */
    private List<TablePartitionElement> tablePartitionElement;

    public TablePartitionEnum getTablePartitionEnum() {
        return tablePartitionEnum;
    }

    public void setTablePartitionEnum(TablePartitionEnum tablePartitionEnum) {
        this.tablePartitionEnum = tablePartitionEnum;
    }

    public List<String> getColumnName() {
        return columnName;
    }

    public void setColumnName(List<String> columnName) {
        this.columnName = columnName;
    }

    public List<TablePartitionElement> getTablePartitionElement() {
        return tablePartitionElement;
    }

    public void setTablePartitionElement(List<TablePartitionElement> tablePartitionElement) {
        this.tablePartitionElement = tablePartitionElement;
    }
}
