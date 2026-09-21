package com.changlu.sqlparserx.core.parser.tidb;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class TidbSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.tidb;
    }
}
