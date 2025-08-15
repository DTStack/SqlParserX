package com.changlu.sqlparserx.core.datasource.doris;

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

public class DorisColumnLineageTest extends BaseColumnLineageAnalyzerTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.doris);
    private String sqlTxtName = "DorisColumnLineageTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/datasource/doris/";
    }

    @Test
    public void test_01_insert() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("wwz_0122",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "wwz_0111.id", "wwz_0122.id");
        assertRelation(relations, "wwz_0111.name", "wwz_0122.name");
    }

    @Test
    public void test_02_insert_allcolumns() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("wwz_012",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "wwz_011.id", "wwz_012.id");
        assertRelation(relations, "wwz_011.name", "wwz_012.name");
    }

    @Test
    public void test_03_insert_overwrite() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("wwz_012",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
                this.put("wwz_013",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertEquals(4, result.getColumnLineages().size());
        assertRelation(relations, "wwz_011.id", "wwz_012.id");
        assertRelation(relations, "wwz_011.name", "wwz_012.name");
        assertRelation(relations, "wwz_011.name", "wwz_013.name");
        assertRelation(relations, "wwz_011.name", "wwz_013.name");
    }

    @Test
    public void test_04_insert_overwrite() {
        String sql = readSqlByIndex(sqlTxtName, 4);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("t1",
                        Arrays.asList(new Column("id1", 0)
                                , new Column("name1", 1)
                        ));
                this.put("t2",
                        Arrays.asList(new Column("ID", 0)
                                , new Column("NAME", 1)
                        ));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertEquals(2, result.getColumnLineages().size());
        // 目前针对目标表的字段列名以SQL中的字段为准，而不是以表中的字段为准
        assertRelation(relations, "t1.id1", "t2.id");
        assertRelation(relations, "t1.name1", "t2.name");
    }

    @Test
    public void test_05_insert_overwrite() {
        String sql = readSqlByIndex(sqlTxtName, 5);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, new HashMap<>());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(30, relations.size());
        assertEquals(30, result.getColumnLineages().size());
        // 目前针对目标表的字段列名以SQL中的字段为准，而不是以表中的字段为准
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_channel_id", "ods.ods_growth_adjust_activity_channel_hf.id");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_id", "ods.ods_growth_adjust_activity_channel_hf.activity_id");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_name", "dim.dim_growth_activity_activity_info_df.activity_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_status", "dim.dim_growth_activity_activity_info_df.activity_status");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.ch_type", "dim.dim_growth_activity_channel_info_df.ch_type");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.first_ch_name", "dim.dim_growth_activity_channel_info_df.first_ch_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.first_ch_sign", "ods.ods_growth_adjust_activity_channel_hf.first_ch");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.second_ch_name", "dim.dim_growth_activity_channel_info_df.second_ch_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.second_ch_sign", "ods.ods_growth_adjust_activity_channel_hf.second_ch");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.third_ch_name", "dim.dim_growth_activity_channel_info_df.third_ch_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.third_ch_sign", "ods.ods_growth_adjust_activity_channel_hf.third_ch");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.ch", "ods.ods_growth_adjust_activity_channel_hf.ch");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_type", "dim.dim_growth_activity_activity_info_df.activity_type");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_type_name", "dim.dim_growth_activity_activity_info_df.activity_type_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.preview_url", "dim.dim_growth_activity_activity_info_df.preview_url");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_url", "dim.dim_growth_activity_activity_info_df.activity_url");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.business_id", "dim.dim_growth_activity_activity_info_df.business_id");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.business_name", "dim.dim_growth_activity_activity_info_df.business_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.reward_amt", "dim.dim_growth_activity_activity_info_df.reward_amt");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_begin_time", "dim.dim_growth_activity_activity_info_df.activity_begin_time");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.activity_end_time", "dim.dim_growth_activity_activity_info_df.activity_end_time");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.register_begin_time", "dim.dim_growth_activity_activity_info_df.register_begin_time");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.register_end_time", "dim.dim_growth_activity_activity_info_df.register_end_time");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.create_user_id", "dim.dim_growth_activity_activity_info_df.create_user_id");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.create_user_name", "dim.dim_growth_activity_activity_info_df.create_user_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.source_system_id", "dim.dim_growth_activity_activity_info_df.source_system_id");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.source_system_name", "dim.dim_growth_activity_activity_info_df.source_system_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.source_system_create_user_id", "dim.dim_growth_activity_activity_info_df.source_system_create_user_id");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.source_system_create_user_name", "dim.dim_growth_activity_activity_info_df.source_system_create_user_name");
        assertRelation(relations, "dim.dim_growth_activity_activity_channel_df.source_system_business_type", "dim.dim_growth_activity_activity_info_df.source_system_business_type");
    }

    @Test
    public void test_06_insert() {
        String sql = readSqlByIndex(sqlTxtName, 6);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, new HashMap<>());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "new_table.id", "table1.id");
        assertRelation(relations, "new_table.name", "table1.name");
    }

    @Test
    public void test_07_insert() {
        String sql = readSqlByIndex(sqlTxtName, 7);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, new HashMap<>());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(4, relations.size());
        assertRelation(relations, "table1.id", "new_table.id");
        assertRelation(relations, "table1.name", "new_table.name");
        assertRelation(relations, "table2.name", "new_table.name");
        assertRelation(relations, "table2.name", "new_table.name");
    }

}
