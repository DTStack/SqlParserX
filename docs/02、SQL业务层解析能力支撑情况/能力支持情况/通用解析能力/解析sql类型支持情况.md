# 通用解析能力 · SQL 类型解析支持情况

> 维护范围：`sqlparserx-core` 中基于 `ISqlParser` / `IParseSqlTypeClient` / `BaseParseSqlTypeClient`
> 的 SQL 业务类型判定能力。
> Core 入口：`ISqlParser.parseSqlType(String)` / `ISqlParser.parseSqlType(SQLStatement)`。
> 上层汇总入口见 [SQL解析能力&数据源支撑适配情况.md](../datasource/SQL解析能力&数据源支撑适配情况.md)。

## 用例示范（快速上手）

```java
ISqlParser parser = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);

SqlType query = parser.parseSqlType("select * from user_info");
SqlType cte = parser.parseSqlType("with u as (select 1) select * from u");
SqlType createAs = parser.parseSqlType("create table t2 as select * from t1");

System.out.println(query);     // QUERY
System.out.println(cte);       // WITH_QUERY
System.out.println(createAs);  // CREATE_AS
```

该能力返回面向业务的 `SqlType`，用于路由解析能力、选择执行策略或做语句准入控制。它不返回表、字段或血缘信息。

## 一、实现目标与判定原则

SQL 类型解析先由当前数据源的语法解析器把 SQL 文本转换成 Druid `SQLStatement`，再依据 AST 节点的实际类型进行分派：

```text
SQL 文本
  -> 当前数据源 SqlSyntaxParser.parseStatement
  -> Druid SQLStatement
  -> BaseParseSqlTypeClient.parseSqlType
  -> instanceof 精确分派
  -> SqlType；未命中返回 OTHER
```

这里不使用 SQL 前缀、正则或字符串包含关系判断，因此换行、注释和关键字大小写不会改变已经成功解析出的 AST 类型。方言是否能解析某条语句，以及同一文本会生成哪种 AST，由当前解析器的 Druid `DbType` 决定。

## 二、Core 入口与扩展位置

| 项目 | 内容 |
|---|---|
| 文本入口 | `ISqlParser.parseSqlType(String sql)` |
| AST 入口 | `ISqlParser.parseSqlType(SQLStatement sqlStatement)` |
| 默认实现 | `BaseSqlParser` → `IParseSqlTypeClient` → `BaseParseSqlTypeClient` |
| 返回类型 | `SqlType` |
| 未识别节点 | 返回 `SqlType.OTHER` |
| 空 AST | 返回 `SqlType.OTHER` |
| 方言扩展 | 数据源 Parser 可覆盖 `getParseSqlTypeClient()`，或继承 Client 覆盖细分类方法 |

文本入口只负责使用当前方言构建 AST，随后与 AST 入口走同一套分类逻辑。SQL 文本语法错误由语法解析层抛出，不会被转换成 `OTHER`；`OTHER` 表示 AST 已生成，但当前业务分类未收录该节点类型。

## 三、SQL 类型支持矩阵

### 3.1 查询与数据操作

| SQL 形态 | `SqlType` | 支持情况 | 判定依据 |
|---|---|---|---|
| `SELECT ... FROM ...` | `QUERY` | **已支持 ✅** | `SQLSelectStatement`，普通主体查询 |
| `SELECT 1` 等无 FROM 查询 | `QUERY_NO_FROM` | **已支持 ✅** | 主体为 `SQLSelectQueryBlock` 且 `from == null` |
| `WITH ... SELECT ...` | `WITH_QUERY` | **已支持 ✅** | `SQLSelect#getWithSubQuery()` 非空 |
| `INSERT` | `INSERT` | **已支持 ✅** | `SQLInsertStatement` |
| `UPDATE` | `UPDATE` | **已支持 ✅** | `SQLUpdateStatement` |
| `DELETE` | `DELETE` | **已支持 ✅** | `SQLDeleteStatement` |
| `TRUNCATE` | `TRUNCATE` | **已支持 ✅** | `SQLTruncateStatement` |
| `MERGE INTO` | `MERGE_INTO` | **已支持 ✅** | `SQLMergeStatement` |
| `BEGIN ... END` / block | `BEGIN_END` | **已支持，依赖方言 ⚠️** | `SQLBlockStatement` / `SQLBeginStatement` |

查询分类的优先级为 `WITH_QUERY` → `QUERY_NO_FROM` → `QUERY`。因此带 CTE 的无 FROM 主查询仍返回 `WITH_QUERY`。

### 3.2 建表、视图与通用 DDL

| SQL 形态 | `SqlType` | 支持情况 | 说明 |
|---|---|---|---|
| `CREATE TABLE` | `CREATE` | **已支持 ✅** | 普通建表 |
| `CREATE TABLE ... AS SELECT` | `CREATE_AS` | **已支持 ✅** | `select != null` 优先判定 |
| `CREATE TABLE ... LIKE ...` | `CREATE_LIKE` | **已支持 ✅** | `like != null` |
| `CREATE TEMPORARY TABLE` | `CREATE_TEMP` | **已支持 ✅** | `isTemporary()` |
| `CREATE VIEW ... AS SELECT` | `CREATE_AS` | **已支持 ✅** | 当前将带子查询的建视图统一归到 `CREATE_AS` |
| 无子查询的 `CREATE VIEW` AST | `CREATE_VIEW` | **已支持 ✅** | `subQuery == null` |
| `DROP TABLE` / `DROP VIEW` | `DROP` | **已支持 ✅** | 两者共用业务类型 |
| `COMMENT ON` | `COMMENT_ON` | **已支持，依赖方言 ⚠️** | `SQLCommentStatement` |
| 未细分的 `ALTER TABLE` | `ALTER` | **已支持 ✅** | alter item 未命中细分类时兜底 |

建表细分优先级为 `CREATE_AS` → `CREATE_LIKE` → `CREATE_TEMP` → `CREATE`。如果某个方言 AST 同时具有多个标记，返回最先命中的业务类型。

### 3.3 ALTER TABLE 细分

| DDL 操作 | `SqlType` | 支持情况 |
|---|---|---|
| 重命名表 | `ALTERTABLE_RENAME` | **已支持 ✅** |
| 重命名字段 | `ALTERTABLE_RENAMECOL` | **已支持 ✅** |
| 增加字段 | `ALTERTABLE_ADDCOLS` | **已支持 ✅** |
| 替换字段 | `ALTERTABLE_REPLACECOLS` | **已支持，依赖方言 ⚠️** |
| 增加分区 | `ALTERTABLE_ADDPARTS` | **已支持，依赖方言 ⚠️** |
| 删除分区 | `ALTERTABLE_DROPPARTS` | **已支持，依赖方言 ⚠️** |
| 重命名分区 | `ALTERTABLE_RENAMEPART` | **已支持，依赖方言 ⚠️** |
| 其他 ALTER TABLE | `ALTER` | **兜底 ✅** |

一个 `ALTER TABLE` 如果包含多个 alter item，当前按 item 顺序返回第一个可识别细分类，不组合返回多个类型。

### 3.4 SHOW、DESCRIBE 与 EXPLAIN

| SQL 形态 | `SqlType` | 支持情况 |
|---|---|---|
| `SHOW TABLES` | `SHOW_TABLES` | **已支持 ✅** |
| `SHOW CREATE TABLE` | `SHOW_CREATETABLE` | **已支持 ✅** |
| `SHOW COLUMNS` | `SHOW_COLUMNS` | **已支持，依赖方言 ⚠️** |
| `SHOW FUNCTIONS` | `SHOW_FUNCTION` | **已支持，依赖方言 ⚠️** |
| 其他 `SHOW` | `SHOW` | **兜底 ✅** |
| `SHOW DATABASES` | `DATABASE_SHOW` | **已支持 ✅** |
| `DESCRIBE DATABASE` | `DATABASE_DESC` | **已支持，依赖方言 ⚠️** |
| `DESCRIBE TABLE` | `DESC_TABLE` | **已支持，依赖方言 ⚠️** |
| `EXPLAIN` / `EXPLAIN ANALYZE` | `EXPLAIN` | **已支持 ✅** |

同一关键字在不同方言中可能对应不同 AST。例如 MySQL 的 `DESC t1` 会被 Druid 解析为执行计划语句，因此返回 `EXPLAIN`；PostgreSQL 方言下测试使用 `DESCRIBE t1`，对应 `SQLDescribeStatement`，返回 `DESC_TABLE`。

### 3.5 物化视图、函数、过程与数据库

| SQL 形态 | `SqlType` | 支持情况 |
|---|---|---|
| 创建物化视图 | `CREATE_MATERIALIZED_VIEW` | **已支持，依赖方言 ⚠️** |
| 删除物化视图 | `DROP_MATERIALIZED_VIEW` | **已支持，依赖方言 ⚠️** |
| 修改物化视图 | `ALTER_MATERIALIZED_VIEW` | **已支持，依赖方言 ⚠️** |
| 刷新物化视图 | `REFRESH_MATERIALIZED_VIEW` | **已支持，依赖方言 ⚠️** |
| 查看物化视图 | `SHOW_MATERIALIZED_VIEW` | **已支持，依赖方言 ⚠️** |
| 查看物化视图建语句 | `SHOW_CREATE_MATERIALIZED_VIEW` | **已支持，依赖方言 ⚠️** |
| 创建函数 | `CREATE_FUNCTION` | **已支持，依赖方言 ⚠️** |
| 删除函数 | `DROP_FUNCTION` | **已支持，依赖方言 ⚠️** |
| `CALL` / SQL Server `EXEC` | `PROCEDURE_EXEC` | **已支持，依赖方言 ⚠️** |
| 创建数据库 | `DATABASE_CREATE` | **已支持 ✅** |
| 删除数据库 | `DATABASE_DROP` | **已支持 ✅** |

“依赖方言”表示分类分支已实现，前提是所选数据源的 Druid Parser 能把输入解析成对应 AST 节点；它不代表所有已接入数据源都接受同一种语法文本。

## 四、返回值约定

`SqlType` 同时保存枚举名、业务标识字符串 `type` 和整数编码 `value`：

```java
SqlType type = parser.parseSqlType(sql);
type.name();      // 例如 WITH_QUERY
type.getType();   // 例如 "with_query"
type.getValue();  // 例如 49
```

- `SqlType.getByType(String)` 忽略大小写并去除首尾空白，未匹配返回 `OTHER`。
- `SqlType.getByValue(Integer)` 按枚举声明顺序匹配，未匹配返回 `OTHER`。
- 当前 `CREATE_TEMP` 与 `COMMENT_ON` 的整数编码都为 `200`；按值反查会先得到 `CREATE_TEMP`。需要无歧义持久化或传输时应优先使用枚举名或 `type` 字符串。
- `QUERY_NO_FROM` 的历史业务标识为 `"xxx"`，当前保持兼容；业务侧不应根据枚举名自行推导 `type`。

## 五、能力边界

| 边界项 | 说明 |
|---|---|
| 类型识别粒度 | 返回一条语句的单一主类型，不返回复合类型集合 |
| 多语句文本 | 文本入口使用单语句解析入口；需要批量分类时应先拆成多个 `SQLStatement` 后逐个调用 AST 入口 |
| 语法错误 | 由当前方言的语法解析层抛出，不会返回 `OTHER` |
| 未支持 AST | 已成功解析但没有分类分支的语句返回 `OTHER`，例如当前测试中的 `GRANT` |
| 方言差异 | 分类依据 AST 类型；同一文本在不同方言下可能解析失败或得到不同分类 |
| ALTER 多动作 | 只返回首个被识别的 alter item 类型 |
| CREATE VIEW | 带查询的建视图当前统一返回 `CREATE_AS`，不会返回 `CREATE_VIEW` |
| DROP | 普通表与普通视图统一返回 `DROP` |
| 业务编码 | `value` 存在历史重复值，按值反查并非所有枚举都可逆 |

## 六、测试与验证

| 项目 | 当前情况 |
|---|---|
| 测试类 | `core/base/sqltype/BaseParseSqlTypeClientTest`，共 6 个测试方法 |
| 查询/DML | 覆盖普通查询、无 FROM、WITH、INSERT、UPDATE、DELETE、TRUNCATE |
| DDL | 覆盖 CREATE、CTAS、LIKE、TEMP、DROP、增加列、重命名列和 ALTER 兜底 |
| 描述类 | 覆盖 SHOW TABLES、SHOW CREATE TABLE、EXPLAIN，以及 MySQL/PostgreSQL 的 DESC 差异 |
| 方言场景 | 覆盖 Oracle COMMENT/CREATE VIEW、PostgreSQL MERGE INTO |
| 两种入口 | 覆盖 SQL 文本入口及 `SQLStatement` AST 入口 |
| 兜底 | 覆盖未分类的 GRANT 返回 `OTHER` |
| 聚合套件 | 已加入 `BaseSuite`，并通过 `AllTestsSuite` 执行 |

最近验证（2026-10-13）：`AllTestsSuite` 114 个通过；core 与依赖模块常规全量 118 个通过。

## 七、变更记录

| 日期 | 变更 |
|---|---|
| 2026-10-12 | 新增 `SqlType`、`IParseSqlTypeClient` 与基于 Druid AST 的默认分类实现，提供文本和 AST 两种入口 |
| 2026-10-13 | 补充查询、DML、DDL、SHOW/DESCRIBE/EXPLAIN 与方言差异测试，并加入 `BaseSuite` |
| 2026-10-13 | 完善支持矩阵、判定优先级、返回值约定、方言依赖、历史编码兼容和能力边界 |
