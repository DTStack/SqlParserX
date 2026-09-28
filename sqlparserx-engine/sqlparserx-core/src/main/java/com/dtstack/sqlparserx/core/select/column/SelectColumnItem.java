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
package com.dtstack.sqlparserx.core.select.column;

import com.dtstack.sqlparserx.core.enums.ColumnTypeEnum;

import java.util.Objects;

public class SelectColumnItem {
    // 真实表信息
    private String tableInfo;
    // 真实字段名
    private String columnName;
    // Column字段类型
    private String columnType;

    public SelectColumnItem() {
        this.columnType = ColumnTypeEnum.BELONGS_TO_FIELD.getFieldName();
    }

    public SelectColumnItem(String tableInfo, String columnName) {
        this.tableInfo = tableInfo;
        this.columnName = columnName;
        this.columnType = ColumnTypeEnum.BELONGS_TO_FIELD.getFieldName();
    }

    public SelectColumnItem(String tableInfo, String columnName, ColumnTypeEnum columnType) {
        this.tableInfo = tableInfo;
        this.columnName = columnName;
        this.columnType = columnType.getFieldName();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(String tableInfo) {
        this.tableInfo = tableInfo;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SelectColumnItem that = (SelectColumnItem) o;
        return Objects.equals(tableInfo, that.tableInfo) && Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableInfo, columnName);
    }

    @Override
    public String toString() {
        return "SelectColumnItem{" +
                "tableInfo='" + tableInfo + '\'' +
                ", columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                '}';
    }
}