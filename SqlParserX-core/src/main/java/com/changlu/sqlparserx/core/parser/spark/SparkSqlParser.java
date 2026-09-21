package com.changlu.sqlparserx.core.parser.spark;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class SparkSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.spark;
    }
}
