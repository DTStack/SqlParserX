package com.changlu.lineage.core.select;

public class SelectColumnItem {
    // 真实表名
    private String tableName;
    // 真实字段名
    private String columnName;

    public SelectColumnItem() {
    }

    public SelectColumnItem(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return "SelectColumnItem{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}