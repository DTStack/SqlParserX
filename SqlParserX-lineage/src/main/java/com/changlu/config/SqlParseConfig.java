package com.changlu.config;

import com.changlu.enums.TableNameMatchStrategy;

// 配置类
public class SqlParseConfig {
    private static TableNameMatchStrategy tableNameStrategy = TableNameMatchStrategy.CHECK_COLUMN_EXISTS;
    
    public static void setTableNameStrategy(TableNameMatchStrategy strategy) {
        tableNameStrategy = strategy;
    }
    
    public static TableNameMatchStrategy getTableNameStrategy() {
        return tableNameStrategy;
    }
}