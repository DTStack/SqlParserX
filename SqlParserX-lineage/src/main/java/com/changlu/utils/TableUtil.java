package com.changlu.utils;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.changlu.lineage.domain.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableUtil {

    public static Table parseTables(String sql, DbType dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> sqlStatements = parser.parseStatementList();
        if (sqlStatements.size() != 1) {
            throw new RuntimeException("解析sql记录条数异常，请传入1条sql");
        }
        SQLStatement sqlStatement = sqlStatements.get(0);

        SchemaStatVisitor schemaStatVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        sqlStatement.accept(schemaStatVisitor);
        Map<TableStat.Name, TableStat> tables = schemaStatVisitor.getTables();
        System.out.println(tables);
        return null;
    }

    public static void main(String[] args) {
        List<String> insertSqlList = new ArrayList<>();
        // 无字段类型指定
        String insertSql_01 = "INSERT INTO digua_ceshi.result_table\n";
        // 复合指定字段类型
        String insertSql_02 = "INSERT INTO digua_ceshi.result_table (\n" +
                "    t1_col1, t1_col2, t1_col3, \n" +
                "    t2_name, t3_name, t4_age,\n" +
                "    t5_level, department, older_count\n" +
                ")\n";
        insertSqlList.add(insertSql_01);
        insertSqlList.add(insertSql_02);
        for (int i = 0; i < insertSqlList.size(); i++) {
            String insertSql = insertSqlList.get(i);
            String sql = "-- 完整查询（包含WITH AS、JOIN、子查询、UNION等）\n" +
                    "WITH \n" +
                    "-- 第一个AS：简单SELECT\n" +
                    "t1 AS (\n" +
                    "    SELECT id, name, age\n" +
                    "    FROM digua_ceshi.demo_table_digua\n" +
                    "),\n" +
                    "\n" +
                    "-- 第二个AS：SELECT FROM (SELECT) 嵌套形式\n" +
                    "t2 AS (\n" +
                    "    SELECT sub.id, sub.name\n" +
                    "    FROM (\n" +
                    "        SELECT id, name\n" +
                    "        FROM digua_ceshi.demo_table_digua\n" +
                    "        WHERE id IS NOT NULL\n" +
                    "    ) AS sub\n" +
                    "),\n" +
                    "\n" +
                    "-- 第三个AS：UNION查询（使用新的表）\n" +
                    "t3 AS (\n" +
                    "    SELECT id, name, age\n" +
                    "    FROM digua_ceshi.new_table\n" +
                    "    WHERE age > 18\n" +
                    "    UNION\n" +
                    "    SELECT id, name, age\n" +
                    "    FROM digua_ceshi.new_table\n" +
                    "    WHERE age IS NULL\n" +
                    "    UNION\n" +
                    "    SELECT id, name, age\n" +
                    "    FROM digua_ceshi.new_table2\n" +
                    "    WHERE age IS NULL\n" +
                    "),\n" +
                    "\n" +
                    "-- 第四个AS：LEFT JOIN查询（使用新的表）\n" +
                    "t4 AS (\n" +
                    "    SELECT t.id, t.name, nt.age AS new_table_age\n" +
                    "    FROM digua_ceshi.demo_table_digua t\n" +
                    "    LEFT JOIN digua_ceshi.new_table nt ON t.id = nt.id\n" +
                    "),\n" +
                    "\n" +
                    "-- 第五个AS：递归CTE（使用新的表）\n" +
                    "t5 AS (\n" +
                    "    -- 递归CTE的初始查询\n" +
                    "    SELECT id, name, manager_id, level\n" +
                    "    FROM digua_ceshi.hierarchy_table\n" +
                    "    WHERE manager_id IS NULL  -- 假设顶级节点的manager_id为NULL\n" +
                    "\n" +
                    "    UNION ALL\n" +
                    "\n" +
                    "    -- 递归部分\n" +
                    "    SELECT h.id, h.name, h.manager_id, t5.level + 1\n" +
                    "    FROM digua_ceshi.hierarchy_table h\n" +
                    "    INNER JOIN t5 ON h.manager_id = t5.id\n" +
                    ")\n" +
                    "\n" +
                    "-- 最终主查询（包含所有JOIN、子查询、UNION操作）\n" +
                    insertSql +
//                "INSERT INTO digua_ceshi.result_table\n" +
                    "SELECT \n" +
                    "    t1.*, \n" +
                    "    t2.name AS t2_name, \n" +
                    "    t3.name AS t3_name, \n" +
                    "    t4.new_table_age AS t4_age,\n" +
                    "    t5.level AS t5_level, -- 添加递归CTE中的层级字段\n" +
                    "    -- 添加与真实表的JOIN\n" +
                    "    dt.department,\n" +
                    "    -- 添加子查询示例\n" +
                    "    (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count\n" +
                    "FROM t1\n" +
                    "JOIN t2 ON t1.id = t2.id\n" +
                    "LEFT JOIN t3 ON t1.id = t3.id\n" +
                    "LEFT JOIN t4 ON t1.id = t4.id\n" +
                    "LEFT JOIN t5 ON t1.id = t5.id\n" +
                    "-- 添加与真实表的JOIN\n" +
                    "JOIN digua_ceshi.departments dt ON t1.id = dt.employee_id\n" +
                    "-- 添加UNION示例\n" +
                    "WHERE t1.age > 20\n" +
                    "\n" +
                    "UNION ALL\n" +
                    "\n" +
                    "SELECT \n" +
                    "    t1.*, \n" +
                    "    t2.name AS t2_name, \n" +
                    "    t3.name AS t3_name, \n" +
                    "    t4.new_table_age AS t4_age,\n" +
                    "    t5.level AS t5_level, -- 添加递归CTE中的层级字段\n" +
                    "    dt.department,\n" +
                    "    0 AS older_count  -- 默认值\n" +
                    "FROM t1\n" +
                    "JOIN t2 ON t1.id = t2.id\n" +
                    "LEFT JOIN t3 ON t1.id = t3.id\n" +
                    "LEFT JOIN t4 ON t1.id = t4.id\n" +
                    "LEFT JOIN t5 ON t1.id = t5.id\n" +
                    "JOIN digua_ceshi.departments dt ON t1.id = dt.employee_id\n" +
                    "WHERE t1.age <= 20;";
            Table table = parseTables(sql, DbType.hive);
        }

    }



}
