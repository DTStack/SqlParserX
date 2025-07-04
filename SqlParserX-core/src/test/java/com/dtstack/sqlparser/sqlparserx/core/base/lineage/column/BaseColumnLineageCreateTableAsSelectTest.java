package com.dtstack.sqlparser.sqlparserx.core.base.lineage.column;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.common.BaseColumnLineageAnalyzerTest;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.domain.ColumnLineage;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @description 测试CREATE TABLE AS SELECT结构
 * @author changlu
 * @date 2025/4/22 11:00
 */
public class BaseColumnLineageCreateTableAsSelectTest extends BaseColumnLineageAnalyzerTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseColumnLineageCreateTableAsSelectTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/lineage/column/";
    }

    /**
     * 普通CREATE TABLE
     */
    @Test
    public void test_01_CreateTable() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        ColumnLineageResult analyze = sqlParser.parseColumnLineages(sql, new HashMap<>());

        assertEquals("students", analyze.getTargetTable());
        assertEquals(0, analyze.getColumnRelations().size());
    }


    /**
     * 字段中带有catalog.schema.table.column 如 default_catalog.changluya_db.orders2.order_id2
     */
    @Test
    public void test_02_SimpleCreateTableAsSelectByCatalogSchemaTableColumn() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, new HashMap<>());
        System.out.println(result);
    }

    /**
     * 测试用例3：CREATE TABLE AS SELECT
     */
    @Test
    public void test_03_SimpleCreateTableByAsSelect() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("source_table", Arrays.asList(new Column("id", 0), new Column("name", 1)));
            }
        };
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
//        result.printColumnLineage();
        assertEquals("new_table", result.getTargetTable());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "new_table.id", "source_table.id");
        assertRelation(relations, "new_table.name", "source_table.name");
    }

    /**
     * 测试用例4-较复杂：CREATE TABLE AS SELECT
     */
    @Test
    public void test_04_ComplexCreateTableAsSelect() {
        String sql = readSqlByIndex(sqlTxtName, 4);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("users",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                        ));
                this.put("orders",
                        Arrays.asList(new Column("amount", 0)
                                , new Column("id", 1)
                        ));
            }
        };
        // 实现字段血缘分析
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.hive);
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
//        result.printColumnLineage();
        assertEquals("user_order_summary", result.getTargetTable());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(4, relations.size());
        assertRelation(relations, "user_order_summary.user_id", "users.id");
        assertRelation(relations, "user_order_summary.name", "users.name");
        assertRelation(relations, "user_order_summary.total_amount", "orders.amount");
        assertRelation(relations, "user_order_summary.order_count", "orders.id");
    }

    /**
     * 测试用例5-子查询：CREATE TABLE AS SELECT xxx from (select xxx, xxx from xxx)
     */
    @Test
    public void test_05_createTableAsSelectSubQuery() {
        String sql = readSqlByIndex(sqlTxtName, 5);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("table2",
                        Arrays.asList(new Column("col1", 0)
                                , new Column("col2", 1)
                        ));
            }
        };
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
//        result.printColumnLineage();
        assertEquals("new_table", result.getTargetTable());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "new_table.col3", "table2.col1");
        assertRelation(relations, "new_table.col4", "table2.col2");
    }

    /**
     * 测试用例6
     */
    @Test
    public void test_06() {
        String sql = readSqlByIndex(sqlTxtName, 6);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
        result.printColumnLineage();
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        System.out.println("=============");
        for (ColumnLineage columnLineage : columnLineages) {
            System.out.println(columnLineage);
        }
        System.out.println("=============");
        System.out.println();
        assertEquals("tz", result.getTargetTable());
        assertEquals(6, columnLineages.size());
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "CDSY_PICTURES", "EID", null, null, "tz", "ID")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "CDSY_PICTURES", "OBJECTCODE", null, null, "tz", "CompanyCode")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "CDSY_PICTURES", "ADDRESS", null, null, "tz", "LOGOPath")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "CDSY_PICTURES", "EITIME", null, null, "tz", "EnterTime")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "CDSY_PICTURES", "EUTIME", null, null, "tz", "UpdateTime")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "CDSY_PICTURES", "EisDel", null, null, "tz", "EISDEL")));
    }

    /**
     * 测试用例7
     */
    @Test
    public void test_07() {
        String sql = readSqlByIndex(sqlTxtName, 7);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
        result.printColumnLineage();
        List<ColumnLineage> columnLineages = result.getColumnLineages();
//        System.out.println("=============");
//        for (ColumnLineage columnLineage : columnLineages) {
//            System.out.println(columnLineage);
//        }
//        System.out.println("=============");
        System.out.println();
        assertEquals("a", result.getTargetTable());
        assertEquals(4, columnLineages.size());
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "b", "id1", null, null, "a", "id1")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "b", "name1", null, null, "a", "name1")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "c", "id2", null, null, "a", "id2")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "c", "name2", null, null, "a", "name2")));
    }

    /**
     * 测试用例8：支持case when end 场景
     */
    @Test
    public void test_08_case_when_end() {
        String sql = readSqlByIndex(sqlTxtName, 8);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
        result.printColumnLineage();
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("tz", result.getTargetTable());
        assertEquals(2, columnLineages.size());
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "employees", "id", null, null, "tz", "id")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "employees", "salary", null, null, "tz", "salary_level")));
    }

    /**
     * 测试用例9
     */
    @Test
    public void test_09() {
        String sql = readSqlByIndex(sqlTxtName, 9);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
        result.printColumnLineage();
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("tz", result.getTargetTable());
        // 暂未确认
        assertEquals(28, columnLineages.size());
    }

    /**
     * 测试用例10：带有复杂case when end & cast函数场景
     */
    @Test
    public void test_10() {
        String sql = readSqlByIndex(sqlTxtName, 10);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
        result.printColumnLineage();
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("tz", result.getTargetTable());
        // 暂未确认（后期可再验证）
        assertEquals(20, columnLineages.size());
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "EID", null, null, "tz", "ID")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "INFOCODE", null, null, "tz", "LAS_Code")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "AttachType", null, null, "tz", "AttachType")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "AttachName", null, null, "tz", "AttachName")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "AttachNo", null, null, "tz", "AttachNo")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNAME", null, null, "tz", "AttachOrigName")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "AttachSize", null, null, "tz", "AttachSize")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "AttachPages", null, null, "tz", "AttachPages")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "SOUATTACHNAME", null, null, "tz", "SouAttchname")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNAME", null, null, "tz", "D_Path")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNAME", null, null, "tz", "F_Name")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "INFOCODE", null, null, "tz", "F_Name")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNO", null, null, "tz", "F_Name")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNAME", null, null, "tz", "F_Type")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNO", null, null, "tz", "F_Url")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "ATTACHNAME", null, null, "tz", "F_Url")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "INFOCODE", null, null, "tz", "F_Url")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "EUTIME", null, null, "tz", "UpdateTime")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "EITIME", null, null, "tz", "EnterTime")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "INFO_NW_RELATTACH", "EISDEL", null, null, "tz", "EisDel")));
    }

    /**
     * 测试用例11：
     */
    @Test
    public void test_11() {
        String sql = readSqlByIndex(sqlTxtName, 11);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        // 校验血缘分析结果
        result.printColumnLineage();
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("tz", result.getTargetTable());
        // 暂未确认（后期可再验证）
        assertEquals(15, columnLineages.size());
    }

    @Test
    public void test_12() {
        String sql = readSqlByIndex(sqlTxtName, 12);
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>();
        // 实现字段血缘分析
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        List<ColumnLineage> columnLineages = result.getColumnLineages();
        assertEquals("newTable", result.getTargetTable());
        assertEquals(4, columnLineages.size());
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "table1", "id", null, null, "newTable", "id")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "table1", "name", null, null, "newTable", "name")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "table2", "id", null, null, "newTable", "id")));
        assertEquals(true, columnLineages.contains(new ColumnLineage(null, null, "table2", "name", null, null, "newTable", "name")));
    }

}
