package lineage;

import com.changlu.lineage.table.TableLineageAnalyzer;
import com.changlu.lineage.table.TableLineageResult;
import com.changlu.lineage.table.TableRelation;
import org.junit.Test;
import static org.junit.Assert.*;

public class CreateTableLineageTest {

    private TableLineageAnalyzer analyzer = new TableLineageAnalyzer("mysql");

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


}