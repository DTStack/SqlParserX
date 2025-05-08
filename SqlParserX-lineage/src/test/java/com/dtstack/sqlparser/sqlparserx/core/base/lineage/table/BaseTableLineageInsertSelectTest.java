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

public class BaseTableLineageInsertSelectTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseTableLineageInsertSelectTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/base/lineage/table/";
    }

    /**
     * insert OVERWRITE结构
     */
    @Test
    public void test_01_select_allcolumn_from() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("dws_jyjs0504", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("dwd_jszt_jyjs0504"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("dwd_jszt_jyjs0504", "dws_jyjs0504")));
    }

    @Test
    public void test_02_select_allcolumn_from() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("dws_jyjs0504", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("dwd_jszt_jyjs0504"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("dwd_jszt_jyjs0504", "dws_jyjs0504")));
    }

    /**
     * insert into结构
     */
    @Test
    public void test_03_insert_into() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        TableLineageResult result = sqlParser.parseTableLineages(sql);

        assertEquals("dws_jyjs0504", result.getTargetTable());
        assertEquals(1, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("dwd_jszt_jyjs0504"));

        assertEquals(1, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("dwd_jszt_jyjs0504", "dws_jyjs0504")));
    }

}
