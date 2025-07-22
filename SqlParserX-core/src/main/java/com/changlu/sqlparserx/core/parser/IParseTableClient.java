package com.changlu.sqlparserx.core.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;

public interface IParseTableClient {

    ParseTableResult parseTables(SQLStatement stmt);
}
