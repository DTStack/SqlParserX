package com.changlu.sqlparserx.core.parser.greenplum;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class GreenplumSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.greenplum;
    }
}
