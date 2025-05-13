package com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column;

public class Column {

    private String columnName;
    private Integer index;

    public Column(String columnName, Integer index) {
        this.columnName = columnName;
        this.index = index;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Column{" +
                "columnName='" + columnName + '\'' +
                ", index=" + index +
                '}';
    }
}
