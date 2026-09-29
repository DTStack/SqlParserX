---
title: Contributing
description: How to contribute to SqlParserX
sidebar_label: Contributing
sidebar_position: 1
---

# Contributing

Thanks for your interest in SqlParserX! Issues and Pull Requests are welcome.

## Ways to Contribute

- Report bugs or request features: [GitHub Issues](https://github.com/DTStack/SqlParserX/issues)
- Submit code: [GitHub Pull Requests](https://github.com/DTStack/SqlParserX/pulls)
- Join discussions: see [Community](/community)

## Development Workflow

1. Fork the repo and create a feature branch: `git checkout -b feature/xxx`
2. Build locally: `mvn clean install -DskipTests`
3. Write code and tests, ensure `mvn test` passes
4. Push the branch and open a Pull Request
5. Merge after CI and review

## Conventions

- Follow the module layering: syntax differences → `sqlparserx-syntax`, business differences → `sqlparserx-core`
- When adding a datasource, also update capability docs and regression tests
- Match the existing code style and avoid unrelated dependencies

## Documentation

- Docs are maintained in the project root `docs/` and synced to this site
- Update the relevant capability docs when adding parsing capabilities