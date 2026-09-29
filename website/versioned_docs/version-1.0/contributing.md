---
title: 贡献指南
description: 参与 SqlParserX 开发的流程与规范
sidebar_label: 贡献指南
sidebar_position: 1
---

# 贡献指南

感谢你对 SqlParserX 的关注！欢迎通过 Issue、Pull Request 参与共建。

## 参与方式

- 提交 Bug 或需求：[GitHub Issues](https://github.com/DTStack/SqlParserX/issues)
- 提交代码：[GitHub Pull Requests](https://github.com/DTStack/SqlParserX/pulls)
- 讨论交流：见[社区](/community)

## 开发流程

1. Fork 仓库并创建特性分支：`git checkout -b feature/xxx`
2. 本地构建：`mvn clean install -DskipTests`
3. 编写代码与测试，确保 `mvn test` 通过
4. 提交并推送分支，发起 Pull Request
5. 通过 CI 与 Review 后合并

## 代码约定

- 遵循现有模块分层：语法差异改 `sqlparserx-syntax`，业务差异改 `sqlparserx-core`
- 新增数据源时，同步补充能力支持文档与回归测试
- 保持与现有代码风格一致，不引入无关依赖

## 文档约定

- 文档统一维护在项目根目录 `docs/`，本站文档从中同步
- 新增解析能力时，更新对应「能力支持情况」文档
- 参考[架构设计](./about/architecture.md)理解两层扩展模式