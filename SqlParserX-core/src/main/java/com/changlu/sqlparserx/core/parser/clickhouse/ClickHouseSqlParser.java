package com.changlu.sqlparserx.core.parser.clickhouse;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class ClickHouseSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.clickhouse;
    }
}
