# 解析 SQL 类型实现方案

> 状态：初步实现
> 能力入口：`ISqlParser#parseSqlType(String)` / `ISqlParser#parseSqlType(SQLStatement)`
> 底座：**仅 Druid 1.2.28**（`sqlparserx-syntax` 已提供 `String → SQLStatement`）
> 目标：SqlParserX 拥有自己的业务层 `SqlType` 定义，并基于 **Druid 语法树**判定 SQL 类型

---

## 一、背景与目标

平台在「SQL 转 API / 权限 / 审计」等场景需要先知道一条 SQL 属于哪一类操作（查询 / 写入 / DDL / SHOW…），再决定后续处理：

- **能力入口**：`ISqlParser#parseSqlType`，提供 `String` 与 `SQLStatement` 两个重载，返回业务枚举 `SqlType`；
- **判定方式**：优先用 **Druid AST 分派**（`SQLStatement` 的具体子类 + 关键字段）判定，而非文本正则；
- **范围收敛**：一期只覆盖通用 Druid 语句；方言专属语句与无法识别的一律归 `SqlType.OTHER`，不做正则兜底。

```text
String SQL  ->  BaseSqlParser.createStatement(sql)  ->  SQLStatement（Druid AST）
                                                          │
                                             IParseSqlTypeClient.parseSqlType(stmt)
                                                          │
                                                       SqlType（业务枚举）
```

---

## 二、业务层 `SqlType` 枚举定义

`com.dtstack.sqlparserx.core.enums.SqlType`，**仅收录可由 `parseSqlType` 判定出的类型**，外加兜底 `OTHER`；`type` 为对外业务标识串，`value` 为整型编码。

### 2.1 查询

| 常量 | type | value | 说明 |
|---|---|---|---|
| `QUERY` | `select` | 44 | 查询 |
| `WITH_QUERY` | `with_query` | 49 | 带 CTE 的查询 |
| `QUERY_NO_FROM` | `xxx` | 48 | 无 from 的简单查询 |

### 2.2 数据操作（DML）

| 常量 | type | value | 说明 |
|---|---|---|---|
| `INSERT` | `insert` | 41 | 插入 |
| `UPDATE` | `update` | 45 | 更新 |
| `DELETE` | `delete` | 46 | 删除 |
| `TRUNCATE` | `truncate` | 43 | 清空表 |
| `MERGE_INTO` | `merge_into` | 50 | Merge Into |
| `BEGIN_END` | `begin_end` | 51 | 过程块 |

### 2.3 结构操作（DDL）

| 常量 | type | value | 说明 |
|---|---|---|---|
| `CREATE` | `create` | 101 | 建表 |
| `CREATE_AS` | `create_as` | 103 | Create As Select（含视图 as） |
| `CREATE_LIKE` | `create_like` | 102 | Create Table Like |
| `CREATE_TEMP` | `create_temp` | 200 | 建临时表 |
| `DROP` | `drop` | 104 | 删表 / 删视图 |
| `ALTER` | `alter` | 105 | 改表（笼统） |
| `ALTERTABLE_RENAME` | `altertable_rename` | 106 | 改表名 |
| `ALTERTABLE_RENAMECOL` | `altertable_renamecol` | 113 | 改列名 |
| `ALTERTABLE_ADDCOLS` | `altertable_addcols` | 114 | 加列 |
| `ALTERTABLE_REPLACECOLS` | `altertable_replacecols` | 115 | 替换列 |
| `ALTERTABLE_ADDPARTS` | `altertable_addparts` | 109 | 加分区 |
| `ALTERTABLE_DROPPARTS` | `altertable_dropparts` | 111 | 删分区 |
| `ALTERTABLE_RENAMEPART` | `altertable_renamepart` | 110 | 改分区名 |
| `COMMENT_ON` | `comment_on` | 200 | Comment On |

### 2.4 描述 / SHOW

| 常量 | type | value | 说明 |
|---|---|---|---|
| `DESC_TABLE` | `desc_table` | 176 | 表描述 |
| `DATABASE_DESC` | `databases_desc` | 13 | 库描述 |
| `SHOW` | `show` | 180 | Show（笼统兜底） |
| `SHOW_TABLES` | `show_tables` | 171 | 显示表 |
| `SHOW_CREATETABLE` | `show_createtable` | 172 | 显示建表语句 |
| `SHOW_COLUMNS` | `show_columns` | 175 | 显示列 |
| `SHOW_FUNCTION` | `show_function` | 25 | 显示函数 |
| `SHOW_MATERIALIZED_VIEW` | `show_materialized_view` | 406 | 显示物化视图 |
| `SHOW_CREATE_MATERIALIZED_VIEW` | `show_create_materialized_view` | 407 | 显示建物化视图语句 |
| `EXPLAIN` | `explain` | 177 | 执行计划 |

### 2.5 视图 / 物化视图 / 函数 / 数据库

| 常量 | type | value | 说明 |
|---|---|---|---|
| `CREATE_VIEW` | `create_view` | 401 | 建视图（无 as select） |
| `CREATE_MATERIALIZED_VIEW` | `create_materialized_view` | 403 | 建物化视图 |
| `DROP_MATERIALIZED_VIEW` | `drop_materialized_view` | 404 | 删物化视图 |
| `ALTER_MATERIALIZED_VIEW` | `alter_materialized_view` | 405 | 改物化视图 |
| `REFRESH_MATERIALIZED_VIEW` | `refresh_materialized_view` | 408 | 刷新物化视图 |
| `CREATE_FUNCTION` | `create_function` | 22 | 建函数 |
| `DROP_FUNCTION` | `drop_function` | 23 | 删函数 |
| `PROCEDURE_EXEC` | `procedure_exec` | 410 | 存储过程 / EXEC |
| `DATABASE_CREATE` | `databases_create` | 11 | 建库 |
| `DATABASE_DROP` | `databases_drop` | 12 | 删库 |
| `DATABASE_SHOW` | `databases_show` | 14 | 列出库 |

### 2.6 兜底

| 常量 | type | value | 说明 |
|---|---|---|---|
| `OTHER` | `other` | 500 | 未识别 / 暂不支持 |

### 2.7 字段与辅助方法

- `getType()` / `getValue()`：读取业务标识串与编码；
- `getByType(String)`：按 `type` 解析（忽略大小写），未命中返回 `OTHER`；
- `getByValue(Integer)`：按 `value` 解析，未命中返回 `OTHER`；
- **value 说明**：`COMMENT_ON` 与 `CREATE_TEMP` 均为 `200`（沿用历史口径），`getByValue(200)` 返回首个匹配项。

---

## 三、Druid 判定方式

### 3.1 结论

**可以。** 上述类型均可由 Druid 的 `SQLStatement` 子类（配合少量关键字段/子项）直接判定，无需文本匹配。

### 3.2 判定总表

| SqlType | Druid 节点 / 条件 |
|---|---|
| `QUERY` | `SQLSelectStatement`，`SQLSelect#getWithSubQuery() == null` 且主体 queryBlock 有 from |
| `QUERY_NO_FROM` | `SQLSelectStatement`，主体 `SQLSelectQueryBlock#getFrom() == null` |
| `WITH_QUERY` | `SQLSelectStatement`，`SQLSelect#getWithSubQuery() != null` |
| `INSERT` | `SQLInsertStatement` |
| `UPDATE` | `SQLUpdateStatement` |
| `DELETE` | `SQLDeleteStatement` |
| `TRUNCATE` | `SQLTruncateStatement` |
| `MERGE_INTO` | `SQLMergeStatement` |
| `BEGIN_END` | `SQLBlockStatement` / `SQLBeginStatement` |
| `CREATE` | `SQLCreateTableStatement`，非 as / like / temp |
| `CREATE_AS` | `SQLCreateTableStatement#getSelect() != null`；或 `SQLCreateViewStatement#getSubQuery() != null` |
| `CREATE_LIKE` | `SQLCreateTableStatement#getLike() != null` |
| `CREATE_TEMP` | `SQLCreateTableStatement#isTemporary()` |
| `DROP` | `SQLDropTableStatement` / `SQLDropViewStatement` |
| `ALTER` | `SQLAlterTableStatement` 默认 |
| `ALTERTABLE_RENAME` | alter item `SQLAlterTableRename` |
| `ALTERTABLE_RENAMECOL` | alter item `SQLAlterTableRenameColumn` |
| `ALTERTABLE_ADDCOLS` | alter item `SQLAlterTableAddColumn` |
| `ALTERTABLE_REPLACECOLS` | alter item `SQLAlterTableReplaceColumn` |
| `ALTERTABLE_ADDPARTS` | alter item `SQLAlterTableAddPartition` |
| `ALTERTABLE_DROPPARTS` | alter item `SQLAlterTableDropPartition` |
| `ALTERTABLE_RENAMEPART` | alter item `SQLAlterTableRenamePartition` |
| `COMMENT_ON` | `SQLCommentStatement` |
| `DESC_TABLE` / `DATABASE_DESC` | `SQLDescribeStatement#getObjectType()`（`DATABASE` → 库描述） |
| `SHOW` / `SHOW_TABLES` / `SHOW_CREATETABLE` / `SHOW_COLUMNS` / `SHOW_FUNCTION` / `SHOW_MATERIALIZED_VIEW` / `SHOW_CREATE_MATERIALIZED_VIEW` | `SQLShowStatement` 及子类（具体子类优先，未命中归 `SHOW`） |
| `EXPLAIN` | `SQLExplainStatement` / `SQLExplainAnalyzeStatement` |
| `CREATE_VIEW` | `SQLCreateViewStatement`，`getSubQuery() == null` |
| `CREATE_MATERIALIZED_VIEW` | `SQLCreateMaterializedViewStatement` |
| `DROP_MATERIALIZED_VIEW` | `SQLDropMaterializedViewStatement` |
| `ALTER_MATERIALIZED_VIEW` | `SQLAlterMaterializedViewStatement` |
| `REFRESH_MATERIALIZED_VIEW` | `SQLRefreshMaterializedViewStatement` |
| `CREATE_FUNCTION` | `SQLCreateFunctionStatement` |
| `DROP_FUNCTION` | `SQLDropFunctionStatement` |
| `PROCEDURE_EXEC` | `SQLServerExecStatement` / `SQLCallStatement` |
| `DATABASE_CREATE` | `SQLCreateDatabaseStatement` |
| `DATABASE_DROP` | `SQLDropDatabaseStatement` |
| `DATABASE_SHOW` | `SQLShowDatabasesStatement` |
| `OTHER` | 以上均未命中 |

---

## 四、核心代码设计

### 4.1 对外 API：`ISqlParser`

```java
/** 根据 SQL 文本解析 SQL 类型 */
SqlType parseSqlType(String sql);

/** 根据 Druid 语法树解析 SQL 类型 */
SqlType parseSqlType(SQLStatement sqlStatement);

/** 默认 SQL 类型解析 Client，数据源可覆盖以支持方言语句 */
default IParseSqlTypeClient getParseSqlTypeClient() {
    return new BaseParseSqlTypeClient();
}
```

### 4.2 Client 接口：`IParseSqlTypeClient`

```java
public interface IParseSqlTypeClient {
    SqlType parseSqlType(SQLStatement stmt);
}
```

### 4.3 默认实现：`BaseParseSqlTypeClient`

`core/parser/base/sqltype/BaseParseSqlTypeClient`，按 `SQLStatement` 子类分派；子判定收敛为保护方法，便于数据源覆盖：

```java
public class BaseParseSqlTypeClient implements IParseSqlTypeClient {

    public SqlType parseSqlType(SQLStatement stmt) {
        if (stmt == null) return SqlType.OTHER;
        if (stmt instanceof SQLSelectStatement)          return selectType((SQLSelectStatement) stmt);
        if (stmt instanceof SQLInsertStatement)          return SqlType.INSERT;
        if (stmt instanceof SQLUpdateStatement)          return SqlType.UPDATE;
        if (stmt instanceof SQLDeleteStatement)          return SqlType.DELETE;
        if (stmt instanceof SQLTruncateStatement)        return SqlType.TRUNCATE;
        if (stmt instanceof SQLMergeStatement)           return SqlType.MERGE_INTO;
        if (stmt instanceof SQLBlockStatement
                || stmt instanceof SQLBeginStatement)    return SqlType.BEGIN_END;
        if (stmt instanceof SQLCreateTableStatement)     return createTableType((SQLCreateTableStatement) stmt);
        if (stmt instanceof SQLCreateViewStatement)      return createViewType((SQLCreateViewStatement) stmt);
        if (stmt instanceof SQLCreateMaterializedViewStatement)
                                                         return SqlType.CREATE_MATERIALIZED_VIEW;
        if (stmt instanceof SQLDropTableStatement
                || stmt instanceof SQLDropViewStatement) return SqlType.DROP;
        if (stmt instanceof SQLDropMaterializedViewStatement)
                                                         return SqlType.DROP_MATERIALIZED_VIEW;
        if (stmt instanceof SQLAlterTableStatement)      return alterTableType((SQLAlterTableStatement) stmt);
        if (stmt instanceof SQLAlterMaterializedViewStatement)
                                                         return SqlType.ALTER_MATERIALIZED_VIEW;
        if (stmt instanceof SQLRefreshMaterializedViewStatement)
                                                         return SqlType.REFRESH_MATERIALIZED_VIEW;
        if (stmt instanceof SQLCommentStatement)         return SqlType.COMMENT_ON;
        if (stmt instanceof SQLDescribeStatement)        return describeType((SQLDescribeStatement) stmt);
        if (stmt instanceof SQLShowDatabasesStatement)   return SqlType.DATABASE_SHOW;
        if (stmt instanceof SQLShowStatement)            return showType((SQLShowStatement) stmt);
        if (stmt instanceof SQLExplainStatement
                || stmt instanceof SQLExplainAnalyzeStatement) return SqlType.EXPLAIN;
        if (stmt instanceof SQLCreateFunctionStatement)  return SqlType.CREATE_FUNCTION;
        if (stmt instanceof SQLDropFunctionStatement)    return SqlType.DROP_FUNCTION;
        if (stmt instanceof SQLServerExecStatement
                || stmt instanceof SQLCallStatement)     return SqlType.PROCEDURE_EXEC;
        if (stmt instanceof SQLCreateDatabaseStatement)  return SqlType.DATABASE_CREATE;
        if (stmt instanceof SQLDropDatabaseStatement)    return SqlType.DATABASE_DROP;
        return SqlType.OTHER;
    }

    protected SqlType selectType(SQLSelectStatement stmt) { /* with / no-from / query */ }

    protected SqlType createTableType(SQLCreateTableStatement stmt) { /* as / like / temp / create */ }

    protected SqlType createViewType(SQLCreateViewStatement stmt) { /* as -> CREATE_AS else CREATE_VIEW */ }

    protected SqlType alterTableType(SQLAlterTableStatement stmt) { /* 遍历 items 细分，否则 ALTER */ }

    protected SqlType describeType(SQLDescribeStatement stmt) { /* DATABASE -> DATABASE_DESC */ }

    protected SqlType showType(SQLShowStatement stmt) { /* 具体子类优先，否则 SHOW */ }
}
```

设计要点：

- **不用通用 visitor**：类型判定本质是语句层级的 `instanceof` 分派；
- **顺序敏感**：`SQLShowDatabasesStatement` 必须先于 `SQLShowStatement`；`SQLCreateViewStatement` 的 `CREATE_AS` 判定在 `CREATE_VIEW` 之前（同一方法内处理）；
- **空安全**：`stmt == null` 直接 `OTHER`。

### 4.4 编排接入：`BaseSqlParser`

```java
@Override
public SqlType parseSqlType(String sql) {
    return parseSqlType(this.createStatement(sql));
}

@Override
public SqlType parseSqlType(SQLStatement sqlStatement) {
    return getParseSqlTypeClient().parseSqlType(sqlStatement);
}
```

`String` 入口先经 syntax 层 `createStatement` 得到 Druid 语法树，再委托 Client。

### 4.5 方言扩展

与表解析 / 参数解析同构，数据源可在自身目录下提供子类并覆盖 `getParseSqlTypeClient()`：

```text
core/parser/base/sqltype/
└── BaseParseSqlTypeClient        (通用 Druid 分派)
core/parser/<datasource>/sqltype/ (按需)
└── XxxParseSqlTypeClient         (覆盖方言专属语句)
```

一期不在任何数据源做专属实现。

### 4.6 兜底策略

- **不做正则兜底**：语法解析失败由 `createStatement` 抛出异常，调用方处理；
- **未命中分支**：统一返回 `SqlType.OTHER`，不抛异常。

---

## 五、实现范围与边界

### 5.1 已覆盖

查询、DML（INSERT/UPDATE/DELETE/TRUNCATE/MERGE_INTO）、`BEGIN_END`、建表/建视图及其 as/like/temp 变体、DROP、ALTER 及常见子项、COMMENT ON、DESCRIBE、SHOW 系列、EXPLAIN、物化视图增删改刷、函数增删、EXEC/CALL、库的增删列示。

### 5.2 一期未覆盖（归 `OTHER`）

方言专属语句：`INSERT OVERWRITE`、`LOAD`、`CREATE MACRO`、`CACHE TABLE`、`CACHE LAZY TABLE`、`INVALIDATE TABLE`、`ZORDER`、`SCHEMA` 操作等。

### 5.3 特殊说明

- **`desc` / `describe` 的差异**：MySQL 中 `desc t1` 被 Druid 解析为 `MySqlExplainStatement`，故归 `EXPLAIN`；PG / Spark / Hive / DM 等方言下为独立 `SQLDescribeStatement`，归 `DESC_TABLE`；
- **`create view ... as select`**：归 `CREATE_AS`（与建表 as select 同一语义分支）；`CREATE_VIEW` 仅在视图无 as select 时返回；
- **ALTER 细分**：`properties / serdeproperties / location` 等无统一 Druid item，归 `ALTER`；
- **多语句**：`parseSqlType(String)` 只取首条 SQL。

### 5.4 后续可扩展

- 方言专属 SqlType（insert overwrite、cache、zorder 等）由对应数据源 Client 覆盖；
- 需要时再评估批量解析与正则兜底（当前不做）。
