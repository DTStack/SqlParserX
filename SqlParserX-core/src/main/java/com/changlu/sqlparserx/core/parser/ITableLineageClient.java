package com.changlu.sqlparserx.core.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

public interface ITableLineageClient {

    TableLineageResult parseTableLineages(SQLStatement sqlStatement);

    void parseInsertSQL(SQLInsertStatement sqlStatement);

    void parseCreateSQL(SQLCreateTableStatement sqlStatement);

}
