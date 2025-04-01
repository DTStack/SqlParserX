package lineage.table;

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
     */
    @Test
    public void testSimpleCreateTableAsSelect() {
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
     */
    @Test
    public void testSimpleCreateTableAsSelect_02() {
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
    
    @Test
    public void testCreateTableAsSelectWithAlias() {
        String sql = "CREATE TABLE new_table (col1, col2) AS SELECT id as user_id, name as user_name FROM source_table";
        
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql);
        
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(2, relations.size());
        assertRelation(relations, "new_table.col1", "user_id");
        assertRelation(relations, "new_table.col2", "user_name");
    }
    
    @Test
    public void testCreateTableAsSelectWithExpression() {
        String sql = "CREATE TABLE stats AS " +
                     "SELECT user_id, SUM(amount) as total_amount, COUNT(*) as order_count " +
                     "FROM orders GROUP BY user_id";
        
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql);
        
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(3, relations.size());
        assertRelation(relations, "stats.user_id", "user_id");
        assertRelation(relations, "stats.total_amount", "amount");
        assertRelation(relations, "stats.order_count", "*");
    }
    
    @Test
    public void testCreateTableAsSelectWithMultiTable() {
        String sql = "CREATE TABLE user_orders AS " +
                     "SELECT u.id as user_id, u.name, o.order_id, o.amount " +
                     "FROM users u JOIN orders o ON u.id = o.user_id";
        
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        ColumnLineageResult result = analyzer.analyze(sql);
        
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(4, relations.size());
        assertRelation(relations, "user_orders.user_id", "u.id");
        assertRelation(relations, "user_orders.name", "u.name");
        assertRelation(relations, "user_orders.order_id", "o.order_id");
        assertRelation(relations, "user_orders.amount", "o.amount");
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