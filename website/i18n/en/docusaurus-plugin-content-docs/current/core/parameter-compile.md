---
title: Parameter Extraction and Prepared SQL
description: Extract dynamic SELECT parameters and build JDBC prepared SQL
sidebar_label: Parameter Extraction
sidebar_position: 6
---

# Parameter Extraction and Prepared SQL

SqlParserX extracts `${variable}` expressions and output fields from SELECT statements. Given parameter values, it produces JDBC `?` placeholders and an ordered binding list.

```java
ISqlParser parser = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);
String sql = "SELECT id, name FROM users "
        + "WHERE dt = '${bizdate}' AND region = '${region}'";

ParseParameterResult parsed = parser.parseParameterSql(
        sql, false, Collections.emptyMap());

Map<String, Object> values = new HashMap<>();
values.put("bizdate", "20261014");
values.put("region", "hangzhou");

PrepareCompileResult compiled = parser.buildSqlByPrepareCompile(
        sql, values, Collections.emptyMap());
System.out.println(compiled.getBeautifulSql());
System.out.println(compiled.getInputFields());
```

```text
SELECT id, name FROM users WHERE dt = ? AND region = ?
[20261014, hangzhou]
```

The common implementation currently targets SELECT statements. Variables used as identifiers in sorting or grouping clauses are inlined.
