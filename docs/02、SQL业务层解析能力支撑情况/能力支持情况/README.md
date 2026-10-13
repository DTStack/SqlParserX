# SQL 解析能力支持情况 · 导航

本目录维护 SqlParserX `sqlparserx-core` 的 SQL 解析能力与数据源支撑情况。按「数据源（总表 + 专项）+ 通用能力」组织。

## 目录结构

```text
能力支持情况/
├── README.md                             # 本导航
├── datasource/                           # 数据源能力支持情况
│   ├── SQL解析能力&数据源支撑适配情况.md  # 数据源总表与索引
│   ├── doris/                            # 语法解析 + 业务解析 两份
│   ├── mysql/
│   └── starrocks/
└── 通用解析能力/                          # 按 BaseSqlParser 场景维护：一种场景一篇
    ├── 函数解析支持情况.md
    ├── 字段血缘解析支持情况.md
    ├── 表血缘解析支持情况.md
    ├── 表解析支持情况.md
    └── 变量参数解析与预编译支持情况.md
```

## 一、数据源总表与索引

| 文档 | 内容 |
|---|---|
| [datasource/SQL解析能力&数据源支撑适配情况.md](datasource/SQL解析能力&数据源支撑适配情况.md) | 业务能力双层支撑矩阵、数据源双层适配矩阵、支持等级判定、验证边界；并作为数据源目录索引 |

## 二、数据源专项

每个数据源一个子目录，两份文档：**SQL语法解析支持情况**（`sqlparserx-syntax`）+ **SQL业务解析支持情况**（`sqlparserx-core`）。

| 数据源 | 语法解析支持 | 业务解析支持 | 支持等级 |
|---|---|---|---|
| Doris | [SQL语法解析支持情况](datasource/doris/SQL语法解析支持情况.md) | [SQL业务解析支持情况](datasource/doris/SQL业务解析支持情况.md) | 重度支持 |
| MySQL | [SQL语法解析支持情况](datasource/mysql/SQL语法解析支持情况.md) | [SQL业务解析支持情况](datasource/mysql/SQL业务解析支持情况.md) | 重点支持 |
| StarRocks | [SQL语法解析支持情况](datasource/starrocks/SQL语法解析支持情况.md) | [SQL业务解析支持情况](datasource/starrocks/SQL业务解析支持情况.md) | 重点支持 |

索引与维护约定见[数据源总表](datasource/SQL解析能力&数据源支撑适配情况.md)。其余已注册方言后续按同一结构补充。

## 三、通用解析能力

按 `BaseSqlParser` 场景维护，记录不区分数据源的通用能力**已支持粒度与深度**。

| 场景 | Core 入口 | 默认实现 |
|---|---|---|
| [函数解析支持情况](通用解析能力/函数解析支持情况.md) | `ISqlParser.parseFunction` | `BaseSqlParser` + Visitor |
| [字段血缘解析支持情况](通用解析能力/字段血缘解析支持情况.md) | `ISqlParser.parseColumnLineages` | `ColumnLineageClient` |
| [表血缘解析支持情况](通用解析能力/表血缘解析支持情况.md) | `ISqlParser.parseTableLineages` | `TableLineageClient` |
| [表解析支持情况](通用解析能力/表解析支持情况.md) | `ISqlParser.parseTables` / `parseCreateTable` | `ParseTablesClient` / `ParseCreateTableClient` |
| [变量参数解析与预编译支持情况](通用解析能力/变量参数解析与预编译支持情况.md) | `ISqlParser.parseParameterSql` / `buildSqlByPrepareCompile` | `IParseParameterClient` / `BaseParseParameterClient` |

## 维护约定

1. **双层同时成立**才算业务能力支持：语法层生成正确 AST，业务层输出正确业务结果。
2. **通用能力**写入 `通用解析能力/`，**单数据源差异**写入 `datasource/<数据源>/`，**总览与等级**写入总表。
3. 支持项标 ✅，不支持/待明确标 ❌。
4. 新增数据源：先建子目录两份文档，再在总表和本导航登记。
5. 支持等级提升前必须补充该数据源专项回归用例。