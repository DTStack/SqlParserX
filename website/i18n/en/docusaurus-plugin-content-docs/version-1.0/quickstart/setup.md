---
title: Quick Start
description: Add the dependency, build and run SqlParserX locally
sidebar_label: Setup & Build
sidebar_position: 1
---

# Quick Start

## 1. Requirements

| Dependency | Version |
|---|---|
| JDK | Java 8+ |
| Maven | 3.6+ |
| Node.js (only `sqlparserx-ui`) | >= 20 |

## 2. Add the Dependency

Add the **business parsing layer** `sqlparserx-core` to your `pom.xml`:

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>sqlparserx-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

:::tip Dependency note
`sqlparserx-core` transitively brings in `sqlparserx-syntax` (syntax layer) and official Druid, so business parsing only needs the single `core` coordinate. If you only need SQL→AST syntax parsing, you can depend on `sqlparserx-syntax` alone.
:::

:::note Use from source
If `1.0-SNAPSHOT` is not published to a remote repository yet, build and install it locally first:

```bash
git clone https://github.com/DTStack/SqlParserX.git
cd SqlParserX
mvn clean install -DskipTests
```
:::

## 3. Hello World: Parse a SQL

A **complete runnable** class covering table parsing, column lineage, table lineage and functions, consistent with the official `sqlparserx-core/README.md` demo:

```java title="SqlParserXDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.DbType;

import java.util.*;

public class SqlParserXDemo {

    public static void main(String[] args) {
        String sql = "CREATE TABLE user_order_summary AS\n"
                + "SELECT\n"
                + "    u.id as user_id,\n"
                + "    u.name,\n"
                + "    SUM(o.amount) as total_amount,\n"
                + "    COUNT(o.id) as order_count\n"
                + "FROM users u JOIN orders o ON u.id = o.user_id\n"
                + "GROUP BY u.id, u.name";

        // 1. Get a parser by datasource type (mysql/hive/doris/... 15+ dialects)
        ISqlParser parser = SqlParserFactory.getSqlParser(DbType.mysql);

        // 2. Parse real tables
        ParseTableResult tableResult = parser.parseTables(sql);
        System.out.println("main table: " + tableResult.getMainTable().getName());
        System.out.println("all tables: " + tableResult.getAllTableName());

        // 3. Parse column lineage (pass metadata for select * cases)
        Map<String, List<Column>> tableColumnsMap = new HashMap<>();
        tableColumnsMap.put("users", Arrays.asList(new Column("id", 0), new Column("name", 1)));
        tableColumnsMap.put("orders", Arrays.asList(new Column("amount", 0), new Column("id", 1)));
        ColumnLineageResult columnLineageResult =
                parser.parseColumnLineages(sql, tableColumnsMap);
        columnLineageResult.printColumnLineage();

        // 4. Parse table lineage
        TableLineageResult tableLineageResult = parser.parseTableLineages(sql);
        List<TableLineage> tableLineages = tableLineageResult.getTableLineages();
        System.out.println("table lineage: " + tableLineages);

        // 5. Parse functions
        Set<String> functions = parser.parseFunction(sql);
        System.out.println("functions: " + functions);
    }
}
```

Console output (example):

```text
main table: users
all tables: [user_order_summary, users, orders]
users.id -> user_order_summary.user_id
users.name -> user_order_summary.name
orders.amount -> user_order_summary.total_amount
orders.id -> user_order_summary.order_count
table lineage: [TableLineage{fromCatalog='null', fromDb='null', fromTable='users', toCatalog='null', toDb='null', toTable='user_order_summary'}, ...]
functions: [sum, count]
```

## 4. Next Steps

Dependency + complete example for each capability:

- [Field lineage](../core/field-lineage.md)
- [Table lineage](../core/table-lineage.md)
- [Table parsing](../core/table-parse.md)
- [Function parsing](../core/function-parse.md)