package com.changlu.sqlparserx.core.parser.base.table.createtable.domain;

import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TableKeyEnum;

import java.util.List;

public class TableKey {

    private TableKeyEnum tableKeyEnum;

    private List<String> columnName;

    public TableKeyEnum getTableKeyEnum() {
        return tableKeyEnum;
    }

    public void setTableKeyEnum(TableKeyEnum tableKeyEnum) {
        this.tableKeyEnum = tableKeyEnum;
    }

    public List<String> getColumnName() {
        return columnName;
    }

    public void setColumnName(List<String> columnName) {
        this.columnName = columnName;
    }
}
