package com.changlu.sqlparserx.core.parser.oracle;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class OracleSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oracle;
    }
}
