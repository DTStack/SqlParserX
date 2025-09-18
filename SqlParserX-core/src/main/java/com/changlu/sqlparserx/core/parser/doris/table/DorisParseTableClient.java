package com.changlu.sqlparserx.core.parser.doris.table;

import com.changlu.sqlparserx.core.parser.base.table.ParseCreateTableClient;
import com.changlu.sqlparserx.core.parser.starrocks.table.StarrocksParseTableClient;

// 继承 StarrocksParseTableClient 以复用其逻辑
public class DorisParseTableClient extends StarrocksParseTableClient {

    @Override
    protected ParseCreateTableClient getParseCreateTableClient() {
        return new DorisParseCreateTableClient();
    }

}
