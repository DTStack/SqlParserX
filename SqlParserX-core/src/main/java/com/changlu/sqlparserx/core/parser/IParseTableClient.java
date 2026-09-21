package com.changlu.sqlparserx.core.parser;

import com.changlu.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.sql.ast.SQLStatement;

public interface IParseTableClient {

    default ParseTableResult parseTables(SQLStatement stmt) {
        return null;
    }

    default ParseCreateTableResult parseCreateTable(SQLStatement stmt) {
        return null;
    }

}
