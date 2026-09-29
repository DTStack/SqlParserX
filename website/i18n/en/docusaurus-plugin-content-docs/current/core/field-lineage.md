---
title: Field Lineage
description: Derive source.table.source_column → target.table.target_column lineage from SQL
sidebar_label: Field Lineage
sidebar_position: 1
---

# Field Lineage

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

A **complete runnable** class from parser creation to printing column lineage, using the official `sqlparserx-core/README.md` sample SQL:

```java title="FieldLineageDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import com.alibaba.druid.DbType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldLineageDemo {

    public static void main(String[] args) {
        String sql = "CREATE TABLE user_order_summary AS\n"
                + "SELECT\n"
                + "    u.id as user_id,\n"
                + "    u.name,\n"
                + "    SUM(o.amount) as total_amount,\n"
                + "    COUNT(o.id) as order_count\n"
                + "FROM users u JOIN orders o ON u.id = o.user_id\n"
                + "GROUP BY u.id, u.name";

        // 1. Metadata: table -> columns (used for select * / column expansion)
        Map<String, List<Column>> tableColumnsMap = new HashMap<>();
        tableColumnsMap.put("users", Arrays.asList(
                new Column("id", 0), new Column("name", 1)));
        tableColumnsMap.put("orders", Arrays.asList(
                new Column("amount", 0), new Column("id", 1)));

        // 2. Get a parser
        ISqlParser parser = SqlParserFactory.getSqlParser(DbType.mysql);

        // 3. Parse column lineage
        ColumnLineageResult result = parser.parseColumnLineages(sql, tableColumnsMap);

        // 4. Print result: target table + target column -> source column
        System.out.println("target table: " + result.getTargetTable());
        result.printColumnLineage();
    }
}
```

Console output (example):

```text
target table: user_order_summary
users.id -> user_order_summary.user_id
users.name -> user_order_summary.name
orders.amount -> user_order_summary.total_amount
orders.id -> user_order_summary.order_count
```

## 3. Goal

Derive **source table.column → target table.column** column-level lineage from SQL.

The core idea: normalize the source select field list and target field list into sets of `(real column, alias, real table)` triples, then generate lineage by fixed matching rules. Field lineage takes insert/create-table SQL as input; pure query SQL produces no lineage.

```text
SQLInsertStatement / SQLCreateTableStatement(AS SELECT)
  → ColumnLineageClient.parseColumnLineages
  → target column list + source select column list
  → addColumnRelations match one by one
  → ColumnLineageResult
```

## 4. Entry & Implementation

| Item | Content |
|---|---|
| Core entry | `ISqlParser.parseColumnLineages(sql, tableColumnsMap)` |
| Default impl | `BaseSqlParser.parseColumnLineages` → `ColumnLineageClient` |
| Return type | `ColumnLineageResult` (`getColumnRelations()` + `getColumnLineages()`) |
| Applicable | `SQLInsertStatement`, `SQLCreateTableStatement` (AS SELECT) |
| Metadata input | `Map<String, List<Column>> tableColumnsMap` |

## 5. Supported Scenarios

| Capability | Status | Notes |
|---|---|---|
| `catalog.db.table.column` | **Supported ✅** | Multi-catalog scenarios such as Hive/Trino/StarRocks |
| `db.table.column` | **Supported ✅** | Standard MySQL/PostgreSQL usage |
| Column expressions (`SQLBinaryOpExpr`) | **Supported ✅** | e.g. `t5.level + 1`, recursively collect columns |
| `*` / `xx.*` expansion | **Supported ✅** | Read from metadata `tableColumnsMap`; keep `*` without metadata |