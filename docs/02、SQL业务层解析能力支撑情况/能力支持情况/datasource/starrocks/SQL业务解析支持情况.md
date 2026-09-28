# StarRocks SQL 业务解析支持情况

> 维护范围：本文件专门维护 StarRocks 数据源在 **SqlParserX 业务解析层（`sqlparserx-core`）** 的支撑情况。
> 语法层解析情况见 [SQL语法解析支持情况.md](SQL语法解析支持情况.md)。
> 上层汇总入口见 [SQL解析能力&数据源支撑适配情况.md](../SQL解析能力&数据源支撑适配情况.md)。

## 一、基本信息

| 项目 | 当前情况 |
|---|---|
| 数据源 | StarRocks |
| Druid `DbType` | `starrocks` |
| 业务解析模块 | `sqlparserx-core` |
| 业务解析器 | `StarrocksSqlParser`（继承 `BaseSqlParser`） |
| 工厂入口 | `SqlParserFactory` |
| 综合支持等级 | **重点支持** |
| 专项测试 | 主要依赖通用测试 |

## 二、业务层实现位置

```text
sqlparserx-core
└── com.dtstack.sqlparserx.core.parser.starrocks
    ├── StarrocksSqlParser.java
    └── table
        ├── StarrocksParseTableClient.java
        └── StarrocksParseCreateTableClient.java
```

| 类型 | 职责 |
|---|---|
| `StarrocksSqlParser` | StarRocks 业务解析器，注册方言真实表解析 Client |
| `StarrocksParseTableClient` | 继承 `BaseParseTableClient`，建表 Client 使用 StarRocks 实现 |
| `StarrocksParseCreateTableClient` | 继承 `ParseCreateTableClient`，处理 StarRocks 建表特有语法；同时被 Doris 复用 |

## 三、方言业务增强

| 增强点 | 实现位置 | 说明 |
|---|---|---|
| 分布方式解析 | `StarrocksParseCreateTableClient.dealDistributeByColumns` | 识别 `StarRocksCreateTableStatement` 的 `DistributedByType`（Hash / Random）及分布列 |
| 键类型识别 | `StarrocksParseCreateTableClient.dealTableKey` | 识别 `StarRocksDuplicateKey` → `DUPLICATE`、`StarRocksAggregateKey` → `AGGREGATE` |

## 四、业务能力支撑矩阵

| 业务能力 | Core 入口 | StarRocks 实现 | 当前结论 | 专项验证 |
|---|---|---|---|---|
| 真实表解析 | `ISqlParser.parseTables` | `StarrocksParseTableClient` | **已支持 ✅** | 通用用例 |
| CREATE TABLE 解析 | `ISqlParser.parseCreateTable` | `StarrocksParseCreateTableClient` | **已支持 ✅** | 通用用例（无 StarRocks 专项） |
| 表血缘解析 | `ISqlParser.parseTableLineages` | 通用 `TableLineageClient` | **已支持 ✅** | 通用用例 |
| 字段血缘解析 | `ISqlParser.parseColumnLineages` | 通用 `ColumnLineageClient` | **已支持 ✅** | 通用用例 |
| 函数提取 | `ISqlParser.parseFunction` | 通用 AST Visitor | **已支持 ✅** | 通用用例 |
| 行级权限改写 | `ISqlParser.getRowLevelPermissionSql` | `BaseSqlFilterClient` | **已支持 ✅** | 通用用例 |
| 列级字段过滤与排序 | `ISqlParser.filterSortSelectField` | Base 返回空字符串 | **预留 ❌** | 无 |
| 参数化 SQL / 预编译 | `parseParameterSql` / `buildSqlByPrepareCompile` | Base 返回 `null` | **预留 ❌** | 无 |

## 五、测试情况

| 项目 | 当前情况 |
|---|---|
| 通用测试 | 表解析、建表解析、表/字段血缘、函数、权限改写通用用例 |
| StarRocks 专项测试 | 当前缺失，建表方言增强主要依赖通用测试间接覆盖 |
| 建议补充 | StarRocks `DISTRIBUTED BY`、`DUPLICATE KEY` / `AGGREGATE KEY` 建表专项用例 |

## 六、能力边界

| 边界项 | 说明 |
|---|---|
| 方言增强范围 | 仅建表解析补充分布方式与键类型，其余能力复用 Base |
| 无专项测试 | 建表方言增强缺少 StarRocks 专项回归，需逐步补充 |
| 与 Doris 复用 | `StarrocksParseCreateTableClient` 被 `DorisParseCreateTableClient` 继承，修改时需同时考虑 Doris |
| 预留能力 | 列级字段过滤、参数化解析、预编译构建尚未完成 |

## 七、变更记录

| 日期 | 变更 |
|---|---|
| 2026-09-28 | 初始化 StarRocks 业务解析支持情况，记录建表方言增强与测试缺口 |