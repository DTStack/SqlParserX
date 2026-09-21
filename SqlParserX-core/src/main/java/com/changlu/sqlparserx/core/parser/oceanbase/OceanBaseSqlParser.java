package com.changlu.sqlparserx.core.parser.oceanbase;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class OceanBaseSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oceanbase;
    }
}
