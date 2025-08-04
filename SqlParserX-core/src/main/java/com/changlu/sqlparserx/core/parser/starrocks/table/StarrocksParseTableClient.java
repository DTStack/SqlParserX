package com.changlu.sqlparserx.core.parser.starrocks.table;
;
import com.changlu.sqlparserx.core.parser.base.table.BaseParseTableClient;
import com.changlu.sqlparserx.core.parser.base.table.ParseCreateTableClient;

public class StarrocksParseTableClient extends BaseParseTableClient {

    @Override
    protected ParseCreateTableClient getParseCreateTableClient() {
        return new StarrocksParseCreateTableClient();
    }
}
