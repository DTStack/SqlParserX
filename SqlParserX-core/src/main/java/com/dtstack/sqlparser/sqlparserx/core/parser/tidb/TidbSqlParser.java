package com.dtstack.sqlparser.sqlparserx.core.parser.tidb;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class TidbSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.tidb;
    }
}
