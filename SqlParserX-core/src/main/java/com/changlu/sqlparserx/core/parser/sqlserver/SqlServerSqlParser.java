package com.changlu.sqlparserx.core.parser.sqlserver;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class SqlServerSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.sqlserver;
    }

}
