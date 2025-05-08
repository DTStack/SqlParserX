package com.dtstack.sqlparser.sqlparserx.core.base.lineage.table;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.table.TableRelation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseTableLineageCreateTableAsSelectTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseTableLineageCreateTableAsSelectTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/base/lineage/table/";
    }

    /**
     * 普通CREATE TABLE
     */
    @Test
    public void test_01_CreateTable() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("students", result.getTargetTable());
        assertEquals(0, result.getSourceTables().size());
        assertEquals(0, result.getRelations().size());
    }


    /**
     * 表中带有default_catalog.changluya_db.orders
     */
    @Test
    public void test_02_SimpleCreateTableAsSelectByCatalogSchemaTableColumn() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("default_catalog.changluya_db.orders"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("default_catalog.changluya_db.orders", "new_table")));
    }

    /**
     * 测试用例3：CREATE TABLE AS SELECT
     */
    @Test
    public void test_03_SimpleCreateTableByAsSelect() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        TableLineageResult result = sqlParser.parseTableLineages(sql);
        assertEquals("new_table", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("source_table"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("source_table", "new_table")));
    }

    /**
     * 测试用例4-较复杂：CREATE TABLE AS SELECT
     */
    @Test
    public void test_04_ComplexCreateTableAsSelect() {
        String sql = readSqlByIndex(sqlTxtName, 4);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("user_order_summary", result.getTargetTable());
        assertEquals(2, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("users"));
        assertTrue(result.getSourceTables().contains("orders"));

        assertEquals(2, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("users", "user_order_summary")));
        assertTrue(result.getRelations().contains(new TableRelation("orders", "user_order_summary")));
    }

    /**
     * 测试用例5-子查询：CREATE TABLE AS SELECT xxx from (select xxx, xxx from xxx)
     */
    @Test
    public void test_05_createTableAsSelectSubQuery() {
        String sql = readSqlByIndex(sqlTxtName, 5);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("new_table", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("table2"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("table2", "new_table")));
    }

    /**
     * 测试用例6-复合join、union、subquery查询
     */
    @Test
    public void test_06_createTableAsComplexFrom() {
        String sql = readSqlByIndex(sqlTxtName, 6);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("new_table", result.getTargetTable());
        assertEquals(6, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("table1"));
        assertTrue(result.getSourceTables().contains("table3"));
        assertTrue(result.getSourceTables().contains("table4"));
        assertTrue(result.getSourceTables().contains("table5"));
        assertTrue(result.getSourceTables().contains("table6"));
        assertTrue(result.getSourceTables().contains("table7"));

        assertEquals(6, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("table1", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table3", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table4", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table5", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table6", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table7", "new_table")));
    }

}
