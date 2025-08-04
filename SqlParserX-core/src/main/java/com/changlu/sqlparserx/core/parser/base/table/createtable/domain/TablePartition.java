package com.changlu.sqlparserx.core.parser.base.table.createtable.domain;

import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TablePartitionEnum;

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
