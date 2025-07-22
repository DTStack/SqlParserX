package com.changlu.sqlparserx.core.parser.oracle;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class OracleSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oracle;
    }
}
