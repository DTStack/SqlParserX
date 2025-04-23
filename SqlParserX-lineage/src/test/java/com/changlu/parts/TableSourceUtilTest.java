package com.changlu.parts;

import com.alibaba.druid.DbType;
import com.changlu.base.AbstractSqlParserTest;
import com.changlu.utils.TableSourceUtil;
import org.junit.Test;

/**
 * @description  数据源工具类单测
 * @author changlu
 * @date 2025/4/22 19:08
 */
public class TableSourceUtilTest extends AbstractSqlParserTest {

    private String sqlTxtName = "TableSourceUtilTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/parts/";
    }

    @Test
    public void test_01_parseTables() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        System.out.println(TableSourceUtil.parseTables(sql, DbType.mysql));
    }

    @Test
    public void test_temp_parseTables_01() {
        String sql = readSqlByIndex("TableSourceUtilTestTemp.txt", 1);
        System.out.println(TableSourceUtil.parseTables(sql, DbType.hive));
    }

}
