# 快速使用指南

针对不同数据源除了解析语法树之外，对于语法树再进行额外单独提供了不同的扩展解析能力。

特性：

1、基于数据源实现不同解析能力扩展

2、支持功能：
 
+   解析表
+   解析函数
+   解析字段血缘
+   解析表血缘


示范：

```java
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
```

# 关于分层能力扩展

语法解析分层：druid-core模块内部

解析业务层分层：datasourcex模块
分层目的
+   1、解耦
+   2、针对不同数据源可重写相应的解析实现
 
 主要一个场景就是由于语法解析层中可能会基于base类进行扩展，那么对应相应解析实现也应当在对应数据源去做扩展维护处理
 
示范：解析表操作

举例对于mysql中对于某个sql实现解析，去自定义了MySqlRenameTableStatement类在druid-core中

而对于通用的ParseTableClient里没有对MySqlRenameTableStatement处理，此时我们就可以在包目录下：

com.changlu.sqlparserx.core.parser.base.table.client.mysql

自定义一个MySqlParseTableClient类，继承ParseTableClient，重写parseTables方法，额外对MySqlRenameTableStatement进行解析处理即可。