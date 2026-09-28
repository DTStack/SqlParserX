# 一、SqlParserX 当前适配的 Druid 版本情况

| 项目 | 当前情况 |
|---|---|
| Druid 版本 | `1.2.28` |
| 版本定义位置 | `sqlparserx-engine/pom.xml` 的 `dependencyManagement` |
| Syntax 引用方式 | `sqlparserx-engine/sqlparserx-syntax/pom.xml` 引入 `com.alibaba:druid`，版本继承 Engine 父 POM |
| Java 版本 | SqlParserX Core 保持 Java 8 编译目标 |
| 适配方式 | SqlParserX 不重复实现 SQL Lexer 和 StatementParser，直接使用 Druid 的 `DbType`、`SQLParserUtils`、方言 Parser 和 `SQLStatement` AST |
| 业务层关系 | Druid 负责 SQL 文本到 AST 的语法解析；SqlParserX 基于 AST 实现真实表、建表结构、表血缘、字段血缘、函数提取和 SQL 改写 |
| 当前 Core 方言范围 | `SqlParserFactory` 注册 15 种方言：MySQL、Hive、Trino、Oracle、Greenplum、TiDB、PostgreSQL、SQL Server、StarRocks、Doris、OceanBase、OceanBase Oracle、DM、ClickHouse、Spark |
| 版本结论 | 截至 2026-09-28，项目使用的 `1.2.28` 与 Druid 官方最新 Release 一致，当前不需要进行版本升级 |

Druid `1.2.28` 与 SqlParserX 当前重点方言直接相关的修复和增强包括：修复 SQL Server `COLLATION`、Redshift `SORTKEY`、Spark/ClickHouse/StarRocks 的 `SQLParserFeature` 传递问题；增加 ClickHouse `ON CLUSTER`、Doris `PARTITION BY`、DB2 CTAS、Snowflake 和 ODPS Parser 增强，同时优化方言加载、Lexer 关键字加载和 SQLDialect 性能。版本信息以 [Druid 1.2.28 官方 Release](https://github.com/alibaba/druid/releases/tag/1.2.28) 为准。

# 二、Druid 最新版本的 SQL 语法支持情况

截至 2026-09-28，Druid 官方最新版本为 `1.2.28`。官方项目说明 SQL Parser 支持 30 种数据库方言，并为各方言提供 Lexer、Parser、AST 和 Visitor 能力；具体支持范围可参考 [Druid 官方仓库的 SQL 方言支持说明](https://github.com/alibaba/druid#sql-%E6%96%B9%E8%A8%80%E6%94%AF%E6%8C%81) 和 [SQL Parser 官方说明](https://github.com/alibaba/druid/wiki/SQL-Parser)。

| 官方分类 | Druid 1.2.28 方言支持 | SqlParserX 当前使用情况 |
|---|---|---|
| 主流关系型数据库 | MySQL、PostgreSQL、Oracle、SQL Server、DB2、H2、Informix | 已注册 MySQL、PostgreSQL、Oracle、SQL Server；其他方言尚未注册为 Core 独立业务解析器 |
| 国产数据库 | DM、Oscar、GaussDB | 已注册 DM；Oscar、GaussDB 尚未注册为 Core 独立业务解析器 |
| 分析型 / MPP | ClickHouse、Doris、StarRocks、Teradata、Redshift | 已注册 ClickHouse、Doris、StarRocks；Teradata、Redshift 尚未注册为 Core 独立业务解析器 |
| 云原生 / 数仓 | BigQuery、Snowflake、Synapse、Hologres、ODPS（MaxCompute） | 当前均未注册为 Core 独立业务解析器 |
| 计算引擎 | Hive、Spark、Presto、Impala、Athena、Blink、Databricks | 已注册 Hive、Spark、Trino；其余尚未注册为 Core 独立业务解析器 |
| 兼容方言 | TiDB、MariaDB、GoldenDB、OceanBase、OceanBase Oracle、Greenplum、EDB、Trino 等可通过 Druid 方言路由或专用 Parser 解析 | 已注册 TiDB、OceanBase、OceanBase Oracle、Greenplum、Trino；部分兼容数据源只在 Web/MCP 映射层复用相近方言 |

需要注意：Druid “支持某种方言”表示语法层具备相应 Lexer、Parser、AST 或兼容路由，不等于 SqlParserX 已经完成该数据源的业务解析适配。只有同时满足以下条件，才能列入 SqlParserX 正式支持范围：Druid 能生成正确 AST、`SqlParserFactory` 已注册对应业务解析器、通用或方言 Client 能正确输出业务结果，并具备相应回归验证。
