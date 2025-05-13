package com.dtstack.sqlparser.sqlparserx.core.parser.oceanbase;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;

public class OceanBaseSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oceanbase;
    }
}
