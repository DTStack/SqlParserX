package com.dtstack.sqlparser.sqlparserx.core.parser.trino;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class TrinoSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.trino;
    }
}
