package com.changlu.sqlparserx.core.parser.postgresql;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class PostgresqlSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.postgresql;
    }
}
