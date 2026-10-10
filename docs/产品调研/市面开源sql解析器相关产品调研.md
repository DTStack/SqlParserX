# 市面开源 SQL 解析器及相关产品调研

> 调研日期：2026-10-10
>
> 调研目标：从 SqlParserX 的产品定位出发，盘点可直接集成的开源 SQL 解析器、血缘分析工具及可在线体验的竞品。本文所称“开箱即用”分为三类：**依赖库可直接集成**、**CLI/服务可直接运行**、**官网可直接体验**。

## 1. 结论摘要

如果只看“可作为 SqlParserX 直接竞品或技术参照”的项目，建议优先关注：

1. **SQLGlot**：多方言解析、转换、优化和血缘能力最完整，Python 生态下开箱体验优秀，是方言转换和 API 设计的首要对标对象。
2. **Alibaba Druid SQL Parser**：Java 技术栈、多数据库方言、手写解析器，和 SqlParserX 的技术路线最接近，适合作为语法解析底座。
3. **JSqlParser**：轻量 Java AST 库，Maven 集成简单，适合作为纯解析/改写能力的横向基线。
4. **Apache Calcite**：提供解析、校验、关系代数和优化器，不是单纯解析器；适合复杂语义校验与查询规划，不适合追求轻量接入的场景。
5. **SQLLineage / DataHub SQL Parser**：直接面向表级、列级血缘，最值得对标 SqlParserX 的业务解析输出和血缘准确度。
6. **Apache DataFusion SQL Parser**：Rust 生态中成熟、可扩展的多方言 AST 解析器，适合观察手写解析器与方言扩展设计。
7. **Gudu SQLFlow / GSP Playground**：商业闭源，但真正做到打开官网即可看到 AST 或列级血缘，最适合作为产品体验和演示交互的竞品标杆。

总体判断：**没有一个开源项目同时完整覆盖“Java、多方言、本地化语法、表/字段血缘、SQL 改写、Web API、可视化 UI”**。SqlParserX 的差异化机会不是再做一个 AST 库，而是把 Druid 的方言能力包装成稳定的业务语义模型、服务接口和可视化体验。

## 2. 产品分类说明

SQL 相关项目经常被放在同一张表里比较，但它们解决的问题不同：

| 类型 | 典型输入输出 | 代表项目 | 与 SqlParserX 的关系 |
| --- | --- | --- | --- |
| 语法解析器 | SQL → Token/AST | Druid、JSqlParser、DataFusion SQL Parser | 底层能力直接竞品 |
| 解析与转换框架 | SQL → AST → 方言 SQL/优化结果 | SQLGlot、Calcite | 能力上限与扩展设计参照 |
| SQL 质量工具 | SQL → 语法树 → Lint/格式化 | SQLFluff | 方言、模板 SQL 和工程体验参照 |
| 血缘分析工具 | SQL + 元数据 → 表/列依赖图 | SQLLineage、DataHub Parser | 业务层直接竞品 |
| 数据血缘平台/标准 | 作业事件、SQL、元数据 → 全局血缘 | OpenLineage、DataHub、OpenMetadata | 上游集成及产品边界参照 |
| 在线商业产品 | SQL → 在线 AST/血缘图 | GSP、Gudu SQLFlow | 官网体验和商业化参照 |

## 3. 开源 SQL 解析器横向对比

### 3.1 核心项目

这里按两个维度拆分：**通用解析器**主要输出 Token/AST，使用者需要继续开发业务逻辑；**业务型解析项目**已经在 AST 之上提供方言转换、血缘、Lint、格式化或语义解析等可直接使用的能力。

#### 3.1.1 通用/底层 SQL 解析器

| 项目 | 语言 | 主要定位 | 方言/能力概况 | 开箱即用方式 | 网址/在线体验 | 许可证 | 主要局限 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Alibaba Druid SQL Parser | Java | 多方言 SQL AST 解析 | MySQL、Oracle、PostgreSQL、SQL Server、DB2、Hive、ODPS 等；Visitor、格式化、参数化 | Maven 依赖，调用 `SQLUtils` 等 API | [GitHub](https://github.com/alibaba/druid) · [SQL Parser 文档](https://github.com/alibaba/druid/wiki/SQL-Parser)；无官方 Playground | Apache-2.0 | AST 与 Druid 类型体系耦合；跨方言转换、业务血缘需上层实现 |
| JSqlParser | Java | SQL → Java AST | Visitor、SQL 反序列化与改写 | Maven/Gradle 依赖 | [GitHub](https://github.com/JSQLParser/JSqlParser) · [使用文档](https://jsqlparser.github.io/JSqlParser/)；无官方 Playground | Apache-2.0 或 LGPL-2.1 双许可 | 方言完整度不等同于数据库原生解析器；复杂血缘需自行建设 |
| Apache Calcite | Java | SQL 解析、校验与关系代数框架 | Parser、Validator、RelNode、规则优化器、方言输出 | Maven 依赖；配置 Parser/Validator | [官网](https://calcite.apache.org/) · [SQL 参考](https://calcite.apache.org/docs/reference.html)；无官方 Playground | Apache-2.0 | 体系重、学习成本高；厂商特有语法通常需要扩展 JavaCC 语法 |
| Apache DataFusion SQL Parser | Rust | 可扩展 SQL Lexer/Parser | ANSI 与多种数据库方言；AST、Visitor、Serde、JSON CLI 示例 | Cargo crate；CLI 示例可输出 JSON | [GitHub](https://github.com/apache/datafusion-sqlparser-rs) · [Crates.io](https://crates.io/crates/sqlparser)；无官方 Playground | Apache-2.0 | 核心是语法 AST，不直接提供业务血缘或 Web 产品 |
| node-sql-parser | TypeScript / JavaScript | SQL AST 解析与反序列化 | 多数据库语法；AST、表/列访问列表、SQLify | npm 包；Node.js/浏览器 API | [GitHub](https://github.com/taozhi8833998/node-sql-parser) · [npm](https://www.npmjs.com/package/node-sql-parser)；无稳定的官方 Playground | Apache-2.0 | 复杂方言和存储过程覆盖需逐项实测；血缘语义较基础 |
| libpg_query | C（多语言绑定） | 复用 PostgreSQL 原生 parser | PostgreSQL 语法树、normalize、fingerprint、scan | C 库及 Ruby/Go/Rust 等绑定 | [GitHub](https://github.com/pganalyze/libpg_query)；无官方 Playground | BSD-3-Clause | 基本只面向 PostgreSQL；版本升级跟随 PG 源码 |
| ANTLR grammars-v4 SQL grammars | Grammar + 多目标语言 | 语法生成器的 SQL 文法集合 | 包含多种 SQL grammar，可生成 Java/Python/Go/C# 等 parser | 选 grammar 后使用 ANTLR 生成代码 | [SQL grammars 目录](https://github.com/antlr/grammars-v4/tree/master/sql) · [ANTLR Lab](https://lab.antlr.org/)（通用 grammar 调试，非 SQL 成品） | BSD-3-Clause（仓库；使用时需复核具体文件） | 更像原材料而非成品；AST、错误恢复、方言维护和业务语义均需自行实现 |

#### 3.1.2 带业务能力、可开箱使用的解析项目/产品

| 项目 | 语言 | 已封装的业务能力 | 开箱即用方式 | 网址/在线体验 | 许可证 | 适合场景 | 主要局限 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Gudu SQLFlow（马哈鱼） | SaaS / 私有化服务 | SQL 解析、表级/列级血缘、关系可视化、SQL 文件与数据库元数据分析 | 浏览器打开网站，登录后提交 SQL 或脚本分析 | **[在线使用](https://sqlflow.gudusoft.com/#/)** · [中文官网](https://www.sqlflow.cn/) · [GSP Playground](https://www.sqlparser.com/playground/) | **商业闭源** | 零部署体验复杂 SQL 血缘；对标 SqlParserX 的产品交互与结果展示 | 不是开源项目；免费额度、账号要求和私有化授权以官网当前政策为准；敏感 SQL 不应上传公网 |
| SQLGlot | Python | 多方言解析、Transpile、AST 改写、优化、表达式执行、血缘 | `pip install sqlglot`，Python API/CLI | [官网/文档](https://sqlglot.com/sqlglot.html) · [GitHub](https://github.com/tobymao/sqlglot)；官网以文档/API 示例为主 | MIT | 方言转换、数据平台 SQL 分析、原型验证 | Python 技术栈；复杂语义仍依赖 schema/metadata；并非数据库原生校验器 |
| SQLFluff | Python | 多方言 Lint、自动修复、格式化、Jinja/dbt 模板解析 | `pip install sqlfluff`，CLI 直接 `lint`、`fix`、`parse` | [官网/文档](https://sqlfluff.com/) · [在线 Demo](https://online.sqlfluff.com/) · [GitHub](https://github.com/sqlfluff/sqlfluff) | MIT | SQL 规范治理、模板 SQL、CI | 产品重点是 Lint，不是通用 AST SDK 或完整血缘引擎 |
| SQLLineage | Python | 源/目标表、表级和列级血缘、图形化展示 | 安装后使用 CLI 或 Python API，可用 `-g` 查看图 | [在线 Demo](https://reata.github.io/sqllineage/) · [文档](https://sqllineage.readthedocs.io/) · [GitHub](https://github.com/reata/sqllineage) | MIT | 最快搭建开源血缘 Demo、SQL 文件批量分析 | 复杂字段消歧、动态 SQL、存储过程通常需要 metadata 或定制 |
| DataHub SQL Parser | Python | schema-aware 表/列血缘、置信度、调试信息、跨语句临时表聚合 | DataHub Python SDK `parse_sql_lineage()` | [功能文档](https://docs.datahub.com/docs/lineage/sql_parsing) · [DataHub Demo](https://demo.datahubproject.io/) · [GitHub](https://github.com/datahub-project/datahub) | Apache-2.0 | 生产级元数据和查询日志血缘 | Parser 隶属 DataHub 体系；公开 Demo 主要展示平台而非独立 SQL Playground |
| OpenMetadata SQLLineage | Python | 基于 SQLFluff/sqlparse 的表级、列级血缘分析 | Python 包/源码集成 | [GitHub](https://github.com/open-metadata/openmetadata-sqllineage) · [OpenMetadata Sandbox](https://sandbox.open-metadata.org/)（平台体验） | Apache-2.0 | 研究可插拔解析后端、接入 OpenMetadata | 需确认与 SQLLineage 上游的版本差异和维护方向；Sandbox 不是独立解析器页面 |
| JSQLTranspiler | Java | 基于 JSqlParser 的方言转换、列解析和血缘 | Maven/Gradle 依赖或命令行集成 | [GitHub](https://github.com/starlake-ai/JSQLTranspiler)；无稳定的官方在线 Playground | Apache-2.0 | Java 技术栈方言转换、与 JSqlParser 配套评估 | 社区规模和方言覆盖需结合自己的 SQL 样本验证 |

其中“官网/文档”只表示无需下载即可阅读示例，**不等于可以在网页中提交 SQL**；只有明确标记“在线 Demo/Playground/在线使用”的入口才按在线开箱体验统计。Gudu SQLFlow 因为与 SqlParserX 的业务和产品形态高度相关，作为商业参照列入本表，但不计入开源项目数量。第三方在线站点可能调整或关闭，正式演示前应再次确认可访问性。

### 3.2 Java 技术栈重点比较

| 维度 | Druid | JSqlParser | Calcite | ShardingSphere SQL Parser |
| --- | --- | --- | --- | --- |
| 核心定位 | 高性能多方言 AST | 轻量 Java AST | 解析 + 校验 + 优化 | 分布式数据库内核的解析引擎 |
| 解析实现 | 手写 parser | JavaCC 生成 | JavaCC 及扩展 parser | ANTLR grammar + Visitor |
| 接入成本 | 低 | 低 | 中高 | 高，不建议单独拆作通用 SDK |
| 方言适配 | 强，国内数据库生态较友好 | 中等，需按 SQL 样本验证 | 默认 SQL 较通用，厂商语法需扩展 | 围绕 ShardingSphere 支持的数据源 |
| 语义校验/优化 | 弱 | 弱 | 强 | 服务于路由、改写、执行 |
| 血缘开箱能力 | 无完整产品级输出 | 无，姊妹项目提供延伸能力 | 需基于关系表达式开发 | 非对外通用血缘产品 |
| 对 SqlParserX 的价值 | 当前底座及方言实现参照 | 轻量 API/AST 易用性参照 | 语义层与优化层参照 | ANTLR、多方言模块化和缓存参照 |

[Apache ShardingSphere](https://github.com/apache/shardingsphere) 本身是分布式 SQL 数据库生态，而不是独立通用解析产品。其[解析引擎文档](https://shardingsphere.apache.org/document/current/en/reference/sharding/parse/)展示了 ANTLR 解析、AST Visitor 和缓存等设计，适合研究，但若只需要“输入 SQL、输出统一 AST/血缘”，直接嵌入的成本通常高于 Druid 或 JSqlParser。

## 4. 开源血缘与业务解析工具

### 4.1 SQLLineage

- 项目：[reata/sqllineage](https://github.com/reata/sqllineage)，文档：[sqllineage.readthedocs.io](https://sqllineage.readthedocs.io/)。
- 定位：输入 SQL 文件或字符串，直接输出源表、目标表以及列级血缘。
- 开箱方式：安装 Python 包后使用 `sqllineage -f test.sql --dialect=ansi`；可用 `-g` 启动图形化展示。
- 优点：用户不需要直接理解 AST；CLI、Python API、可视化链路完整，很适合做血缘 Demo 和回归基准。
- 局限：解析正确不代表血缘一定正确；`SELECT *`、同名字段、动态 SQL、临时表和存储过程通常需要 schema 或上下文辅助。
- 对标价值：重点参考“单条命令产出血缘”“中间结果可解释”“图形化预览”。

### 4.2 DataHub SQL Parser

- 项目文档：[DataHub SQL Parsing](https://docs.datahub.com/docs/lineage/sql_parsing)；源码位于 [datahub-project/datahub](https://github.com/datahub-project/datahub)。
- 定位：基于 SQLGlot 增强的 SQL 血缘解析器，服务于 DataHub 元数据平台。
- 能力：表级血缘、列级血缘、CTE、子查询、Union，以及临时表、rename/swap 等跨语句聚合场景；SDK 会返回置信度和调试信息。
- 开箱方式：通过 DataHub Python SDK 调用 `parse_sql_lineage()`，也可结合查询日志采集器批量解析。
- 亮点：不是只从 AST 猜测字段，而是结合平台 schema 解析列名；“置信度 + debug info”非常值得 SqlParserX 借鉴。
- 注意：官方宣称的准确率来自其自有 benchmark，不能直接当作对 SqlParserX 的公平结论，应使用同一测试集复测。

### 4.3 OpenMetadata SQLLineage 分支

- 项目：[open-metadata/openmetadata-sqllineage](https://github.com/open-metadata/openmetadata-sqllineage)。
- 定位：OpenMetadata 社区维护的 SQLLineage 分支，引入 SQLFluff 作为解析后端。
- 价值：适合观察“底层 parser 可插拔”和多方言血缘的工程方式。
- 风险：选型前需确认与上游 SQLLineage 的版本差异、维护节奏和最终集成方向，不宜只按名称判断成熟度。

### 4.4 OpenLineage 与 Marquez（相邻产品，不是解析器）

- [OpenLineage](https://openlineage.io/) 是作业、运行和数据集血缘事件的开放标准，并非 SQL AST 库。
- [Marquez](https://github.com/MarquezProject/marquez) 是 OpenLineage 的参考后端和可视化实现。
- 它们适合承接 SqlParserX 的解析结果：SqlParserX 负责“从 SQL 识别依赖”，OpenLineage 负责“以标准事件传递依赖”，Marquez/DataHub/OpenMetadata 负责“存储与展示全局血缘”。

## 5. 打开官网即可用的产品与 Demo

| 产品 | 在线入口 | 是否开源 | 无部署可体验内容 | 对标重点 |
| --- | --- | --- | --- | --- |
| GSP SQL Parser Playground | [sqlparser.com/playground](https://www.sqlparser.com/playground/) | 否，商业产品 | 多方言语法解析、对象识别、AST、列级关系 | AST 可读性、错误反馈、方言切换 |
| Gudu SQLFlow（马哈鱼） | [在线使用](https://sqlflow.gudusoft.com/#/) / [中文站](https://www.sqlflow.cn/) | 否，商业产品 | 在线提交 SQL、表/列级血缘图；部分能力需注册 | 零安装体验、血缘图交互、复杂脚本展示 |
| SQLLineage Demo | [reata.github.io/sqllineage](https://reata.github.io/sqllineage/) | 是 | 浏览器查看 SQL 血缘示例/在线能力（以页面当前功能为准） | 开源项目 Demo、快速展示 |
| SQLGlot | [sqlglot.com](https://sqlglot.com/) | 是 | 官方文档、示例；社区存在基于其构建的 Playground | 方言互转 API 和表达式展示 |

需要特别区分两个同名项目：本文的 **Gudu SQLFlow** 是 Gudusoft 的数据血缘商业产品；[sql-machine-learning/sqlflow](https://github.com/sql-machine-learning/sqlflow) 则是“用扩展 SQL 描述机器学习流程”的开源项目，两者不是同一产品。

## 6. 开箱即用能力评分

评分说明：5 分表示几乎无需二次建设；该评分是面向 SqlParserX 产品场景的相对评价，不代表项目综合质量。

| 项目 | 集成易用性 | 方言广度 | AST/改写 | 方言互转 | 表/列血缘 | CLI/服务/UI | 综合建议 |
| --- | :---: | :---: | :---: | :---: | :---: | :---: | --- |
| Druid | 5 | 5 | 5 | 2 | 2 | 2 | Java 多方言解析底座首选之一 |
| JSqlParser | 5 | 3 | 4 | 2 | 2 | 2 | 适合作为轻量 Java API 基线 |
| Calcite | 3 | 3 | 5 | 3 | 2 | 1 | 适合语义校验/优化，不宜只为 AST 引入 |
| SQLGlot | 5 | 5 | 5 | 5 | 4 | 4 | 综合能力最值得对标 |
| DataFusion SQL Parser | 4 | 4 | 4 | 2 | 1 | 2 | Rust 生态和 parser 设计参照 |
| node-sql-parser | 5 | 3 | 4 | 2 | 2 | 3 | Node/浏览器场景便捷，需实测复杂 SQL |
| SQLFluff | 5 | 5 | 3 | 1 | 1 | 4 | Lint、格式化、模板 SQL 的标杆 |
| SQLLineage | 5 | 4 | 2 | 1 | 4 | 4 | 最快搭建开源血缘 Demo |
| DataHub SQL Parser | 3 | 5 | 3 | 1 | 5 | 3 | 带 schema、置信度的生产血缘参照 |
| GSP / SQLFlow | 5（在线） | 5 | 4 | 2 | 5 | 5 | 产品体验标杆，但闭源且商业授权 |

## 7. 建议的实测方案

仅比较官网声明容易失真，建议建立统一 SQL 语料库，对候选项目跑同一套测试。首轮可选择 **Druid、JSqlParser、SQLGlot、SQLLineage、DataHub SQL Parser**。

### 7.1 测试语料

1. 基础 DML：SELECT、INSERT SELECT、UPDATE、DELETE、MERGE。
2. DDL：CTAS、CREATE VIEW、ALTER TABLE、分区 DDL。
3. 复杂查询：多层 CTE、相关子查询、UNION、窗口函数、LATERAL VIEW、PIVOT。
4. 字段语义：`*` 展开、同名字段、别名覆盖、CASE WHEN、聚合函数、UDF。
5. 脚本上下文：临时表、变量替换、多语句、存储过程、动态 SQL。
6. 方言：MySQL、Oracle、PostgreSQL、SQL Server、Hive、Spark、Flink、Doris/StarRocks、ClickHouse、Trino。
7. 异常输入：半截 SQL、注释、模板占位符、超长 SQL、深层嵌套。

### 7.2 评价指标

| 指标 | 建议定义 |
| --- | --- |
| Parse Success Rate | 能生成完整 AST 的 SQL 数 / 总 SQL 数 |
| AST Correctness | 表、字段、表达式、语句类型是否与预期一致 |
| Lineage Precision / Recall | 产出的正确血缘边占比 / 应产出血缘边覆盖率 |
| Round-trip | AST 反序列化后是否保持原语义并可被目标数据库解析 |
| Error Quality | 是否给出位置、期望 token、上下文及可恢复结果 |
| Performance | 单条 P50/P95、吞吐量、内存、首次启动耗时 |
| Extensibility | 新增一种语法/方言所需改动范围、测试成本 |
| Operability | CLI、API、批处理、日志、指标、容器化、UI 完整度 |

## 8. 对 SqlParserX 的产品建议

### 8.1 短期：补齐“真的能马上用”

- 提供无需部署的公开 Playground，支持粘贴 SQL、选择方言、切换 AST/表解析/字段血缘/格式化视图。
- 提供单一可执行包或 Docker 镜像，并给出 5 分钟 Quick Start。
- 输出统一 JSON Schema，避免调用者直接依赖 Druid AST 类型。
- 错误结果包含行列号、失败阶段、期望语法以及“部分解析结果”。
- 内置 10～20 条典型复杂 SQL 示例，使用户打开页面即可看到产品上限。

### 8.2 中期：形成区别于底层 parser 的业务能力

- 建立 schema-aware 列解析：支持 `*` 展开、同名字段消歧和 UDF 规则。
- 为每一条血缘边保留来源表达式、SQL 位置、解析规则和置信度。
- 支持多语句上下文，追踪临时表、CTE、rename、swap 和脚本变量。
- 增加方言互转评估；不一定自己完整实现，可先探索以 SQLGlot 作为旁路服务。
- 输出 OpenLineage/DataHub/OpenMetadata 可消费的格式，降低进入数据治理平台的集成成本。

### 8.3 竞品借鉴优先级

| 优先级 | 对标对象 | 建议借鉴内容 |
| --- | --- | --- |
| P0 | GSP Playground / Gudu SQLFlow | 在线体验、示例 SQL、AST/血缘交互和结果解释 |
| P0 | SQLGlot | 极简 API、方言选择、Transpile、表达式树操作 |
| P0 | DataHub SQL Parser | schema-aware、置信度、debug 信息、跨语句聚合 |
| P1 | SQLLineage | CLI 一条命令、血缘图、测试样例组织 |
| P1 | SQLFluff | 模板 SQL、错误定位、插件化方言和工程文档 |
| P2 | Calcite / ShardingSphere | 校验与优化、ANTLR 缓存、多阶段编译架构 |

## 9. 选型建议

- **Java 项目，只需解析、改写**：优先实测 Druid 与 JSqlParser。
- **Java 项目，需要校验、关系代数、优化**：选择 Calcite，接受较高接入复杂度。
- **多方言转换或 Python 数据平台**：优先 SQLGlot。
- **快速生成表/列血缘 Demo**：优先 SQLLineage。
- **生产级元数据平台血缘**：评估 DataHub/OpenMetadata，将 SQL parser 与 schema、查询日志结合。
- **Rust 查询引擎或代理**：选择 Apache DataFusion SQL Parser。
- **必须与 PostgreSQL 原生语法一致**：选择 libpg_query 及对应语言绑定。
- **希望浏览器打开即体验**：GSP Playground 和 Gudu SQLFlow 最成熟，但需注意其商业授权和数据安全要求。

## 10. 风险与注意事项

1. **方言数量不等于覆盖质量**：项目声明“支持某方言”通常不意味着覆盖全部 DDL、存储过程和最新版本语法。
2. **语法成功不等于语义正确**：没有 catalog/schema 时，`*`、非限定字段、UDF 和动态 SQL 的列级血缘不可避免地存在歧义。
3. **许可证需要最终复核**：引入前应固定具体版本并由法务/开源治理流程复核许可证、依赖许可证及 NOTICE 要求。
4. **在线工具的数据安全**：生产 SQL 可能包含表名、字段名、业务规则甚至常量数据，不应直接提交到第三方官网。
5. **活跃度会变化**：版本、支持方言和维护状态应在正式选型时以仓库 release、commit 和 issue 响应重新核验。
6. **不要把 OpenLineage 当成 parser**：它解决血缘事件标准化，不负责完整理解任意 SQL。

## 11. 参考资料

- [Alibaba Druid SQL Parser Wiki](https://github.com/alibaba/druid/wiki/SQL-Parser)
- [JSqlParser GitHub](https://github.com/JSQLParser/JSqlParser)
- [Apache Calcite 官方文档](https://calcite.apache.org/docs/)
- [SQLGlot GitHub](https://github.com/tobymao/sqlglot)
- [Apache DataFusion SQL Parser GitHub](https://github.com/apache/datafusion-sqlparser-rs)
- [node-sql-parser GitHub](https://github.com/taozhi8833998/node-sql-parser)
- [SQLFluff GitHub](https://github.com/sqlfluff/sqlfluff)
- [SQLLineage 文档](https://sqllineage.readthedocs.io/)
- [DataHub SQL Parsing 文档](https://docs.datahub.com/docs/lineage/sql_parsing)
- [OpenLineage 官方文档](https://openlineage.io/docs/)
- [Apache ShardingSphere Parse Engine](https://shardingsphere.apache.org/document/current/en/reference/sharding/parse/)
- [GSP Online SQL Parser Playground](https://www.sqlparser.com/playground/)
- [Gudu SQLFlow](https://www.sqlflow.cn/)
