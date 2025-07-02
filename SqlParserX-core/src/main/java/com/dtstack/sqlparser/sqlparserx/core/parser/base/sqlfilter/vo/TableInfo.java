package com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter.vo;

/**
 * @description  Table信息类
 * @author changlu
 * @date 2025/7/1 23:27
 */
public class TableInfo {
    // 表名（可能为catalog.db.table、db.table、table）
    String tableName;
    String alias;

    public TableInfo(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAlias() {
        return alias;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "tableName='" + tableName + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }
}