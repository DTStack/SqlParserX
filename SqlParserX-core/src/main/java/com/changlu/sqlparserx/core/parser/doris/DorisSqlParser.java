package com.changlu.sqlparserx.core.parser.doris;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.enums.SQLParseStrategy;
import com.changlu.sqlparserx.core.parser.IColumnLineageClient;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;
import com.changlu.sqlparserx.core.parser.base.lineage.column.Column;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageClient;

import java.util.List;
import java.util.Map;

public class DorisSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.doris;
    }

    @Override
    public IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        // doris支持大小写敏感，所以需要针对SQL中的字段 & 元数据表字段忽略大小写
        return new ColumnLineageClient(tableColumnsMap, SQLParseStrategy.COLUMN_IGNORE_CASE);
    }
}
