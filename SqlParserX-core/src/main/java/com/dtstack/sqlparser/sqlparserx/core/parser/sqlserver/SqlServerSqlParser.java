package com.dtstack.sqlparser.sqlparserx.core.parser.sqlserver;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.IColumnLineageClient;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.Column;

import java.util.List;
import java.util.Map;

public class SqlServerSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.sqlserver;
    }

}
