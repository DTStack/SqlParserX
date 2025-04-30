package com.changlu.core.parser.base;

import com.changlu.core.parser.IColumnLineageClient;
import com.changlu.core.parser.IParseTableClient;
import com.changlu.core.parser.ISqlParser;
import com.changlu.core.parser.ITableLineageClient;
import com.changlu.core.parser.base.lineage.column.Column;
import com.changlu.core.parser.base.lineage.table.TableLineageClient;
import com.changlu.core.parser.base.table.ParseTableClient;
import com.changlu.core.parser.base.lineage.column.ColumnLineageClient;

import java.util.List;
import java.util.Map;

public class BaseSqlParser implements ISqlParser {

    @Override
    public IParseTableClient getParseTableClient() {
        return new ParseTableClient();
    }

    @Override
    public IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        return new ColumnLineageClient(tableColumnsMap);
    }

    @Override
    public ITableLineageClient getTableLineageClient() {
        return new TableLineageClient();
    }


}
