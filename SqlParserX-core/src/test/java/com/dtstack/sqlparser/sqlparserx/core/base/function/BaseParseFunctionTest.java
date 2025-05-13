package com.dtstack.sqlparser.sqlparserx.core.base.function;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.SqlParserFactory;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * 测试BaseSqlParser中的parseFunction 解析函数
 */
public class BaseParseFunctionTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseParseFunctionTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/base/function/";
    }

    @Test
    public void test_01() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.trino);
        Set<String> functions = sqlParser.parseFunction(sql);
        assertEquals(3, functions.size());
        assertEquals(true, functions.contains("sum"));
        assertEquals(true, functions.contains("row_number"));
        assertEquals(true, functions.contains("substr"));
    }

}
