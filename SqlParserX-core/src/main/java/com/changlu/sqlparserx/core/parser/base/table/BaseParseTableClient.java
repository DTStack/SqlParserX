package com.changlu.sqlparserx.core.parser.base.table;

import com.changlu.sqlparserx.core.parser.IParseTableClient;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.sql.ast.SQLStatement;

/**
 * @description  解析表基类client
 *   目前支持支持解析真实表-parseTables
 *   解析create table类型-parseCreateTable
 * @author changlu
 * @date 2025/8/4 11:30
 */
public class BaseParseTableClient implements IParseTableClient {

    @Override
    public ParseCreateTableResult parseCreateTable(SQLStatement stmt) {
        return getParseCreateTableClient().parseCreateTable(stmt);
    }

    @Override
    public ParseTableResult parseTables(SQLStatement stmt) {
        return getParseTableClient().parseTables(stmt);
    }

    protected ParseTablesClient getParseTableClient() {
        return new ParseTablesClient();
    }

    protected ParseCreateTableClient getParseCreateTableClient() {
        return new ParseCreateTableClient();
    }
}
