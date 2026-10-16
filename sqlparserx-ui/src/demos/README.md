# SQL Demo 样例维护

示例 SQL 由前端统一维护，目录结构为：

```text
src/demos/sql/{工具}/{数据源方言}/{场景}.sql
```

例如：

```text
sql/lineage/COMMON/insert-join.sql
sql/table-parser/MYSQL/cte-union.sql
sql/create-table-parser/DORIS/duplicate-key.sql
```

- `COMMON` 表示通用方言样例；没有某个数据源专用文件时会回退到它。
- 专用方言目录中的同名场景会覆盖 `COMMON` 版本；也可以新增该方言独有场景。
- 每个文件第一行可写 `-- demo-title: 场景名称`，用于界面展示；该元数据行不会载入编辑器。
- 血缘示例可额外写 `-- demo-min-relations: 数量`，校验时要求后端至少返回这么多条血缘关系。
- 工具目录使用 `lineage`、`sql-splitter`、`syntax-validator`、`table-parser`、`create-table-parser`、`parameter-workbench`。

新增或调整示例后，启动对应版本的 SqlParserX 服务，并运行：

```bash
DEMO_SQL_API_BASE_URL=http://127.0.0.1:16758 npm run validate:demos
```

校验脚本会逐一调用对应工具接口，并覆盖服务端返回的所有数据源类型及其实际匹配到的示例。仓库现有解析用例可参考 `sqlparserx-engine/sqlparserx-core/src/test/resources`；前端示例保持为独立、精简的 SQL 文件，避免构建依赖测试资源目录。
