---
title: Function Parsing
description: Extract functions, arguments and contexts used in SQL
sidebar_label: Function Parsing
sidebar_position: 4
---

# Function Parsing

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

Using the official `sqlparserx-core/README.md` sample SQL to extract the set of functions:

```java title="FunctionParseDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;

import com.alibaba.druid.DbType;

import java.util.Set;

public class FunctionParseDemo {

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

        // 2. Parse functions used in the SQL
        Set<String> functions = parser.parseFunction(sql);

        // 3. Read the result
        System.out.println("functions: " + functions);
    }
}
```

Console output (example):

```text
functions: [sum, count]
```

## 3. Goal

Extract **function calls** from SQL as a de-duplicated set of function names for function-level governance and dependency analysis.

```text
SQLStatement AST
  → BaseSqlParser.parseFunction
  → traverse AST to collect function expressions
  → Set<String> functions
```

## 4. Entry & Implementation

| Item | Content |
|---|---|
| Core entry | `ISqlParser.parseFunction(sql)` |
| Default impl | `BaseSqlParser.parseFunction` |
| Return type | `Set<String>` (function names, lower-cased) |
| Coverage | built-in, aggregate, window, custom functions |

## 5. Supported Scenarios

| Capability | Status | Notes |
|---|---|---|
| Regular function calls | **Supported ✅** | e.g. `substr(name, 1, 3)` |
| Aggregate functions | **Supported ✅** | e.g. `sum`, `count`, `max` |
| Window functions | **Supported ✅** | e.g. `row_number() over(...)` |
| Nested functions | **Supported ✅** | Recursively collect inner and outer functions |