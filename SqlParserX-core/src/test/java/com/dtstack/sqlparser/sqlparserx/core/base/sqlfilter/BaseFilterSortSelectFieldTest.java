package com.dtstack.sqlparser.sqlparserx.core.base.sqlfilter;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.SqlParserFactory;
import org.junit.Test;

import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

public class BaseFilterSortSelectFieldTest extends AbstractSqlParserTest {

    private String sqlTxtName = "BaseFilterSortSelectFieldTest.txt";
    private ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);

    @Override
    public String getSqlTestPath() {
        return "/core/base/sqlfilter/";
    }

    @Test
    public void test_01() {
//        String sql = readSqlByIndex(sqlTxtName, 1);
//        LinkedHashSet<String> outputParams = new LinkedHashSet<>();
//        outputParams.add("name");
//        outputParams.add("id");
//        String filterSql = sqlParser.filterSortSelectField(sql, outputParams);
//        assertEquals(readSqlByIndex(sqlTxtName, 1) , filterSql);
    }

}
