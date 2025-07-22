package com.changlu.sqlparserx.core.base.lineage.table;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.common.AbstractSqlParserTest;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableRelation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseTableLineageWithAsInsertTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseTableLineageWithAsInsertTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/lineage/table/";
    }

    /**
     * insert into结构
     */
    @Test
    public void test_01_insert_into_withas() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("new_table", result.getTargetTable());
        assertEquals(8, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("table1"));
        assertTrue(result.getSourceTables().contains("table2"));
        assertTrue(result.getSourceTables().contains("table3"));
        assertTrue(result.getSourceTables().contains("table4"));
        assertTrue(result.getSourceTables().contains("table5"));
        assertTrue(result.getSourceTables().contains("table6"));
        assertTrue(result.getSourceTables().contains("table7"));
        assertTrue(result.getSourceTables().contains("table8"));

        assertEquals(8, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("table1", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table2", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table3", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table4", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table5", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table6", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table7", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table8", "new_table")));
    }

    /**
     * insert into结构 table 全字段 场景 复杂sql场景
     */
    @Test
    public void test_02_insert_into_withas_complex() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("digua_ceshi.result_table", result.getTargetTable());
        assertEquals(5, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("digua_ceshi.demo_table_digua"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.new_table"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.new_table2"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.hierarchy_table"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.departments"));

        assertEquals(5, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.demo_table_digua", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.new_table", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.new_table2", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.hierarchy_table", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.departments", "digua_ceshi.result_table")));
    }

    /**
     * insert into结构 指定插入字段 复杂sql场景
     */
    @Test
    public void test_03_insert_into() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("digua_ceshi.result_table", result.getTargetTable());
        assertEquals(5, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("digua_ceshi.demo_table_digua"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.new_table"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.new_table2"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.hierarchy_table"));
        assertTrue(result.getSourceTables().contains("digua_ceshi.departments"));

        assertEquals(5, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.demo_table_digua", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.new_table", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.new_table2", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.hierarchy_table", "digua_ceshi.result_table")));
        assertTrue(result.getRelations().contains(new TableRelation("digua_ceshi.departments", "digua_ceshi.result_table")));
    }


}
