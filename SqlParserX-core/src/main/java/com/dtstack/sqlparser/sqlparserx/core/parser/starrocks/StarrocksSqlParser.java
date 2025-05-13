package com.dtstack.sqlparser.sqlparserx.core.parser.starrocks;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class StarrocksSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.starrocks;
    }
}
