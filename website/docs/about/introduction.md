---
title: SqlParserX 介绍
description: SqlParserX 的定位、能力与模块组成
sidebar_label: SqlParserX介绍
sidebar_position: 1
---

# SqlParserX 介绍

SqlParserX 是一个**多方言 SQL 解析引擎与工具包**，提供 SQL 类型识别、表/DDL 解析、表与字段血缘、函数提取以及参数提取与预编译能力。

## 解决的典型问题

| 问题 | 方案 |
|---|---|
| 不同 SQL 方言语法差异大 | 通过 `DbType` 路由 Druid 方言，统一 AST |
| 真实表与 DDL 藏在 SQL 文本中 | 表、CTE、`CREATE TABLE`、分区与属性解析 |
| 数据依赖难以追踪 | 覆盖 JOIN、UNION、子查询、聚合与 CTE 的表/字段血缘 |
| 脚本中包含多条语句 | SQL 拆分与语法校验 |
| 权限谓词难以维护 | 行级权限 SQL 改写 |

## 模块组成

| 模块 | 层次 | 说明 | 技术栈 |
|---|---|---|---|
| `sqlparserx-syntax` | 语法解析层 | SQL 转 AST、校验、拆分与 Druid 方言扩展 | Java 8 + Druid 1.2.28 |
| `sqlparserx-core` | 业务解析层 | 表、血缘、DDL、函数与 SQL 改写 | Java 8 + `sqlparserx-syntax` |
| `sqlparserx-web` | 访问层 | Spring Boot REST 服务 | Spring Boot 2.3.12 + Java 8 |
| `sqlparserx-ui` | 访问层 | 血缘可视化与在线解析工具 | Vue 2.7 + Element UI 2.x + Vite 5 + ECharts 5 |

## 数据源支持

| 数据源 | 支持说明 |
|---|---|
| MySQL | 重点支持，增强 `RENAME TABLE` 处理 |
| Doris | 重度支持，DDL 与血缘有专项测试 |
| StarRocks | 支持独立的 `DISTRIBUTED BY` 与关键 DDL |
| Hive / ClickHouse / Oracle / DM | 已注册解析器，通用业务实现 |

完整的语法层与业务层支撑矩阵见[核心解析能力使用](../core/field-lineage.md)与项目 `docs/` 目录下的能力支持文档。

## 延伸阅读

- [架构设计](./architecture.md)
- 项目背景：[SqlParserX 的诞生起源背景](/blog/origin-of-sqlparserx)
