# SqlParserX SQL 解析能力与数据源支撑适配情况

> 本文件位于 `datasource/`，作为**数据源能力支持情况的总表与索引**。上层导航见 [`../README.md`](../README.md)。
> 统计口径：以 `sqlparserx-core` 当前代码为准，从业务解析能力出发，分别检查 Druid SQL 语法解析层能否提供所需 AST，以及 SqlParserX 业务解析层是否具备对应实现、方言增强和专项测试。接口已定义但 Base 实现为空的能力标记为“预留”，不计为正式支持。

## 一、支撑关系说明

一项 SQL 业务解析能力可用，需要上下两层同时成立：

```text
SQL + DbType
    → Druid SQL 语法解析层：SQL 能否生成正确、完整的方言 AST
    → SqlParserX 业务解析层：能否将 AST 转换成正确的业务结果
```

| 检查维度 | 关注内容 | 支持判定 |
|---|---|---|
| Druid SQL 语法解析层 | Lexer、StatementParser、Statement 类型、表达式节点和方言属性 | 指定 `DbType` 后能够生成业务能力所需的正确 AST |
| SqlParserX 业务解析层 | `ISqlParser`、`BaseSqlParser`、通用 Client、方言 Client 和解析策略 | 能够消费 AST 并输出稳定的表、字段、血缘、建表结构或改写 SQL |
| 数据源专项验证 | 该方言真实 SQL 用例及预期业务结果 | 具备对应数据源的回归用例，且覆盖其方言差异 |

## 二、业务解析能力双层支撑矩阵

> 各业务能力对应的 `BaseSqlParser` 通用实现的**支持粒度与深度**，见 [`../README.md`](../README.md) 导航与 [`../通用解析能力/`](../通用解析能力/)：[函数解析](../通用解析能力/函数解析支持情况.md)、[字段血缘](../通用解析能力/字段血缘解析支持情况.md)、[表血缘](../通用解析能力/表血缘解析支持情况.md)、[表解析](../通用解析能力/表解析支持情况.md)。

| 业务解析能力 | Druid 语法层所需支撑 | SqlParserX 业务层实现 | 当前结论 | 主要输出 / 说明 |
|---|---|---|---|---|
| SQL 语法解析 | 按 `DbType` 提供 Lexer、StatementParser 和 AST | 通过 `BaseSqlParser` 创建或接收 `SQLStatement` | **已支持 ✅** | 为其他业务解析能力提供统一 AST 输入 |
| SQL 语法校验 | Parser 能识别完整 SQL，并在非法语法处抛出异常 | 基于 Druid Parser 的成功或异常判断校验结果 | **已支持 ✅** | 输出校验成功或语法异常；属于语法层直接能力 |
| 多 SQL 切分 | 正确识别字符串、注释、语句边界和方言语法 | `SQLSplitterUtil.splitSQL` 组织切分结果 | **已支持 ✅** | 输出 SQL 语句列表 |
| 真实表解析 | 需要正确的 SELECT、INSERT、UPDATE、DELETE、CREATE、DROP、TRUNCATE 等 Statement 和表源节点 | `parseTables`、`BaseParseTableClient`、`ParseTablesClient` | **已支持 ✅** | 输出主表与全部真实表；MySQL 增强 `RENAME TABLE` 识别 |
| CREATE TABLE 解析 | 需要完整的建表 Statement、列定义、键、分区、分桶和 properties AST | `parseCreateTable`、`ParseCreateTableClient` 及方言 Client | **已支持 ✅** | 输出表、字段、键、分区、分桶等；Doris、StarRocks 有专项扩展 |
| 表血缘解析 | 需要正确的目标表、查询表源、CTE、子查询及集合操作 AST | `parseTableLineages`、`TableLineageClient`、`TableLineageAnalyzer` | **已支持 ✅** | 输出来源表到目标表关系；Doris 启用 CTE 前向引用策略 |
| 字段血缘解析 | 需要正确的 SelectItem、表达式、别名、JOIN、UNION、CTE、窗口函数等 AST | `parseColumnLineages`、`ColumnLineageClient`、`ColumnLineageAnalyzer` | **已支持 ✅** | 输出来源字段到目标字段关系；`SELECT *` 完整展开依赖字段元数据 |
| SQL 函数提取 | 需要正确生成 `SQLMethodInvokeExpr`、`SQLAggregateExpr` 等表达式节点 | `parseFunction` 使用 AST Visitor 遍历函数节点 | **已支持 ✅** | 输出去重后的普通函数和聚合函数名称 |
| 行级权限 SQL 改写 | 需要可遍历、可修改并可重新输出的 SELECT、INSERT、UPDATE、DELETE AST | `getRowLevelPermissionSql`、`BaseSqlFilterClient`、`RowLevelPermissionHandler` | **已支持 ✅** | 输出注入权限过滤条件后的 SQL |
| 列级字段过滤与排序 | 需要正确的 SELECT 输出字段及表达式 AST | 已定义 `filterSortSelectField` 契约，Base 当前返回空字符串 | **预留 ❌** | 尚不可作为正式能力使用 |
| 参数化 SQL 解析 | 需要变量节点、字段表达式和类型相关 AST | 已定义 `parseParameterSql` 和结果模型，Base 当前返回 `null` | **预留 ❌** | 尚未形成可用参数解析结果 |
| 预编译 SQL 构建 | 需要变量、表达式和可序列化 AST | 已定义 `buildSqlByPrepareCompile` 和结果模型，Base 当前返回 `null` | **预留 ❌** | 尚未形成可用预编译构建结果 |

## 三、核心业务接口与结果模型

| 业务能力 | Core 入口 | 主要结果类型 | 当前实现位置 |
|---|---|---|---|
| 真实表解析 | `ISqlParser.parseTables` | `ParseTableResult` | `parser.base.table` + 方言 Table Client |
| CREATE TABLE 解析 | `ISqlParser.parseCreateTable` | `ParseCreateTableResult` | `ParseCreateTableClient` + Doris/StarRocks 方言实现 |
| 字段血缘 | `ISqlParser.parseColumnLineages` | `ColumnLineageResult` | `parser.base.lineage.column` + 方言策略 |
| 表血缘 | `ISqlParser.parseTableLineages` | `TableLineageResult` | `parser.base.lineage.table` + 方言策略 |
| 函数提取 | `ISqlParser.parseFunction` | `Set<String>` | `BaseSqlParser` AST Visitor |
| 行级权限改写 | `ISqlParser.getRowLevelPermissionSql` | 改写后的 SQL | `parser.base.sqlfilter` |
| 参数化 SQL | `ISqlParser.parseParameterSql` | `ParseParameterResult` | 已定义模型，当前实现预留 |
| 预编译 SQL | `ISqlParser.buildSqlByPrepareCompile` | `PrepareCompileResult` | 已定义模型，当前实现预留 |

## 四、数据源双层适配矩阵

下表中的“语法层支撑”表示 Druid 存在对应 `DbType` 并被 Core 方言解析器使用；“业务层支撑”表示 `SqlParserFactory` 存在独立解析器。业务层没有方言定制时，默认继承通用 Base 能力。

> 每个数据源一个子目录、两份文档：`SQL语法解析支持情况.md`（`sqlparserx-syntax`）与 `SQL业务解析支持情况.md`（`sqlparserx-core`）。数据源名称链接到其业务解析支持情况文档。
> 维护约定：① 双层同时成立才算支持；② 语法问题只记语法文档，业务语义问题只记业务文档；③ 新增数据源先建子目录两份文档再在本表登记；④ 支持等级提升前必须补充专项回归用例。

| 数据源 / 方言 | Druid `DbType` | Druid 语法层支撑 | SqlParserX 业务层支撑 | 方言业务增强 | 专项验证 | 综合等级 |
|---|---|---|---|---|---|---|
| [Doris](doris/SQL业务解析支持情况.md) | `doris` | 独立方言 Parser / AST | `DorisSqlParser` | 建表解析、真实表、字段忽略大小写、CTE 前向引用血缘策略 | 字段血缘、表血缘、CREATE TABLE | **重度支持** |
| [MySQL](mysql/SQL业务解析支持情况.md) | `mysql` | 独立方言 Parser / AST | `MySqlParser` | `MysqlParseTablesClient` 补充 `RENAME TABLE` | 主要依赖通用测试 | **重点支持** |
| [StarRocks](starrocks/SQL业务解析支持情况.md) | `starrocks` | 独立方言 Parser / AST | `StarrocksSqlParser` | StarRocks 建表解析 Client | 主要依赖通用测试 | **重点支持** |
| Hive | `hive` | 独立方言 Parser / AST | `HiveSqlParser` | 预留 `HiveColumnLineageClient`；当前主要复用通用逻辑 | 主要依赖通用测试 | 标准支持 |
| Trino | `trino` | 独立方言 Parser / AST | `TrinoSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| Oracle | `oracle` | 独立方言 Parser / AST | `OracleSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| Greenplum | `greenplum` | 独立方言 Parser / AST | `GreenplumSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| TiDB | `tidb` | 独立方言 Parser / AST | `TidbSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| PostgreSQL | `postgresql` | 独立方言 Parser / AST | `PostgresqlSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| SQL Server | `sqlserver` | 独立方言 Parser / AST | `SqlServerSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| OceanBase（MySQL 模式） | `oceanbase` | 独立方言 Parser / AST | `OceanBaseSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| OceanBase（Oracle 模式） | `oceanbase_oracle` | 独立方言 Parser / AST | `OceanBaseOracleSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| 达梦 DM | `dm` | 独立方言 Parser / AST | `DmSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持（仅 Core） |
| ClickHouse | `clickhouse` | 独立方言 Parser / AST | `ClickHouseSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |
| Spark SQL | `spark` | 独立方言 Parser / AST | `SparkSqlParser` | 复用通用业务实现 | 主要依赖通用测试 | 标准支持 |

## 五、支持等级判定

| 等级 | Druid 语法层要求 | SqlParserX 业务层要求 | 验证要求 | 当前数据源 |
|---|---|---|---|---|
| 重度支持 | 独立方言 Parser / AST | 多项明确的方言业务增强 | 已有数据源专项测试 | Doris |
| 重点支持 | 独立方言 Parser / AST | 至少一项明确的方言业务增强 | 当前可主要依赖通用测试 | MySQL、StarRocks |
| 标准支持 | 独立方言 Parser / AST | 已注册独立业务解析器，主要复用 Base | 当前可主要依赖通用测试 | Hive、Trino、Oracle、Greenplum、TiDB、PostgreSQL、SQL Server、OceanBase、OceanBase Oracle、DM、ClickHouse、Spark SQL |

## 六、业务能力与重点数据源交叉检查

| 数据源 | 真实表解析 | CREATE TABLE | 表血缘 | 字段血缘 | 函数提取 | 行级权限 | 说明 |
|---|---|---|---|---|---|---|---|
| Doris | 通用 + 方言增强 | **方言增强 / 专项测试** | **策略增强 / 专项测试** | **策略增强 / 专项测试** | 通用 | 通用 | 当前业务层适配和验证最完整 |
| MySQL | **方言增强** | 通用 | 通用 | 通用 | 通用 | 通用 | 增强 `RENAME TABLE`，其余主要复用 Base |
| StarRocks | 通用 | **方言增强** | 通用 | 通用 | 通用 | 通用 | 建表属性使用方言 Client |
| Hive | 通用 | 通用 | 通用 | 独立扩展点，当前复用通用逻辑 | 通用 | 通用 | 尚无 Hive 专项业务测试 |
| 其余已注册方言 | 通用 | 通用 | 通用 | 通用 | 通用 | 通用 | “通用”表示具备统一实现，不代表已完成同等规模专项验证 |

## 七、Core 与对外数据源编码差异

| 项目 | 当前情况 | 对业务解析的影响 |
|---|---|---|
| Core 正式支持集合 | `SqlParserFactory` 注册 15 种 `DbType` | Java Core 可直接选择这些方言解析器 |
| Web 数据源编码 | 通过 `DatasourceTypeEnum` 和 `DataSourceType2DbType` 映射，并使用 `SqlParserFactory.supports` 校验 | Web `/api/datasource/types` 只展示 Core 已注册的 15 种正式方言；兼容映射不再冒充独立支持 |
| TiDB 路由 | Web 已映射到 `DbType.tidb`，与 Core 的 `TidbSqlParser` 一致 | Web 与 Java Core 均按 TiDB 独立方言解析 |
| DM 暴露 | Web 已补充 DM 数据源编码并映射到 `DbType.dm` | DM 可通过 Web 与 Core 两种入口选择 |
| 未注册方言回退 | 工厂对未注册 `DbType` 返回 `BaseSqlParser` | 回退仅表示尝试使用通用能力，不代表该数据源获得正式支持 |

## 八、验证覆盖与能力边界

| 边界项 | 当前说明 |
|---|---|
| 双层同时成立 | Druid 能解析 SQL 只是前提；只有 SqlParserX 能正确消费 AST 并输出业务结果，才算业务解析能力支持 |
| 通用实现 | “通用支持”表示该方言继承统一业务算法，不表示所有复杂方言 SQL 都已逐条验证 |
| 专项测试 | 当前以 Doris 的字段血缘、表血缘和建表解析专项测试为主，其他方言需逐步补充 |
| 元数据依赖 | 字段血缘中的 `SELECT *` 等场景需要表字段元数据，缺少元数据时无法保证完整展开 |
| 兼容方言 | MariaDB、GoldenDB、EDB、Presto 等可能映射到相近 `DbType`，属于兼容路由，不等于独立双层适配 |
| 预留能力 | 参数解析、预编译构建和列级字段过滤已有接口，但 Base 实现未完成，不能标记为正式支持 |

## 九、新增数据源或能力的检查顺序

| 步骤 | 检查内容 | 处理层次 |
|---|---|---|
| 1 | 使用目标 `DbType` 验证 SQL 能否生成正确 AST | Druid SQL 语法解析层 |
| 2 | 核对业务能力依赖的 Statement、表源、表达式和方言属性是否完整 | Druid SQL 语法解析层 |
| 3 | 使用 `SqlParserFactory` 获取方言解析器并执行目标业务 API | SqlParserX 业务解析层 |
| 4 | AST 正确但业务结果错误时，判断是通用问题还是方言差异 | SqlParserX Base 或方言 Client |
| 5 | 补充最小方言 SQL、预期 AST 特征和预期业务结果 | 双层测试 |
| 6 | 只有通过专项回归后，再提升该数据源的支持等级 | 支持矩阵维护 |
