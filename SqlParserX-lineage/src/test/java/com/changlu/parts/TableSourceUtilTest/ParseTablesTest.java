package com.changlu.parts.TableSourceUtilTest;

import com.alibaba.druid.DbType;
import com.changlu.base.AbstractSqlParserTest;
import com.changlu.utils.TableSourceUtil;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @description  数据源工具类单测
 * @author changlu
 * @date 2025/4/22 19:08
 */
public class ParseTablesTest extends AbstractSqlParserTest {

    private String sqlTxtName = "ParseTablesTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/parts/TableSourceUtilTest/";
    }

    @Test
    public void test_01_parseTables() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        List<String> tables = TableSourceUtil.parseTables(sql, DbType.mysql);
        assertEquals(9, tables.size());
        assertEquals(true, tables.contains("table1"));
        assertEquals(true, tables.contains("table2"));
        assertEquals(true, tables.contains("table3"));
        assertEquals(true, tables.contains("table4"));
        assertEquals(true, tables.contains("table5"));
        assertEquals(true, tables.contains("table6"));
        assertEquals(true, tables.contains("table7"));
        assertEquals(true, tables.contains("table8"));
        assertEquals(true, tables.contains("new_table"));
    }

    @Test
    public void test_02_parseTables() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        List<String> tables = TableSourceUtil.parseTables(sql, DbType.hive);
        assertEquals(3, tables.size());
        assertEquals(true, tables.contains("table1"));
        assertEquals(true, tables.contains("table2"));
        assertEquals(true, tables.contains("new_table"));
    }

//    @Test
    public void test_temp_parseTables_01() {
        String sql = readSqlByIndex("TableSourceUtilTestTemp.txt", 1);
        System.out.println(TableSourceUtil.parseTables(sql, DbType.hive));
    }

}
