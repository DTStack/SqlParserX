---
title: Architecture
description: The two-layer unified extension architecture of SqlParserX
sidebar_label: Architecture
sidebar_position: 2
---

# Two-Layer Unified Extension Architecture

The SqlParserX parsing kernel consists of two connected, structurally symmetric extension systems, each in its own Jar module:

- **SQL syntax layer (`sqlparserx-syntax`)**: built on official Druid, extending Lexer, StatementParser and AST nodes per datasource through unified dialect routing, parser base and AST model.
- **SqlParserX business layer (`sqlparserx-core`)**: extending table parsing, lineage and SQL rewriting per datasource through unified dialect routing, the `ISqlParser` contract and business clients.

Both layers follow the pattern **unified entry → common base → unified extension point → datasource implementation → unified result**. When adding a datasource, syntax differences are extended in the Druid layer and business differences in the SqlParserX layer, aligned via `DbType` and Druid AST.

![SqlParserX two-layer unified extension architecture](/img/architecture/SqlParserX两层架构-en.png)

## Overall Architecture

```mermaid
flowchart TB
    subgraph Business[sqlparserx-core｜Business parsing layer]
        direction LR
        B_ROUTE[Dialect routing<br/>SqlParserFactory]
        B_BASE[Common base<br/>ISqlParser · BaseSqlParser · Base Client]
        B_EXT[Extension points<br/>Dialect Client · Strategy]
        B_DIALECT[Datasource implementations<br/>MySQL · Doris · StarRocks · ...]
        B_RESULT[Unified results<br/>Table · DDL · Lineage · SQL]
        B_ROUTE --> B_BASE --> B_EXT --> B_DIALECT --> B_RESULT
    end

    subgraph Syntax[sqlparserx-syntax｜Syntax parsing layer, on Druid]
        direction LR
        S_ROUTE[Dialect routing<br/>SqlSyntaxParserFactory · DbType]
        S_BASE[Common base<br/>Lexer · StatementParser · AST]
        S_EXT[Extension points<br/>Dialect Lexer · Parser · AST Node]
        S_DIALECT[Datasource implementations<br/>MySQL · Doris · Oracle · ...]
        S_RESULT[Unified results<br/>SQLStatement AST]
        S_ROUTE --> S_BASE --> S_EXT --> S_DIALECT --> S_RESULT
    end

    B_ROUTE -. DbType alignment .-> S_ROUTE
    B_DIALECT -. same datasource dialect .-> S_DIALECT
    B_BASE -->|SQL + DbType| S_BASE
    S_RESULT -->|SQLStatement AST| B_BASE
```

## Module Dependencies

```text
Access: sqlparserx-web (REST) / sqlparserx-ui
                 │ unified ISqlParser contract
Business layer:  sqlparserx-core
                 │ SQL + DbType / SQLStatement AST
Syntax layer:    sqlparserx-syntax (Druid 1.2.28)
```

Business extensions follow `SqlParserFactory` → `ISqlParser` → `BaseSqlParser` → dialect implementations.