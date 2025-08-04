package com.changlu.sqlparserx.core.parser.base.table.createtable.enums;

public enum TableKeyEnum {

    // DUPLICATE(1)重复键
    // AGGREGATE(2)聚合键
    // UNIQUE(3)唯一键
    // PRIMARY(4)主键
    DUPLICATE(1),AGGREGATE(2),UNIQUE(3),PRIMARY(4);

    private final Integer code;

    TableKeyEnum(int code) {
        this.code = code;
    }

    public static TableKeyEnum getByCode(int type) {
        TableKeyEnum[] values = TableKeyEnum.values();

        for (TableKeyEnum value : values) {
            if (value.code == type) {
                return value;
            }
        }
        return null;
    }
}
