package com.changlu.core.parser;

import com.alibaba.druid.DbType;
import com.changlu.core.parser.base.BaseSqlParser;
import com.changlu.core.parser.mysql.MySqlParser;

/**
 * 可根据不同数据源类型来完成不同组件的使用
 */
public class SqlParserFactory {

    public static ISqlParser getSqlParser(DbType dbType) {
        if (dbType == DbType.mysql) {
            return new MySqlParser();
        }
        return new BaseSqlParser();
    }

}
