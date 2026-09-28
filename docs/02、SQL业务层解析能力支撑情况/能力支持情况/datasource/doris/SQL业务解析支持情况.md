# Doris SQL 业务解析支持情况

> 维护范围：本文件专门维护 Doris 数据源在 **SqlParserX 业务解析层（`sqlparserx-core`）** 的支撑情况，即基于 Doris 方言 AST 能输出哪些稳定的业务结果。
> 语法层解析情况见 [SQL语法解析支持情况.md](SQL语法解析支持情况.md)。
> 上层汇总入口见 [SQL解析能力&数据源支撑适配情况.md](../SQL解析能力&数据源支撑适配情况.md)。

## 一、基本信息

| 项目 | 当前情况 |
|---|---|
| 数据源 | Doris |
| Druid `DbType` | `doris` |
| 业务解析模块 | `sqlparserx-core` |
| 业务解析器 | `DorisSqlParser`（继承 `BaseSqlParser`） |
| 工厂入口 | `SqlParserFactory` |
| 综合支持等级 | **重度支持** |
| 专项测试 | 字段血缘、表血缘、CREATE TABLE |

## 二、业务层实现位置

```text
sqlparserx-core
└── com.dtstack.sqlparserx.core.parser.doris
    ├── DorisSqlParser.java
    └── table
        ├── DorisParseTableClient.java
        └── DorisParseCreateTableClient.java
```

| 类型 | 职责 |
|---|---|
| `DorisSqlParser` | Doris 业务解析器，决定字段血缘策略、表解析 Client 和表血缘策略 |
| `DorisParseTableClient` | 继承 `StarrocksParseTableClient` 复用真实表解析逻辑，建表 Client 使用 Doris 实现 |
| `DorisParseCreateTableClient` | 继承 `StarrocksParseCreateTableClient`，开启动态分区识别 |

## 三、方言业务增强

| 增强点 | 实现位置 | 说明 |
|---|---|---|
| 字段血缘忽略大小写 | `DorisSqlParser.getColumnLineageClient` | 使用 `COLUMN_IGNORE_CASE`：SQL 字段与元数据表字段忽略大小写匹配 |
| 子查询字段忽略大小写 | `DorisSqlParser.getColumnLineageClient` | 使用 `SUBQUERY_COLUMN_IGNORE_CASE` |
| CTE 前向引用血缘 | `DorisSqlParser.getTableLineageClient` | 使用 `CTE_FORWARD_REFERENCE`，支持 CTE 前向引用推断 |
| 真实表解析复用 | `DorisParseTableClient` | 继承 StarRocks 表解析逻辑 |
| 动态分区建表解析 | `DorisParseCreateTableClient` | `isDynamicPartitionEnabled()` 返回 `true`，识别 `dynamic_partition.enable=true` |

## 四、业务能力支撑矩阵

| 业务能力 | Core 入口 | Doris 实现 | 当前结论 | 专项验证 |
|---|---|---|---|---|
| 真实表解析 | `ISqlParser.parseTables` | `DorisParseTableClient` | **已支持 ✅** | 复用 StarRocks/通用用例 |
| CREATE TABLE 解析 | `ISqlParser.parseCreateTable` | `DorisParseCreateTableClient` | **已支持 ✅** | `DorisCreateTableTest` |
| 表血缘解析 | `ISqlParser.parseTableLineages` | `TableLineageClient` + CTE 前向引用策略 | **已支持 ✅** | `DorisTableLineageTest` |
| 字段血缘解析 | `ISqlParser.parseColumnLineages` | `ColumnLineageClient` + 忽略大小写策略 | **已支持 ✅** | `DorisColumnLineageTest` |
| 函数提取 | `ISqlParser.parseFunction` | 通用 AST Visitor | **已支持 ✅** | 通用用例 |
| 行级权限改写 | `ISqlParser.getRowLevelPermissionSql` | `BaseSqlFilterClient` | **已支持 ✅** | 通用用例 |
| 列级字段过滤与排序 | `ISqlParser.filterSortSelectField` | Base 返回空字符串 | **预留 ❌** | 无 |
| 参数化 SQL / 预编译 | `parseParameterSql` / `buildSqlByPrepareCompile` | Base 返回 `null` | **预留 ❌** | 无 |

## 五、专项测试

| 测试类 | 资源文件 | 覆盖能力 |
|---|---|---|
| `DorisColumnLineageTest` | `core/datasource/doris/DorisColumnLineageTest.txt` | 字段血缘，含忽略大小写、CTE 等场景 |
| `DorisTableLineageTest` | `core/datasource/doris/DorisTableLineageTest.txt` | 表血缘，含 CTE 前向引用 |
| `DorisCreateTableTest` | `core/datasource/doris/DorisCreateTableTest.txt` | CREATE TABLE，含键、分区、分桶、动态分区 |

测试入口：`sqlparserx-core/src/test/java/com/dtstack/sqlparserx/core/datasource/DatasourceSuite.java`。

## 六、能力边界

| 边界项 | 说明 |
|---|---|
| `SELECT *` 展开 | 需要表字段元数据，缺少元数据时无法保证完整展开 |
| 动态分区识别 | 依赖 `dynamic_partition.enable=true` 属性判断 |
| 未覆盖语法 | Doris 特有新分区、新数据类型等语法需先确认语法层 AST 正确性 |
| 预留能力 | 列级字段过滤、参数化解析、预编译构建尚未完成 |

## 七、变更记录

| 日期 | 变更 |
|---|---|
| 2026-09-28 | 初始化 Doris 业务解析支持情况，整理方言增强、业务矩阵与专项测试 |