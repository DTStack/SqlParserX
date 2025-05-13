package com.dtstack.sqlparser.sqlparserx.core.base.lineage.column;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparser.sqlparserx.core.common.BaseColumnLineageAnalyzerTest;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import org.junit.Test;

import java.util.*;

/**
 * @description  测试WITH AS搭配INSERT的结构
 * @author changlu
 * @date 2025/4/22 11:00
 */
public class BaseColumnLineageWithAsInsertTest extends BaseColumnLineageAnalyzerTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
    private String sqlTxtName = "BaseColumnLineageWithAsInsertTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/base/lineage/column/";
    }

    /**
     * 复杂复合查询sql，主体insert两种：
     *  1、with as select xxx insert into table select xxx
     *  2、with as select xxx insert into table(column1, column2, xxx) select xxx
     * 复合内容涉及：
     *  1、from各类场景 unionall、join
     *  2、with as select unionall 各种情况
     *  3、select 内部字段各种情况场景
     */
    @Test
    public void test_01_02_insert_complex_sql() {
        List<String> insertSqlList = new ArrayList<>();
        // 01、insert无字段类型指定
        // String insertSql_01 = "INSERT INTO digua_ceshi.result_table\n";
        insertSqlList.add(readSqlByIndex(sqlTxtName, 1));
        // 02、insert复合指定字段类型  其他结构与01相同
//        String insertSql_02 = "INSERT INTO digua_ceshi.result_table (\n" +
//                "    t1_col1, t1_col2, t1_col3, \n" +
//                "    t2_name, t3_name, t4_age,\n" +
//                "    t5_level, department, older_count\n" +
//                ")\n";
        insertSqlList.add(readSqlByIndex(sqlTxtName, 2));
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>(){
            {
                this.put("digua_ceshi.demo_table_digua",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                                , new Column("age", 2)
                        ));
                this.put("digua_ceshi.new_table",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                                , new Column("age", 2)
                        ));
                this.put("digua_ceshi.new_table2",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                                , new Column("age", 2)
                        ));
                this.put("digua_ceshi.hierarchy_table",
                        Arrays.asList(new Column("id", 0)
                                , new Column("name", 1)
                                , new Column("manager_id", 2)
                                , new Column("level", 3)
                        ));
                this.put("digua_ceshi.departments",
                        Arrays.asList(new Column("department", 0)
                                , new Column("employee_id", 1)
                        ));
            }
        };
        for (int i = 0; i < insertSqlList.size(); i++) {
            String insertSql = insertSqlList.get(i);

            ColumnLineageResult result = sqlParser.parseColumnLineages(insertSql, tableColumnsMap);
            Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();

            assertRelation(relations, "digua_ceshi.result_table.id", "digua_ceshi.demo_table_digua.id");
            assertRelation(relations, "digua_ceshi.result_table.name", "digua_ceshi.demo_table_digua.name");
            assertRelation(relations, "digua_ceshi.result_table.age", "digua_ceshi.demo_table_digua.age");
            assertRelation(relations, "digua_ceshi.result_table.t2_name", "digua_ceshi.demo_table_digua.name");
            assertRelation(relations, "digua_ceshi.result_table.t3_name", "digua_ceshi.new_table.name");
            assertRelation(relations, "digua_ceshi.result_table.t3_name", "digua_ceshi.new_table2.name");
            assertRelation(relations, "digua_ceshi.result_table.t4_age", "digua_ceshi.new_table.age");
            assertRelation(relations, "digua_ceshi.result_table.t5_level", "digua_ceshi.hierarchy_table.level");
            assertRelation(relations, "digua_ceshi.result_table.department", "digua_ceshi.departments.department");
            assertRelation(relations, "digua_ceshi.result_table.older_count", "digua_ceshi.demo_table_digua.id");
            assertRelation(relations, "digua_ceshi.result_table.older_count", "digua_ceshi.demo_table_digua.name");
            assertRelation(relations, "digua_ceshi.result_table.older_count", "digua_ceshi.demo_table_digua.age");
        }
    }

}
