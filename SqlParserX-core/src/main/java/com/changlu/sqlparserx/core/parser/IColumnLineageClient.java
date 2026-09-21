package com.changlu.sqlparserx.core.parser;

import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import com.alibaba.druid.sql.ast.SQLStatement;

public interface IColumnLineageClient {

    ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement);

}
