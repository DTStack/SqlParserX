package com.changlu.parser;

import com.alibaba.druid.DbType;
import com.changlu.parser.common.SqlParserImpl;
import com.changlu.parser.mysql.MySqlParser;

public class SqlParserFactory {

    public static ISqlParser getSqlParser(DbType dbType) {
        if (dbType == DbType.mysql) {
            return new MySqlParser();
        }
        return new SqlParserImpl();
    }

}
