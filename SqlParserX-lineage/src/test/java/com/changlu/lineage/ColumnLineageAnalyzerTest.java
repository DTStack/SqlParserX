package com.changlu.lineage;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.column.ColumnLineageAnalyzer;
import com.changlu.lineage.column.ColumnLineageResult;
import com.changlu.lineage.column.Column;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ColumnLineageAnalyzerTest {

    private ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();

    /**
     * 测试用例1：普通CREATE TABLE
     */
    @Test
    public void test_01_CreateTable() {
        String sql = "CREATE TABLE students (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    name VARCHAR(50) NOT NULL,\n" +
                "    age INT,\n" +
                "    gender ENUM('Male', 'Female')\n" +
                ");";
        ColumnLineageResult analyze = analyzer.analyze(sql);

        assertEquals("students", analyze.getTargetTable());
        assertEquals(0, analyze.getColumnRelations().size());
    }

    @Test
    public void test_002_SimpleCreateTableAsSelect() {
        String sql = "CREATE TABLE new_table AS select default_catalog.changluya_db.orders2.order_id2 from default_catalog.changluya_db.orders";
        // 实现字段血缘分析
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, new HashMap<>());
        System.out.println(result);
    }

    /**
     * 测试用例2：CREATE TABLE AS SELECT
     *  例子1:较简单
     */
    @Test
    public void test_02_SimpleCreateTableAsSelect() {
        String sql = "CREATE TABLE new_table AS SELECT id, name FROM source_table as st";
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("source_table", Arrays.asList(new Column("id", 0), new Column("name", 1)));
            }
        };
        // 实现字段血缘分析
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, tableColumnsMap);
        // 校验血缘分析结果
//        result.printColumnLineage();
        assertEquals("new_table", result.getTargetTable());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "new_table.id", "source_table.id");
        assertRelation(relations, "new_table.name", "source_table.name");
    }

    /**
     * 测试用例3：CREATE TABLE AS SELECT
     *  例子2:较复杂
     */
    @Test
    public void test_03_SimpleCreateTableAsSelect() {
        String sql = "CREATE TABLE user_order_summary AS\n" +
                "SELECT \n" +
                "    u.id as user_id, \n" +
                "    u.name,\n" +
                "    SUM(o.amount) as total_amount,\n" +
                "    COUNT(o.id) as order_count\n" +
                "FROM users u JOIN orders o ON u.id = o.user_id\n" +
                "GROUP BY u.id, u.name";
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
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, tableColumnsMap);
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
        String sql = "CREATE TABLE new_table AS\n" +
                "-- 理解为 table2.col1、table2.col2\n" +
                "select col3, col4 from (\n" +
                "    select col1 as col3, col2 as col4, col5 from table2\n" +
                ")as table3";
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("table2",
                        Arrays.asList(new Column("col1", 0)
                                , new Column("col2", 1)
                        ));
            }
        };
        // 实现字段血缘分析
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, tableColumnsMap);
        // 校验血缘分析结果
//        result.printColumnLineage();
        assertEquals("new_table", result.getTargetTable());
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "new_table.col3", "table2.col1");
        assertRelation(relations, "new_table.col4", "table2.col2");
    }

    @Test
    public void test_06_insert_with_as_sql() {
        // 无字段类型指定
        String insertSql = "INSERT INTO digua_ceshi.result_table";
        // 复合指定字段类型
//        String insertSql = "INSERT INTO digua_ceshi.result_table (\n" +
//                "    t1_col1, t1_col2, t1_col3, \n" +
//                "    t2_name, t3_name, t4_age,\n" +
//                "    t5_level, department, older_count\n" +
//                ")";
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
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.hive);
        SQLStatement stmt = parser.parseStatement();
        System.out.println(stmt);
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, new HashMap<>());
        System.out.println(result);
    }

    @Test
    public void test_07_createTableAsSelectSubQuery() {
//        String sql = "-- 插入联合查询的结果\n" +
//                "INSERT INTO TABLE target_table\n" +
//                "SELECT s.id, s.name, s.value + t.value AS value\n" +
//                "FROM source_table s\n" +
//                "JOIN another_table t ON s.id = t.id;";
//        String sql = "-- 插入部分列\n" +
//                "INSERT INTO TABLE target_table (id, name)\n" +
//                "SELECT id, name\n" +
//                "FROM source_table;";
        String sql = "insert into table ifre_loan_mx_huabei partition (cdate = '2019-12-27')\n" +
                "select a.cdate as encash_date,--上游：ifre_day_loan_stat_huabei.cdate\n" +
                "a.contract_no,--上游：ifre_day_loan_stat_huabei.contract_no\n" +
                "a.name,--上游：ifre_day_loan_stat_huabei.name\n" +
                "a.cert_no,--上游：ifre_day_loan_stat_huabei.cert_no;下游：ifre_ovd_all_huabei.a18、a20、a21、a22\n" +
                "a.begin_prin/100 as encash_amt,--上游：ifre_day_loan_stat_huabei.begin_prin\n" +
                "nvl(sum1,0)/100 as sum1,--上游：huabei_bill_repay_detail.paid_prin_amt\n" +
                "nvl(sum2,0)/100 as sum2,--上游：huabei_bill_repay_detail.paid_ovd_prin_amt\n" +
                "nvl(sum3,0)/100 as sum3,--上游：huabei_bill_repay_detail.paid_int_amt\n" +
                "nvl(sum4,0)/100 as sum4,--上游：huabei_bill_repay_detail.paid_ovd_int_amt\n" +
                "nvl(sum5,0)/100 as sum5,--上游：huabei_bill_repay_detail.paid_ovd_prin_pnlt_amt\n" +
                "nvl(sum6,0)/100 as sum6,--上游：huabei_bill_repay_detail.paid_ovd_int_pnlt_amt\n" +
                "'' as start_date,--上游：无\n" +
                "'' as end_date,--上游：无\n" +
                "0 as dkqx,--上游：无\n" +
                "'' as repay_mode,--上游：无\n" +
                "nvl(cont,0) as cont,--上游：无\n" +
                "(prin_bal+ovd_prin_bal)/100 as loan_amt,--上游：ifre_day_loan_stat_huabei.prin_bal、ovd_prin_bal;下游：ifre_ovd_all_huabei.a6、a7、a5、a13、a14、a15、a24\n" +
                "0 as day_rate,--上游：无\n" +
                "status,--上游：ifre_day_loan_stat_huabei.status\n" +
                "asset_class,--上游：ifre_day_loan_stat_huabei.asset_class\n" +
                "fiveclass,--上游：无\n" +
                "ovd_prin_bal/100 as ovd_prin_bal,--上游：ifre_day_loan_stat_huabei.ovd_prin_bal\n" +
                "ovd_int_bal/100 as ovd_int_bal,--上游：ifre_day_loan_stat_huabei.ovd_int_bal\n" +
                "cast(prin_ovd_days as INT),--上游：ifre_day_loan_stat_huabei.prin_ovd_days\n" +
                "cast(int_ovd_days as INT),--上游：ifre_day_loan_stat_huabei.int_ovd_days\n" +
                "cast(nvl(case when status = 'OVD' then date_add('2019-12-27',0-(case when prin_ovd_days <= int_ovd_days and int_bal + ovd_int_bal > 0 then int_ovd_days else prin_ovd_days end)) end,'') as string) as ovd_date, --上游：ifre_day_loan_stat_huabei.int_ovd_days、prin_ovd_days\n" +
                "cast(case when prin_ovd_days <= int_ovd_days and int_bal + ovd_int_bal > 0 then int_ovd_days else prin_ovd_days end as int) as ovd_days,--上游：ifre_day_loan_stat_huabei.int_ovd_days、prin_ovd_days\n" +
                "contract_type --上游：ifre_day_loan_stat_huabei.contract_type\n" +
                "from\n" +
                "(select * from ifre_day_loan_stat_huabei where cdate = '2019-12-27') a\n" +
                "left join\n" +
                "(select contract_no,count(*) as cont,sum(paid_prin_amt) as sum1,\n" +
                "sum(paid_ovd_prin_amt) as sum2, sum(paid_int_amt) as sum3, sum(paid_ovd_int_amt) as sum4,\n" +
                "sum(paid_ovd_prin_pnlt_amt) as sum5, sum(paid_ovd_int_pnlt_amt) as sum6\n" +
                "from dispatching_cdh_prod.huabei_bill_repay_detail where cdate <= '2019-12-27'\n" +
                "group by contract_no) c\n" +
                "on a.contract_no = c.contract_no\n" +
                "inner join\n" +
                "(\n" +
                "select cert_no,case max(fiveclass) when '1'  then 'A1' when '2' then 'A2' when '3' then 'B1' when '4' then 'B2'\n" +
                "when '5' then 'C1' when '6' then 'C2'  when '7' then 'C3' when '8' then 'D1' when '9' then 'D2' when '10' then 'D3' when '11' then 'E' end as fiveclass\n" +
                "from\n" +
                "(select cert_no,\n" +
                "case when prin_ovd_days = 0 and int_ovd_days = 0 and ovd_prin_bal = 0  and ovd_int_bal = 0 and status <> 'OVD' then '1'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 0 and 30 then '2'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 31 and 60 then '3'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 61 and 90 then '4'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 91 and 150 then '5'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 151 and 210 then '6'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 211 and 270 then '7'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 271 and 360 then '8'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 361 and 450 then '9'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) BETWEEN 451 and 540 then '10'\n" +
                "when  (case when prin_ovd_days >= int_ovd_days and status = 'OVD' then prin_ovd_days else int_ovd_days end) > 540 then '11' end as fiveclass\n" +
                "from ifre_day_loan_stat_huabei where cdate = '2019-12-27' and status = 'OVD')tmp\n" +
                "group by cert_no\n" +
                ") d\n" +
                "on a.cert_no = d.cert_no\n" +
                "where status = 'OVD' or (status <> 'OVD' and fiveclass not in('A1','A2'));";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.hive);
        SQLStatement stmt = parser.parseStatement();
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql, new HashMap<>());
        System.out.println(result);
    }


    /**
     * 验证关联关系
     * @param relations
     * @param target 目标端字段 table.column
     * @param source 来源字段 table.column
     */
    private void assertRelation(Map<String, List<ColumnLineageResult.ColumnRelation>> relations, 
                              String target, String source) {
        List<ColumnLineageResult.ColumnRelation> rels = relations.get(target);
        boolean flag = false;
        // check指定来源字段中的关联关系中包含有期望来源：expectedSource
        for (ColumnLineageResult.ColumnRelation rel : rels) {
            if (rel.getSource().equals(source)) {
                flag = true;
                break;
            }
        }
        assertTrue(flag);
    }
}