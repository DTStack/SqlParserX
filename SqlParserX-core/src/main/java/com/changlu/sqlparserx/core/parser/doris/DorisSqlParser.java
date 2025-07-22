package com.changlu.sqlparserx.core.parser.doris;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class DorisSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.doris;
    }
}
