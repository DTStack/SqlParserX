package com.dtstack.sqlparser.sqlparserx.core.parser.clickhouse;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class ClickHouseSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.clickhouse;
    }
}
