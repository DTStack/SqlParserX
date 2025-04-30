package com.changlu.core.parser;

import com.changlu.core.parser.base.lineage.column.Column;

import java.util.List;
import java.util.Map;

public interface ISqlParser {

    IParseTableClient getParseTableClient();

    IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap);

    ITableLineageClient getTableLineageClient();

}
