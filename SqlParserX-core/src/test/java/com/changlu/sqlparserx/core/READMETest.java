package com.changlu.sqlparser.core;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.column.Column;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.changlu.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import org.junit.Test;

import java.util.*;

public class READMETest {
    @Test
    public void demoTest() {
        // 准备sql
        String sql = getSql();
        // 1、获取到sqlparser解析器
        // 方式一：工厂方式获取
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);
        // 方式二：直接new
//        MySqlParser mySqlParser = new MySqlParser();

        // 2、实现解析功能
        // 2.1、解析表
        ParseTableResult parseTableResult = sqlParser.parseTables(sql);
        System.out.println("====解析表====");
        System.out.println(parseTableResult.getMainTable());// 获取主表
        System.out.println(parseTableResult.getAllTableName()); // 获取解析到的所有表
//        Set<Table> allTables = parseTableResult.getAllTables();
//        for (Table allTable : allTables) {
//            System.out.println(allTable.getCatalog());
//            System.out.println(allTable.getDb());
//            System.out.println(allTable.getName());
//        }

        // 2.2、解析字段血缘
        System.out.println("\n====解析字段血缘====");
        // 针对select * from table 场景，可传入tableColumnsMap封装真实的表字段信息
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
        ColumnLineageResult columnLineageResult = sqlParser.parseColumnLineages(sql, tableColumnsMap);
        columnLineageResult.printColumnLineage();

        // 2.3、解析表血缘
        System.out.println("\n====解析表血缘====");
        TableLineageResult tableLineageResult = sqlParser.parseTableLineages(sql);
        List<TableLineage> tableLineages = tableLineageResult.getTableLineages();
        System.out.println(tableLineages);

        // 2.4、解析函数
        System.out.println("\n====解析函数====");
        Set<String> functions = sqlParser.parseFunction(sql);
        System.out.println(functions);
    }

    public String getSql() {
        return "CREATE TABLE user_order_summary AS\n" +
                "SELECT\n" +
                "    u.id as user_id,\n" +
                "    u.name,\n" +
                "    SUM(o.amount) as total_amount,\n" +
                "    COUNT(o.id) as order_count\n" +
                "FROM users u JOIN orders o ON u.id = o.user_id\n" +
                "GROUP BY u.id, u.name";
    }

}
