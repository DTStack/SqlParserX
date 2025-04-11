package com.changlu.enums;

public enum TableNameMatchStrategy {
    DEFAULT_FROM_TABLE,      // 策略1：默认使用from表名
    CHECK_COLUMN_EXISTS      // 策略2：根据外界元数据传入表字段来检查列是否存在
}