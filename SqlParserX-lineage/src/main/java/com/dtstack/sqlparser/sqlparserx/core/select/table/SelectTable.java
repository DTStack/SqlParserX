package com.dtstack.sqlparser.sqlparserx.core.select.table;

public class SelectTable {

    // 真实表信息
    private String tableInfo;

    // 别名
    private String aliasName;

    public SelectTable(String tableInfo, String aliasName) {
        this.tableInfo = tableInfo;
        this.aliasName = aliasName;
    }

    public String getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(String tableInfo) {
        this.tableInfo = tableInfo;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }
}
