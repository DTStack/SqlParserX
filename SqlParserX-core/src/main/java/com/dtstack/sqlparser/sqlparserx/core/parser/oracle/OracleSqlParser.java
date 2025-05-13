package com.dtstack.sqlparser.sqlparserx.core.parser.oracle;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class OracleSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oracle;
    }
}
