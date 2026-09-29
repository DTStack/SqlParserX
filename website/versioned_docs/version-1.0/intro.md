---
title: SqlParserX 文档
description: SqlParserX 多方言 SQL 解析引擎与工具包的文档入口
slug: /intro
sidebar_label: 文档导读
sidebar_position: 1
---

# 欢迎使用 SqlParserX

SqlParserX 是一个**多方言 SQL 解析引擎与工具包**，面向表/DDL 解析、表与字段血缘、函数提取、SQL 拆分以及行级权限改写等场景。

项目采用两层架构：`sqlparserx-syntax` 负责把 SQL 解析为 Druid AST 并承载语法扩展；`sqlparserx-core` 通过统一的 `ISqlParser` 契约消费 AST，产出业务解析结果。`sqlparserx-web` 与 `sqlparserx-ui` 提供服务化与可视化访问能力。

## 从这里开始

- 🚀 [快速开始](./quickstart/setup.md)
- 📖 [SqlParserX 介绍](./about/introduction.md)
- 🏗️ [架构设计](./about/architecture.md)

## 文档结构

- **关于 SqlParserX**：项目介绍与两层架构设计
- **快速开始**：环境准备、构建与运行
- **核心解析能力使用**：字段血缘、表血缘、表解析、函数解析
- **贡献指南**：参与贡献的流程与规范

:::tip 提示
本站右侧导航栏可切换**版本**（`1.0` / `Next`）与**语言**（简体中文 / English）。
:::