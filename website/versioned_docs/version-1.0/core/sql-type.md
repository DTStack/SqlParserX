---
title: SQL 类型识别
description: 基于解析后的语法树识别 SQL 业务类型
sidebar_label: SQL 类型识别
sidebar_position: 1
---

# SQL 类型识别

`parseSqlType` 先按所选数据源方言构建 Druid AST，再返回统一的 `SqlType`，适合语句路由、审计分类和执行策略选择。

```java title="SqlTypeDemo.java"
import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;
import com.dtstack.sqlparserx.core.enums.SqlType;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;

ISqlParser parser = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);

SqlType queryType = parser.parseSqlType("SELECT id FROM users");
SqlType insertType = parser.parseSqlType(
        "INSERT INTO target_table SELECT id FROM source_table");

System.out.println(queryType);   // QUERY
System.out.println(insertType);  // INSERT
```

## 支持范围

| 分类 | 典型类型 |
|---|---|
| 查询 | `QUERY`、`WITH_QUERY`、`QUERY_NO_FROM` |
| DML | `INSERT`、`UPDATE`、`DELETE`、`MERGE_INTO` |
| DDL | `CREATE`、`CREATE_AS`、`ALTER`、`DROP`、`TRUNCATE` |
| 管理与查看 | `SHOW_TABLES`、`SHOW_COLUMNS`、`DESC_TABLE`、`EXPLAIN` |
| 其他 | 无法归类的 AST 返回 `OTHER` |

具体 SQL 能否构建 AST，仍取决于所选数据源的方言支持情况。
