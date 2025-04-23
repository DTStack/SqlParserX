package com.changlu.lineage.ColumnLineageAnalyzerTest.basic;

import com.alibaba.druid.DbType;
import com.changlu.base.BaseColumnLineageAnalyzerTest;
import com.changlu.lineage.column.Column;
import com.changlu.lineage.column.ColumnLineageAnalyzer;
import com.changlu.lineage.column.ColumnLineageResult;
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
public class InsertSelectTest extends BaseColumnLineageAnalyzerTest {

    private ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
    private String sqlTxtName = "InsertSelectTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/lineage/ColumnLineage/basic/";
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
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, tableColumnsMap);
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
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, tableColumnsMap, DbType.hive);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(4, relations.size());
        assertRelation(relations, "dws_jyjs0504.gh", "dwd_jszt_jyjs0504.gh");
        assertRelation(relations, "dws_jyjs0504.xm", "dwd_jszt_jyjs0504.xm");
        assertRelation(relations, "dws_jyjs0504.dqszdwmc", "dwd_jszt_jyjs0504.dqszdwmc");
        assertRelation(relations, "dws_jyjs0504.zdxsjsjlmc", "dwd_jszt_jyjs0504.zdxsjsjlmc");
    }

}
