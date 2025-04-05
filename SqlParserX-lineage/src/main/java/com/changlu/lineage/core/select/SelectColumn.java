package com.changlu.lineage.core.select;

import java.util.Objects;

// 通用select 转换的字段追溯信息
public class SelectColumn {

    // 真实表名
    private String tableName;
    // 真实字段名
    private String columnName;
    // 字段别名
    private String aliasName;

    public SelectColumn() {
    }

    public SelectColumn(String tableName, String columnName, String aliasName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.aliasName = aliasName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectColumn that = (SelectColumn) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(columnName, that.columnName) && Objects.equals(aliasName, that.aliasName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, columnName, aliasName);
    }

    @Override
    public String toString() {
        return "SelectColumn{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", aliasName='" + aliasName + '\'' +
                '}';
    }
}
