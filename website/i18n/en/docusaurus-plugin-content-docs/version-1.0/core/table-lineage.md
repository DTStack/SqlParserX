---
title: Table Lineage
description: Parse source-to-target table lineage from SQL
sidebar_label: Table Lineage
sidebar_position: 2
---

# Table Lineage

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

Using the official `sqlparserx-core/README.md` sample SQL, from parser creation to printing table lineage:

```java title="TableLineageDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

import com.alibaba.druid.DbType;

import java.util.List;

public class TableLineageDemo {

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

        // 2. Parse table lineage
        TableLineageResult result = parser.parseTableLineages(sql);

        // 3. Read the result
        System.out.println("target table: " + result.getTargetTable());
        System.out.println("source tables: " + result.getSourceTables());

        List<TableLineage> tableLineages = result.getTableLineages();
        for (TableLineage lineage : tableLineages) {
            System.out.println(lineage.getFromTable() + " -> " + lineage.getToTable());
        }
    }
}
```

Console output (example):

```text
target table: user_order_summary
source tables: [users, orders]
users -> user_order_summary
orders -> user_order_summary
```

## 3. Goal

Parse **target table → source table** table-level lineage from SQL, covering JOIN, UNION, subqueries, aggregates and CTEs.

```text
SQLStatement AST
  → TableLineageClient.parseTableLineages
  → collect source tables + target tables
  → TableLineageResult
```

## 4. Entry & Implementation

| Item | Content |
|---|---|
| Core entry | `ISqlParser.parseTableLineages(sql)` |
| Default impl | `BaseSqlParser.parseTableLineages` → `TableLineageClient` |
| Return type | `TableLineageResult` (`getTargetTable()` + `getSourceTables()` + `getTableLineages()`) |
| Coverage | JOIN, UNION, subquery, aggregate, CTE |

## 5. Supported Scenarios

| Capability | Status | Notes |
|---|---|---|
| Single/multi-table JOIN | **Supported ✅** | All participating tables |
| UNION / UNION ALL | **Supported ✅** | Merge branches |
| Subquery / derived table | **Supported ✅** | Recursively collect real tables |
| CTE (WITH) | **Supported ✅** | Recognize CTE and real sources |
| `INSERT INTO ... SELECT` | **Supported ✅** | Output target and source tables |