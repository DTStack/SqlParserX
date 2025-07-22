package com.changlu.sqlparserx.core.parser.starrocks;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class StarrocksSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.starrocks;
    }
}
