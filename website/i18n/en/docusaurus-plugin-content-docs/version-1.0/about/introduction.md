---
title: Introduction
description: Positioning, capabilities and modules of SqlParserX
sidebar_label: Introduction
sidebar_position: 1
---

# Introduction to SqlParserX

SqlParserX is a **multi-dialect SQL parsing engine and toolkit** offering SQL type detection, table/DDL parsing, table and column lineage, function extraction, parameter extraction, and prepared SQL generation.

## Problems It Solves

| Problem | Solution |
|---|---|
| Dialects differ greatly | Route Druid dialects via `DbType` with a unified AST |
| Real tables and DDL hide in SQL text | Table, CTE, `CREATE TABLE`, partition and property parsing |
| Data dependencies are hard to trace | Table/column lineage across JOIN, UNION, subqueries, aggregates and CTEs |
| Scripts contain multiple statements | SQL splitting and syntax validation |
| Permission predicates are hard to maintain | Row-level permission SQL rewriting |

## Modules

| Module | Layer | Description | Technology |
|---|---|---|---|
| `sqlparserx-syntax` | Syntax parsing | SQL-to-AST, validation, splitting and Druid dialect extensions | Java 8 + Druid 1.2.28 |
| `sqlparserx-core` | Business parsing | Tables, lineage, DDL, functions and SQL rewriting | Java 8 + `sqlparserx-syntax` |
| `sqlparserx-web` | Access layer | Spring Boot REST service | Spring Boot 2.3.12 + Java 8 |
| `sqlparserx-ui` | Access layer | Lineage visualization and online parsing tools | Vue 2.7 + Element UI 2.x + Vite 5 + ECharts 5 |

## Supported Datasources

| Datasource | Support |
|---|---|
| MySQL | Priority support with enhanced `RENAME TABLE` handling |
| Doris | Strong support with dedicated DDL and lineage tests |
| StarRocks | Dedicated `DISTRIBUTED BY` and key DDL support |
| Hive / ClickHouse / Oracle / DM | Registered parsers with common business implementation |

## Further Reading

- [Architecture](./architecture.md)
- Background: [The Origin of SqlParserX](/blog/origin-of-sqlparserx)
