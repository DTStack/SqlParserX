import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.SQLType;
import com.changlu.core.parser.base.lineage.table.Table;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TmpDruidTest {

    @Test
    public void tempTest() {
        String sql = "SELECT * FROM my_catalog.my_catalog.my_schema.my_table";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        System.out.println(stmt);
    }

    @Test
    public void tempTest2() {
        String sql = "SELECT * FROM batch63.sales_order_details WHERE quantity = (SELECT MAX(quantity) FROM batch63.sales_order_details)";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.spark);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();

        SQLType sqlType = SQLParserUtils.getSQLType(sql, DbType.spark);
        System.out.println(sqlType);
        System.out.println(stmt);
    }

    @Test
    public void tempTest_01() {
        String sql = "SELECT \n" +
                "        id, \n" +
                "        origin_news_id, \n" +
                "        company_code, \n" +
                "        company_name, \n" +
                "        company_credit_code, \n" +
                "        company_tag_code, \n" +
                "        company_sentiment, \n" +
                "        company_sentiment_level, \n" +
                "        is_major_company, \n" +
                "        create_time, \n" +
                "        update_time, \n" +
                "        status, \n" +
                "        supplier, \n" +
                "        rec_id\n" +
                "FROM \n" +
                "        \"dwldata\".\"t_data_ibfa_news_company\"\n" +
                "where \n" +
                "  company_name like '%${company_name}%'\n" +
                "    and company_credit_code = ${company_credit_code}\n" +
                "    and create_time > ('${date_dt}'::date - INTERVAL '1 year')::timestamp";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.postgresql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        System.out.println(stmt);
    }


    @Test
    public void tempTest_02_starrocks() {
        String sql = "insert into ods.t_dtinsight_test values(6, '1' ,1,1.1,'2022-02-02 00:00:00');\n";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.starrocks);
        SQLStatement sqlStatement = parser.parseStatement();
        System.out.println(sqlStatement);
    }

    @Test
    public void tempTest_03() {
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
                "),\n" +
                "\n" +
                "-- 第四个AS：LEFT JOIN查询（使用新的表）\n" +
                "t4 AS (\n" +
                "    SELECT id, name, age\n" +
                "    FROM digua_ceshi.new_table\n" +
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
                "INSERT INTO digua_ceshi.result_table\n" +
                "SELECT \n" +
                "    t1.*, \n" +
                "    t2.name AS t2_name, \n" +
                "    t3.name AS t3_name, \n" +
                "    t4.age AS t4_age,\n" +
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
                "    t4.age AS t4_age,\n" +
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
//        String sql = "SELECT t1.id, t1.name, dt.age\n" +
//                "FROM (\n" +
//                "    SELECT id, name\n" +
//                "    FROM digua_ceshi.demo_table_digua\n" +
//                "    WHERE age > 25\n" +
//                ") t1\n" +
//                "JOIN digua_ceshi.demo_table_digua dt ON t1.id = dt.id;";
//        String sql = "SELECT t1.id, t1.name, dt.age\n" +
//                "FROM (\n" +
//                "    SELECT id, name\n" +
//                "    FROM digua_ceshi.demo_table_digua\n" +
//                "    WHERE age > 25\n" +
//                ") t1";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.hive);
        SQLStatement sqlStatement = parser.parseStatement();
//        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        List<Table> tables = new ArrayList<>();
        SQLUtils.acceptTableSource(
                sql,
                DbType.oracle,
                e -> {
                    if (e instanceof SQLExprTableSource) {
                        SQLExprTableSource tableSource = (SQLExprTableSource) e;
//                        Table table = new Table(tableSource.getTableName(), tableSource.getAlias());
//                        tables.add(table);
                    }
                },
                e -> e instanceof SQLExprTableSource
        );
        System.out.println(tables);
    }
}
