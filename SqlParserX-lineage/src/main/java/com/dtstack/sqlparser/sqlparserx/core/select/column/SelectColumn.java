package com.dtstack.sqlparser.sqlparserx.core.select.column;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 通用select 转换的字段追溯信息
public class SelectColumn{

    // 真实表信息
    private String tableInfo;
    // 真实字段名 （可能为多个，例如select concat(t.id, '_', t.name) as name => id,name，同时tableInfo也是一一对应 table1,table2）
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

    /**
     * 属于优化逻辑
     * 一般是union场景，在合并之后会出现一个别名字段对应相应的真实表名-真实字段 重复情况，需要过滤掉
     *    真实字段                  真实表名            别名字段
     *   col1,col1,col1    table1,table4,table1        cc1
     *
     *   过滤之后，去除掉相同真实表名 & 真实字段情况
     *
     *    真实字段            真实表名            别名字段
     *   col1,col1       table1,table4           cc1
     *
     * @param selectColumn selectColumn
     * @return SelectColumn
     */
    public static SelectColumn filterDuplicateRealColumns(SelectColumn selectColumn) {
        if (selectColumn == null) {
            return null;
        }

        String[] tableInfos = selectColumn.getTableInfo().split(",", -1);
        String[] columnNames = selectColumn.getColumnName().split(",", -1);
        String aliasName = selectColumn.getAliasName();

        if (tableInfos.length != columnNames.length) {
            throw new IllegalArgumentException("tableInfo 和 columnName 数量不匹配");
        }

        // 使用 Map 去重（key: tableInfo + columnName, value: 是否已存在）
        Map<String, Boolean> seen = new LinkedHashMap<>();
        List<String> filteredTableInfos = new ArrayList<>();
        List<String> filteredColumnNames = new ArrayList<>();

        for (int i = 0; i < tableInfos.length; i++) {
            String key = tableInfos[i] + "|" + columnNames[i];
            if (!seen.containsKey(key)) {
                seen.put(key, true);
                filteredTableInfos.add(tableInfos[i]);
                filteredColumnNames.add(columnNames[i]);
            }
        }

        SelectColumn filtered = new SelectColumn();
        filtered.setTableInfo(String.join(",", filteredTableInfos));
        filtered.setColumnName(String.join(",", filteredColumnNames));
        filtered.setAliasName(aliasName);

        return filtered;
    }

    /**
     * 克隆一份自己全新的对象
     * @return SelectColumn
     */
    public SelectColumn cloneOwn() {
        return new SelectColumn(tableInfo, columnName, aliasName);
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
