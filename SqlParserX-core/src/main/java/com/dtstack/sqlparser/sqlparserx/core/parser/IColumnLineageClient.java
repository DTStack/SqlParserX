package com.dtstack.sqlparser.sqlparserx.core.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

public interface IColumnLineageClient {

    ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement);

    void parseInsertSQL(SQLInsertStatement sqlStatement);

    void parseCreateSQL(SQLCreateTableStatement sqlStatement);

}
