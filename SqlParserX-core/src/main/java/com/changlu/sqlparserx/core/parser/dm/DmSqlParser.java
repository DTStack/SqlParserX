package com.changlu.sqlparserx.core.parser.dm;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class DmSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.dm;
    }
}
