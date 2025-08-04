package com.changlu.sqlparserx.core.parser.base.table.createtable.domain;

import java.util.List;

public class TablePartitionElement {

    private String name;

    private List<String> values;

    private boolean isMaxvalue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public boolean isMaxvalue() {
        return isMaxvalue;
    }

    public void setMaxvalue(boolean maxvalue) {
        isMaxvalue = maxvalue;
    }
}
