package com.changlu.sqlparserx.core.parser.doris;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.enums.SQLParseStrategy;
import com.changlu.sqlparserx.core.parser.IColumnLineageClient;
import com.changlu.sqlparserx.core.parser.IParseTableClient;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;
import com.changlu.sqlparserx.core.parser.base.lineage.column.Column;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageClient;
import com.changlu.sqlparserx.core.parser.doris.table.DorisParseTableClient;

import java.util.List;
import java.util.Map;

public class DorisSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.doris;
    }

    @Override
    public IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        // doris支持大小写敏感
        return new ColumnLineageClient(tableColumnsMap,
                SQLParseStrategy.COLUMN_IGNORE_CASE,  // SQL中的字段 & 元数据表字段忽略大小写
                SQLParseStrategy.SUBQUERY_COLUMN_IGNORE_CASE // 子查询字段忽略大小写匹配
       );
    }

    @Override
    public IParseTableClient getParseTableClient() {
        return new DorisParseTableClient();
    }
}
