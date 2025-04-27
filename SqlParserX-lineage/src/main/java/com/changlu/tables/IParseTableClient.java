package com.changlu.tables;

import com.alibaba.druid.sql.ast.statement.*;

public interface IParseTableClient {

    void parseSelectTables(SQLSelectStatement sqlSelectStatement);

    void parseUpdateTables(SQLUpdateStatement sqlSelectStatement);

    void parseDeleteTables(SQLDeleteStatement sqlSelectStatement);

    void parseDropTables(SQLDropTableStatement sqlSelectStatement);

    void parseTruncateTables(SQLTruncateStatement sqlSelectStatement);

    void parseCommentTables(SQLCommentStatement sqlSelectStatement);

    void parseCreateTables(SQLCreateTableStatement sqlSelectStatement);

    void parseInsertTables(SQLInsertStatement sqlSelectStatement);

}
