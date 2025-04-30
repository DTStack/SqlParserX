package com.changlu.core.parser.mysql;

import com.changlu.core.parser.IParseTableClient;
import com.changlu.core.parser.base.BaseSqlParser;
import com.changlu.core.parser.mysql.table.MysqlParseTableClient;

/**
 * @description  MySqlParser 提供各类处理器
 * @author changlu
 * @date 2025/4/27 12:07
 */
public class MySqlParser extends BaseSqlParser {

    @Override
    public IParseTableClient getParseTableClient() {
        return new MysqlParseTableClient();
    }

}
