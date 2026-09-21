package com.changlu.sqlparserx.core.parser.greenplum;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class GreenplumSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.greenplum;
    }
}
