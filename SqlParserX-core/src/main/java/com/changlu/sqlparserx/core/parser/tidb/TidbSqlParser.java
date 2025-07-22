package com.changlu.sqlparserx.core.parser.tidb;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class TidbSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.tidb;
    }
}
