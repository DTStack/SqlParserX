---
title: 函数解析
description: 提取 SQL 中使用的函数及其参数与上下文
sidebar_label: 函数解析
sidebar_position: 4
---

# 函数解析

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

沿用官方 `sqlparserx-core/README.md` 的示例 SQL，提取 SQL 中出现的函数集合：

```java title="FunctionParseDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;

import com.alibaba.druid.DbType;

import java.util.Set;

public class FunctionParseDemo {

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

        // 2、解析 SQL 中出现的函数
        Set<String> functions = parser.parseFunction(sql);

        // 3、读取结果
        System.out.println("函数集合: " + functions);
    }
}
```

控制台输出（示例）：

```text
函数集合: [sum, count]
```

## 3. 实现目标

从 SQL 中提取**函数调用**信息，返回去重后的函数名集合，便于做函数级治理与依赖分析。

```text
SQLStatement AST
  → BaseSqlParser.parseFunction
  → 遍历 AST 收集函数表达式
  → Set<String> 函数集合
```

## 4. 入口与实现位置

| 项目 | 内容 |
|---|---|
| Core 入口 | `ISqlParser.parseFunction(sql)` |
| 默认实现 | `BaseSqlParser.parseFunction` |
| 返回类型 | `Set<String>`（函数名集合，统一小写） |
| 覆盖范围 | 内置函数、聚合函数、窗口函数、自定义函数 |

## 5. 支持粒度

| 能力项 | 支持情况 | 说明 |
|---|---|---|
| 普通函数调用 | **已支持 ✅** | 如 `substr(name, 1, 3)` |
| 聚合函数 | **已支持 ✅** | 如 `sum`、`count`、`max` |
| 窗口函数 | **已支持 ✅** | 如 `row_number() over(...)` |
| 嵌套函数 | **已支持 ✅** | 递归收集内外层函数 |

> 更多函数场景与数据源差异，以项目 `docs/02、SQL业务层解析能力支撑情况/能力支持情况/通用解析能力/函数解析支持情况.md` 为准。