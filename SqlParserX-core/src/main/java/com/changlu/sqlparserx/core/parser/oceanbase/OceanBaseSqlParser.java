package com.changlu.sqlparserx.core.parser.oceanbase;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class OceanBaseSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oceanbase;
    }
}
