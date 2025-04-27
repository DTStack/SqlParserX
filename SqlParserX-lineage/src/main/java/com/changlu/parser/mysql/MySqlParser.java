package com.changlu.parser.mysql;

import com.changlu.parser.IParseTableClient;
import com.changlu.parser.common.SqlParserImpl;
import com.changlu.parser.mysql.table.MysqlParseTableClient;

/**
 * @description  MySqlParser 提供各类处理器
 * @author changlu
 * @date 2025/4/27 12:07
 */
public class MySqlParser extends SqlParserImpl {

    @Override
    public IParseTableClient getParseTableClient() {
        return new MysqlParseTableClient();
    }

}
