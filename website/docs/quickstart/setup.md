---
title: 快速开始
description: 引入依赖、构建并在本地运行 SqlParserX 的完整示例
sidebar_label: 环境准备与构建
sidebar_position: 1
---

# 快速开始

## 1. 环境要求

| 依赖 | 版本 |
|---|---|
| JDK | Java 8+ |
| Maven | 3.6+ |
| Node.js（仅 `sqlparserx-ui`） | >= 20 |

## 2. 引入依赖

在业务工程的 `pom.xml` 中引入**业务解析层** `sqlparserx-core` 即可：

```xml
<dependency>
    <groupId>com.dtstack</groupId>
    <artifactId>sqlparserx-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

:::tip 依赖说明
`sqlparserx-core` 会**传递依赖** `sqlparserx-syntax`（语法层）与官方 Druid，因此业务解析只需引入 `core` 一个坐标；若只做 SQL→AST 的语法解析，也可以单独引入 `sqlparserx-syntax`。
:::

:::note 从源码使用
`1.0-SNAPSHOT` 若尚未发布到远程仓库，请先拉取源码并安装到本地仓库：

```bash
git clone https://github.com/DTStack/SqlParserX.git
cd SqlParserX
mvn clean install -DskipTests
```

之后上面的坐标即可在本地直接使用。
:::

## 3. Hello World：解析一段 SQL

下面是一个**完整可运行**的类，覆盖解析表、字段血缘、表血缘与函数四类能力，
与官方 `sqlparserx-core/README.md` 中的示例保持一致：

```java title="SqlParserXDemo.java"
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.DbType;

import java.util.*;

public class SqlParserXDemo {

    public static void main(String[] args) {
        String sql = "CREATE TABLE user_order_summary AS\n"
                + "SELECT\n"
                + "    u.id as user_id,\n"
                + "    u.name,\n"
                + "    SUM(o.amount) as total_amount,\n"
                + "    COUNT(o.id) as order_count\n"
                + "FROM users u JOIN orders o ON u.id = o.user_id\n"
                + "GROUP BY u.id, u.name";

        // 1、按数据源类型获取解析器（支持 mysql/hive/doris/... 15+ 方言）
        ISqlParser parser = SqlParserFactory.getSqlParser(DbType.mysql);

        // 2、解析真实表
        ParseTableResult tableResult = parser.parseTables(sql);
        System.out.println("主表: " + tableResult.getMainTable().getName());
        System.out.println("所有表: " + tableResult.getAllTableName());

        // 3、解析字段血缘（select * 场景可传入真实字段元数据）
        Map<String, List<Column>> tableColumnsMap = new HashMap<>();
        tableColumnsMap.put("users", Arrays.asList(new Column("id", 0), new Column("name", 1)));
        tableColumnsMap.put("orders", Arrays.asList(new Column("amount", 0), new Column("id", 1)));
        ColumnLineageResult columnLineageResult =
                parser.parseColumnLineages(sql, tableColumnsMap);
        columnLineageResult.printColumnLineage();

        // 4、解析表血缘
        TableLineageResult tableLineageResult = parser.parseTableLineages(sql);
        List<TableLineage> tableLineages = tableLineageResult.getTableLineages();
        System.out.println("表血缘: " + tableLineages);

        // 5、解析函数
        Set<String> functions = parser.parseFunction(sql);
        System.out.println("函数集合: " + functions);
    }
}
```

控制台输出（示例）：

```text
主表: users
所有表: [user_order_summary, users, orders]
users.id -> user_order_summary.user_id
users.name -> user_order_summary.name
orders.amount -> user_order_summary.total_amount
orders.id -> user_order_summary.order_count
表血缘: [TableLineage{fromCatalog='null', fromDb='null', fromTable='users', toCatalog='null', toDb='null', toTable='user_order_summary'}, ...]
函数集合: [sum, count]
```

## 4. 下一步

各核心能力的**依赖引入 + 完整示例**见：

- [字段血缘解析](../core/field-lineage.md)
- [表血缘解析](../core/table-lineage.md)
- [表解析](../core/table-parse.md)
- [函数解析](../core/function-parse.md)