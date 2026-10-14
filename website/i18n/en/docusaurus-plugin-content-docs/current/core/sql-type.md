---
title: SQL Type Detection
description: Detect the business SQL type from the parsed syntax tree
sidebar_label: SQL Type Detection
sidebar_position: 1
---

# SQL Type Detection

`parseSqlType` parses SQL with the selected datasource dialect and maps the resulting Druid AST to a unified `SqlType`. It can drive statement routing, audit classification, and execution policies.

```java
ISqlParser parser = SqlParserFactory.getSqlParser(SqlParserDataSourceType.MYSQL);

SqlType queryType = parser.parseSqlType("SELECT id FROM users");
SqlType insertType = parser.parseSqlType(
        "INSERT INTO target_table SELECT id FROM source_table");

System.out.println(queryType);   // QUERY
System.out.println(insertType);  // INSERT
```

The common implementation recognizes query, DML, DDL, SHOW, DESCRIBE, and EXPLAIN statements. An unclassified AST returns `OTHER`; syntax support still depends on the selected dialect.
