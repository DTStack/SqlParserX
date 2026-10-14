---
title: SqlParserX Docs
description: Entry point for the SqlParserX multi-dialect SQL parsing engine
sidebar_label: Docs Guide
sidebar_position: 1
---

# Welcome to SqlParserX

SqlParserX is a **multi-dialect SQL parsing engine and toolkit** for SQL type detection, table/DDL parsing, table and column lineage, function extraction, parameter extraction, and prepared SQL generation.

It is built around two layers: `sqlparserx-syntax` parses SQL into Druid ASTs and owns syntax extensions; `sqlparserx-core` consumes those ASTs through the unified `ISqlParser` contract and produces business parsing results. `sqlparserx-web` and `sqlparserx-ui` provide service and visualization access.

## Get Started

- 🚀 [Quick Start](./quickstart/setup.md)
- 📖 [Introduction](./about/introduction.md)
- 🏗️ [Architecture](./about/architecture.md)

## Documentation Map

- **About SqlParserX**: project introduction and two-layer architecture
- **Quick Start**: environment setup, build and run
- **Core Parsing Capabilities**: SQL type detection, field lineage, table lineage, table parsing, function extraction, and parameter extraction
- **Contributing**: how to contribute

:::tip Tip
Use the navbar to switch **version** (`1.0` / `Next`) and **language** (简体中文 / English).
:::
