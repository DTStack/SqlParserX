
# SqlParserX 数据中台主流数据源支持调研（初版）

> **调研日期**：2026-10-23  
> **项目地址**：https://github.com/DTStack/SqlParserX  
> **文档定位**：开源项目方言支持优先级初筛与迭代规划，不作为市场份额统计报告  
> **结论概览**：优先补齐 **Flink SQL**，深化 **Spark SQL / Hive SQL**；持续强化 **Doris / StarRocks / ClickHouse**。保留 MySQL / PostgreSQL 的核心兼容性，随后扩展 Trino 与湖仓生态。

## 一、调研背景与目标

SqlParserX 是基于 **Druid 语法解析层 + SqlParserX 业务解析层**的 Java 多方言 SQL 解析引擎，提供表解析、CREATE TABLE 解析、表/字段血缘、函数提取、SQL 切分、校验及部分 SQL 改写能力。项目主要面向数据开发、数据中台、数据治理与 SQL 工具链场景。

本轮调研要解决三个问题：

1. 数据中台最值得优先支持的 SQL 引擎 / 数据源有哪些？
2. 当前 SqlParserX 已有注册方言中，哪些值得进一步深化？
3. 有限维护资源下，如何安排最先落地的适配工作？

**范围边界**：这里的“支持数据源”主要指 **SQL 方言的解析与业务 AST 能力**，不等于 JDBC 连通、数据同步、执行 SQL 或作业调度。`Iceberg`、`Delta Lake`、`Hudi` 等首先是表格式/数据湖技术，原则上应按照实际 SQL 执行引擎（Spark、Flink、Trino 等）及其扩展语法评估，不能简单地与 MySQL、Doris 并列为独立 SQL 数据库。

## 二、SqlParserX 当前基线

根据 2026-10-23 查阅的仓库中文 README，Core 正式列出 **15 种注册方言**（OceanBase 的 MySQL/Oracle 模式分别统计）；当前支持深度不一致。

| 分类 | 数据源 | 当前 README 描述 | 本轮策略 |
|---|---|---|---|
| 已重点增强 | MySQL | RENAME TABLE 真实表识别增强，其他主要复用通用实现 | 维持基础能力与回归测试 |
| 已重点增强 | Doris | 建表、表/字段血缘、大小写匹配、CTE 前向引用与专项测试 | 深化复杂方言 |
| 已重点增强 | StarRocks | DISTRIBUTED BY、DUPLICATE KEY / AGGREGATE KEY 建表扩展 | 深化复杂方言 |
| 已注册、主要复用 Base | Hive、Spark、ClickHouse、Trino | 标准支持，当前以通用测试为主 | 优先补充专项 SQL 语料与 AST 验证 |
| 已注册、主要复用 Base | PostgreSQL、Oracle、SQL Server、Greenplum、TiDB、达梦 DM、OceanBase（MySQL/Oracle 模式） | 标准支持 | 按用户需求维护与扩展 |
| 未列入正式 Core 注册方言 | Flink SQL | 本轮 README 中未列出 | **最高优先级新增** |

> **重要**：注册方言 ≠ 该方言所有语法均可解析 ≠ 各项业务能力（字段血缘、行级权限等）均完整支持。实际兼容范围需通过对应语法层/业务层专项测试证明。

### 现有架构对规划的影响

- `sqlparserx-syntax`：负责 SQL → Druid AST、方言语法扩展。
- `sqlparserx-core`：`SqlParserFactory` + `ISqlParser` + `BaseSqlParser` + 方言适配 Client，负责业务解析。
- 对同一数据源，需要分别验收 **语法可解析** 和 **业务结果正确**。
- 仓库 README 记录 `sqlparserx-core` 的 Surefire 默认跳过测试，建议将方言回归测试纳入 CI 后再扩大“完整支持”的对外宣称。

**来源**：
- 项目中文 README：https://github.com/DTStack/SqlParserX/blob/master/README_zh-CN.md
- 项目主页：https://github.com/DTStack/SqlParserX

## 三、数据中台主流引擎分层

本次“主流”是**基于数据中台典型工作负载、生态成熟度与维护收益的定性判断**，非基于严谨市场占有率的数据排名。

| 场景 | 主流/值得关注的产品 | 对 SQL Parser 的意义 |
|---|---|---|
| 业务源库 / ODS | MySQL、PostgreSQL、Oracle、SQL Server、TiDB、OceanBase | 采集任务 SQL、元数据识别、业务表结构解析 |
| 离线数仓与批处理 | Hive SQL、Spark SQL | INSERT OVERWRITE、分区、CTAS、复杂 ETL 血缘 |
| 实时计算 / 实时数仓 | Flink SQL | WITH 连接器参数、WATERMARK、计算列、窗口、时态 JOIN |
| 交互式 OLAP / 实时分析 | Doris、StarRocks、ClickHouse | 引擎特有 CREATE TABLE、物化视图、分区/分桶、DML |
| 联邦查询 / 湖仓查询 | Trino、Spark SQL、Databricks SQL | Catalog、跨源查询、湖仓表操作 |
| 新兴轻量分析 | DuckDB | 嵌入式分析、数据文件直接查询 |
| 国际云数据仓库 | Snowflake、BigQuery | 对国际化用户有价值，但国内传统中台优先级暂低 |

## 四、建议支持优先级总表

优先级兼顾“真实场景需求 × SQL 方言差异 × 开源项目吸引力 ÷ 维护成本”。表中 **P0/P1/P2 为项目建议，并非行业公认排序**。

| 优先级 | 引擎/方言 | 当前项目状态 | 主要场景 | 建议动作 |
|---|---|---|---|---|
| **P0** | **Flink SQL** | 尚未列入 Core 正式支持 | 实时数据开发、流式 ETL | **新增语法和业务适配** |
| **P0** | **Spark SQL** | 标准支持 | 离线计算、湖仓 ETL | 深化专项解析与血缘 |
| **P0** | **Hive SQL** | 标准支持 | 离线数仓、Hive 任务 | 深化专项解析与血缘 |
| **P0** | **Doris** | 重点/重度支持 | 实时 OLAP | 持续强化高级 DDL/血缘 |
| **P0** | **StarRocks** | 重点支持 | 实时分析、湖仓查询 | 持续强化高级 DDL/血缘 |
| **P0** | **ClickHouse** | 标准支持 | 指标、日志、行为分析 | 补齐特有 DDL/DML |
| P1 | MySQL | 重点支持 | OLTP / ODS | 保持高质量兼容，回归覆盖 |
| P1 | PostgreSQL | 标准支持 | 业务库、分析系统 | 增强复杂语法 |
| P1 | Trino | 标准支持 | 联邦查询、数据湖 | 增强 Catalog / 方言语法 |
| P1 | Databricks SQL | 尚未列入 | 云端湖仓 | 根据社区需求新增 |
| P2 | Impala | 尚未列入 | Hadoop 交互式查询 | 有客户需求再投入 |
| P2 | DuckDB | 尚未列入 | 轻量分析、嵌入式 ETL | 社区反馈驱动 |
| P2 | Snowflake / BigQuery | 尚未列入 | 海外云数仓 | 国际化阶段考虑 |

**优先级说明**：从“新功能增量”角度，Flink SQL 排第一；从“巩固已有覆盖”角度，Spark/Hive 应优于同时再增加多个新名字；Doris/StarRocks 已有相对较强基础，可以通过高价值复杂 SQL 用例继续拉开差异化。

## 五、第一批重点支持对象与关键 SQL

### 5.1 Flink SQL（最值得新增）

- **场景**：实时同步、实时 ETL、实时数仓、维表关联。
- **优先支持**：`CREATE TABLE ... WITH (...)`、计算列、`WATERMARK FOR`、`INSERT INTO`、`CREATE VIEW`、Catalog/Database 语法。
- **第二梯队**：事件时间窗口 TVF、Temporal Join、特殊连接器和版本差异。
- **关键挑战**：Flink 表语法、连接器属性与流处理时间语义难以直接套用普通 OLTP SQL Parser。
- **验收输出**：解析器能正确解析 DDL/DML；识别 source/sink 表；CTE/字段血缘正确；参数字符串不会被误认作表名。
- **官方参考**：https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/table/sql/overview/

### 5.2 Spark SQL（已有标准支持，建议深化）

- **场景**：离线 ETL、批量数仓、湖仓一体。
- **优先支持**：CTAS、`INSERT OVERWRITE`、动态分区、`MERGE INTO`（需区分具体表格式与扩展）、`LATERAL VIEW`、多层 CTE、窗口函数。
- **关键挑战**：原生 Spark SQL 与 Iceberg/Delta 等扩展能力的边界；Catalog 命名与表解析。
- **官方参考**：https://spark.apache.org/docs/latest/sql-ref.html

### 5.3 Hive SQL（已有标准支持，建议深化）

- **场景**：传统离线数仓、ODS/DWD/DWS/ADS 分层任务。
- **优先支持**：`CREATE EXTERNAL TABLE`、`PARTITIONED BY`、`STORED AS`、`LOCATION`、`INSERT OVERWRITE`、动态分区、`LATERAL VIEW EXPLODE`。
- **关键挑战**：DDL 存储属性、复杂类型、分区字段与真实表血缘。
- **官方参考**：https://cwiki.apache.org/confluence/display/Hive/LanguageManual

### 5.4 Doris（已有重度支持，持续增强）

- **场景**：实时分析、指标服务、数据应用。
- **优先支持**：`DUPLICATE KEY` / `UNIQUE KEY` / `AGGREGATE KEY`、`DISTRIBUTED BY`、分区、物化视图、CTAS、数据装载相关 SQL。
- **关键挑战**：原有建表规则与新增引擎版本特性兼容；不能把“语法通过”当作“字段血缘正确”。
- **官方参考**：https://doris.apache.org/docs/

### 5.5 StarRocks（已有重点支持，持续增强）

- **场景**：实时 OLAP、湖仓查询、面向分析服务。
- **优先支持**：主键/明细/聚合表、分桶、分区、物化视图、CTAS、特殊属性。
- **关键挑战**：看起来与 Doris 相似但语义不完全一致，建议独立测试语料，不直接共用“全部通过”承诺。
- **官方参考**：https://docs.starrocks.io/

### 5.6 ClickHouse（已有标准支持，建议强化方言）

- **场景**：大规模日志、用户行为分析、时序指标。
- **优先支持**：`ENGINE = MergeTree` 系列、`ORDER BY`、`PARTITION BY`、`TTL`、`ARRAY JOIN`、物化视图、`ALTER ... UPDATE/DELETE`。
- **关键挑战**：ClickHouse 的 `ORDER BY` 在 DDL 中可能是排序键定义，不是常规查询排序；引擎函数/表函数丰富。
- **官方参考**：https://clickhouse.com/docs/

## 六、能力分级与验收口径

不建议将数据库方言的存在与“完全支持”划等号。推荐 README 和文档采用分级表达：

| 等级 | 建议名称 | 准入要求 |
|---|---|---|
| L0 | 已注册 | 工厂可路由到方言解析器，但不保证覆盖率 |
| L1 | 基础语法支持 | SELECT / INSERT / 常见 DDL 可生成正确 AST，基础用例通过 |
| L2 | 业务解析支持 | 表解析、CTAS/INSERT 表血缘、建表结果等核心业务能力通过专项测试 |
| L3 | 深度方言支持 | 特有 DDL/DML、复杂 CTE/JOIN、字段血缘、异常 SQL 与版本语料稳定 |

建议每个 P0 引擎维护至少以下测试维度：

1. **基础 DML**：SELECT、JOIN、UNION、CTE、子查询、窗口函数。
2. **写入 SQL**：INSERT INTO、INSERT OVERWRITE、CTAS、MERGE（仅适用方言）。
3. **建表 SQL**：字段、注释、分区、分桶、表属性、引擎特定语法。
4. **血缘**：表级源/目标表；字段级别名、聚合、CASE、CTE、`SELECT *`（依赖元数据）。
5. **脚本处理**：多语句切分、注释、字符串、方言关键字。
6. **异常处理**：不合法 SQL 的报错位置、降级与不误报。
7. **版本兼容**：区分被支持的 SQL 引擎版本与特有语法，不笼统宣称“所有版本支持”。

建议建立 `dialect × capability × fixture` 测试矩阵，并在 CI 输出按方言、语句类别统计的通过率；覆盖率目标须以真实语料规模和质量为前提，不应仅用一个宽泛百分比对外宣传。

## 七、推荐实施路线

### 第一阶段：实时 + 离线数仓三大核心方言

**Flink SQL + Spark SQL + Hive SQL**。

- Flink：优先完成最小可用 `CREATE TABLE` / `INSERT INTO` / `SELECT` / 表级血缘，再补时间语义与复杂场景。
- Spark/Hive：基于现有注册解析器建立专项语料库，逐条修复 AST 与业务解析问题。
- 形成三份单独的“语法层支持矩阵”和“业务层支持矩阵”。

**建议里程碑**：三种方言均完成最小测试基线；至少验证一条 `源表 → 计算 → 目标表` 的真实数仓 SQL 链路。

### 第二阶段：实时 OLAP 方言加深

**Doris + StarRocks + ClickHouse**。

- 补齐引擎特有建表属性及物化视图解析。
- 对复杂 CTE、窗口函数、聚合与字段血缘建立回归集。
- 同一逻辑下的三种方言分别保留独立 SQL 用例。

**建议里程碑**：六种重点引擎形成稳定公开兼容性矩阵。

### 第三阶段：湖仓/联邦查询扩展

**Trino + Databricks SQL（按需求）+ Spark/Flink/Iceberg 相关扩展**。

- 先确认实际社区请求与具体 SQL 样例，再扩充新方言。
- 把 Catalog、多级对象名、湖仓操作纳入通用 AST 与血缘设计。
- DuckDB、Impala、Snowflake、BigQuery 以 Issue/用户场景驱动，不与 P0 争夺资源。

## 八、开源项目建议

1. **README 展示两张表**：已注册方言表、核心能力深度支持矩阵，而不是只展示“支持 X 种数据库”。
2. **明确价值链路**：`MySQL / PostgreSQL → Flink / Spark / Hive → Doris / StarRocks / ClickHouse`。注意这仅是典型架构示意，并不表示 SqlParserX 自身负责数据同步。
3. **优先做真实语料与专项测试**：比增加空壳方言枚举更容易带来稳定性和开发者信任。
4. **开放社区语料贡献**：按引擎目录组织合法、匿名化 SQL fixture，配预期 AST/血缘结果。
5. **对外用“已验证”替代“全面兼容”**：明确 SQL 引擎版本、SQL 类别和已知限制。
6. **可借鉴其他开源项目的方言矩阵**，但不能直接迁移“官方支持”等级到 SqlParserX。

## 九、调研结论

**建议的第一批集中投入：Flink SQL、Spark SQL、Hive SQL。** 理由：Flink 补齐实时计算空缺，Spark/Hive 稳固离线与湖仓基础；现有 Doris/StarRocks 能形成贯穿数仓计算到 OLAP 应用的能力闭环。

**第一批对外重点打造的六种核心 SQL 引擎**：Flink、Spark、Hive、Doris、StarRocks、ClickHouse。MySQL 则作为业务数据库与来源 SQL 的基础能力持续维护。

本版本是**方向性调研与初步开发优先级建议**，尚未做代码级兼容率实测、企业采用份额统计或人天估算。下一版应补充每一种引擎的实际 SQL 样本、语法通过情况、字段血缘正确性与改造工作量。

## 十、资料与官方链接

| 资料 | 链接 | 用途 |
|---|---|---|
| SqlParserX 项目 | https://github.com/DTStack/SqlParserX | 支持现状与架构 |
| SqlParserX 中文 README | https://github.com/DTStack/SqlParserX/blob/master/README_zh-CN.md | 正式支持清单 |
| Apache Flink SQL | https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/table/sql/overview/ | Flink 方言参考 |
| Apache Spark SQL | https://spark.apache.org/docs/latest/sql-ref.html | Spark 方言参考 |
| Apache Hive LanguageManual | https://cwiki.apache.org/confluence/display/Hive/LanguageManual | Hive 语法参考 |
| Apache Doris Docs | https://doris.apache.org/docs/ | Doris 方言参考 |
| StarRocks Docs | https://docs.starrocks.io/ | StarRocks 方言参考 |
| ClickHouse Docs | https://clickhouse.com/docs/ | ClickHouse 方言参考 |
| Trino SQL | https://trino.io/docs/current/sql.html | 联邦查询语法 |
| Databricks SQL | https://docs.databricks.com/en/sql/language-manual/index.html | 湖仓 SQL 参考 |
| SQLGlot 项目 | https://github.com/tobymao/sqlglot | 多方言兼容矩阵设计参考 |
| DTStack dt-sql-parser（另一个项目） | https://github.com/DTStack/dt-sql-parser | ANTLR/TypeScript 方言语法和示例参考；**并非 SqlParserX** |

---

*版本：v0.1 · 调研整理日期：2026-10-23*
