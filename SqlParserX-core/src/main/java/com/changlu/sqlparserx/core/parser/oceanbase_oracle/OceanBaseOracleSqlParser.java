package com.changlu.sqlparserx.core.parser.oceanbase_oracle;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;

public class OceanBaseOracleSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.oceanbase_oracle;
    }
}
