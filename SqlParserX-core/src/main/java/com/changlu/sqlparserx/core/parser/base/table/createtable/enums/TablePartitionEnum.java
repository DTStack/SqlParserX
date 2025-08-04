package com.changlu.sqlparserx.core.parser.base.table.createtable.enums;

public enum TablePartitionEnum {

    RANGER(1), DATE_TRUNC(2), LIST(3), NORMAL(4),;

    final int type;

    TablePartitionEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static TablePartitionEnum getByCode(int type) {
        TablePartitionEnum[] values = TablePartitionEnum.values();

        for (TablePartitionEnum value : values) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }
}
