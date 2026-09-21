package com.changlu.sqlparserx.core.parser.clickhouse;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class ClickHouseSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.clickhouse;
    }
}
