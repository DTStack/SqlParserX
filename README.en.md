# SqlParserX

> **English** | [简体中文](README.md)

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Language: Java](https://img.shields.io/badge/Language-Java%208-blue.svg)
![Dialects](https://img.shields.io/badge/Dialects-15%2B-orange.svg)
[![MCP](https://img.shields.io/badge/MCP-SSE-purple.svg)](SqlParserX-mcp-server-sse)

A multi-dialect SQL parsing engine and toolkit: table parsing, table/column lineage parsing, function parsing, SQL splitting, row-level permission SQL filtering, plus a Web service, an MCP Server and a visualization frontend.

SqlParserX is a SQL parsing middleware built on **Druid SQL AST (official druid 1.2.28)**, following a layered design of "syntax parsing layer (Druid) + parsing business layer (extensible per datasource)". The core library `SqlParserX-core` provides a unified `ISqlParser` contract for Java developers; on top of it, a Spring Boot Web service (REST API + static frontend) and an MCP Server (SSE) expose the SQL parsing capability directly to AI agents and LLM applications.

## Features

- **Multi-dialect support**: 15 built-in database dialect parsers (MySQL, Hive, Doris, StarRocks, ClickHouse, Oracle, DM, OceanBase, PostgreSQL, Spark, SQL Server, TiDB, Trino, Greenplum, etc.), with up to 38 datasource types mappable at the Web layer.
- **Table parsing**: resolves the main table and all real tables in SQL, supporting SELECT / INSERT / UPDATE / DELETE / DROP / TRUNCATE / CREATE TABLE statements, WITH AS alias substitution and CTE expansion.
- **Create-table parsing**: parses table name, comment, columns, partitions, buckets, keys and properties from CREATE TABLE (Doris / StarRocks have dedicated extensions for dynamic partitions, DUPLICATE / AGGREGATE Key, etc.).
- **Column lineage parsing**: traces the real source tables/columns of SELECT fields for INSERT / CREATE TABLE AS SELECT, covering subqueries, JOIN, UNION, LATERAL VIEW, CASE, CAST, aggregate functions, OVER clauses, `*` expansion and recursive CTE.
- **Table lineage parsing**: resolves target table, source tables and table relations, supporting WITH AS (including CTE forward-reference inference).
- **Function parsing**: extracts all function names appearing in SQL.
- **SQL splitting**: splits a SQL script with multiple statements, supporting both custom and Druid-native splitting strategies.
- **Row-level permission SQL filtering**: injects row-level permission conditions into INSERT / SELECT / UPDATE / DELETE, handling JOIN, subqueries, UNION and multi-table prefix qualification automatically.
- **Configurable parsing strategies**: column case-insensitive matching, subquery column case-insensitive matching, CTE forward references, etc. (enabled by default for the Doris dialect).
- **MCP Server**: registers the SQL parsing capability as MCP tools over SSE, callable directly by AI agents such as Claude / Qwen.
- **Visualization frontend**: ECharts-based table/column lineage visualization plus online tools for table parsing, create-table parsing, SQL splitting and syntax validation.

## Module Architecture

| Module | Description | Tech Stack |
|---|---|---|
| `SqlParserX-core` | Core parsing library implementing all parsing capabilities | Java 8 + Druid (official 1.2.28) |
| `SqlParserX-web` | Spring Boot REST service exposing HTTP APIs and hosting frontend static resources | Spring Boot 2.3.12 + Java 8 |
| `SqlParserX-mcp-server-sse` | MCP Server (SSE mode) registering core capabilities as MCP tools | Spring Boot 3.4.0 + Spring AI 1.0.0 + Java 17 |
| `SqlParserX-ui` | Frontend toolkit (lineage visualization, parsing/validation/splitting testers) | Vue 2.7 + Element UI 2.x + Vite 5 + ECharts 5 |

```
┌──────────────────────────────────────────────────────┐
│   SqlParserX-ui (Vue 2 + ECharts lineage viz)         │
└───────────────┬──────────────────────────────────────┘
                │ HTTP
┌───────────────▼──────────────────────────────────────┐
│   SqlParserX-web (Spring Boot REST :8888)             │
│   /api/datasource  /api/lineage  /api/sql             │
└───────────────┬──────────────────────────────────────┘
                │
┌───────────────▼──────────────────────────────────────┐
│   SqlParserX-mcp-server-sse (MCP Tools :8067)         │
└───────────────┬──────────────────────────────────────┘
                │ depends on
┌───────────────▼──────────────────────────────────────┐
│   SqlParserX-core (ISqlParser / dialect impls)        │
│   Druid SQL AST (official 1.2.28)                     │
└──────────────────────────────────────────────────────┘
```

### Core Layered Design

`SqlParserX-core` follows the "factory → interface → base class → dialect" layering:

- **Factory** `SqlParserFactory`: returns the dialect parser for a Druid `DbType`, falling back to the default implementation when unmatched.
- **Unified contract** `ISqlParser`: `parseTables` / `parseCreateTable` / `parseColumnLineages` / `parseTableLineages` / `parseFunction` / `parseParameterSql` / `buildSqlByPrepareCompile` / `getRowLevelPermissionSql`.
- **Base class** `BaseSqlParser`: converts SQL to a Druid AST and delegates to the corresponding client (table / lineage / parameter / filter).
- **Dialect implementations**: each datasource extends the base class and overrides table parsing or lineage parsing as needed (e.g., Doris CTE forward references, MySQL RENAME TABLE, Hive column lineage).

## Supported Datasource Dialects

| Category | Dialects |
|---|---|
| Built-in parsers (15) | MySQL, Hive, Doris, StarRocks, ClickHouse, Oracle, DM, OceanBase, OceanBase (Oracle mode), PostgreSQL, Greenplum, Spark, SQL Server, TiDB, Trino |
| Mappable at Web layer (38) | Extended on top of the above: MariaDB, GoldenDB, DRDS, PolarDB-X, ElasticSearch, EDB, GaussDB, Hologres, Redshift, JTDS, H2, Lealone, Blink, DB2, ODPS, Phoenix, Presto, Athena, BigQuery, Snowflake, Informix, Impala, Oscar, Teradata, etc. (mapped to Druid `DbType` via `DataSourceType2DbType`) |

## Quick Start

### 1. Use the Core Library as a Maven Dependency

Add `SqlParserX-core` to your project and obtain a parser from the factory:

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>SqlParserX-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

```java
import com.alibaba.druid.DbType;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);

String sql = "INSERT INTO result_table (a, b) SELECT t1.col1, t2.col2 FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id";

// 1. Table parsing
ParseTableResult tables = sqlParser.parseTables(sql);

// 2. Column lineage parsing (optionally pass a tableColumnsMap metadata to resolve select *)
ColumnLineageResult columnLineage = sqlParser.parseColumnLineages(sql, null);

// 3. Table lineage parsing
TableLineageResult tableLineage = sqlParser.parseTableLineages(sql);
```

**Sample output** (column lineages from `parseColumnLineages` and table lineages from `parseTableLineages`):

```json
// Column lineages
[
  { "fromTable": "table1", "fromColumn": "col1", "toTable": "result_table", "toColumn": "a" },
  { "fromTable": "table2", "fromColumn": "col2", "toTable": "result_table", "toColumn": "b" }
]

// Table lineages
[
  { "fromTable": "table1", "toTable": "result_table" },
  { "fromTable": "table2", "toTable": "result_table" }
]
```

> The full fields of a column lineage item (`ColumnLineage`) are `fromCatalog / fromDb / fromTable / fromColumn / toCatalog / toDb / toTable / toColumn`; a table lineage item (`TableLineage`) contains `fromCatalog / fromDb / fromTable / toCatalog / toDb / toTable`.

> [!TIP]
> See [`SqlParserX-core/README.md`](SqlParserX-core/README.md) for more complete examples and layered extension notes.

### 2. Deploy the Web Service

```bash
# Build and package (core tests are skipped by default; add -DskipTests=false to run them)
mvn -pl SqlParserX-core,SqlParserX-web -am clean package -DskipTests

# Output directory target/dtstack-platform, used together with conf/ external config and bin/ scripts
bin/centos-sqlparserx.sh start     # start, default port 8888
bin/centos-sqlparserx.sh stop      # stop
bin/centos-sqlparserx.sh status    # check status
```

After startup, open `http://localhost:8888` in a browser to use the frontend toolkit (lineage visualization, table parsing, create-table parsing, SQL splitting, syntax validation).

### 3. Start the MCP Server

`SqlParserX-mcp-server-sse` is a standalone Spring Boot application (Java 17) exposing MCP tools over SSE:

```bash
mvn -pl SqlParserX-mcp-server-sse -am clean package -DskipTests
java -jar SqlParserX-mcp-server-sse/target/*.jar
# SSE endpoint: http://localhost:8067/sse
```

Register the SSE service in an MCP-capable client (Claude Desktop / Cursor, etc.) to obtain the following tools:

| MCP Tool | Function |
|---|---|
| `getSupportedDataSourceTypes` | Get all supported datasource types and their datasourceType numbers |
| `parseTables` | Parse all real table names in SQL |
| `parseColumnLineages` | Parse column lineage relations in SQL |
| `parseTableLineages` | Parse table lineage relations in SQL |
| `parseFunction` | Parse function names in SQL |

## REST API

Unified response structure: `{ "code": 200, "msg": "...", "data": ... }` (success code=200, failure code=500).

| Endpoint | Method | Description |
|---|---|---|
| `/api/datasource/types` | GET | Get the list of supported datasource types |
| `/api/datasource/grammarCheck?datasourceType=` | POST | SQL syntax validation (body is plain-text SQL) |
| `/api/datasource/parseTables` | POST | Table parsing, body is `{datasourceType, sql}` |
| `/api/datasource/parseCreateTable` | POST | Create-table statement parsing, body is `{datasourceType, sql}` |
| `/api/lineage/query?datasourceType=` | POST | Column lineage query (body is plain-text SQL), returns `tableData` + `relationData` for lineage visualization |
| `/api/sql/split?datasourceType=` | POST | SQL script splitting (body is plain-text SQL) |

## Testing

`SqlParserX-core` ships about 78 unit test cases covering:

- Table lineage: `CREATE TABLE AS SELECT` / `INSERT SELECT` / `WITH AS INSERT`
- Column lineage: the same three scenarios, covering aggregates, subqueries, JOIN, UNION, CASE, OVER, etc.
- Table parsing, function parsing, SQL filtering (row-level permission injection)
- Doris specific: column lineage, table lineage, create-table parsing

```bash
# Run all core module tests (surefire skipTests=true by default; enable explicitly)
mvn -pl SqlParserX-core test -DskipTests=false
```

Test entry: `SqlParserX-core/src/test/java/com/changlu/sqlparserx/core/AllTestsSuite.java` (`BaseSuite` + `DatasourceSuite` + strategy unit tests).

## Directory Structure

```
SqlParserX
├── SqlParserX-core/            # Core parsing library (Java 8)
│   └── src/main/java/com/changlu/sqlparserx/core
│       ├── parser/             # Factory, unified interface, base class and 15 dialect implementations
│       │   ├── base/           # Base implementations for table/lineage/parameter/filter
│       │   ├── mysql/ hive/ doris/ starrocks/ ...   # Dialect extensions
│       │   └── ...
│       ├── select/             # SELECT tracing engine (shared by column/table lineage)
│       ├── enums/              # Parsing strategy, match strategy, column type enums
│       └── utils/              # SQL splitting, table source parsing utilities
├── SqlParserX-web/             # Spring Boot REST service (:8888)
├── SqlParserX-mcp-server-sse/  # MCP Server, SSE mode (:8067)
├── SqlParserX-ui/              # Vue2 + Vite frontend toolkit
├── bin/                        # Start/stop scripts (centos-sqlparserx.sh / base.sh)
├── conf/                       # External config: application.properties, logback.xml, static frontend assets
└── assembly.xml                # Directory-style deployment packaging descriptor
```

## Known Limitations

- Parameter parsing (`parseParameterSql` / `buildSqlByPrepareCompile`) and `filterSortSelectField` are placeholder implementations at the base layer (returning empty results); they are reserved capabilities not yet implemented.
- Maven surefire for `SqlParserX-core` sets `skipTests=true` by default, so CI builds do not run tests by default.
- The frontend `SqlParserX-ui/README.md` still contains the Vite template default text; the actual stack is Vue 2.7 + Element UI 2.x, which does not match the template text.

## Contributing

Contributions of any kind are welcome: bug fixes, new dialect support, lineage parsing enhancements, documentation improvements, etc.

- **Issues**: Please open an issue first to describe the reproduction steps and expected behavior when reporting bugs or requesting new capabilities.
- **Pull Requests**: Fork this repository, develop on a feature branch (e.g. `feat_1.0.x_x`), follow the existing layered extension pattern (add a dialect implementation under the `parser` package or override the corresponding client), add relevant test cases, then submit a Pull Request.
- **Development guidelines**: Keep the core library Java 8 compatible; prefer extending `BaseSqlParser` for new dialects and only override methods where real differences exist; organize test cases following the `AllTestsSuite` suite pattern.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
