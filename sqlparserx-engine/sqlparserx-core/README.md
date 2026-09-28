# 快速使用指南

sqlparserx-core 是 SqlParserX 的核心解析模块。它在 Druid 语法树的基础上，针对不同数据源提供了统一的扩展解析能力：只需传入 SQL 与数据源类型，即可获得表、函数、字段血缘、表血缘等结构化结果。

## 特性

- **面向数据源扩展**：基于数据源实现差异化解析，同一套 API 覆盖多方言 SQL。
- **统一解析能力**，包含：
  - 解析表
  - 解析函数
  - 解析字段血缘
  - 解析表血缘

## 使用示范

```java
package com.dtstack.sqlparserx.core;

import com.alibaba.druid.DbType;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;
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

SqlParserX 采用分层设计，便于解耦与按数据源定制解析逻辑：

- **语法解析层**：位于 `sqlparserx-syntax` 模块，基于 Druid 负责 SQL 语法、Lexer/Parser 和 AST 解析扩展。
- **解析业务层**：位于本模块（`sqlparserx-core`），基于语法层输出的 AST 负责面向业务场景的解析能力。

分层的目的：

1. 解耦：语法解析与业务解析职责分离。
2. 可扩展：不同数据源可重写相应的解析实现。

主要场景：当 `sqlparserx-syntax` 语法层针对某个数据源扩展了 Lexer、Parser 或 AST 节点时，`sqlparserx-core` 中对应数据源的业务解析实现也应同步扩展维护。

## 扩展示范：解析表操作

以 MySQL 为例，语法解析层在 `sqlparserx-syntax` 中自定义了 `MySqlRenameTableStatement`，而通用的 `ParseTableClient` 并未处理该节点。此时可在如下包目录下：

```
com.dtstack.sqlparserx.core.parser.base.table.client.mysql
```

自定义一个 `MySqlParseTableClient` 类，继承 `ParseTableClient` 并重写 `parseTables` 方法，额外对 `MySqlRenameTableStatement` 进行解析处理即可。
