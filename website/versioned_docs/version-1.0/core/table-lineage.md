---
title: 表血缘解析
description: 解析 SQL 中来源表到目标表的血缘关系
sidebar_label: 表血缘解析
sidebar_position: 2
---

# 表血缘解析

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

沿用官方 `sqlparserx-core/README.md` 的示例 SQL，从获取解析器到打印表血缘：

```java title="TableLineageDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

import com.alibaba.druid.DbType;

import java.util.List;

public class TableLineageDemo {

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

        // 2、解析表血缘
        TableLineageResult result = parser.parseTableLineages(sql);

        // 3、读取结果
        System.out.println("目标表: " + result.getTargetTable());
        System.out.println("来源表: " + result.getSourceTables());

        List<TableLineage> tableLineages = result.getTableLineages();
        for (TableLineage lineage : tableLineages) {
            System.out.println(lineage.getFromTable() + " -> " + lineage.getToTable());
        }
    }
}
```

控制台输出（示例）：

```text
目标表: user_order_summary
来源表: [users, orders]
users -> user_order_summary
orders -> user_order_summary
```

## 3. 实现目标

从 SQL 中解析**目标表 → 来源表**的表级血缘关系，覆盖 JOIN、UNION、子查询、聚合与 CTE 等结构。

```text
SQLStatement AST
  → TableLineageClient.parseTableLineages
  → 收集来源表 + 目标表
  → TableLineageResult
```

## 4. 入口与实现位置

| 项目 | 内容 |
|---|---|
| Core 入口 | `ISqlParser.parseTableLineages(sql)` |
| 默认实现 | `BaseSqlParser.parseTableLineages` → `TableLineageClient` |
| 返回类型 | `TableLineageResult`（`getTargetTable()` + `getSourceTables()` + `getTableLineages()`） |
| 覆盖结构 | JOIN、UNION、子查询、聚合、CTE |

## 5. 支持粒度

| 能力项 | 支持情况 | 说明 |
|---|---|---|
| 单表 / 多表 JOIN | **已支持 ✅** | 解析所有参与表 |
| UNION / UNION ALL | **已支持 ✅** | 归并各分支来源表 |
| 子查询 / 派生表 | **已支持 ✅** | 递归收集真实表 |
| CTE（WITH） | **已支持 ✅** | 识别 CTE 与其真实来源 |
| `INSERT INTO ... SELECT` | **已支持 ✅** | 输出目标表与来源表 |

> 更多表血缘场景与数据源差异，以项目 `docs/02、SQL业务层解析能力支撑情况/能力支持情况/通用解析能力/表血缘解析支持情况.md` 为准。