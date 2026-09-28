# SqlParserX

> [English](README.md) | **简体中文**

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Language: Java](https://img.shields.io/badge/Language-Java%208-blue.svg)
![Dialects](https://img.shields.io/badge/Dialects-15%2B-orange.svg)

## 介绍

> 多数据库方言 SQL 解析引擎与工具集：表解析、表/字段血缘解析、函数解析、SQL 切分、行级权限 SQL 过滤，并提供 Java Module、Web 服务与可视化前端。

SqlParserX 是一套「**Druid 语法解析层 + SqlParserX 业务解析层**」的 SQL 解析中间件：

- `sqlparserx-syntax` 负责多数据源 SQL→AST、语法校验和 SQL 切分。
- `sqlparserx-core` 基于统一 AST 提供表、建表、血缘、函数和 SQL 改写等业务结果。
- `sqlparserx-web` 和 `sqlparserx-ui` 提供 REST 与可视化使用方式。

SqlParserX 起源于对多数据源 SQL 解析、AST 统一和业务解析复用问题的持续实践，项目在内部解析能力改造的基础上逐步沉淀为独立的双层 SQL 解析引擎。关于项目为什么诞生、内部改造过程及设计思考，可阅读技术博客：[SqlParserX 的诞生起源背景 & 内部改造](docs/blogs/20260928：SqlParserX的诞生起源背景%26内部改造.md)。

### 能力解决的问题

| 使用问题 | SqlParserX 解决方式 |
|---|---|
| 不同数据库 SQL 方言不一致 | 通过 Druid 方言解析和 `DbType` 路由统一生成 AST |
| 只拿到 SQL 文本，难以识别真实表和建表结构 | 提供主表、真实表、CTE、CREATE TABLE、分区和属性解析 |
| 数据加工链路无法追溯 | 提供表血缘和字段血缘，覆盖 JOIN、UNION、子查询、聚合和 CTE |
| 多条 SQL 脚本难以逐条处理 | 提供多语句解析、SQL 切分和语法校验 |
| 权限条件需要手工拼接且容易出错 | 支持向查询、写入和更新 SQL 注入行级权限条件 |
| 解析能力难以接入现有系统或工具 | 提供 Java Module、Web 服务和可视化前端 |

## 架构

SqlParserX 采用分层解耦设计，各层职责清晰、可独立演进：

![SqlParserX 双层统一扩展架构](docs/架构设计/SqlParserX两层架构.png)

> Draw.io 源文件与完整设计说明见：[双层架构设计](docs/架构设计/架构设计.md)｜[SqlParserX两层架构.drawio](docs/架构设计/SqlParserX两层架构.drawio)。

```
┌──────────────────────────────────────────────────────────┐
│  能力出口层                                                │
│  sqlparserx-web (REST :8888) │ sqlparserx-ui (Vue + ECharts) │
│  sqlparserx-ui (Vue2 + ECharts 血缘可视化)                  │
└───────────────────────────┬──────────────────────────────┘
                            │ 依赖统一契约 ISqlParser
┌───────────────────────────▼──────────────────────────────┐
│  解析业务层（本项目自研）  sqlparserx-core                  │
│  工厂 → 接口 → 基类 → 方言扩展                              │
│  表解析 / 血缘 / 建表 / 函数 / 切分 / 行级权限过滤            │
└───────────────────────────┬──────────────────────────────┘
                            │ SQL + DbType / SQLStatement AST
┌───────────────────────────▼──────────────────────────────┐
│  语法解析层  sqlparserx-syntax                              │
│  统一语法 API / Druid 1.2.28 / 方言 Lexer·Parser·AST 扩展    │
└──────────────────────────────────────────────────────────┘
```

**业务层内部分层**：`SqlParserFactory`（按 Druid `DbType` 选择方言）→ `ISqlParser`（统一契约）→ `BaseSqlParser`（AST 分发到各 Client）→ 各方言实现（按需重写表/血缘解析）。

| 模块 | 层次 | 说明 | 技术栈 |
|---|---|---|---|
| `sqlparserx-syntax` | 语法解析层 | SQL→AST、多语句解析、SQL 切分及 Druid 方言语法扩展 | Java 8 + Druid 官方 1.2.28 |
| `sqlparserx-core` | 解析业务层 | 表、血缘、建表、函数和 SQL 改写等业务解析能力 | Java 8 + `sqlparserx-syntax` |
| `sqlparserx-web` | 能力出口层 | Spring Boot REST 服务，提供 HTTP API 并托管前端静态资源 | Spring Boot 2.3.12 + Java 8 |
| `sqlparserx-ui` | 能力出口层 | 前端工具集（血缘可视化、解析/校验/切分测试） | Vue 2.7 + Element UI 2.x + Vite 5 + ECharts 5 |

### 核心契约 `ISqlParser`

业务层对外统一契约，所有方言解析器均实现该接口：

```java
ParseTableResult   parseTables(String sql);                                          // 表解析
ParseCreateTableResult parseCreateTable(String sql);                                 // 建表解析
ColumnLineageResult parseColumnLineages(String sql, Map<String,List<Column>> meta);  // 字段血缘
TableLineageResult parseTableLineages(String sql);                                   // 表血缘
Set<String>        parseFunction(String sql);                                        // 函数解析
String             getRowLevelPermissionSql(String sql, List<...> permissionParams); // 行级权限过滤
```

### 分层扩展模式

方言实现优先继承 `BaseSqlParser`，仅在确有差异时重写对应 Client。例如当 Druid 语法层新增了 `MySqlRenameTableStatement`，而通用 `ParseTableClient` 未处理时，可在 `parser.mysql` 包下自定义 `MySqlParseTableClient` 继承并重写 `parseTables`。详见 [`sqlparserx-core/README.md`](sqlparserx-engine/sqlparserx-core/README.md)。

## Support Datasource（数据源支持）

| 数据源名称 | 业务解析文档 | 语法解析文档 | 支持说明 |
|---|---|---|---|
| MySQL | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/mysql/SQL业务解析支持情况.md) | [语法解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/mysql/SQL语法解析支持情况.md) | 重点支持：补充 RENAME TABLE 真实表识别，其余能力主要复用通用实现。 |
| Doris | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/doris/SQL业务解析支持情况.md) | [语法解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/doris/SQL语法解析支持情况.md) | 重度支持：覆盖建表、表/字段血缘、大小写匹配和 CTE 前向引用，并有专项测试。 |
| StarRocks | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/starrocks/SQL业务解析支持情况.md) | [语法解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/datasource/starrocks/SQL语法解析支持情况.md) | 重点支持：扩展 DISTRIBUTED BY、DUPLICATE KEY / AGGREGATE KEY 建表解析。 |
| Hive | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| ClickHouse | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| Oracle | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| DM（达梦） | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| OceanBase | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| OceanBase（Oracle 模式） | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| PostgreSQL | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| Greenplum | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| Spark | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| SQL Server | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| TiDB | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |
| Trino | [业务解析文档](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | [语法解析文档](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | 标准支持：已注册独立业务解析器，主要复用 Base 实现，当前以通用测试为主。 |

> 说明：MySQL、Doris、StarRocks 已建立数据源专项文档；当前表仅列出 SqlParserX Core 已注册的正式方言。Web 的 `/api/datasource/types` 与 Core 的 `SqlParserFactory` 保持同源校验，Web-only 兼容映射数据源不纳入支持列表。

## 解析能力支持

SqlParserX 的能力支持需要同时检查“语法层能否生成正确 AST”和“业务层能否输出稳定结果”。当前能力文档按以下层次维护：

| 文档 | 内容 |
|---|---|
| [SQL 语法层解析能力支撑情况](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md) | Druid 版本、官方方言语法范围及 SqlParserX 语法层适配边界 |
| [SQL 业务层解析能力支撑导航](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md) | 数据源总表、语法/业务双层适配情况、通用解析能力与专项文档索引 |
| [模块依赖包引用结构设计](docs/02、SQL业务层解析能力支撑情况/模块依赖包引用结构设计.md) | `sqlparserx-syntax`、`sqlparserx-core` 及 `sqlparserx-engine` 的依赖边界 |
| [双层架构设计](docs/架构设计/架构设计.md) | 语法解析层与业务解析层的统一扩展模式和协作边界 |

当前核心解析能力包括：

| 能力 | 说明 |
|---|---|
| 表解析 | 识别主表、真实表、CTE、子查询及多种 DML/DDL 场景 |
| 建表解析 | 提取表、字段、注释、分区、分桶、Key 和 properties 等结构信息 |
| 字段血缘 | 支持 INSERT/CTAS、JOIN、UNION、聚合、CASE、窗口函数、`*` 展开和 CTE |
| 表血缘 | 输出目标表、来源表及表间关系，支持 CTE 前向引用推断 |
| 函数解析 | 提取 SQL 中使用的函数名称 |
| SQL 切分 | 支持多语句脚本切分及 Druid 原生切分策略 |
| SQL 改写 | 支持行级权限条件注入及相关 SQL 过滤策略 |
## Quick Start

两种使用方式：作为 Maven 依赖嵌入 Java 项目、部署 Web 服务。

### 1. 作为 Maven 依赖使用核心库

`sqlparserx-core` 提供面向多数据源的表解析、函数解析、字段血缘和表血缘能力。依赖引入：

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>sqlparserx-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

完整 Java 使用案例、输出示例和分层扩展说明请直接参考 [sqlparserx-core/README.md](sqlparserx-engine/sqlparserx-core/README.md)。

### 2. 部署 Web 服务

```bash
# 编译打包（core 的测试默认跳过，需执行测试时加 -DskipTests=false）
mvn -pl sqlparserx-engine/sqlparserx-core,sqlparserx-web -am clean package -DskipTests

bin/centos-sqlparserx.sh start     # 启动，默认端口 8888
bin/centos-sqlparserx.sh stop      # 停止
bin/centos-sqlparserx.sh status    # 查看状态
```

启动后浏览器访问 `http://localhost:8888` 即可使用前端工具集（血缘可视化、表解析、建表解析、SQL 切分、语法校验）。

### 测试

`sqlparserx-core` 内置约 78 个单元测试用例，覆盖：

- 表血缘：`CREATE TABLE AS SELECT` / `INSERT SELECT` / `WITH AS INSERT` 三类场景
- 字段血缘：同上三类场景，覆盖聚合、子查询、JOIN、UNION、CASE、OVER 等
- 表解析、函数解析、SQL 过滤（行级权限注入）
- Doris 专项：字段血缘、表血缘、建表解析

```bash
# 执行 core 模块全部测试（默认 surefire skipTests=true，需显式开启）
mvn -f sqlparserx-engine/pom.xml test -DskipTests=false
```

测试入口：`sqlparserx-engine/sqlparserx-core/src/test/java/com/dtstack/sqlparserx/core/AllTestsSuite.java`（`BaseSuite` + `DatasourceSuite` + 策略单测）。

## 目录结构

```
SqlParserX
├── sqlparserx-engine/          # SQL 解析引擎聚合父模块（packaging=pom）
│   ├── sqlparserx-syntax/      # SQL 语法解析层（Java 8 + Druid）
│   └── sqlparserx-core/        # SQL 业务解析层（Java 8）
│       └── src/main/java/com/dtstack/sqlparserx/core
│       ├── parser/             # 工厂、统一接口、基类与 15 种方言实现
│       │   ├── base/           # 表/血缘/参数/过滤的基础实现
│       │   ├── mysql/ hive/ doris/ starrocks/ ...   # 方言扩展
│       │   └── ...
│       ├── select/             # select 追溯引擎（字段/表血缘共用）
│       ├── enums/              # 解析策略、匹配策略、字段类型枚举
│       └── utils/              # SQL 切分、表源解析等工具
├── sqlparserx-web/             # Spring Boot REST 服务（:8888）
├── sqlparserx-ui/              # Vue2 + Vite 前端工具集
├── bin/                        # 启停脚本（centos-sqlparserx.sh / base.sh）
├── conf/                       # 外部配置：application.properties、logback.xml、static 前端产物
└── assembly.xml                # 目录式部署打包描述
```

## 已知限制

- 参数解析（`parseParameterSql` / `buildSqlByPrepareCompile`）与 `filterSortSelectField` 在 base 层为占位实现（返回空），属于预留能力，尚未落地具体逻辑。
- `sqlparserx-core` 的 Maven surefire 默认 `skipTests=true`，CI 构建默认不执行测试。
- 前端 `sqlparserx-ui/README.md` 为 Vite 模板默认文案，实际技术栈为 Vue 2.7 + Element UI 2.x，与模板文案不符。

## Questions

[FAQ Reference](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md)。如有使用问题、Bug 或支持需求，请提交 Issue，并附上数据源类型、SQL 示例、期望结果和实际结果，我们会及时跟进。

常见问题可优先参考：

- [架构设计](docs/架构设计/架构设计.md)
- [SQL 语法层解析能力支撑情况](docs/01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md)
- [SQL 业务层解析能力支撑导航](docs/02、SQL业务层解析能力支撑情况/能力支持情况/README.md)

## Contribution

欢迎任何形式的贡献：Bug 修复、新方言支持、血缘解析增强、文档改进等。

- **提 Issue**：使用中发现 Bug 或希望新增能力，请先提交 Issue 描述复现步骤与期望行为。
- **提 PR**：Fork 本仓库，在功能分支（如 `feat_1.0.x_x`）上开发，遵循现有分层扩展模式（在 `parser` 包下新增方言实现或重写对应 Client），补充对应测试用例后提交 Pull Request。
- **开发规范**：核心库保持 Java 8 兼容；新增方言实现优先继承 `BaseSqlParser`，仅在确有差异时重写方法；测试用例可参考 `AllTestsSuite` 的套件组织方式。

提交 Pull Request 前，请先阅读 [Contributing Guide](docs/02、SQL业务层解析能力支撑情况/标准迭代维护扩展文档/SqlParserX解析能力标准迭代维护扩展指南.md) 以及项目中的架构设计、模块依赖和能力支持文档，确保新增逻辑放在正确的语法层或业务层。

## Contributor

感谢所有参与代码、测试、文档和问题反馈的贡献者。新增贡献者可通过提交 Issue 或 Pull Request 加入项目协作。

[查看项目贡献者与提交记录](https://gitlab.prod.dtstack.cn/changlu/SqlParserX/-/graphs/master)

## License

SqlParserX 采用 [Apache License 2.0](LICENSE)，许可证详情参见 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)。
