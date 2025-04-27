package com.changlu.parser.common;

import com.changlu.parser.IParseTableClient;
import com.changlu.parser.ISqlParser;
import com.changlu.parser.common.table.ParseTableClient;

public class SqlParserImpl implements ISqlParser {

    @Override
    public IParseTableClient getParseTableClient() {
        return new ParseTableClient();
    }
}
