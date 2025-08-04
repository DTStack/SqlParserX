package com.changlu.sqlparserx.core.parser.base.table.createtable.enums;

public enum DistributedByTypeEnum {

    Hash(1), Random(2);

    private final Integer code;

    DistributedByTypeEnum(int code) {
        this.code = code;
    }

    public static DistributedByTypeEnum getByCode(int type) {
        DistributedByTypeEnum[] values = DistributedByTypeEnum.values();

        for (DistributedByTypeEnum value : values) {
            if (value.code == type) {
                return value;
            }
        }
        return null;
    }

}
