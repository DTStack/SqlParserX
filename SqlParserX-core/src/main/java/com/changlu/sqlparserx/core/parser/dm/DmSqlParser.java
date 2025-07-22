package com.changlu.sqlparserx.core.parser.dm;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class DmSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.dm;
    }
}
