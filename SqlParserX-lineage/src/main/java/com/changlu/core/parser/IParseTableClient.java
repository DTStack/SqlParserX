package com.changlu.core.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.core.parser.base.table.domain.ParseTableResult;

public interface IParseTableClient {

    ParseTableResult parseTables(SQLStatement stmt);

    void parseSelectTables(SQLSelectStatement sqlSelectStatement);

    void parseUpdateTables(SQLUpdateStatement sqlSelectStatement);

    void parseDeleteTables(SQLDeleteStatement sqlSelectStatement);

    void parseDropTables(SQLDropTableStatement sqlSelectStatement);

    void parseTruncateTables(SQLTruncateStatement sqlSelectStatement);

    void parseCommentTables(SQLCommentStatement sqlSelectStatement);

    void parseCreateTables(SQLCreateTableStatement sqlSelectStatement);

    void parseInsertTables(SQLInsertStatement sqlSelectStatement);

}
