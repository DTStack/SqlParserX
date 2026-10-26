# SqlParserX 当前能力合集与数据源支持情况

> 本文用于汇总当前已经提供的能力及其调用入口。能力是否适用于某一数据源，还需要同时参考该数据源的语法层、业务层和专项测试情况。

## 一、目前已有能力合集

### 1.1 语法解析层能力（`sqlparserx-syntax`）

| 能力 | 调用方法 | 说明 |
|---|---|---|
| 单条 SQL 解析 | `SqlSyntaxParser.parseStatement(String sql)` | 将单条 SQL 解析为 Druid `SQLStatement` AST |
| 多 SQL 解析 | `SqlSyntaxParser.parseStatements(String sql)` | 将多语句脚本解析为 `List<SQLStatement>` |
| SQL 切分 | `SqlSyntaxParser.split(String sql)` | 按语句边界切分 SQL，保留原始 SQL 形态 |
| SQL 切分并格式化 | `SqlSyntaxParser.splitAndFormat(String sql)` | 切分多语句并输出格式化后的 SQL |
| Druid 原生 SQL 切分 | `SqlSyntaxParser.splitByDruid(String sql)` | 使用 Druid 原生策略进行 SQL 切分 |
| 方言语法路由 | `SqlSyntaxParserFactory.getParser(DbType dbType)` | 根据 Druid `DbType` 获取对应语法解析器 |

### 1.2 业务解析层能力（`sqlparserx-core`）

| 能力 | 调用方法 | 返回结果 | 当前说明 |
|---|---|---|---|
| 真实表解析 | `ISqlParser.parseTables(String sql)` | `ParseTableResult` | 识别主表、真实表、CTE、子查询及常见 DML/DDL 表来源 |
| CREATE TABLE 解析 | `ISqlParser.parseCreateTable(String sql)` | `ParseCreateTableResult` | 解析表、字段、注释、分区、分桶、Key 和 Properties 等结构 |
| 表血缘解析 | `ISqlParser.parseTableLineages(String sql)` | `TableLineageResult` | 输出目标表、来源表及表之间的关系 |
| 字段血缘解析 | `ISqlParser.parseColumnLineages(String sql, Map<String, List<Column>> tableColumnsMap)` | `ColumnLineageResult` | 支持 JOIN、UNION、CTE、子查询、聚合、CASE、窗口函数和元数据辅助的 `SELECT *` 展开 |
| SQL 函数提取 | `ISqlParser.parseFunction(String sql)` | `Set<String>` | 提取 SQL 中使用的函数名称，结果去重 |
| SQL 类型解析 | `ISqlParser.parseSqlType(String sql)` | `SqlType` | 识别 SELECT、INSERT、UPDATE、DELETE、DDL、SHOW、EXPLAIN 等 SQL 类型 |
| 行级权限改写 | `ISqlParser.getRowLevelPermissionSql(String sql, List<RowLevelPermissionFilterParam> permissionParams)` | `String` | 向查询及部分 DML SQL 注入行级权限过滤条件 |
| 参数提取 | `ISqlParser.parseParameterSql(String sql, Boolean isIgnoreSyntaxCheck, Map<String, List<DataTypeColumn>> tableColumnsMap)` | `ParseParameterResult` | 解析 `${name}` 变量、输入参数、输出字段和字段类型；当前面向 SELECT 场景 |
| 预编译 SQL 构建 | `ISqlParser.buildSqlByPrepareCompile(String sql, Map<String, Object> inParams, Map<String, ParamInfo> columns)` | `PrepareCompileResult` | 将变量改写为 `?`，并返回格式化 SQL 与有序参数值；当前面向 SELECT 场景 |

## 二、目前支持的数据源情况

| 数据源/方言 | `SqlParserDataSourceType` | Druid `DbType` | 业务解析器 | 当前支持说明 |
|---|---|---|---|---|
| MySQL | `MYSQL` | `mysql` | `MySqlParser` | 重点支持；包含 `RENAME TABLE` 表解析增强 |
| Doris | `DORIS` | `doris` | `DorisSqlParser` | 重度支持；CREATE TABLE、表/字段血缘和 CTE 策略有专项增强与测试 |
| StarRocks | `STARROCKS` | `starrocks` | `StarrocksSqlParser` | 重点支持；包含分布键、Key 模型等建表解析扩展 |
| Hive | `HIVE` | `hive` | `HiveSqlParser` | 已注册，主要复用通用业务解析能力 |
| Trino | `TRINO` | `trino` | `TrinoSqlParser` | 已注册，主要复用通用业务解析能力 |
| Oracle | `ORACLE` | `oracle` | `OracleSqlParser` | 已注册，主要复用通用业务解析能力 |
| Greenplum | `GREENPLUM` | `greenplum` | `GreenplumSqlParser` | 已注册，主要复用通用业务解析能力 |
| TiDB | `TIDB` | `tidb` | `TidbSqlParser` | 已注册，主要复用通用业务解析能力 |
| PostgreSQL | `POSTGRESQL` | `postgresql` | `PostgresqlSqlParser` | 已注册，主要复用通用业务解析能力 |
| SQL Server | `SQLSERVER` | `sqlserver` | `SqlServerSqlParser` | 已注册，主要复用通用业务解析能力 |
| OceanBase（MySQL 模式） | `OCEANBASE` | `oceanbase` | `OceanBaseSqlParser` | 已注册，主要复用通用业务解析能力 |
| OceanBase（Oracle 模式） | `OCEANBASE_ORACLE` | `oceanbase_oracle` | `OceanBaseOracleSqlParser` | 已注册，主要复用通用业务解析能力 |
| 达梦 DM | `DM` | `dm` | `DmSqlParser` | 已注册，主要复用通用业务解析能力 |
| ClickHouse | `CLICKHOUSE` | `clickhouse` | `ClickHouseSqlParser` | 已注册，主要复用通用业务解析能力 |
| Spark SQL | `SPARK` | `spark` | `SparkSqlParser` | 已注册，主要复用通用业务解析能力 |
