---
title: Table Parsing
description: Parse real tables, CTEs, CREATE TABLE, partitions and properties from SQL
sidebar_label: Table Parsing
sidebar_position: 3
---

# Table Parsing

## 1. Add the Dependency

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>sqlparserx-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

> `sqlparserx-core` transitively brings in `sqlparserx-syntax` and Druid.

## 2. Complete Example

Using the official `sqlparserx-core/README.md` sample SQL to demonstrate both "parse real tables" and "parse CREATE TABLE":

```java title="TableParseDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.DbType;

public class TableParseDemo {

    public static void main(String[] args) {
        String sql = "CREATE TABLE user_order_summary AS\n"
                + "SELECT\n"
                + "    u.id as user_id,\n"
                + "    u.name,\n"
                + "    SUM(o.amount) as total_amount,\n"
                + "    COUNT(o.id) as order_count\n"
                + "FROM users u JOIN orders o ON u.id = o.user_id\n"
                + "GROUP BY u.id, u.name";

        // 1. Get a parser
        ISqlParser parser = SqlParserFactory.getSqlParser(DbType.mysql);

        // 2. Parse real tables in the SQL
        ParseTableResult tableResult = parser.parseTables(sql);
        System.out.println("main table: " + tableResult.getMainTable().getName());
        System.out.println("all tables: " + tableResult.getAllTableName());

        // 3. Parse the CREATE TABLE structure
        ParseCreateTableResult createResult = parser.parseCreateTable(sql);
        System.out.println("created table: " + createResult.getDb() + "." + createResult.getTableName());
        System.out.println("column count: " + createResult.getTableElementList().size());
    }
}
```

Console output (example):

```text
main table: users
all tables: [user_order_summary, users, orders]
created table: null.user_order_summary
column count: 4
```

## 3. Goal

Identify **real tables** from SQL text, including query tables, CTEs, `CREATE TABLE` definitions, partitions and table properties.

```text
SQLStatement AST
  → ParseTableClient.parseTables / parseCreateTable
  → collect tables / CTEs / DDL / partitions / properties
  → ParseTableResult / ParseCreateTableResult
```

## 4. Entry & Implementation

| Item | Content |
|---|---|
| Core entry | `ISqlParser.parseTables(sql)`, `ISqlParser.parseCreateTable(sql)` |
| Default impl | `BaseSqlParser.parseTables` → `ParseTableClient` |
| Return type | `ParseTableResult`, `ParseCreateTableResult` |
| Coverage | query tables, CTE, `CREATE TABLE`, partitions, properties |

## 5. Supported Scenarios

| Capability | Status | Notes |
|---|---|---|
| Query table detection | **Supported ✅** | SELECT / INSERT / UPDATE / DELETE |
| CTE detection | **Supported ✅** | WITH-defined temporary tables |
| `CREATE TABLE` DDL | **Supported ✅** | Table name, columns, types, comments |
| Partitions & properties | **Supported ✅** | Dialect differences internalized in syntax layer |
| `catalog.db.table` | **Supported ✅** | Multi-catalog scenarios |