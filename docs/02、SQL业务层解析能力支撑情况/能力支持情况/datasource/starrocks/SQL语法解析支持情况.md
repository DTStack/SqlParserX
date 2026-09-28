# StarRocks SQL 语法解析支持情况

> 维护范围：本文件专门维护 StarRocks 数据源在 **SQL 语法解析层（`sqlparserx-syntax`）** 的支撑情况，即 SQL 文本能否通过 Druid 生成正确、完整的 StarRocks 方言 AST。
> 业务层解析情况见 [SQL业务解析支持情况.md](SQL业务解析支持情况.md)。
> 上层汇总入口见 [SQL解析能力&数据源支撑适配情况.md](../SQL解析能力&数据源支撑适配情况.md)。

## 一、基本信息

| 项目 | 当前情况 |
|---|---|
| 数据源 | StarRocks |
| Druid `DbType` | `starrocks` |
| 语法解析模块 | `sqlparserx-syntax` |
| 语法 Parser 入口 | `SqlSyntaxParserFactory.getParser(DbType.starrocks)` |
| 默认实现 | `DruidSqlSyntaxParser` |
| Druid 版本 | `1.2.28`（定义于 `sqlparserx-engine/pom.xml` 的 `dependencyManagement`） |
| 是否独立方言 Parser / AST | 是，Druid 提供独立 StarRocks 方言 AST（`StarRocksCreateTableStatement` 等） |
| 当前语法等级 | 独立方言语法支持 |

## 二、语法层统一契约

```java
SqlSyntaxParser parser = SqlSyntaxParserFactory.getParser(DbType.starrocks);

SQLStatement statement = parser.parseStatement(sql);
List<SQLStatement> statements = parser.parseStatements(sqlScript);
List<String> sqlList = parser.split(sqlScript);
```

## 三、语法能力支撑矩阵

| 语法能力 | Druid StarRocks 方言支撑 | 当前结论 | 说明 |
|---|---|---|---|
| SQL 语法校验 | 独立方言 Lexer / StatementParser | **已支持 ✅** | 非法语法可抛出异常 |
| 多语句解析 | 支持 `parseStatementList` | **已支持 ✅** | 供脚本场景使用 |
| SQL 切分 | 支持按语句边界切分 | **已支持 ✅** | 统一语法 API 提供 |
| SELECT / INSERT / UPDATE / DELETE | 独立方言 AST | **已支持 ✅** | 业务表解析、血缘依赖 |
| CREATE TABLE（StarRocks 扩展） | `StarRocksCreateTableStatement`、分布键、键类型 | **已支持 ✅** | 建表业务解析依赖 |
| 分布方式（`DISTRIBUTED BY`） | `DistributedByType`（Hash / Random） | **已支持 ✅** | 建表解析依赖 |
| 键类型（`DUPLICATE KEY` / `AGGREGATE KEY`） | `StarRocksDuplicateKey`、`StarRocksAggregateKey` | **已支持 ✅** | 建表解析依赖 |
| DROP / TRUNCATE | 独立方言 AST | **已支持 ✅** | 真实表解析依赖 |
| CTE / 子查询 / 集合操作 | 独立方言 AST | **已支持 ✅** | 表、字段血缘依赖 |
| 函数与表达式 | 函数调用与聚合表达式节点 | **已支持 ✅** | 函数提取依赖 |

## 四、业务能力对语法层的依赖

| 业务能力 | 语法层必须提供 | 状态 |
|---|---|---|
| 真实表解析 | DML/DDL 语句及表源节点 | 满足 ✅ |
| CREATE TABLE 解析 | 建表 Statement、分布键、键类型、列定义、properties AST | 满足 ✅ |
| 表血缘解析 | 目标表、查询表源、CTE、子查询、集合操作 AST | 满足 ✅ |
| 字段血缘解析 | SelectItem、表达式、别名、JOIN、UNION、CTE、窗口函数 AST | 满足 ✅ |
| 函数提取 | 函数调用与聚合表达式节点 | 满足 ✅ |
| 行级权限改写 | 可遍历、可修改、可重新输出的 DML / SELECT AST | 满足 ✅ |

## 五、语法层禁止事项

- 不在 `sqlparserx-syntax` 内实现真实表、建表结构、血缘等业务语义。
- 不在业务层用正则替换规避 StarRocks 语法解析问题。
- 需要扩充 StarRocks 关键字、Statement 或 AST 节点时，扩展位置为 `sqlparserx-syntax`。

## 六、验证与维护

| 项目 | 当前情况 |
|---|---|
| 语法专项测试 | 依赖 Druid StarRocks 方言回归；SqlParserX 侧主要依赖通用测试间接覆盖 |
| 语法测试入口 | `SqlSyntaxParserFactory.getParser(DbType.starrocks)` + `SqlSyntaxParser` |
| 升级约束 | 升级 Druid 前先运行 StarRocks 语法解析测试，再运行业务层回归 |
| 待补充 | StarRocks 特有建表/分布语法的独立语法用例 |

## 七、变更记录

| 日期 | 变更 |
|---|---|
| 2026-09-28 | 初始化 StarRocks 语法解析支持情况，同步 `sqlparserx-syntax` 统一语法契约 |