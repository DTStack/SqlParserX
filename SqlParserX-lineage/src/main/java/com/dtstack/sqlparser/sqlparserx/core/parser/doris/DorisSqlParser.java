package com.dtstack.sqlparser.sqlparserx.core.parser.doris;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class DorisSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.doris;
    }
}
