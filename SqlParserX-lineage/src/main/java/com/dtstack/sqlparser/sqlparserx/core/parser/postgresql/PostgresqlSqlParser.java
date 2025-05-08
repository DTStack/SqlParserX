package com.dtstack.sqlparser.sqlparserx.core.parser.postgresql;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class PostgresqlSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.postgresql;
    }
}
