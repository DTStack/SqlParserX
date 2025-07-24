package com.changlu.sqlparserx.core.base.lineage.column;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.common.BaseColumnLineageAnalyzerTest;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.column.Column;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @description  测试INSERT INTO/OVERWRITE SELECT结构
 * @author changlu
 * @date 2025/4/22 11:00
 */
public class BaseColumnLineageInsertSelectTest extends BaseColumnLineageAnalyzerTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseColumnLineageInsertSelectTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/lineage/column/";
    }

    // ************ select * from **************
    /**
     * insert OVERWRITE结构
     */
    @Test
    public void test_01_select_allcolumn_from() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("dwd_jszt_jyjs0504",
                        Arrays.asList(new Column("gh", 0)
                                , new Column("xm", 1)
                                , new Column("dqszdwmc", 2)
                                , new Column("zdxsjsjlmc", 3)
                        ));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(4, relations.size());
        assertRelation(relations, "dws_jyjs0504.gh", "dwd_jszt_jyjs0504.gh");
        assertRelation(relations, "dws_jyjs0504.xm", "dwd_jszt_jyjs0504.xm");
        assertRelation(relations, "dws_jyjs0504.dqszdwmc", "dwd_jszt_jyjs0504.dqszdwmc");
        assertRelation(relations, "dws_jyjs0504.zdxsjsjlmc", "dwd_jszt_jyjs0504.zdxsjsjlmc");
    }

    @Test
    public void test_02_select_allcolumn_from() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("dwd_jszt_jyjs0504",
                        Arrays.asList(new Column("gh", 0)
                                , new Column("xm", 1)
                                , new Column("dqszdwmc", 2)
                                , new Column("zdxsjsjlmc", 3)
                        ));
            }
        };
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.hive);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(4, relations.size());
        assertRelation(relations, "dws_jyjs0504.gh", "dwd_jszt_jyjs0504.gh");
        assertRelation(relations, "dws_jyjs0504.xm", "dwd_jszt_jyjs0504.xm");
        assertRelation(relations, "dws_jyjs0504.dqszdwmc", "dwd_jszt_jyjs0504.dqszdwmc");
        assertRelation(relations, "dws_jyjs0504.zdxsjsjlmc", "dwd_jszt_jyjs0504.zdxsjsjlmc");
    }

    @Test
    public void test_03_insert_select() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("a1",
                        Arrays.asList(new Column("id1", 0)
                                , new Column("name1", 1)
                        ));
                this.put("a2",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
                this.put("a3",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
                this.put("a4",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
            }
        };
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "a1.id1", "a2.id");
        assertRelation(relations, "a1.id1", "a3.id");
        assertRelation(relations, "a1.id1", "a4.id");
        assertRelation(relations, "a1.name1", "a2.name");
        assertRelation(relations, "a1.name1", "a3.name");
        assertRelation(relations, "a1.name1", "a4.name");
    }


}
