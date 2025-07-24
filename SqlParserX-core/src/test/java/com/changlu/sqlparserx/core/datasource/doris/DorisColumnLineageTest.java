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

}
