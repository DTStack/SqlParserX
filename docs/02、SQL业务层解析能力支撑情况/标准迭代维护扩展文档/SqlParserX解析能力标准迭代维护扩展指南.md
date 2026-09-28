# SqlParserX 解析能力标准迭代维护扩展指南

> 文档定位：本文件是 SqlParserX 解析能力的**核心标准维护文档**，统一约定**语法层（`sqlparserx-syntax`）**与**业务层（`sqlparserx-core`）**的后续迭代、扩展、修复、测试与验收流程。
> 相关文档：[双层架构设计](../../架构设计/架构设计.md)、[模块依赖包引用结构设计](../模块依赖包引用结构设计.md)、[Druid 版本与 SQL 语法支持情况](../../01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md)、[能力支持情况导航](../能力支持情况/README.md)。

## 一、总原则

1. **语法问题扩展 Druid（Syntax），业务语义扩展 SqlParserX（Core）**；通用能力沉淀 Base，数据源差异按方言覆盖。
2. 所有 SQL→AST 操作必须经过 `SqlSyntaxParser`，Core/Web 不直接创建 Druid StatementParser。
3. 任何改动以「**语法层能生成正确 AST + 业务层能输出正确业务结果 + 具备回归用例**」三者同时成立为完成标准。
4. 改动 Core 后**必须**运行聚合单测 `AllTestsSuite`，确认未影响解析主流程。
5. 代码改动同步更新对应能力支持文档，见[文档同步矩阵](#八文档同步矩阵)。

## 二、分层与模块结构

```text
SqlParserX（根 Reactor）
├── sqlparserx-engine（聚合父模块，packaging=pom）
│   ├── sqlparserx-syntax（jar，基于 Druid）    ← 语法层
│   └── sqlparserx-core（jar，依赖 syntax）     ← 业务层
├── sqlparserx-web（依赖 core，必要时显式依赖 syntax）
├── sqlparserx-mcp-server-sse（独立构建，依赖 core/syntax）
└── sqlparserx-ui（前端，仅通过 Web HTTP API）
```

| 层 | 模块 | 入口 | 职责边界 |
|---|---|---|---|
| 语法层 | `sqlparserx-syntax` | `SqlSyntaxParserFactory.getParser(DbType)` | 只做 SQL→AST、多语句解析、切分、方言语法扩展 |
| 业务层 | `sqlparserx-core` | `SqlParserFactory.getSqlParser(DbType)` | 基于 AST 做表解析、建表、血缘、函数、SQL 改写 |

禁止的依赖方向：

```text
sqlparserx-syntax -X-> sqlparserx-core
sqlparserx-core   -X-> sqlparserx-web / sqlparserx-mcp-server-sse
Druid             -X-> 任意 SqlParserX 业务模型
```

## 三、问题归属判定

| 现象 | 归属层 | 处理位置 |
|---|---|---|
| SQL 无法解析、关键字不识别、Parser 直接报错 | 语法层 | `syntax` 方言 Lexer / StatementParser |
| AST 节点或方言属性缺失/错误 | 语法层 | `syntax` 方言 AST Node |
| AST 正确，但表/建表/血缘/函数结果错误 | 业务层 | `core` 通用 Client 或方言 Client |
| 所有数据源都有同样问题 | 业务层通用逻辑 | `core.parser.base`、`core.select`、`core.utils` |
| 只有单个数据源结果不同 | 业务层方言扩展 | `core.parser.<dialect>` 下对应 Client |

核心判断方法：用目标 `DbType` 单独调用 `SqlSyntaxParserFactory.getParser(dbType).parseStatement(sql)`，能生成正确 AST 则问题在业务层，否则在语法层。

## 四、核心扩展

> 后续迭代只面对两个扩展面：**语法层扩展**与**业务层扩展**。

### 4.1 语法层扩展（`sqlparserx-syntax`）

**何时扩展**

- 新增/变更数据源关键字；
- SQL 在 Parser 阶段报语法错误；
- Druid 未生成正确的 Statement 或表达式节点；
- 新语法需要新增 AST Node；
- 方言特有 Lexer / StatementParser 行为。

**包结构约定**

```text
sqlparserx-syntax
└── com.dtstack.sqlparserx.syntax
    ├── SqlSyntaxParser.java
    ├── SqlSyntaxParserFactory.java
    └── druid
        ├── DruidSqlSyntaxParser.java
        └── dialect.<db>            ← 数据源语法扩展（Lexer/Parser/AST Node）
```

**扩展步骤**

1. 在 `syntax.druid.dialect.<db>` 下新增方言 Lexer / StatementParser / AST Node 扩展；
2. 在 `SqlSyntaxParserFactory` 中登记方言解析器（后续作为方言注册入口）；
3. 补充语法层单测（`sqlparserx-syntax/src/test`）；
4. 运行 `mvn -f sqlparserx-engine/pom.xml test` 验证语法层；
5. 确认 Core 现有业务能力仍能消费新 AST。

**禁止事项**

- 不实现真实表、建表结构、血缘等业务语义；
- 不复制自有 AST，直接复用 Druid AST；
- 不在业务层用大量正则替换规避语法问题。

**Druid 版本升级**

1. 在 `sqlparserx-engine/pom.xml` 的 `dependencyManagement` 中统一改版本；
2. 先跑 Syntax 方言语法测试，再跑 Core 全量回归（`AllTestsSuite`）；
3. 更新[《Druid版本与SQL语法支持情况》](../../01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md)。

### 4.2 业务层扩展（`sqlparserx-core`）

**能力入口与扩展点**

| 业务能力 | Core 入口 | 扩展点接口/基类 |
|---|---|---|
| 真实表解析 | `ISqlParser.parseTables` | `IParseTableClient` / `ParseTablesClient` / `BaseParseTableClient` |
| CREATE TABLE 解析 | `ISqlParser.parseCreateTable` | `ParseCreateTableClient` |
| 字段血缘 | `ISqlParser.parseColumnLineages` | `IColumnLineageClient` / `ColumnLineageClient` |
| 表血缘 | `ISqlParser.parseTableLineages` | `ITableLineageClient` / `TableLineageClient` |
| 函数提取 | `ISqlParser.parseFunction` | `BaseSqlParser` + `SQLASTVisitorAdapter` |
| 行级权限改写 | `ISqlParser.getRowLevelPermissionSql` | `ISqlFilterClient` / `BaseSqlFilterClient` |
| 参数化 / 预编译 | `parseParameterSql` / `buildSqlByPrepareCompile` | `IParseParameterClient` / `BaseParseParameterClient`（预留） |

**通用 vs 方言**

```text
问题在所有数据源出现 → 修改 base 通用实现（core.parser.base / core.select / core.utils）
问题只在单数据源出现 → 在 core.parser.<dialect> 继承并覆盖对应 Client
```

方言解析器标准骨架：

```java
public class XxxSqlParser extends BaseSqlParser {
    @Override
    public DbType getDbType() {
        return DbType.xxx;
    }

    @Override
    public IParseTableClient getParseTableClient() {
        return new XxxParseTableClient();
    }

    @Override
    public IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        return new ColumnLineageClient(tableColumnsMap /*, SQLParseStrategy... */);
    }

    @Override
    public ITableLineageClient getTableLineageClient() {
        return new TableLineageClient(/* SQLParseStrategy... */);
    }
}
```

**扩展步骤**

1. 判断通用问题还是方言差异，确定改 Base 还是方言 Client；
2. 在对应 Client 继承基类，仅覆盖有差异的方法（参考 `MysqlParseTablesClient`、`DorisParseCreateTableClient`、`StarrocksParseCreateTableClient`）；
3. 在 `SqlParserFactory` 注册方言解析器（若为新数据源）；
4. 补充该数据源 / 能力的测试用例与资源文件，并挂载到对应 Suite；
5. 运行 `AllTestsSuite` 回归。

**方言策略（`SQLParseStrategy`）**

| 策略 | 含义 | 典型数据源 |
|---|---|---|
| `COLUMN_IGNORE_CASE` | SQL 字段与元数据字段忽略大小写匹配 | Doris |
| `SUBQUERY_COLUMN_IGNORE_CASE` | 子查询字段忽略大小写匹配 | Doris |
| `CTE_FORWARD_REFERENCE` | CTE 前向引用（引用后面定义的 CTE） | Doris |

新增策略：增加枚举 → 在 Base/方言逻辑用 `isEnabled` 判定 → 在方言解析器中组合启用 → 补单测。

**禁止事项**

- 不直接调用 `SQLParserUtils.createSQLStatementParser` 或自行实例化 StatementParser；
- 不在 Web/MCP 层实现解析算法，只做编排与协议转换；
- 不为隔离 Druid 而复制自有 AST。

## 五、新增数据源标准流程

| 步骤 | 动作 | 层次 |
|---|---|---|
| 1 | 确认 Druid 是否已支持该方言（`DbType` 是否存在、能否生成正确 AST） | 语法层 |
| 2 | 语法不足时，在 `syntax.druid.dialect.<db>` 扩展 Lexer/StatementParser/AST | 语法层 |
| 3 | 新增 `XxxSqlParser extends BaseSqlParser` 并注册到 `SqlParserFactory` | 业务层 |
| 4 | 按需实现方言 Client（表/建表/血缘/策略） | 业务层 |
| 5 | 在 `core/datasource/<db>/` 补充专项测试与资源 | 测试 |
| 6 | 挂载到 `DatasourceSuite` → `AllTestsSuite` | 测试 |
| 7 | 建立 `能力支持情况/datasource/<db>/` 两份文档并在总表登记 | 文档 |

## 六、测试标准

### 6.1 聚合单测（强制）

改动 Core **必须**运行聚合单测 `AllTestsSuite`：

```text
AllTestsSuite
├── BaseSuite            # 通用模板测试：血缘（字段/表）、表解析、函数、字段过滤
├── DatasourceSuite      # 数据源专项测试：doris 等
└── SQLParseStrategySimpleTest   # 策略单测
```

| 新增内容 | 应挂载位置 |
|---|---|
| 通用能力用例 | `BaseSuite`（或对应子 Suite，如 `BaseLineageSuite`） |
| 数据源专项用例 | `DatasourceSuite` |
| 策略/工具用例 | `SQLParseStrategySimpleTest` 或对应工具测试 |

> 约束：新增测试类必须挂到 Suite，否则不会被聚合单测执行。

### 6.2 测试基类与资源约定

| 项目 | 约定 |
|---|---|
| 基类 | `AbstractSqlParserTest`、`BaseColumnLineageAnalyzerTest` 等 |
| 资源路径 | 重写 `getSqlTestPath()`，如 `/core/datasource/doris/` |
| 资源文件 | `<测试类同名>.txt`，多条 SQL 用 `-----N-----` 分隔，UTF-8 |
| 读取方式 | `readSqlByIndex(file, index)`（从 1 开始）或 `readSqlFile(file)` |
| 断言 | 直接断言解析结果对象/枚举/集合 |

资源文件示例：

```text
--------------------1--------------------
insert into new_table
WITH t1 AS (SELECT id, name FROM demo_table)
SELECT * FROM t1
--------------------2--------------------
select * from demo_table2
```

### 6.3 测试层次

| 层次 | 验证内容 |
|---|---|
| Syntax 单测 | SQL→AST、多语句解析、切分、方言语法扩展 |
| Core 通用测试 | 表解析、建表、字段/表血缘、函数、权限改写 |
| Core 方言测试 | Doris、MySQL、StarRocks 等方言业务差异 |
| Web/MCP 测试 | 参数转换、接口协议、结果序列化（不重复测解析算法） |

## 七、构建与验收

```bash
# 只构建并测试 Syntax + Core
mvn -f sqlparserx-engine/pom.xml test

# 从根 Reactor 精确测试 Syntax + Core
mvn -pl sqlparserx-engine/sqlparserx-syntax,sqlparserx-engine/sqlparserx-core -am test -DskipTests=false

# 完整 Reactor 构建
mvn clean package
```

验收清单（全部满足才可合并）：

1. 语法层：目标方言 SQL 能生成正确 AST（必要时补 Syntax 单测）；
2. 业务层：目标能力输出正确业务结果；
3. 回归：`AllTestsSuite` 全绿，未破坏主流程；
4. 新增测试已挂载到对应 Suite；
5. 相关能力支持文档已同步更新。

## 八、文档同步矩阵

| 改动类型 | 需同步的文档 |
|---|---|
| 新增/调整语法能力 | [Druid版本与SQL语法支持情况](../../01、SQL语法层解析能力支撑情况/Druid版本与SQL语法支持情况.md)、数据源`SQL语法解析支持情况.md` |
| 新增/调整业务能力 | [能力支持情况导航](../能力支持情况/README.md)、[数据源总表](../能力支持情况/datasource/SQL解析能力&数据源支撑适配情况.md)、数据源`SQL业务解析支持情况.md` |
| 通用 Base 能力变化 | [通用解析能力](../能力支持情况/通用解析能力/) 对应场景文档 |
| 模块/依赖结构变化 | [模块依赖包引用结构设计](../模块依赖包引用结构设计.md)、[双层架构设计](../../架构设计/架构设计.md) |
| 新增数据源 | 数据源目录两份文档 + 总表登记 + [能力支持情况导航](../能力支持情况/README.md) |

## 九、变更记录

| 日期 | 变更 |
|---|---|
| 2026-09-28 | 初始化语法层与业务层标准迭代维护扩展指南 |
