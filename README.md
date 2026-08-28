# SqlParserX

> [English](README.en.md) | **简体中文**

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Language: Java](https://img.shields.io/badge/Language-Java%208-blue.svg)
![Dialects](https://img.shields.io/badge/Dialects-15%2B-orange.svg)
[![MCP](https://img.shields.io/badge/MCP-SSE-purple.svg)](SqlParserX-mcp-server-sse)

> 多数据库方言 SQL 解析引擎与工具集：表解析、表/字段血缘解析、函数解析、SQL 切分、行级权限 SQL 过滤，并提供 Web 服务、MCP Server 与可视化前端。

SqlParserX 是一个基于 **Druid SQL AST（二开 druid 1.2.25）** 构建的 SQL 解析中间件，采用「语法解析层（Druid） + 解析业务层（按数据源扩展）」的分层设计。核心库 `SqlParserX-core` 面向 Java 开发者提供统一的 `ISqlParser` 解析契约；上层封装了 Spring Boot Web 服务（REST API + 静态前端）与 MCP Server（SSE），可将 SQL 解析能力直接暴露给 AI Agent 与大模型应用。

## 特性

- **多方言支持**：内置 15 种数据库方言解析实现（MySQL、Hive、Doris、StarRocks、ClickHouse、Oracle、达梦、OceanBase、PostgreSQL、Spark、SQL Server、TiDB、Trino、Greenplum 等），Web 层可映射 38 种数据源类型。
- **表解析**：解析 SQL 中的主表与全部真实表，支持 SELECT / INSERT / UPDATE / DELETE / DROP / TRUNCATE / CREATE TABLE 等语句，支持 WITH AS 别名替换与 CTE 展开。
- **建表解析**：解析 CREATE TABLE 的表名、注释、字段、分区、分桶、键与 properties（Doris / StarRocks 有专属扩展，支持动态分区、DUPLICATE / AGGREGATE Key）。
- **字段血缘解析**：针对 INSERT / CREATE TABLE AS SELECT 追溯 select 字段的真实来源表与字段，覆盖子查询、JOIN、UNION、LATERAL VIEW、CASE、CAST、聚合函数、OVER 子句、`*` 展开与递归 CTE。
- **表血缘解析**：解析目标表与来源表集合及表间关系，支持 WITH AS（含 CTE 前向引用推断）。
- **函数解析**：提取 SQL 中出现的全部函数名。
- **SQL 切分**：将包含多条语句的 SQL 脚本按语句切分，支持自定义与 Druid 原生两种切分策略。
- **行级权限 SQL 过滤**：向 INSERT / SELECT / UPDATE / DELETE 注入行级权限条件，自动处理 JOIN、子查询、UNION 与多表前缀限定。
- **解析策略可配置**：字段匹配忽略大小写、子查询字段忽略大小写、CTE 前向引用等策略（Doris 方言默认启用）。
- **MCP Server**：以 SSE 方式将 SQL 解析能力注册为 MCP 工具，供 Claude / 通义等 AI Agent 直接调用。
- **可视化前端**：基于 ECharts 的字段/表血缘可视化，以及表解析、建表解析、SQL 切分、语法校验等在线工具。

## 模块架构

| 模块 | 说明 | 技术栈 |
|---|---|---|
| `SqlParserX-core` | 核心解析库，所有解析能力的实现载体 | Java 8 + Druid（二开 1.2.25） |
| `SqlParserX-web` | Spring Boot REST 服务，提供 HTTP API 并托管前端静态资源 | Spring Boot 2.3.12 + Java 8 |
| `SqlParserX-mcp-server-sse` | MCP Server（SSE 模式），将核心能力注册为 MCP 工具 | Spring Boot 3.4.0 + Spring AI 1.0.0 + Java 17 |
| `SqlParserX-ui` | 前端工具集（血缘可视化、解析/校验/切分测试） | Vue 2.7 + Element UI 2.x + Vite 5 + ECharts 5 |

```
┌──────────────────────────────────────────────────────┐
│   SqlParserX-ui (Vue 2 + ECharts 血缘可视化)           │
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
                │ 依赖
┌───────────────▼──────────────────────────────────────┐
│   SqlParserX-core (ISqlParser / 方言实现)              │
│   Druid SQL AST（二开 1.2.25）                          │
└──────────────────────────────────────────────────────┘
```

### 核心分层设计

`SqlParserX-core` 遵循「工厂 → 接口 → 基类 → 方言」的分层：

- **工厂** `SqlParserFactory`：按 Druid `DbType` 返回对应方言解析器，未匹配则回退默认实现。
- **统一契约** `ISqlParser`：`parseTables` / `parseCreateTable` / `parseColumnLineages` / `parseTableLineages` / `parseFunction` / `parseParameterSql` / `buildSqlByPrepareCompile` / `getRowLevelPermissionSql`。
- **基类** `BaseSqlParser`：将 SQL 转为 Druid AST 后委派给对应的 Client（表 / 血缘 / 参数 / 过滤）。
- **方言实现**：各数据源继承基类，按需重写表解析、血缘解析等（如 Doris 的 CTE 前向引用、MySQL 的 RENAME TABLE、Hive 的字段血缘）。

## 支持的数据源方言

| 分类 | 方言 |
|---|---|
| 内置解析器（15 种） | MySQL、Hive、Doris、StarRocks、ClickHouse、Oracle、DM（达梦）、OceanBase、OceanBase(Oracle 模式)、PostgreSQL、Greenplum、Spark、SQL Server、TiDB、Trino |
| Web 层可映射类型（38 种） | 在以上基础上扩展：MariaDB、GoldenDB、DRDS、PolarDB-X、ElasticSearch、EDB、GaussDB、Hologres、Redshift、JTDS、H2、Lealone、Blink、DB2、ODPS、Phoenix、Presto、Athena、BigQuery、Snowflake、Informix、Impala、Oscar、Teradata 等（经 `DataSourceType2DbType` 映射到 Druid `DbType`） |

## 快速开始

### 1. 作为 Maven 依赖使用核心库

在项目中引入 `SqlParserX-core`，通过工厂获取解析器：

```xml
<dependency>
    <groupId>com.changlu</groupId>
    <artifactId>SqlParserX-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

```java
import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);

String sql = "INSERT INTO result_table (a, b) SELECT t1.col1, t2.col2 FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id";

// 1. 表解析
ParseTableResult tables = sqlParser.parseTables(sql);

// 2. 字段血缘解析（可选传入 tableColumnsMap 元数据解决 select * 场景）
ColumnLineageResult columnLineage = sqlParser.parseColumnLineages(sql, null);

// 3. 表血缘解析
TableLineageResult tableLineage = sqlParser.parseTableLineages(sql);
```

**输出示例**（以字段血缘 `columnLineages` 与表血缘 `tableLineages` 为例）：

```json
// parseColumnLineages 返回的字段血缘
[
  { "fromTable": "table1", "fromColumn": "col1", "toTable": "result_table", "toColumn": "a" },
  { "fromTable": "table2", "fromColumn": "col2", "toTable": "result_table", "toColumn": "b" }
]

// parseTableLineages 返回的表血缘
[
  { "fromTable": "table1", "toTable": "result_table" },
  { "fromTable": "table2", "toTable": "result_table" }
]
```

> 字段血缘明细（`ColumnLineage`）完整字段为 `fromCatalog / fromDb / fromTable / fromColumn / toCatalog / toDb / toTable / toColumn`；表血缘明细（`TableLineage`）为 `fromCatalog / fromDb / fromTable / toCatalog / toDb / toTable`。

> [!TIP]
> 更完整的示例与分层扩展说明见 [`SqlParserX-core/README.md`](SqlParserX-core/README.md)。

### 2. 部署 Web 服务

```bash
# 编译打包（core 的测试默认跳过，需执行测试时加 -DskipTests=false）
mvn -pl SqlParserX-core,SqlParserX-web -am clean package -DskipTests

# 产物目录 target/dtstack-platform，配合 conf/ 外部配置与 bin/ 启停脚本使用
bin/centos-sqlparserx.sh start     # 启动，默认端口 8888
bin/centos-sqlparserx.sh stop      # 停止
bin/centos-sqlparserx.sh status    # 查看状态
```

启动后浏览器访问 `http://localhost:8888` 即可使用前端工具集（血缘可视化、表解析、建表解析、SQL 切分、语法校验）。

### 3. 启动 MCP Server

`SqlParserX-mcp-server-sse` 为独立 Spring Boot 应用（Java 17），以 SSE 方式对外提供 MCP 工具：

```bash
mvn -pl SqlParserX-mcp-server-sse -am clean package -DskipTests
java -jar SqlParserX-mcp-server-sse/target/*.jar
# SSE 端点：http://localhost:8067/sse
```

在支持 MCP 的客户端（Claude Desktop / Cursor 等）中注册该 SSE 服务，即可获得以下工具：

| MCP 工具 | 功能 |
|---|---|
| `getSupportedDataSourceTypes` | 获取所有支持的数据源类型及 datasourceType 数字 |
| `parseTables` | 解析 SQL 中的全部真实表名 |
| `parseColumnLineages` | 解析 SQL 中的字段血缘关系 |
| `parseTableLineages` | 解析 SQL 中的表血缘关系 |
| `parseFunction` | 解析 SQL 中的函数名 |

## REST API

统一响应结构：`{ "code": 200, "msg": "...", "data": ... }`（成功 code=200，失败 code=500）。

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/datasource/types` | GET | 获取支持的数据源类型列表 |
| `/api/datasource/grammarCheck?datasourceType=` | POST | SQL 语法校验（body 为纯文本 SQL） |
| `/api/datasource/parseTables` | POST | 表解析，body 为 `{datasourceType, sql}` |
| `/api/datasource/parseCreateTable` | POST | 建表语句解析，body 为 `{datasourceType, sql}` |
| `/api/lineage/query?datasourceType=` | POST | 字段血缘查询（body 为纯文本 SQL），返回 `tableData` + `relationData` 供血缘可视化 |
| `/api/sql/split?datasourceType=` | POST | SQL 脚本切分（body 为纯文本 SQL） |

## 测试

`SqlParserX-core` 内置约 78 个单元测试用例，覆盖：

- 表血缘：`CREATE TABLE AS SELECT` / `INSERT SELECT` / `WITH AS INSERT` 三类场景
- 字段血缘：同上三类场景，覆盖聚合、子查询、JOIN、UNION、CASE、OVER 等
- 表解析、函数解析、SQL 过滤（行级权限注入）
- Doris 专项：字段血缘、表血缘、建表解析

```bash
# 执行 core 模块全部测试（默认 surefire skipTests=true，需显式开启）
mvn -pl SqlParserX-core test -DskipTests=false
```

测试入口：`SqlParserX-core/src/test/java/com/changlu/sqlparserx/core/AllTestsSuite.java`（`BaseSuite` + `DatasourceSuite` + 策略单测）。

## 目录结构

```
SqlParserX
├── SqlParserX-core/            # 核心解析库（Java 8）
│   └── src/main/java/com/changlu/sqlparserx/core
│       ├── parser/             # 工厂、统一接口、基类与 15 种方言实现
│       │   ├── base/           # 表/血缘/参数/过滤的基础实现
│       │   ├── mysql/ hive/ doris/ starrocks/ ...   # 方言扩展
│       │   └── ...
│       ├── select/             # select 追溯引擎（字段/表血缘共用）
│       ├── enums/              # 解析策略、匹配策略、字段类型枚举
│       └── utils/              # SQL 切分、表源解析等工具
├── SqlParserX-web/             # Spring Boot REST 服务（:8888）
├── SqlParserX-mcp-server-sse/  # MCP Server，SSE 模式（:8067）
├── SqlParserX-ui/              # Vue2 + Vite 前端工具集
├── bin/                        # 启停脚本（centos-sqlparserx.sh / base.sh）
├── conf/                       # 外部配置：application.properties、logback.xml、static 前端产物
└── assembly.xml                # 目录式部署打包描述
```

## 已知限制

- 参数解析（`parseParameterSql` / `buildSqlByPrepareCompile`）与 `filterSortSelectField` 在 base 层为占位实现（返回空），属于预留能力，尚未落地具体逻辑。
- `SqlParserX-core` 的 Maven surefire 默认 `skipTests=true`，CI 构建默认不执行测试。
- 前端 `SqlParserX-ui/README.md` 为 Vite 模板默认文案，实际技术栈为 Vue 2.7 + Element UI 2.x，与模板文案不符。

## 贡献指南

欢迎任何形式的贡献：Bug 修复、新方言支持、血缘解析增强、文档改进等。

- **提 Issue**：使用中发现 Bug 或希望新增能力，请先提交 Issue 描述复现步骤与期望行为。
- **提 PR**：Fork 本仓库，在功能分支（如 `feat_1.0.x_x`）上开发，遵循现有分层扩展模式（在 `parser` 包下新增方言实现或重写对应 Client），补充对应测试用例后提交 Pull Request。
- **开发规范**：核心库保持 Java 8 兼容；新增方言实现优先继承 `BaseSqlParser`，仅在确有差异时重写方法；测试用例可参考 `AllTestsSuite` 的套件组织方式。

## 许可证

本项目采用 [MIT License](LICENSE)。
