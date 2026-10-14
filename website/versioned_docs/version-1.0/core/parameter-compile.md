---
title: 参数提取与预编译
description: 提取 SELECT 动态参数并生成 JDBC 预编译 SQL
sidebar_label: 参数提取与预编译
sidebar_position: 6
---

# 参数提取与预编译

SqlParserX 可识别 SELECT 中的 `${变量}`，返回输入参数和输出字段；传入参数值后，可生成 JDBC `?` 占位符 SQL 与按出现顺序排列的绑定值。

```java title="ParameterCompileDemo.java"
import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

ISqlParser parser = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);
String sql = "SELECT id, name FROM users "
        + "WHERE dt = '${bizdate}' AND region = '${region}'";

// 第一步：提取输入参数与输出字段
ParseParameterResult parsed = parser.parseParameterSql(
        sql, false, Collections.emptyMap());
parsed.getInputParams().forEach(param ->
        System.out.println(param.getName() + " / " + param.getSqlPart()));

// 第二步：传入参数值并生成预编译 SQL
Map<String, Object> values = new HashMap<>();
values.put("bizdate", "20261014");
values.put("region", "hangzhou");

PrepareCompileResult compiled = parser.buildSqlByPrepareCompile(
        sql, values, Collections.emptyMap());
System.out.println(compiled.getBeautifulSql());
System.out.println(compiled.getInputFields());
```

输出示例：

```text
SELECT id, name FROM users WHERE dt = ? AND region = ?
[20261014, hangzhou]
```

## 返回内容

| 阶段 | 返回内容 |
|---|---|
| 参数提取 | 输入参数名称、类型、是否必填、默认值、SQL 片段以及输出字段 |
| 预编译 | 格式化后的占位符 SQL 与有序绑定值列表 |

当前通用实现面向 SELECT；排序、分组等标识符位置的变量会以内联方式处理。
