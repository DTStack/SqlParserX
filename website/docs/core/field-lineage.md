---
title: 字段血缘解析
description: 从 SQL 推导来源表.来源字段 → 目标表.目标字段 的字段级血缘关系
sidebar_label: 字段血缘解析
sidebar_position: 1
---

# 字段血缘解析

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>sqlparserx-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

> `sqlparserx-core` 已传递依赖 `sqlparserx-syntax` 与 Druid，无需额外引入。

## 2. 完整示例

从获取解析器、构造元数据到打印字段血缘的**完整可运行**类：

```java title="FieldLineageDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import com.alibaba.druid.DbType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldLineageDemo {

    public static void main(String[] args) {
        String sql = "CREATE TABLE user_order_summary AS\n"
                + "SELECT\n"
                + "    u.id as user_id,\n"
                + "    u.name,\n"
                + "    SUM(o.amount) as total_amount,\n"
                + "    COUNT(o.id) as order_count\n"
                + "FROM users u JOIN orders o ON u.id = o.user_id\n"
                + "GROUP BY u.id, u.name";

        // 1、准备元数据：表 -> 字段列表（用于 select * / 字段展开）
        Map<String, List<Column>> tableColumnsMap = new HashMap<>();
        tableColumnsMap.put("users", Arrays.asList(
                new Column("id", 0), new Column("name", 1)));
        tableColumnsMap.put("orders", Arrays.asList(
                new Column("amount", 0), new Column("id", 1)));

        // 2、获取解析器
        ISqlParser parser = SqlParserFactory.getSqlParser(DbType.mysql);

        // 3、解析字段血缘
        ColumnLineageResult result = parser.parseColumnLineages(sql, tableColumnsMap);

        // 4、打印结果：目标表 + 目标字段 -> 来源字段
        System.out.println("目标表: " + result.getTargetTable());
        result.printColumnLineage();
    }
}
```

控制台输出（示例）：

```text
目标表: user_order_summary
users.id -> user_order_summary.user_id
users.name -> user_order_summary.name
orders.amount -> user_order_summary.total_amount
orders.id -> user_order_summary.order_count
```

## 3. 实现目标

从 SQL 中推导 **来源表.来源字段 → 目标表.目标字段** 的字段级血缘关系。

核心思路：把「来源 select 字段列表」与「目标字段列表」各自归一为 `(真实字段, 别名字段, 真实表名)` 的三元组集合，再按固定匹配规则生成血缘。字段血缘的输入是插入/建表类 SQL，纯查询 SQL 不产生血缘。

```text
SQLInsertStatement / SQLCreateTableStatement(AS SELECT)
  → ColumnLineageClient.parseColumnLineages
  → 目标字段列表 + 来源 select 字段列表
  → addColumnRelations 逐条匹配
  → ColumnLineageResult
```

## 4. 入口与实现位置

| 项目 | 内容 |
|---|---|
| Core 入口 | `ISqlParser.parseColumnLineages(sql, tableColumnsMap)` |
| 默认实现 | `BaseSqlParser.parseColumnLineages` → `ColumnLineageClient` |
| 返回类型 | `ColumnLineageResult`（`getColumnRelations()` + `getColumnLineages()`） |
| 适用语句 | `SQLInsertStatement`、`SQLCreateTableStatement`（AS SELECT） |
| 元数据输入 | `Map<String, List<Column>> tableColumnsMap`（表 → 字段列表） |

## 5. 支持粒度与深度

### 字段场景

| 能力项 | 支持情况 | 说明 |
|---|---|---|
| `catalog.db.table.column` 字段识别 | **已支持 ✅** | 支持 Hive/Trino/StarRocks 等多 catalog 场景 |
| `db.table.column` 字段识别 | **已支持 ✅** | MySQL/PostgreSQL 等标准用法 |
| 字段表达式计算（`SQLBinaryOpExpr`） | **已支持 ✅** | 如 `t5.level + 1`，左右递归收集字段 |
| `*` / `xx.*` 展开 | **已支持 ✅** | 依据元数据 `tableColumnsMap` 读取字段；无元数据保留 `*` |

> 更多字段场景、表达式与子查询的覆盖情况，以项目 `docs/02、SQL业务层解析能力支撑情况/能力支持情况/通用解析能力/字段血缘解析支持情况.md` 为准。