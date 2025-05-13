package com.dtstack.sqlparser.sqlparserx.core.parser.hive;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.IColumnLineageClient;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.Column;

import java.util.List;
import java.util.Map;

public class HiveSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.hive;
    }

    @Override
    public IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        return new HiveColumnLineageClient();
    }
}
