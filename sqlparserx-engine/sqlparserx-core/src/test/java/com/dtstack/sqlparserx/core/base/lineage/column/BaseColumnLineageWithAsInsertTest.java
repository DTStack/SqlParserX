/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.core.base.lineage.column;

import com.dtstack.sqlparserx.core.common.BaseColumnLineageAnalyzerTest;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import com.alibaba.druid.DbType;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

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
        return "/core/base/lineage/column/";
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
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>() {
            {
                this.put("digua_ceshi.demo_table_digua",
                        Arrays.asList(new Column("id", 0), new Column("name", 1), new Column("age", 2)));
                this.put("digua_ceshi.new_table",
                        Arrays.asList(new Column("id", 0), new Column("name", 1), new Column("age", 2)));
                this.put("digua_ceshi.new_table2",
                        Arrays.asList(new Column("id", 0), new Column("name", 1), new Column("age", 2)));
                this.put("digua_ceshi.hierarchy_table",
                        Arrays.asList(new Column("id", 0), new Column("name", 1), new Column("manager_id", 2),
                                new Column("level", 3)));
                this.put("digua_ceshi.departments",
                        Arrays.asList(new Column("department", 0), new Column("employee_id", 1)));
            }
        };
        // sql1测试
        String insertSql1 = insertSqlList.get(0);
        ColumnLineageResult result = sqlParser.parseColumnLineages(insertSql1, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        assertEquals(12, result.getColumnLineages().size());
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

        // sql2测试
        String insertSql2 = insertSqlList.get(1);
        result = sqlParser.parseColumnLineages(insertSql2, tableColumnsMap);
        relations = result.getColumnRelations();
        assertEquals(12, result.getColumnLineages().size());
        assertRelation(relations, "digua_ceshi.result_table.t1_col1", "digua_ceshi.demo_table_digua.id");
        assertRelation(relations, "digua_ceshi.result_table.t1_col2", "digua_ceshi.demo_table_digua.name");
        assertRelation(relations, "digua_ceshi.result_table.t1_col3", "digua_ceshi.demo_table_digua.age");
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

    @Test
    public void test_01_02_insert_complex_sql111() {
        String sql = "INSERT INTO b\n" +
                "WITH cte AS (\n" +
                "    SELECT *\n" +
                "    FROM a\n" +
                ")\n" +
                "SELECT * FROM cte;";
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>() {
            {
                this.put("digua_ceshi.a",
                        Arrays.asList(new Column("id", 0), new Column("name", 1)));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        System.out.println(relations);
    }

    @Test
    public void test_01_02_insert_complex_sql1112() {
        String sql = "INSERT INTO wwz_00001(id,name)\n" +
                "WITH wwz_00003 AS (\n" +
                "    SELECT *\n" +
                "    FROM wwz_00002\n" +
                ")\n" +
                "SELECT * FROM wwz_00003;";
        Map<String, List<Column>> tableColumnsMap = new HashMap<String, List<Column>>() {
            {
                this.put("digua_ceshi.wwz_00002",
                        Arrays.asList(new Column("id", 0), new Column("name", 1)));
            }
        };
        ColumnLineageResult result = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        Map<String, List<ColumnLineageResult.ColumnRelation>> relations = result.getColumnRelations();
        System.out.println(relations);
    }

}
