package com.changlu.lineage.core.select.column;

import com.changlu.lineage.domain.Table;

import java.util.Objects;

public class SelectColumnItem {
    // 真实表信息
    private String tableInfo;
    // 真实字段名
    private String columnName;

    public SelectColumnItem() {
    }

    public SelectColumnItem(String tableInfo, String columnName) {
        this.tableInfo = tableInfo;
        this.columnName = columnName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
                '}';
    }
}