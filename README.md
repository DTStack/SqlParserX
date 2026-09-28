# SqlParserX

> **English** | [简体中文](README_zh-CN.md)

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Language: Java](https://img.shields.io/badge/Language-Java%208-blue.svg)
![Dialects](https://img.shields.io/badge/Dialects-15%2B-orange.svg)

## Introduction

SqlParserX is a multi-dialect SQL parsing engine and toolkit for table/DDL parsing, table and column lineage, function extraction, SQL splitting and row-level permission rewriting.

It is built around two layers: `sqlparserx-syntax` parses SQL into Druid ASTs and owns syntax extensions; `sqlparserx-core` consumes those ASTs through the unified `ISqlParser` contract and provides business parsing results. `sqlparserx-web` and `sqlparserx-ui` provide service and visualization access.

SqlParserX originated from practical work on multi-dialect SQL parsing, AST unification and reusable business parsing. It gradually evolved from an internal parsing refactoring into an independent two-layer SQL parsing engine. For the project background, internal refactoring journey and design rationale, see [The Origin and Internal Refactoring of SqlParserX](docs/blogs/20260928：SqlParserX的诞生起源背景%26内部改造.md).

| Problem | Solution |
|---|---|
| Different SQL dialects | Druid dialect routing through `DbType` and a unified AST |
| Real tables and DDL are hidden in SQL text | Table, CTE, CREATE TABLE, partition and property parsing |
| Data dependencies are difficult to trace | Table and column lineage across JOIN, UNION, subqueries, aggregates and CTEs |
| Scripts contain multiple statements | SQL splitting and syntax validation |
| Permission predicates are hard to maintain | Row-level permission SQL rewriting |

## Architecture

![SqlParserX two-layer unified extension architecture](docs/架构设计/SqlParserX两层架构-en.png)

> English editable source: [SqlParserX两层架构-en.drawio](docs/架构设计/SqlParserX两层架构-en.drawio). See the [architecture design](docs/架构设计/架构设计.md) for details.

```text
Access: sqlparserx-web (REST) / sqlparserx-ui
                 │ unified ISqlParser contract
Business layer:  sqlparserx-core
                 │ SQL + DbType / SQLStatement AST
Syntax layer:    sqlparserx-syntax (Druid 1.2.28)
```

| Module | Layer | Description | Technology |
|---|---|---|---|
| `sqlparserx-syntax` | Syntax parsing | SQL-to-AST, validation, splitting and Druid dialect extensions | Java 8 + Druid 1.2.28 |
| `sqlparserx-core` | Business parsing | Tables, lineage, DDL, functions and SQL rewriting | Java 8 + `sqlparserx-syntax` |
| `sqlparserx-web` | Access layer | Spring Boot REST service | Spring Boot 2.3.12 + Java 8 |
| `sqlparserx-ui` | Access layer | Lineage visualization and online parsing tools | Vue 2.7 + Element UI 2.x + Vite 5 + ECharts 5 |

Business extensions follow `SqlParserFactory` → `ISqlParser` → `BaseSqlParser` → dialect implementations. See [`sqlparserx-core/README.md`](sqlparserx-engine/sqlparserx-core/README.md).

## Support Datasource

The Web list is derived from Core's registered parsers; compatibility-only mappings are not advertised as independent support.

| Datasource | Business parsing documentation | Syntax parsing documentation | Support description |
|---|---|---|---|
| MySQL | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/mysql/SQL业务解析支持情况.md) | [Syntax](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/mysql/SQL语法解析支持情况.md) | Priority support; enhanced RENAME TABLE handling. |
| Doris | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/doris/SQL业务解析支持情况.md) | [Syntax](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/doris/SQL语法解析支持情况.md) | Strong DDL and lineage support with dedicated tests. |
| StarRocks | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/starrocks/SQL业务解析支持情况.md) | [Syntax](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/starrocks/SQL语法解析支持情况.md) | Dedicated DISTRIBUTED BY and key DDL support. |
| Hive | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| ClickHouse | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| Oracle | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| DM | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| OceanBase | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| OceanBase (Oracle mode) | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| PostgreSQL | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| Greenplum | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| Spark | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| SQL Server | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |
| TiDB | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Independent TiDB parser in Core and Web. |
| Trino | [Business](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [Syntax](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Registered parser; common business implementation. |

## Parsing Capability Support

| Documentation | Scope |
|---|---|
| [SQL syntax parsing support](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Druid version and syntax-layer coverage |
| [SQL business parsing support](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | Two-layer datasource matrix and business capability index |
| [Module dependency design](docs/02、SQL业务层解析能力支撑情况/模块依赖包引用结构设计.md) | Syntax/core/engine dependency boundaries |
| [Architecture design](docs/架构设计/架构设计.md) | Layer responsibilities and extension model |

Current capabilities: table parsing, CREATE TABLE parsing, table/column lineage, function extraction, SQL splitting and row-level permission rewriting. Syntax support and business support must be checked together.

## Quick Start

### Core library

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>sqlparserx-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

See [`sqlparserx-core/README.md`](sqlparserx-engine/sqlparserx-core/README.md) for the complete Java example.

### Web service

```bash
mvn -pl sqlparserx-engine/sqlparserx-core,sqlparserx-web -am clean package -DskipTests
bin/centos-sqlparserx.sh start   # default port 8888
bin/centos-sqlparserx.sh stop
bin/centos-sqlparserx.sh status
```

Open `http://localhost:8888` for the frontend tools.

## Questions

For questions, bugs and support requests, please open an issue with the datasource, SQL sample, expected result and actual result. See the [FAQ reference](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md).

## Contribution

Please read the [Contributing Guide](docs/02、SQL业务层解析能力支撑情况/标准迭代维护扩展文档/SqlParserX解析能力标准迭代维护扩展指南.md) before opening a pull request. Keep the core library Java 8 compatible and place new logic in the correct syntax or business layer.

## Contributor

Issues and pull requests are welcome. See the [project contributors and commit history](https://gitlab.prod.dtstack.cn/changlu/SqlParserX/-/graphs/master).

## License

SqlParserX is licensed under the [Apache License 2.0](LICENSE).
