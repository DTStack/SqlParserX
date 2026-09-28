# MySQL SQL 业务解析支持情况

> 维护范围：本文件专门维护 MySQL 数据源在 **SqlParserX 业务解析层（`sqlparserx-core`）** 的支撑情况。
> 语法层解析情况见 [SQL语法解析支持情况.md](SQL语法解析支持情况.md)。
> 上层汇总入口见 [SQL解析能力&数据源支撑适配情况.md](../SQL解析能力&数据源支撑适配情况.md)。

## 一、基本信息

| 项目 | 当前情况 |
|---|---|
| 数据源 | MySQL |
| Druid `DbType` | `mysql` |
| 业务解析模块 | `sqlparserx-core` |
| 业务解析器 | `MySqlParser`（继承 `BaseSqlParser`） |
| 工厂入口 | `SqlParserFactory` |
| 综合支持等级 | **重点支持** |
| 专项测试 | 主要依赖通用测试 |

## 二、业务层实现位置

```text
sqlparserx-core
└── com.dtstack.sqlparserx.core.parser.mysql
    ├── MySqlParser.java
    └── table
        └── MysqlParseTablesClient.java
```

| 类型 | 职责 |
|---|---|
| `MySqlParser` | MySQL 业务解析器，注册方言真实表解析 Client |
| `MysqlParseTablesClient` | 继承 `ParseTablesClient`，补充 `RENAME TABLE` 真实表识别 |

## 三、方言业务增强

| 增强点 | 实现位置 | 说明 |
|---|---|---|
| `RENAME TABLE` 真实表识别 | `MysqlParseTablesClient.parseTables` | 命中 `MySqlRenameTableStatement` 时解析 `mainTable` 与 `allTables` |

```java
// 命中 MySqlRenameTableStatement 时，主表取第一个表名，全部表取所有表名
if (stmt instanceof MySqlRenameTableStatement) {
    this.parseMySqlRenameTable((MySqlRenameTableStatement) stmt);
}
```

## 四、业务能力支撑矩阵

| 业务能力 | Core 入口 | MySQL 实现 | 当前结论 | 专项验证 |
|---|---|---|---|---|
| 真实表解析 | `ISqlParser.parseTables` | `MysqlParseTablesClient`（增强 `RENAME TABLE`） | **已支持 ✅** | 通用用例 |
| CREATE TABLE 解析 | `ISqlParser.parseCreateTable` | 通用 `ParseCreateTableClient` | **已支持 ✅** | 通用用例 |
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
| MySQL 专项测试 | 当前缺失，`RENAME TABLE` 增强主要依赖通用测试间接覆盖 |
| 建议补充 | 针对 `RENAME TABLE`、MySQL 特有 DDL 的专项用例 |

## 六、能力边界

| 边界项 | 说明 |
|---|---|
| 方言增强范围 | 仅真实表解析补充 `RENAME TABLE`，其余能力复用 Base |
| 无专项测试 | 增强逻辑缺少 MySQL 专项回归，需逐步补充 |
| 兼容路由 | Web 保留部分历史兼容映射，但 TiDB 已切换为 Core 独立的 `DbType.tidb`；兼容映射不会出现在正式支持列表 |
| 预留能力 | 列级字段过滤、参数化解析、预编译构建尚未完成 |

## 七、变更记录

| 日期 | 变更 |
|---|---|
| 2026-09-28 | 初始化 MySQL 业务解析支持情况，记录 `RENAME TABLE` 增强与测试缺口 |
