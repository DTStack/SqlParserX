package lineage;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.column.ColumnLineageAnalyzer;
import com.changlu.lineage.column.ColumnLineageResult;
import com.changlu.lineage.domain.Column;
import com.changlu.lineage.table.TableLineageAnalyzer;
import com.changlu.lineage.table.TableLineageResult;
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
    public void test_06_createTableAsSelectSubQuery() {
        String sql = "select col1, col2 from table2 union select col1";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLStatement stmt = parser.parseStatement();
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