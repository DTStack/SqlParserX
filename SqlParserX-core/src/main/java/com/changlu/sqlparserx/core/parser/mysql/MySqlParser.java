package com.changlu.sqlparserx.core.parser.mysql;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.IParseTableClient;
import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;
import com.changlu.sqlparserx.core.parser.mysql.table.MysqlParseTablesClient;

/**
 * @description  MySqlParser 提供各类处理器
 * @author changlu
 * @date 2025/4/27 12:07
 */
public class MySqlParser extends BaseSqlParser {

    @Override
    public IParseTableClient getParseTableClient() {
        return new MysqlParseTablesClient();
    }

    @Override
    public DbType getDbType() {
        return DbType.mysql;
    }
}
