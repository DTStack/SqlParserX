package com.changlu.lineage;

import com.alibaba.druid.DbType;
import com.changlu.lineage.table.TableLineageAnalyzer;
import com.changlu.lineage.table.TableLineageResult;
import com.changlu.lineage.table.TableRelation;
import org.junit.Test;
import static org.junit.Assert.*;

public class CreateTableLineageTest {

    private TableLineageAnalyzer analyzer = new TableLineageAnalyzer(DbType.mysql);

    @Test
    public void test_01_CreateTableAsSelect() {
        String sql = "CREATE TABLE db.new_table AS SELECT * FROM source_table s JOIN another_table a ON s.id = a.id";
        TableLineageResult result = analyzer.analyze(sql);

        assertEquals("new_table", result.getTargetTable());
        assertEquals(2, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("source_table"));
        assertTrue(result.getSourceTables().contains("another_table"));

        assertEquals(2, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("source_table", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("another_table", "new_table")));
    }

    @Test
    public void test_02_CreateTableAsSelect_union() {
        String sql = "CREATE TABLE new_table AS\n" +
                "SELECT col1, col2 FROM table1\n" +
                "UNION\n" +
                "SELECT col3, col4 FROM table2;";
        TableLineageResult result = analyzer.analyze(sql);

        assertEquals("new_table", result.getTargetTable());
        assertEquals(2, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("table1"));
        assertTrue(result.getSourceTables().contains("table2"));

        assertEquals(2, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("table1", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table2", "new_table")));
    }

    @Test
    public void test_03_CreateTableAsSelect_with() {
        String sql = "CREATE TABLE new_table AS\n" +
                "SELECT col1, col1 * 2 AS col2, \n" +
                "       (SELECT MAX(col3) FROM table2) AS col3\n" +
                "FROM table1;";
        TableLineageResult result = analyzer.analyze(sql);

        assertEquals("new_table", result.getTargetTable());
        assertEquals(2, result.getSourceTables().size());
        assertTrue(result.getSourceTables().contains("table1"));
        assertTrue(result.getSourceTables().contains("table2"));

        assertEquals(2, result.getRelations().size());
        assertTrue(result.getRelations().contains(new TableRelation("table1", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("table2", "new_table")));
    }

    @Test
    public void test_04_hive_withas() {
//        String sql = "-- 完整查询（包含WITH AS、JOIN、子查询、UNION等）\n" +
//                "WITH \n" +
//                "-- 第一个AS：简单SELECT\n" +
//                "t1 AS (\n" +
//                "    SELECT id, name, age\n" +
//                "    FROM digua_ceshi.demo_table_digua\n" +
//                "),\n" +
//                "\n" +
//                "-- 第二个AS：SELECT FROM (SELECT) 嵌套形式\n" +
//                "t2 AS (\n" +
//                "    SELECT sub.id, sub.name\n" +
//                "    FROM (\n" +
//                "        SELECT id, name\n" +
//                "        FROM digua_ceshi.demo_table_digua\n" +
//                "        WHERE id IS NOT NULL\n" +
//                "    ) AS sub\n" +
//                "),\n" +
//                "\n" +
//                "-- 第三个AS：UNION查询（使用新的表）\n" +
//                "t3 AS (\n" +
//                "    SELECT id, name, age\n" +
//                "    FROM digua_ceshi.new_table\n" +
//                "    WHERE age > 18\n" +
//                "    UNION\n" +
//                "    SELECT id, name, age\n" +
//                "    FROM digua_ceshi.new_table\n" +
//                "    WHERE age IS NULL\n" +
//                "),\n" +
//                "\n" +
//                "-- 第四个AS：LEFT JOIN查询（使用新的表）\n" +
//                "t4 AS (\n" +
//                "    SELECT id, name, age\n" +
//                "    FROM digua_ceshi.new_table\n" +
//                "),\n" +
//                "\n" +
//                "-- 第五个AS：递归CTE（使用新的表）\n" +
//                "t5 AS (\n" +
//                "    -- 递归CTE的初始查询\n" +
//                "    SELECT id, name, manager_id, level\n" +
//                "    FROM digua_ceshi.hierarchy_table\n" +
//                "    WHERE manager_id IS NULL  -- 假设顶级节点的manager_id为NULL\n" +
//                "\n" +
//                "    UNION ALL\n" +
//                "\n" +
//                "    -- 递归部分\n" +
//                "    SELECT h.id, h.name, h.manager_id, t5.level + 1\n" +
//                "    FROM digua_ceshi.hierarchy_table h\n" +
//                "    INNER JOIN t5 ON h.manager_id = t5.id\n" +
//                ")\n" +
//                "\n" +
//                "-- 最终主查询（包含所有JOIN、子查询、UNION操作）\n" +
//                "INSERT INTO digua_ceshi.result_table\n" +
//                "SELECT \n" +
//                "    t1.*, \n" +
//                "    t2.name AS t2_name, \n" +
//                "    t3.name AS t3_name, \n" +
//                "    t4.age AS t4_age,\n" +
//                "    t5.level AS t5_level, -- 添加递归CTE中的层级字段\n" +
//                "    -- 添加与真实表的JOIN\n" +
//                "    dt.department,\n" +
//                "    -- 添加子查询示例\n" +
//                "    (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count\n" +
//                "FROM t1\n" +
//                "JOIN t2 ON t1.id = t2.id\n" +
//                "LEFT JOIN t3 ON t1.id = t3.id\n" +
//                "LEFT JOIN t4 ON t1.id = t4.id\n" +
//                "LEFT JOIN t5 ON t1.id = t5.id\n" +
//                "-- 添加与真实表的JOIN\n" +
//                "JOIN digua_ceshi.departments dt ON t1.id = dt.employee_id\n" +
//                "-- 添加UNION示例\n" +
//                "WHERE t1.age > 20\n" +
//                "\n" +
//                "UNION ALL\n" +
//                "\n" +
//                "SELECT \n" +
//                "    t1.*, \n" +
//                "    t2.name AS t2_name, \n" +
//                "    t3.name AS t3_name, \n" +
//                "    t4.age AS t4_age,\n" +
//                "    t5.level AS t5_level, -- 添加递归CTE中的层级字段\n" +
//                "    dt.department,\n" +
//                "    0 AS older_count  -- 默认值\n" +
//                "FROM t1\n" +
//                "JOIN t2 ON t1.id = t2.id\n" +
//                "LEFT JOIN t3 ON t1.id = t3.id\n" +
//                "LEFT JOIN t4 ON t1.id = t4.id\n" +
//                "LEFT JOIN t5 ON t1.id = t5.id\n" +
//                "JOIN digua_ceshi.departments dt ON t1.id = dt.employee_id\n" +
//                "WHERE t1.age <= 20;";
        String sql = "CREATE TABLE catalog2.db2.new_table\n" +
                "AS SELECT * FROM catalog1.db1.source_table;";
        analyzer = new TableLineageAnalyzer(DbType.hive);
        TableLineageResult result = analyzer.analyze(sql);

        assertEquals("new_table", result.getTargetTable());
//        assertEquals(2, result.getSourceTables().size());
//        assertTrue(result.getSourceTables().contains("table1"));
//        assertTrue(result.getSourceTables().contains("table2"));
//
//        assertEquals(2, result.getRelations().size());
//        assertTrue(result.getRelations().contains(new TableRelation("table1", "new_table")));
//        assertTrue(result.getRelations().contains(new TableRelation("table2", "new_table")));
    }


}