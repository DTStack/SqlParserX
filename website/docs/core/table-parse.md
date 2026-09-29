---
title: 表解析
description: 从 SQL 文本中解析真实表、CTE、CREATE TABLE、分区与属性
sidebar_label: 表解析
sidebar_position: 3
---

# 表解析

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

同样沿用官方 `sqlparserx-core/README.md` 的示例 SQL，演示「解析真实表」与「解析建表语句」：

```java title="TableParseDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.DbType;

public class TableParseDemo {

    public static void main(String[] args) {
        String sql = "CREATE TABLE user_order_summary AS\n"
                + "SELECT\n"
                + "    u.id as user_id,\n"
                + "    u.name,\n"
                + "    SUM(o.amount) as total_amount,\n"
                + "    COUNT(o.id) as order_count\n"
                + "FROM users u JOIN orders o ON u.id = o.user_id\n"
                + "GROUP BY u.id, u.name";

        // 1、获取解析器
        ISqlParser parser = SqlParserFactory.getSqlParser(DbType.mysql);

        // 2、解析 SQL 中的真实表
        ParseTableResult tableResult = parser.parseTables(sql);
        System.out.println("主表: " + tableResult.getMainTable().getName());
        System.out.println("所有表: " + tableResult.getAllTableName());

        // 3、解析 CREATE TABLE 语句结构
        ParseCreateTableResult createResult = parser.parseCreateTable(sql);
        System.out.println("建表名: " + createResult.getDb() + "." + createResult.getTableName());
        System.out.println("字段数: " + createResult.getTableElementList().size());
    }
}
```

控制台输出（示例）：

```text
主表: users
所有表: [user_order_summary, users, orders]
建表名: null.user_order_summary
字段数: 4
```

## 3. 实现目标

从 SQL 文本中识别**真实表**，包括普通查询用表、CTE、`CREATE TABLE` 定义、分区与表属性等。

```text
SQLStatement AST
  → ParseTableClient.parseTables / parseCreateTable
  → 收集表 / CTE / 建表结构 / 分区 / 属性
  → ParseTableResult / ParseCreateTableResult
```

## 4. 入口与实现位置

| 项目 | 内容 |
|---|---|
| Core 入口 | `ISqlParser.parseTables(sql)`、`ISqlParser.parseCreateTable(sql)` |
| 默认实现 | `BaseSqlParser.parseTables` → `ParseTableClient` |
| 返回类型 | `ParseTableResult`、`ParseCreateTableResult` |
| 覆盖对象 | 查询用表、CTE、`CREATE TABLE`、分区、表属性 |

## 5. 支持粒度

| 能力项 | 支持情况 | 说明 |
|---|---|---|
| 查询用表识别 | **已支持 ✅** | SELECT / INSERT / UPDATE / DELETE |
| CTE 识别 | **已支持 ✅** | WITH 定义的临时表 |
| `CREATE TABLE` DDL | **已支持 ✅** | 表名、字段、类型、注释 |
| 分区与表属性 | **已支持 ✅** | 各数据源方言差异内聚在语法层 |
| `catalog.db.table` 多级命名 | **已支持 ✅** | 多 catalog 场景 |

> 更多表解析场景与数据源差异，以项目 `docs/02、SQL业务层解析能力支撑情况/能力支持情况/通用解析能力/表解析支持情况.md` 为准。