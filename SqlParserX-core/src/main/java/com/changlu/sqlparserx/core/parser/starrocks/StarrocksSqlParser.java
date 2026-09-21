package com.changlu.sqlparserx.core.parser.starrocks;

import com.changlu.sqlparserx.core.parser.IParseTableClient;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;
import com.changlu.sqlparserx.core.parser.starrocks.table.StarrocksParseTableClient;

import com.alibaba.druid.DbType;

public class StarrocksSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.starrocks;
    }

    @Override
    public IParseTableClient getParseTableClient() {
        return new StarrocksParseTableClient();
    }
}
