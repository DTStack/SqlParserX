package com.changlu.lineage.core.select.column;

// 通用select 转换的字段追溯信息
public class SelectColumn{

    /**
     * 真实表信息
     */
    private String tableInfo;
    // 真实字段名
    private String columnName;
    // 字段别名
    private String aliasName;

    public SelectColumn() {
    }

    public SelectColumn(String tableInfo, String columnName, String aliasName) {
        this.tableInfo = tableInfo;
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

    public String getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(String tableInfo) {
        this.tableInfo = tableInfo;
    }

    @Override
    public String toString() {
        return "SelectColumn{" +
                "tableInfo='" + tableInfo + '\'' +
                ", columnName='" + columnName + '\'' +
                ", aliasName='" + aliasName + '\'' +
                '}';
    }
}
