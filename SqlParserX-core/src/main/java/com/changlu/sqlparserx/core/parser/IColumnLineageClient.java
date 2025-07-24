package com.changlu.sqlparserx.core.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.changlu.sqlparserx.core.enums.SQLParseStrategy;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

public interface IColumnLineageClient {

    ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement);

}
