package com.changlu.sqlparserx.core.parser.spark;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class SparkSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.spark;
    }
}
