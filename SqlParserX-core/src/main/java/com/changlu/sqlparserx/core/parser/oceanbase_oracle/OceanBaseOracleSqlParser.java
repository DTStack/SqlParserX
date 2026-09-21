package com.changlu.sqlparserx.core.parser.oceanbase_oracle;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

public class OceanBaseOracleSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oceanbase_oracle;
    }
}
