package com.dtstack.sqlparser.sqlparserx.core.parser.mysql;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.parser.IParseTableClient;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.BaseSqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.mysql.table.MysqlParseTableClient;

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

    @Override
    public DbType getDbType() {
        return DbType.mysql;
    }
}
