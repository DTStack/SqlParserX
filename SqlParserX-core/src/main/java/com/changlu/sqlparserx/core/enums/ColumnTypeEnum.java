package com.changlu.sqlparserx.core.enums;

public enum ColumnTypeEnum {

    // 属于字段（默认）
    BELONGS_TO_FIELD("BELONGS_TO_FIELD"),
    //字面量值
    LITERAL_VALUE("LITERAL_VALUE");

    private final String fieldName;

    // 构造函数
    ColumnTypeEnum(String fieldName) {
        this.fieldName = fieldName;
    }

    // 获取字段名称
    public String getFieldName() {
        return fieldName;
    }
}
