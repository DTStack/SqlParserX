# 变量参数解析与预编译（纯 Druid）实现方案

> 状态：初步实现（持续完善）
> 能力入口：`ISqlParser#parseParameterSql` / `ISqlParser#buildSqlByPrepareCompile`
> 底座：**仅 Druid 1.2.28**（`sqlparserx-syntax` 已提供 `String → SQLStatement`）
> 关联文档：[变量参数解析与预编译支持情况](../../能力支持情况/通用解析能力/变量参数解析与预编译支持情况.md)
> 说明：本文为该能力的**纯 Druid 实现方案**，也是其唯一设计文档。

---

## 一、背景与目标

「SQL 转 API」场景下，用户手写带 `${变量}` 的 SQL，平台需要：

- **配置期**：解析出有哪些 `${变量}`、绑定哪个字段、操作符、是否必填，以及 `SELECT` 输出字段 → `parseParameterSql`；
- **运行期**：用请求值把 `${变量}` 改写为 JDBC `?`，并按顺序给出绑定值 → `buildSqlByPrepareCompile`。

```text
配置期：SQL 里的 ${变量}  ->  解析成 API 入参配置（变量名/绑定字段/操作符/是否必填）
运行期：调用方传入参数值   ->  ${变量} 改写成 ? 并按序绑定执行  ->  返回标准结果
```

> 范围说明：本能力面向「SQL 转 API」的**查询（SELECT）**场景——API 契约要求同时具备**输入参数**（`${变量}`）与**输出参数**（`SELECT` 投影列）；因此仅支持 SELECT，INSERT/UPDATE/DELETE 等非查询语句不支持。

---

## 二、纯 Druid 实现可行性（基于 Druid 1.2.28 实测）

**核心结论：纯 Druid 可以承载该能力。**

> Druid 1.2.28 **原生**把 `${name}` 解析为 `SQLVariantRefExpr`（name = `${name}`），绝大多数位置**无需任何预处理**。

### 2.1 解析实测

| 输入 SQL（片段） | Druid 解析结果 | 是否可用 |
|---|---|---|
| `WHERE tenant_id = ${tenantId}` | `SQLBinaryOpExpr`，右值 `SQLVariantRefExpr("${tenantId}")` | ✅ |
| `WHERE id in (${ids})` | `SQLInListExpr`，`targetList=[SQLVariantRefExpr]` | ✅ |
| `SELECT ${col} AS c` | select 项为 `SQLVariantRefExpr` | ✅ |
| `concat(name, ${suf})` | 函数参数为 `SQLVariantRefExpr` | ✅ |
| `LIMIT ${offset}, ${rows}` | `SQLLimit.offset/rowCount` 均为 `SQLVariantRefExpr` | ✅ |
| `ORDER BY ${col}` / `ORDER BY ${col} asc` | `SQLSelectOrderByItem.expr` 为 `SQLVariantRefExpr` | ✅ |
| `WHERE name = '${x}'` | 右值是 `SQLCharExpr`（**字符串字面量，非变量**） | ✅ 天然排除 |
| `ORDER BY ${col} ${dir}` | **解析失败**（方向位 VARIANT 非法） | ⚠️ 需兜底 |
| `LIMIT ${a}*10` | **解析失败**（illegal variable） | ⚠️ 需兜底 |

### 2.2 结论

- 变量识别直接依赖 Druid 原生 `SQLVariantRefExpr`，**无需任何字符串占位替换**、**无需字符坐标**；
- Druid 对「ORDER BY 方向变量 / LIMIT 算术表达式」等位置原生解析失败；当前实现没有预处理兜底，需调用方调整 SQL 或另行实现预处理（见 6.1）。

---

## 三、整体架构设计

### 3.1 核心设计原则：结构递归分派，结果模型参考血缘解析

**关键结论：参数解析不能用一个通用 `SQLASTVisitorAdapter` 遍历变量节点来完成。** 原因是血缘解析早已证明的做法——真正需要的是**递归、按节点类型分派、带上下文与别名映射的 Service**：

| 维度 | 血缘解析（现有） | 参数解析（本方案） |
|---|---|---|
| 编排 | `ColumnLineageClient` 判定语句类型 → 解析目标/来源 → 组装结果 | `BaseParseParameterClient` 委托 `ParamParseService` / `PrepareSqlRewriter` |
| 遍历 | `SelectColumnSpecialLineageService` **按 `SQLTableSource`/`SQLExpr` 类型递归分派** | `ParamParseService` **按 `SQLSelectQueryBlock`/`SQLExpr` 类型递归分派** |
| 别名归一 | `SelectTableSpecialLineageService.queryFromTable`（from/join/子查询/union/lateral + with-as） | 设计目标复用血缘解析；当前只按简单字段名/owner 查元数据，别名穿透待完善 |
| 结果 | `SelectColumn`（真实表+字段+别名） | `ParseParameterResult`（输入为 `ParamInfo`，输出为 `ApiParamSpan`；当前 `tables` 为空） |

配置期按 SQL 语句结构和表达式类型递归分派，条件节点由父级处理以同时获得字段侧和变量侧；运行期对 AST 做结构改写和条件剪枝。当前有序值收集使用 AST 节点访问顺序收集，SQL 渲染仍由 `SQLUtils.toSQLString` 完成；尚未实现设计中的 `SQLASTOutputVisitor` 子类同步收集（见 4.2.4）。

### 3.2 整体架构

```text
调用方
 └─ ISqlParser#parseParameterSql / buildSqlByPrepareCompile        (String 入口)
      └─ BaseSqlParser → createStatement(sql)                     (syntax 层 Druid)
           └─ BaseParseParameterClient（入口编排）
                ├─ ParamParseService        配置期语句分派、AST 递归解析和直接字段元数据匹配
                ├─ PrepareSqlRewriter       克隆 AST、条件剪枝、变量改写、IN 展开、排序/分组内联
                ├─ SqlConditionPruner       SELECT WHERE / HAVING 可选条件剪枝
                └─ ParamDataTypeTransfer    默认通用类型转换；由数据源 Parser 注入方言实现

数据源扩展：`parser/mysql/parameter` 和 `parser/doris/parameter` 各自提供 `ParseParameterClient` 与类型转换实现；对应的 `MySqlParser` / `DorisSqlParser` 覆盖 `getParseParameterClient()`。
```

### 3.3 数据源类型传递（业务数据源类型，无需改接口）

先区分两个概念：

- **业务数据源类型** `SqlParserDataSourceType`（`MYSQL`/`ORACLE`/`DORIS`…）：当前用于选择类型转换实现；分页方言尚未实现；
- **Druid 方言** `DbType`：**仅用于 AST 渲染**（`SQLUtils.toSQLString` / `createOutputVisitor`）。

`ISqlParser#getDbType()` 返回 Druid `DbType`。默认入口用 `SqlParserDataSourceType.fromDbType(getDbType())` 构造 `BaseParseParameterClient`；数据源 Client 子类通过覆盖其可扩展方法来注入专属转换器：

```java
// ISqlParser 默认方法：反查业务类型，使用通用类型转换器
default IParseParameterClient getParseParameterClient() {
    return new BaseParseParameterClient(SqlParserDataSourceType.fromDbType(getDbType()));
}

// BaseParseParameterClient：提供默认实现，子类可覆盖
public BaseParseParameterClient(SqlParserDataSourceType dataSourceType) {
    DbType dbType = dataSourceType == null ? null : dataSourceType.getDbType();
    this.parseService = getParamParseService();
    this.prepareSqlRewriter = getPrepareSqlRewriter(dbType);      // 转换器在此注入
}
protected ParamParseService getParamParseService() { return new ParamParseService(); }
protected PrepareSqlRewriter getPrepareSqlRewriter(DbType dbType) {
    return new PrepareSqlRewriter(dbType, new BaseParamDataTypeTransfer());
}

// MySqlParser / DorisSqlParser 覆盖 getParseParameterClient()，返回自己的 Client 子类；
// 子类仅覆盖 getPrepareSqlRewriter(...) 注入方言转换器
```

- `ISqlParser#getSqlFilterClient(getDbType())` 已是同类先例；此处 **不改动 `IParseParameterClient` 方法签名**；
- `BaseParseParameterClient` 由 `dataSourceType` 得到 Druid `DbType` 用于 SQL 渲染，并在 `getPrepareSqlRewriter(dbType)` 内创建并注入 `ParamDataTypeTransfer`；base 不维护数据源转换注册表；
- 转换器注入点收敛为 `getPrepareSqlRewriter(DbType)` 一个可覆盖方法，新增数据源时在其 `parser/<datasource>/parameter` 下扩展实现并由对应 Parser 选择。

### 3.4 核心数据模型（沿用现有 VO/Domain）

```text
ParseParameterResult
├── inputParams   List<ParamInfo>      // 入参定义
├── outputParams  List<ApiParamSpan>   // 出参定义
└── tables        List<Table>

PrepareCompileResult
├── beautifulSql  String               // ${变量} 已改写为 ?
└── inputFields   List<Object>         // 与每个 ? 一一对应的有序绑定值

DataTypeColumn { name, type, comment }        // 绑定字段的方言原始类型
ApiParamSpan   { sqlPart, alias, bindColumns }
ParamInfo      { name, value, imperative, dataType, defaultValue, desc, fieldName, operator, sqlPart }
```

### 3.5 操作符口径：`ParamOperatorEnum`

`ParamInfo.operator` 目前是 `String`，语义上混合两类值：

| 类别 | 取值示例 | 来源 |
|---|---|---|
| 真实 SQL 操作符 | `=` / `<>` / `>=` / `like` / `in` / `not in` / `between` | Druid `SQLBinaryOperator`、`SQLInListExpr` 等 |
| 内部伪操作符 | `CUSTOM` / `CASE_WHEN` / `ORDER_BY_CASE_WHEN` / `API_PARAM_IN_SELECT_LIST` | SQL 结构（ORDER BY/LIMIT/CASE WHEN/select 变量） |

项目已维护枚举 `ParamOperatorEnum`（位于 `core/enums`）统一常见 SQL 操作符和内部伪操作符口径：

```java
public enum ParamOperatorEnum {
    EQ("="), NE("<>"), GT(">"), GTE(">="), LT("<"), LTE("<="),
    IS_NULL("is null"), IS_NOT_NULL("is not null"),
    LIKE("like"), NOT_LIKE("not like"),
    IN("in"), NOT_IN("not in"),
    BETWEEN("between"), NOT_BETWEEN("not between"),
    CUSTOM("CUSTOM"), CASE_WHEN("CASE_WHEN"),
    ORDER_BY_CASE_WHEN("ORDER_BY_CASE_WHEN"), SELECT_LIST("API_PARAM_IN_SELECT_LIST"),
    UNKNOWN("UNKNOWN");

    private final String symbol;
    public String getSymbol() { return symbol; }

    public static ParamOperatorEnum ofSymbol(String symbol) { ... }         // 由符号解析（容错大小写）
    public static ParamOperatorEnum ofDruid(SQLBinaryOperator op) { ... }   // 由 Druid 二元操作符映射
}
```

- **保留 `ParamInfo.operator: String`**（对外/落库兼容，旧值即 `"="`/`"in"`/`"CUSTOM"`）；解析过程通过内部 `ParameterSpan` 暂存操作符，再写入最终入参定义。

---

## 四、核心实现方法设计

### 4.1 核心方法一：`parseParameterSql`（配置期）

```java
ParseParameterResult parseParameterSql(SQLStatement stmt,
        Boolean isIgnoreSyntaxCheck,
        Map<String, List<DataTypeColumn>> tableColumnsMap);
```

#### 4.1.1 全流程细节步骤

```text
BaseSqlParser.parseParameterSql(String sql)                          (String 入口)
  └─ createStatement(sql) -> SQLStatement                            (syntax 层 Druid 解析)
  └─ ISqlParser.getParseParameterClient() -> BaseParseParameterClient
       └─ ParamParseService.parse(stmt, isIgnoreSyntaxCheck, tableColumnsMap)

1. 语句类型判定：仅 SQLSelectStatement 继续解析
     - 其他语句：isIgnoreSyntaxCheck=true 返回空结果，否则抛 IllegalArgumentException
2. 初始化结果容器
     - LinkedHashMap<String,ParamInfo> 入参（按首次出现顺序）
     - List<ApiParamSpan> 出参
3. SELECT 递归（walkSelect）
     3.1 WITH 子查询：遍历 CTE entry，逐个 walkSelect
     3.2 主体查询（walkQuery）
           - SQLSelectQueryBlock -> walkBlock
           - SQLUnionQuery -> 各 relation 递归 walkQuery，再处理 union 级 ORDER BY / LIMIT
     3.3 SELECT 级 ORDER BY / LIMIT（walkOrderBy / walkLimit，兼容极端形态）
4. QueryBlock 结构递归（walkBlock），严格按结构顺序：
     from -> where -> group by(items + having) -> order by -> limit -> select list
     4.1 walkTable：按 from 形态递归（派生表 / JOIN / UNION 子查询 / LATERAL VIEW）
     4.2 walkExpr(where)：表达式分派（比较 / IN / BETWEEN / 子查询 / 变量 / 复合表达式兜底）
     4.3 group items、having、order items、limit offset/rowCount 分别 walkExpr
     4.4 select list：变量登记为必填（携带别名）；非变量生成出参 ApiParamSpan 并 walkExpr 递归
5. 表达式递归（walkExpr）命中变量时构造内部 ParameterSpan
     - sqlPart = 节点文本、operator、params = [变量名]、mustFlag、bindColumns = bind(左值)
6. register 写入 ParamInfo（LinkedHashMap）
     - 首次：写入 name / imperative / operator / sqlPart / (fieldName / dataType / desc)
     - 同名再次出现：mergeParamInfo —— 必填优先、sqlPart 补齐、绑定信息补齐
7. bind 绑定字段：对简单 SQLIdentifierExpr / SQLPropertyExpr 取字段名 + owner，匹配 tableColumnsMap 回填类型/注释
8. 组装 ParseParameterResult(inputParams, outputParams, tables = [])
```

补充说明：

- 参数对象按首次出现顺序保存在 `LinkedHashMap`；`isIgnoreSyntaxCheck` 只控制非 SELECT 语句的处理（为 `true` 返回空结果，否则抛 `IllegalArgumentException`）。
- 出参 `outputParams` 由非变量 select 项生成；`tables` 当前返回空集合。

#### 4.1.2 递归分派（Service 内部，非通用 visitor）

**按结构分派（`SQLSelectQueryBlock` 的各部位）**：

| 部位 | 入口方法 | 当前实现说明 |
|---|---|---|
| `where` | `walkExpr(where)` | 条件块 |
| `groupBy.having` | `walkExpr(having)` | 条件块 |
| `groupBy.items` | `walkExpr` | 收集变量；运行期内联变量值 |
| `orderBy.items` | `walkExpr` | 收集变量；运行期内联变量值 |
| `limit` | `walkExpr` | offset / rowCount 变量按通用变量登记；未区分最外层 LIMIT |
| `selectList` | `walkBlock` | 直接变量登记为必填；普通输出列生成 `ApiParamSpan` |
| `from` | `walkTable` | 递归子查询 / join / union / lateral |
| union 的 order by / limit | `walkQuery` | UNION 顶层 ORDER BY / LIMIT 挂在 `SQLUnionQuery` 上，需单独递归 |
| SELECT 级 order by / limit | `walkSelect` | 兼容挂在 `SQLSelect` 上的形态 |

**按表达式分派（`walkExpr`，同 `buildSelectColumnItem` 的分派风格）**：

| 表达式类型 | Druid 节点 | 处理 |
|---|---|---|
| 比较条件 | `SQLBinaryOpExpr` | 右值为变量 → 生成 span；`operator = ParamOperatorEnum.ofDruid(op)`；`bindColumns = bindField(left)`；否则递归左右 |
| `IN/NOT IN` | `SQLInListExpr` | 左值 bindField；`targetList` 中变量 → params；operator=`IN`/`NOT_IN` |
| `BETWEEN / NOT BETWEEN` | `SQLBetweenExpr` | 区间两端变量归到同一参数定义；被比较值为绑定字段；operator=`BETWEEN`/`NOT BETWEEN` |
| `LIKE` | `SQLBinaryOpExpr`（`SQLBinaryOperator.Like`） | operator=`LIKE` |
| 函数 / CASE / 其他复合表达式 | Druid `SQLExpr` 子类 | 使用 `walkChildren` 的 Druid visitor 兜底收集变量，未单独生成 CASE 参数语义 |
| 子查询 | `SQLQueryExpr` | 递归进入内部 select |
| `IN 子查询/EXISTS` | `SQLInSubQueryExpr`/`SQLExistsExpr` | 递归进入内部 select；IN 左侧表达式同时递归 |
| 字面量 | `SQLCharExpr`/`SQLIntegerExpr` | 跳过（`'${x}'` 即此，天然排除） |
| 变量 | `SQLVariantRefExpr` | 命中变量（`${...}` 判定） |

#### 4.1.3 变量识别

```java
static boolean isApiVariable(SQLExpr e) {
    if (!(e instanceof SQLVariantRefExpr)) return false;   // '${x}' 已是 SQLCharExpr，天然排除
    String n = ((SQLVariantRefExpr) e).getName();
    return n != null && n.startsWith("${") && n.endsWith("}");
}
static String varName(SQLExpr e) { return ((SQLVariantRefExpr) e).getName().substring(2, ((SQLVariantRefExpr) e).getName().length() - 1); }
```

#### 4.1.4 树处理要点（候选收集 · 必填 · 递归）

1. **从结构入口识别主要参数块**：从 `where` / `having` / `limit` / `order by` / `group by` / `select list` / 子查询进入；主要条件节点由 `walkExpr` 按类型识别，其他复合表达式使用 visitor 兜底。
2. **必填（`mustFlag`）由结构决定**：`select-list` 参数视为**必填**；`where` 等条件参数默认**非必填**（与「缺省即删条件」呼应）。
3. **递归进子查询**：`field in (select ...)`、`exists (select ...)`、`from (select ...)` 递归进入内部 `select`。
4. **外层判定用结构而非文本（后续目标）**：如「最外层 `LIMIT`」应以「当前 `select` 是否为主体」判定；当前尚未区分最外层与子查询内的 `LIMIT`。
5. **with-as / CTE**：当前递归解析 CTE 子查询中的变量；字段绑定尚未复用血缘解析的 CTE/派生表别名穿透，元数据查找只处理直接字段引用。
6. **同名变量合并**：同一 `${变量}` 出现多次时只产出一条 `ParamInfo` —— **必填优先**（任一次为必填则整体必填），字段/类型/注释等绑定信息**补齐首次缺失项**，操作符与绑定字段保留首次出现；运行期每次出现各产生一个 `?`，值同源。

#### 4.1.5 参数与表字段绑定解析（核心难点）

**为什么要专门解决**：`${变量}` 只声明了「值来自入参」，但要确定预编译时该 `?` 应绑定成**什么类型**，必须先知道这个参数**绑定的是哪张表的哪个字段**：

```text
配置期：param -> 真实表字段(绑定) -> 字段类型            // parseParameterSql
            |                         |
            +------ 落成入参配置 ParamInfo(dataType) ------+
                                                          v
运行期：buildSqlByPrepareCompile 用 columns[param].dataType 决定每个 ? 的类型
```

**期望支持的绑定语义**（当前实际只从简单 `SQLIdentifierExpr` / `SQLPropertyExpr` 提取字段元数据，函数、算术、CASE 左值的字段集合尚未实现）：

| 场景 | 示例 | 绑定字段 |
|---|---|---|
| 字段 = 变量 | `t.tenant_id = ${tenantId}` | 左值字段 `t.tenant_id` |
| 表达式比较 | `func(a, b) > ${x}`、`a + b = ${x}` | 左值表达式内真实字段集合（API 取第一个） |
| IN / NOT IN | `t.status in (${states})` | 左值字段 `t.status` |
| LIKE | `t.name like ${kw}` | 左值字段 `t.name` |
| 无字段 | `ORDER BY ${col} ${dir}`、`LIMIT ${n}` | 无绑定字段，类型走兜底（`OBJECT`/`String`） |

**当前字段绑定实现**：已在 parameter 模块增加独立的 `ParameterColumnResolver`，第一阶段支持普通表、schema/catalog 表、表别名、JOIN 和裸字段唯一性消歧。`ParamParseService.bind` 先根据字段所在 `SQLSelectQueryBlock` 的 `FROM` 作用域将 owner 解析为真实表，再用真实表名和字段名精确查询 `tableColumnsMap`；派生表/CTE/UNION 输出列穿透及复杂表达式字段提取仍待完善。

- **`t.field`**：在当前查询块中解析 `t -> real_table`，再精确匹配 `real_table.field`；
- **裸字段 `field`**：只在当前查询块的真实表集合中匹配，唯一命中才绑定；多表同名时保留字段名但不猜测类型；
- **函数 / 算术 / CASE 左值**：尚未可靠提取其中真实字段集合，不能保证类型绑定正确；
- **元数据匹配规则**：优先使用作用域解析出的真实表名和字段名精确查找；查不到或存在歧义时仍创建只有字段名、type/comment 为空的 `DataTypeColumn`；
- **后续目标**：递归还原派生表/CTE 输出列，并对多表同名字段结合作用域消歧。

因此，普通表别名与 JOIN 多表同名字段已经做到确定性匹配，并复用了 `TableSourceUtil`；CTE、派生表输出列和多级别名仍未做到血缘级别的确定性归一。

##### 4.1.5.1 优化前的确定性问题与优化后状态

优化前的 `bind(field, metadata)` 只有“字段表达式 + 全局元数据”，没有使用字段所在 `SQLSelectQueryBlock` 的 `FROM` 和查询作用域，所以它无法回答 owner 是 SQL 别名时对应哪张真实表。例如：

```sql
select u.id
from user_info u
where u.tenant_id = ${tenantId}
```

优化前的实际执行路径是：

```text
u.tenant_id
  -> owner=u, column=tenant_id
  -> 遍历整个 tableColumnsMap
  -> metadata key=user_info 与 owner=u 不匹配
  -> 退化为只按 tenant_id 查找 fallback
```

这里最终得到 `user_info.tenant_id`，只是因为测试元数据中恰好只有一个 `tenant_id`，并不是已经解析出 `u -> user_info`。一旦多张表存在同名字段，结果就可能随 `Map` 遍历顺序变化：

```sql
select u.id
from user_info u
join audit_log a on a.user_id = u.id
where u.tenant_id = ${tenantId}
```

```text
user_info.tenant_id -> BIGINT
audit_log.tenant_id -> VARCHAR(64)
```

因为 `u` 与两个 metadata key 都不相等，两个字段都会成为 fallback，最后一次遍历到的字段可能覆盖前一个字段，导致 `tenantId` 被错误标记成 `VARCHAR(64)`。随后运行期会依据错误的 `ParamInfo.dataType` 转换绑定值，问题会从“字段归属不准”继续传播为“JDBC 参数类型不准”。

问题按第一阶段优化后的状态整理如下：

| 场景 | 优化后行为 | 状态 |
|---|---|---|
| 表别名 | `u.id` 通过当前查询块的 `FROM user_info u` 还原为 `user_info.id` | 已解决 |
| schema/catalog 表 | `app.user_info u` 同时登记全名、短表名和别名，元数据仍按真实全名查询 | 已解决 |
| JOIN 限定字段 | JOIN 左右表别名分别指向自己的真实表，同名字段不再互相覆盖 | 已解决 |
| 裸字段 | 只在当前查询块的真实表集合中查找；唯一命中才绑定，多表同名时不猜类型 | 已解决 |
| 同名别名嵌套 | 从字段 AST 向上取最近的 `SQLSelectQueryBlock`，内层别名遮蔽外层别名 | 已解决 |
| 派生表 | `t.id` 尚未沿 `t -> 子查询输出列 -> 源表字段` 追溯 | 后续阶段 |
| CTE / UNION | 外层 CTE/UNION 输出列尚未穿透到源表字段 | 后续阶段 |
| 复杂左值 | `cast(u.id as char)`、`u.amount + u.tax` 尚未递归提取真实字段 | 后续阶段 |
| 变量在比较左侧 | `${minId} < u.id` 仍会退化为 CUSTOM，尚未做左右归一 | 后续阶段 |

##### 4.1.5.2 优化边界：只增强参数解析，不修改字段血缘行为

本次优化已收敛在 parameter 模块内。现有字段血缘逻辑保持原有入口、数据模型和行为，没有修改 `ColumnLineageClient`、`SelectColumnSpecialLineageService` 的匹配规则。

实际新增的轻量组件如下：

```text
parameter
├── service/ParamParseService
└── resolve
    ├── ParameterColumnResolver          // 字段表达式 + 当前查询作用域 -> 真实表字段
    └── ResolvedParameterColumn          // tableName / columnName
```

实际职责拆分：

```text
ParamParseService
  -> 识别变量所在条件，并取得作为绑定候选的字段表达式

ParameterColumnResolver
  -> 从字段 AST 的 parent 链定位最近 SQLSelectQueryBlock
  -> 遍历当前块 FROM/JOIN，解析表别名和字段来源
  -> 输出 ResolvedParameterColumn(realTable, realColumn)

ParameterColumnResolver.findColumn
  -> 使用 realTable + realColumn 在 tableColumnsMap 中精确查询

ParamParseService
  -> 回填 ParamInfo.fieldName/dataType/desc
```

组件复用遵循“参数解析调用稳定公共工具，不反向修改字段血缘”的边界：

- 已复用 `TableSourceUtil.extractTableName/removeQuotes` 解析真实表名、catalog、db 和引号；
- 普通表别名和 JOIN 规则在 parameter 的轻量解析器中独立实现，避免引入完整血缘解析状态；
- 后续复杂表达式和派生表穿透可参考 `SelectColumnSpecialLineageService.buildSelectColumnItem/queryFromTableByColumns` 的递归分派思路；
- 不直接调用 `ColumnLineageClient`，因为它面向 INSERT/CTAS 完整血缘，结果模型不含参数所需的类型和注释；
- 不把 `DataTypeColumn` 强行改造成血缘 `Column`；真实字段定位完成后仍由 parameter 使用原 `tableColumnsMap` 查询类型。

##### 4.1.5.3 参数场景的解析流程

当前实现不修改 `walkExpr/span/bind` 的整条方法签名，而是利用 Druid AST 已有的 parent 关系从字段节点反向找到最近查询块。实际调用链如下：

```text
ParamParseService.walkExpr
  -> 识别 u.tenant_id = ${tenantId}
  -> span(part, field=u.tenant_id, variable=tenantId)
  -> bind(field, tableColumnsMap)
       -> ParameterColumnResolver.resolve(field, metadata)
            1. fieldReference：拆成 owner=u、column=tenant_id
            2. findQueryBlock：沿 field.parent 找最近 SQLSelectQueryBlock
            3. collectTables：递归当前 block.getFrom()
                 - 普通表：登记 alias/fullTable/shortTable -> realTable
                 - JOIN：递归收集 left/right
                 - LATERAL VIEW：递归内部 tableSource
            4. owner 非空：在当前作用域执行 u -> user_info
               owner 未命中别名时，再兼容直接真实表名/schema.table 写法
            5. owner 为空：在当前作用域各真实表中查字段
               - 唯一命中：返回该真实表字段
               - 多个命中：tableName=null，标记无法确定
            6. 返回 ResolvedParameterColumn(tableName, columnName)
       -> findColumn：只在 realTable 对应元数据中精确查字段
       -> 生成 DataTypeColumn(name, type, comment)
  -> register/applyBindInfo
       -> 写入 ParamInfo.fieldName/dataType/desc
```

最近查询块策略天然形成作用域隔离：如果外层和内层都声明 `u`，内层字段向上首先遇到内层 `SQLSelectQueryBlock`，因此只扫描内层 `FROM`，不会串用外层的 `u`。

限定字段和裸字段采用不同算法：

| 字段形态 | 定位规则 | 无法确定时的行为 |
|---|---|---|
| `u.tenant_id` | `u -> realTable` 后精确查 `realTable.tenant_id` | 保留 `tenant_id`，类型/注释为空 |
| `user_info.tenant_id` | owner 直接与元数据表名匹配 | 保留字段名，类型/注释为空 |
| `tenant_id` | 只检查当前查询块内真实表，要求恰好一个表包含该字段 | 多表同名时不绑定类型 |
| 无查询块上下文 | 仅允许全局元数据唯一命中 | 多个候选时不绑定类型 |

元数据表名匹配兼容 `table`、`schema.table` 和 `catalog.schema.table`：先做忽略大小写的全名比较，再做带点边界的后缀比较。字段名也忽略大小写，但字段查找始终限制在已经确定的真实表中。

普通表别名的最小闭环如下：

```text
FROM user_info u
WHERE u.tenant_id = ${tenantId}

u.tenant_id
  -> tablesByQualifier["u"] = "user_info"
  -> tableColumnsMap["user_info"] 查 tenant_id
  -> DataTypeColumn(tenant_id, BIGINT, 租户)
  -> ParamInfo(fieldName=tenant_id, dataType=BIGINT, desc=租户)
```

当前已完成第一阶段：普通表、schema/catalog 表、表别名、JOIN、查询块作用域隔离和裸字段唯一性消歧。全局字段名 fallback 已被严格限制为“没有可识别 FROM 实表且全局元数据恰好唯一命中”；任何歧义都不会再依赖 `Map` 顺序选择。第二阶段支持派生表、CTE、UNION 输出列穿透；第三阶段补充复杂表达式多字段和相关子查询的外层引用。

##### 4.1.5.4 可直接暴露现有问题的单测

原有测试主要验证“变量是否被发现”，字段名又大多不冲突，因此旧 fallback 容易被掩盖。第一阶段已经增加 9 个真实表定位回归测试，其中编号 18 是正式测试资源中的嵌套查询：

| 已覆盖场景 | 核心断言 |
|---|---|
| JOIN 同名字段 + `u` 别名 | 必须绑定 `user_info.tenant_id/BIGINT` |
| JOIN 同名字段 + `a` 别名 | 必须绑定 `audit_log.tenant_id/VARCHAR(64)` |
| 反向调整 `LinkedHashMap` 插入顺序 | 字段类型和注释结果完全一致 |
| JOIN 裸字段多表同名 | 保留字段名，`dataType/desc` 必须为空 |
| JOIN 裸字段唯一命中 | 绑定唯一包含该字段的真实表类型 |
| schema 表别名 | `app.user_info u` 正确绑定 `app.user_info.tenant_id` |
| 内外层使用同名别名 | 参数绑定最近查询块内的真实表字段 |
| 编号 18 复杂嵌套查询 | 多 CTE、派生表、JOIN、CTE 内 UNION、标量子查询和主查询 UNION 中的 8 个参数分别绑定真实字段类型，并生成 8 个有序运行期值；最终输出严格等于最外层第一 UNION 分支的 7 列 |
| 配置期到运行期组合 | 解析得到 `BIGINT` 后，字符串 `"9"` 最终转换为 `Long 9L` |

所有定向测试同时断言 `fieldName/dataType/desc`；顺序测试使用 `LinkedHashMap` 主动交换元数据顺序；组合测试把 `parseParameterSql` 结果直接传入 `buildSqlByPrepareCompile`，验证字段定位错误不会在运行期被隐藏。测试类中的每一次 `parseParameterSql` 调用还必须同时断言 `inputParams` 和 `outputParams` 的准确数量，防止只验证参数名称或单个绑定结果时遗漏 CTE、派生表、标量子查询及 UNION 内部投影污染最终 API 输出的问题。派生表、CTE、UNION 字段穿透和复杂左值的测试将在对应阶段实现时补充。

**类型如何传到预编译**：

- `buildSqlByPrepareCompile(stmt, inParams, columns)` **不接收 `tableColumnsMap`**，运行期类型只能来自 `columns[param].dataType`；
- 因此配置期必须把「绑定字段类型」写入 `ParamInfo.dataType` 随配置落库；
- 运行期据此调用 `ParamDataTypeTransfer.transfer(dataType, value)` 决定每个 `?` 的绑定类型（见 4.2.6）；
- 元数据缺失时，当前 base 类型转换器在 dataType 为空时原样返回 value。

#### 4.1.6 出参与元数据

- 输出参数表示 SQL 转 API 的最终结果集结构，只收集最外层查询投影；CTE、FROM 派生表、标量子查询的内部投影均使用临时输出容器，不追加到最终 `outputParams`；
- 顶层 `UNION/UNION ALL` 的各分支按位置共享同一套结果结构，因此只使用第一分支生成输出参数，后续分支仍解析输入变量但不重复追加输出列；
- 当前将最外层非变量 select 项转成 `ApiParamSpan`（表达式文本、别名及简单字段绑定）；尚未复用血缘输出列解析，也未展开 `*`；
- 当前参数服务通过独立的 `ParameterColumnResolver` 先定位真实表字段，再查询 `tableColumnsMap`；输出字段仍只支持简单字段绑定，尚未展开 `*` 或穿透派生表/CTE 输出列。

##### 4.1.6.1 复杂嵌套 SQL 的输入、输出分流步骤

配置期递归过程中，输入参数和输出字段具有不同的传播范围：`${变量}` 无论出现在外层还是任意子查询中，都是调用该 API 时需要提供的输入，因此所有层级共享同一个 `LinkedHashMap<String, ParamInfo> params`；输出字段只描述最终结果集，只有最外层主体查询可以写入主 `outputs`。实现通过“共享 `params`、按查询边界切换 `outputs` 容器”保证两者不会相互污染：

```text
parse(statement)
  创建 sharedParams                    // 整棵 AST 共享，汇总所有层级输入变量
  创建 finalOutputs                    // 只承载 API 最终结果结构
  walkSelect(root, sharedParams, finalOutputs)

walkSelect(select, params, outputs)
  1. WITH 中每个 CTE：
       walkSelect(cte.query, params, new temporaryOutputs())
       // 收集 CTE 内参数，丢弃 CTE 中间投影
  2. 主体 query：walkQuery(select.query, params, outputs)
  3. 继续收集 SELECT 级 ORDER BY / LIMIT 中的变量

walkTable(from, params, outputs)
  1. FROM (SELECT ...) 派生表：使用 temporaryOutputs
  2. FROM (query UNION query) ：使用 temporaryOutputs
  3. JOIN：左右表源继续递归，并解析 ON 条件参数
  // 表源子查询只提供中间数据，不改变外层 API 输出结构

walkExpr(expr, params)
  1. 标量子查询、EXISTS、IN 子查询：共享 params，使用 temporaryOutputs
  2. 普通条件：提取变量并绑定条件另一侧真实字段
  // 子查询 SELECT 列不进入 finalOutputs，其中的变量仍进入 sharedParams

walkQuery(union, params, outputs)
  1. 第一个分支：使用当前 outputs，建立 UNION 最终列结构
  2. 第二个及后续分支：共享 params，使用 temporaryOutputs
  3. 解析 UNION 自身 ORDER BY / LIMIT 参数
  // UNION 各分支按列位置形成一套结果，不能把每个分支的投影纵向累加
```

这里的临时容器不是跳过子查询解析。子查询仍完整遍历，其变量、操作符、字段绑定类型和首次出现顺序都会写入共享 `params`；只有不属于最终结果集的内部投影被隔离。临时容器在该递归分支返回后即可丢弃。

以编号 18 的复杂 SQL 为例：

```text
输入参数：
  CTE t1/t2/t3/t4 内参数 5 个
  主查询标量子查询 olderAge 1 个
  主查询两个 UNION 分支条件参数 2 个
  => 去重后 inputParams = 8

最终输出：
  只取最外层 UNION 第一分支：
  t1.id, t1.name, t2_name, t3_name, t4_age, dt.department, older_count
  => outputParams = 7

被隔离的内部投影：
  各 CTE 的 SELECT 列、FROM 派生表列、older_count 标量子查询内部 COUNT(*)、
  最外层 UNION 第二分支的重复投影
```

修复前，递归调用始终传递同一个 `outputs`，导致上述内部投影被逐层追加，编号 18 最终错误地产生 30 个输出字段。修复后每个查询边界明确选择最终容器或临时容器，并由单测同时锁定输入、输出数量及最外层输出名称。

#### 4.1.7 LIMIT / 分页

- 当前遍历 LIMIT offset / rowCount 并识别变量；参数属性按普通表达式变量处理，没有固定设为 INT/imperative，也未区分最外层 LIMIT；
- 运行期保留 Druid 当前方言的 LIMIT AST 形态并改写其中变量；尚未实现跨方言分页语法重写器。

---

### 4.2 核心方法二：`buildSqlByPrepareCompile`（运行期）

```java
PrepareCompileResult buildSqlByPrepareCompile(SQLStatement stmt,
        Map<String, Object> inParams,
        Map<String, ParamInfo> columns);
```

#### 4.2.1 全流程细节步骤

```text
BaseSqlParser.buildSqlByPrepareCompile(String sql, inParams, columns)   (String 入口)
  └─ createStatement(sql) -> SQLStatement                              (syntax 层 Druid 解析)
  └─ ISqlParser.getParseParameterClient() -> BaseParseParameterClient
       └─ PrepareSqlRewriter.rewrite(stmt, inParams, columns)

0. 语句校验：仅 SQLSelectStatement，非查询直接抛 IllegalArgumentException
1. 克隆 AST：statement.clone()（不改写调用方语法树）；规整 inParams / columns（空 Map 兜底）
2. Pass 1 条件剪枝（SqlConditionPruner）
     遍历 SQLSelectQueryBlock，对 where / having 递归剪枝：
       - 缺值的可选条件移除；AND/OR 一侧为空保留另一侧；两侧皆空则整体置空
       - 参数标记 imperative=true 时，缺值前调用 ParamInfo.hasValue() 抛缺值异常
3. Pass 2 变量改写（一次 visitor 遍历，记录 IdentityHashMap<占位符, 绑定值>）
     3.1 SQLInListExpr：变量值按集合展开为多个占位符（id IN (?, ?, ?)）
     3.2 SQLSelectOrderByItem / SQLSelectGroupByClause：变量内联为 SQLIdentifierExpr（值直接写入，不产生 ?）
     3.3 SQLVariantRefExpr：${变量} -> Constant.PREPARED_PLACEHOLDER("?")，
                            并按 columns[name].dataType 经 ParamDataTypeTransfer 转换绑定值
4. Pass 3 收集绑定值：按改写后 AST 的 SQLVariantRefExpr 遍历顺序收集 inputFields（与渲染顺序一致）
5. 渲染：SQLUtils.toSQLString(sql, dbType) -> beautifulSql
6. 返回 PrepareCompileResult(inputFields, beautifulSql)
```

关键点：

- 占位符统一使用 `Constant.PREPARED_PLACEHOLDER`；每个 `?` 与 `inputFields` 一一对应、按序绑定。
- 运行期类型来自 `columns[param].dataType`（配置期 `ParamInfo.dataType` 落库传递），与 `tableColumnsMap` 无关。
- 当前实现没有自定义 `SQLASTOutputVisitor`：绑定值来自改写后 AST 的 visitor 遍历顺序，之后独立调用 `SQLUtils.toSQLString` 渲染；顺序一致性与其风险见 4.2.4。

#### 4.2.2 条件剪枝：`SqlConditionPruner`

- 遍历 SELECT QueryBlock，对 WHERE 与 HAVING 调用 `SqlConditionPruner`；缺值参数对应条件被移除；
- 逻辑表达式（AND/OR）一侧移除时保留另一侧；两侧都可空则整体置空；
- 如果参数定义标记 `imperative=true`，剪枝前调用 `ParamInfo.hasValue()` 触发缺值异常；
- 仅处理 SELECT 的 WHERE/HAVING；非 SELECT 语句在运行期直接拒绝（抛 `IllegalArgumentException`）。

#### 4.2.3 变量改写与绑定记录

```java
// 变量 -> ?，记录绑定值
Object bind = transfer.transfer(columns.get(name).getDataType(), inParams.get(name));
ref.setName(Constant.PREPARED_PLACEHOLDER);
pairing.put(ref, bind);                    // IdentityHashMap

// IN/NOT IN 展开：值按 Iterable / 数组 / 单值转列表，展开为多个 ?
List<SQLExpr> targets = new ArrayList<>();
for (Object v : toList(value)) {
    SQLVariantRefExpr vref = new SQLVariantRefExpr(Constant.PREPARED_PLACEHOLDER);
    pairing.put(vref, transfer.transfer(dataType, v));
    targets.add(vref);
}
inExpr.setTargetList(targets);             // id IN (?, ?, ?)

// ORDER BY / GROUP BY 内联：值直接写成标识符，不使用占位符、不计入 inputFields
// ORDER BY ${col} ASC -> setExpr(new SQLIdentifierExpr(value))
```

- `ORDER BY ${col} ASC` → `setExpr(new SQLIdentifierExpr(value))`；`GROUP BY ${x}` → 写入实际值；
- 这些位置不能用 `?`（改变语义），值内联；调用方需限制这些位置的值为可信合法标识符。

#### 4.2.4 参数值收集与顺序一致性

```java
List<Object> ordered = new ArrayList<>();
rewrittenSql.accept(new SQLASTVisitorAdapter() {
    @Override public boolean visit(SQLVariantRefExpr x) {
        if (Constant.PREPARED_PLACEHOLDER.equals(x.getName()) && pairing.containsKey(x))
            ordered.add(pairing.get(x));
        return true;
    }
});
String beautifulSql = SQLUtils.toSQLString(rewrittenSql, dbType);
```

顺序一致性依赖「AST 遍历顺序」与「Druid 输出顺序」一致。对 SELECT 常见结构（WHERE/GROUP BY/HAVING/ORDER BY/LIMIT、子查询/CTE/UNION/JOIN）两者一致，已由测试覆盖；但这并非 Druid 的通用保证，已知例外：`WINDOW` 子句（accept 先于 WHERE，输出后于 WHERE）与 PostgreSQL/Oracle 的 `LIMIT ... OFFSET ...`（输出顺序与 accept 相反），这些位置当前不在支持范围内。彻底方案是改用输出 visitor，在打印占位符时同步收集。

#### 4.2.5 LIMIT / OFFSET 当前处理范围

- `offset/rowCount` 是变量则 `setName(Constant.PREPARED_PLACEHOLDER)` 并记录值；是常量则写字面量；
- 当前不重写分页语法，序列化保留输入 AST 所属方言的表达方式；MySQL 的 `LIMIT offset, rowCount` 变量会参与普通 `?` 改写及参数收集；
- PostgreSQL / Oracle / DB2 / Trino 等跨方言分页结构转换属于后续工作。

#### 4.2.6 字段类型转换：`ParamDataTypeTransfer`

负责把「方言原始类型 + 原始值」转换为最终放入 `PrepareCompileResult.inputFields` 的 Java 对象。运行期类型来自 `columns[param].dataType`；配置期解析得到的 `ParamInfo.dataType` 可以直接作为 `columns` 配置传入。

```java
public interface ParamDataTypeTransfer {
    Object transfer(String dataType, Object value);   // 方言原始类型 -> 预编译绑定对象
}
```

- base 默认实现：整数类型前缀转 `Integer/Long`，DECIMAL/NUMERIC/NUMBER 转 `BigDecimal`，FLOAT/DOUBLE 转对应浮点类型，BOOL 转 `Boolean`，TIMESTAMP 转 `Timestamp`；未识别的非空类型转 `String`；转换异常回退 `value.toString()`；`dataType` 或 value 为空时原样返回；
- 注入方式：`BaseParseParameterClient` 通过可覆盖的 `getPrepareSqlRewriter(dbType)` 创建 `PrepareSqlRewriter`，转换器在该方法内注入；base 默认使用 `BaseParamDataTypeTransfer`，MySQL / Doris 分别覆盖该方法注入自身 `parser/<datasource>/parameter` 包下的实现；base 不维护方言注册中心；
- 配合：`transfer(columns[param].dataType, inParams.get(param)) -> 绑定值`；
- 当前方言差异实现：

| 数据源 | 实现位置 | 覆盖类型 | 最终对象 |
|---|---|---|---|
| Base 默认 | `parser/base/parameter/transfer/BaseParamDataTypeTransfer` | 常见整数、DECIMAL/NUMERIC/NUMBER、FLOAT/DOUBLE、BOOL、TIMESTAMP；其他非空类型作为字符串 | `Integer` / `Long` / `BigDecimal` / `Float` / `Double` / `Boolean` / `Timestamp` / `String` |
| MySQL | `parser/mysql/parameter/MysqlParamDataTypeTransfer` | 继承 base；额外处理 BIT/BOOL、DATETIME/TIMESTAMP、DATE、YEAR | BIT/BOOL → `Integer`；DATETIME/TIMESTAMP → `Timestamp`；DATE → `java.sql.Date`；YEAR → `Integer` |
| Doris | `parser/doris/parameter/DorisParamDataTypeTransfer` | 继承 base；额外处理 BOOLEAN、DATETIME、DATE、LARGEINT | BOOLEAN → `Boolean`；DATETIME → `Timestamp`；DATE → `java.sql.Date`；LARGEINT 当前作为 `String` |

Oracle、PostgreSQL、Hive、Spark、StarRocks、Trino、ClickHouse 等目前走 base 默认转换，尚无专属实现。

---

### 4.3 端到端示例

#### 4.3.1 配置期

```sql
SELECT id, name FROM user_info WHERE tenant_id = ${tenantId} AND status = ${status}
```

设计目标中的绑定解析：`tenantId -> user_info.tenant_id`、`status -> user_info.status`，类型由 `tableColumnsMap` 回填。当前实现会将这类字段类型写入 `ParamInfo`；参数识别过程的变量列表和绑定列保存在内部 `ParameterSpan`，不会混入只表示最终输出字段的 `ApiParamSpan`。

以下 JSON 是目标 API 配置形态示意，**不是当前 `ParseParameterResult.inputParams` 的序列化结果**：

```json
{
  "inputParams": [
    {"sqlPart":"tenant_id = ${tenantId}","params":["tenantId"],"operator":"=",
     "bindColumns":[{"name":"tenant_id","type":"BIGINT","comment":"租户ID"}]},
    {"sqlPart":"status = ${status}","params":["status"],"operator":"=",
     "bindColumns":[{"name":"status","type":"VARCHAR","comment":"状态"}]}
  ],
  "outputParams": [{"sqlPart":"id"},{"sqlPart":"name"}]
}
```

#### 4.3.2 运行期

输入 `inParams={tenantId:1001, status:"OPEN"}`：

```json
{
  "beautifulSql": "SELECT id, name FROM user_info WHERE tenant_id = ? AND status = ?",
  "inputFields": [1001, "OPEN"]
}
```

`tenant_id = ?` ↔ `inputFields[0]`（MySQL BIGINT 经 base 转换器转为 `Long`），`status = ?` ↔ `inputFields[1]`（VARCHAR→`String`）。

#### 4.3.3 关键流程时序

```text
配置期：
Caller -> ISqlParser.parseParameterSql(sql, ...)
  -> BaseSqlParser.createStatement(sql)                    (syntax 层 Druid)
  -> ISqlParser.getParseParameterClient()                  (MySQL / Doris 使用各自 parser Client)
  -> BaseParseParameterClient.parseParameterSql(stmt, ...)
       -> ParamParseService.parse(...)                     (四类 SQL 语句分派)
       -> walkSelect / walkQuery / walkBlock / walkTable / walkExpr
       -> bind(...)                                        (简单字段名查 tableColumnsMap)
       -> 组装 ParamInfo 与 outputParams
       -> return ParseParameterResult

运行期：
Caller -> ISqlParser.buildSqlByPrepareCompile(sql, inParams, columns)
  -> BaseSqlParser.createStatement(sql)
  -> ISqlParser.getParseParameterClient()
  -> BaseParseParameterClient.buildSqlByPrepareCompile(stmt, inParams, columns)
       -> PrepareSqlRewriter.rewrite(...)
       -> SqlConditionPruner（SELECT WHERE / HAVING）
       -> 注入的 ParamDataTypeTransfer 转换值
       -> AST 遍历收集 inputFields；SQLUtils 渲染 beautifulSql
       -> return PrepareCompileResult(beautifulSql, inputFields)
```

---

## 五、核心的类包结构

```text
core/enums/
└── ParamOperatorEnum                  (操作符口径：SQL 操作符 + 内部伪操作符)

core/config/
└── Constant                           (PREPARED_PLACEHOLDER = "?")

core/parser/base/parameter/
├── BaseParseParameterClient           (入口编排；可覆盖 getParamParseService / getPrepareSqlRewriter)
├── support/ParameterAstUtils          (变量识别、取值、IN 值转集合)
├── service/
│   └── ParamParseService              (SELECT 解析及 AST 递归)
├── domain/                            (已有：ApiParamSpan / DataTypeColumn / ParamInfo)
├── vo/                                (已有：ParseParameterResult / PrepareCompileResult)
├── rewrite/
│   ├── PrepareSqlRewriter             (克隆、改写、绑定映射、参数收集与渲染)
│   └── SqlConditionPruner             (SELECT WHERE / HAVING 条件剪枝)
└── transfer/
    ├── ParamDataTypeTransfer          (转换接口)
    └── BaseParamDataTypeTransfer      (通用实现)

core/parser/mysql/parameter/
├── MysqlParseParameterClient          (覆盖 getPrepareSqlRewriter 注入 MySQL 类型转换器)
└── MysqlParamDataTypeTransfer

core/parser/doris/parameter/
├── DorisParseParameterClient          (覆盖 getPrepareSqlRewriter 注入 Doris 类型转换器)
└── DorisParamDataTypeTransfer
```

- `BaseParseParameterClient` 只编排配置期解析和运行期预编译：配置期委托 `ParamParseService`，运行期委托 `PrepareSqlRewriter`。构造时用 `SqlParserDataSourceType` 得到 Druid `DbType`，并通过可覆盖的 `getPrepareSqlRewriter(dbType)` 创建改写器、在该方法内注入 `ParamDataTypeTransfer`；
- `ISqlParser` 默认方法根据 `getDbType()` 反查 `SqlParserDataSourceType` 并创建 base Client。MySQL 和 Doris 分别由 `MySqlParser`、`DorisSqlParser` 覆盖 `getParseParameterClient()`，返回自己的 `MysqlParseParameterClient` / `DorisParseParameterClient`；这两个子类仅覆盖 `getPrepareSqlRewriter(dbType)` 注入方言转换器。新增数据源沿该结构在自己的 parser 目录扩展，base 不包含数据源注册表；
- 当前参数字段绑定尚未复用 `core/select/*` 的 alias / lineage 解析。

---

## 六、核心目前的实现范围边界情况

### 6.1 非法语法边界

| 位置 | 现象 | 兜底 |
|---|---|---|
| `ORDER BY ${col} ${dir}` | 方向位 `${dir}` 解析失败 | 当前没有预处理兜底；调用方需调整 SQL 或后续补充预处理 |
| `LIMIT ${a}*10` | `illegal variable`（变量参与算术） | 当前不支持；后续可用轻量表达式求值 |

当前实现没有 `PreprocessFallback`；`'${x}'` 无需处理（Druid 已解析为字符串字面量）。

### 6.2 运行期尚未接入的行为

- 仅支持 SELECT：配置期与运行期对非 SELECT 语句分别忽略/拒绝；
- 条件剪枝只处理 SELECT 的 WHERE/HAVING；
- LIMIT 变量按普通变量改写，不进行跨方言分页重写，也未区分最外层 LIMIT；
- 空集合 IN 的专门 SQL 语义尚未定义；
- 输出字段 `*` 未展开，主体 SELECT 输出边界与 `tables` 结果尚未实现。

### 6.3 边界与风险

| 项 | 说明 | 处置 |
|---|---|---|
| 变量语法 | 仅 `${name}`（`[a-zA-Z_0-9]+`） | 正则约束；不支持 `${bdp.system.bizdate}` |
| 引号内变量 | `'${x}'` | Druid 天然识别为 `SQLCharExpr`，无需处理 |
| **遍历方式** | 配置期结构递归；复合表达式兜底 visitor | 当前已实现主要节点递归分派 |
| 有序参数顺序 | AST 遍历收集与 SQLUtils 单独渲染 | 常见结构一致并由测试覆盖；WINDOW / 跨方言 LIMIT-OFFSET 等除外，见 4.2.4 |
| 参数-字段绑定 | 别名/子查询/CTE/多表同名字段 | 当前只按简单字段名/owner 匹配；别名穿透及歧义消解待完善 |
| 无绑定字段 | ORDER BY/LIMIT/自定义 SQL | 使用 `columns[param].dataType`；空类型时原值不转换 |
| 元数据缺失 | `bindColumns.type` 为空 | base transfer 空类型保留原值；未知非空类型转字符串 |
| 操作符口径 | `operator` 混用 SQL/伪操作符 | 当前在 `ParamOperatorEnum` 中定义常见符号；部分节点使用 CUSTOM |
| SQL 重新渲染 | 大小写/换行可能变化 | SQLUtils 重新渲染，格式与原文可能不同 |
| ORDER BY 方向变量 | 原生解析失败 | 当前无预处理支持 |
| LIMIT 算术 | 原生解析失败 | 一期不支持，二期表达式求值 |
| 条件剪枝 | AND/OR/括号/空 WHERE | 当前 SELECT WHERE/HAVING 使用递归剪枝；非 SELECT 语句直接拒绝 |
| 重复变量 | 同一变量多次出现 | 每次出现各产生一个 `?`，值同源 |
| 类型转换 | 方言类型繁多 | base / MySQL / Doris 三层实现；其他数据源当前走 base |

### 6.4 测试覆盖

测试 SQL 统一放在 `core/base/parameter/BaseParseParameterClientTest.txt`（**均为查询语句**），目前有 18 条编号 SQL；Java 测试通过资源 SQL与内联场景，共覆盖 37 个场景。覆盖内容：

- **基础**：字段=变量、比较操作符（`>` / `>=` / `<` / `<=` / `<>` / `like`）、`IN`/`NOT IN`、`BETWEEN`/`NOT BETWEEN`、select-list 变量、字符串字面量排除、`select *`；
- **结构**：`GROUP BY`/`HAVING`/`ORDER BY`/`LIMIT` 内联与改写；
- **嵌套**：CTE、派生表、多表 JOIN、UNION（顶层 + FROM 内）、EXISTS、多层 IN 子查询、SELECT 列表标量子查询；
- **顺序**：多个 SELECT 列表标量子查询（配置顺序与渲染顺序不同）、UNION 分支与 `LIMIT` 等复杂嵌套下 `inputFields` 与 `?` 渲染顺序一致性（含 `?` 相对位置断言）；
- **去重/合并**：同一变量重复出现；同名变量在 `where` 与 `select-list` 同时出现时必填优先；
- **输入/输出数量契约**：测试类中全部 27 次 `parseParameterSql` 调用均同时断言 `inputParams` 与 `outputParams` 的准确数量；嵌套 SQL 的内部投影不得计入最终输出，顶层 UNION 只以第一分支定义结果结构；
- **输出字段**：除数量外，对最外层输出投影名称及 `ParamInfo`/`ApiParamSpan`/`ParseParameterResult` 的关键字段做断言；编号 18 明确验证 8 个输入参数、7 个最终输出字段，避免此前内部投影累计为 30 个输出字段的问题回归；
- **组合**：`parseParameterSql` 生成的 `ParamInfo` 直接作为 `buildSqlByPrepareCompile` 的 `columns`；
- **类型**：MySQL 与 Doris 类型转换断言最终对象类型。
- **定义侧取值**：请求参数缺失时从 `ParamInfo.value/defaultValue` 回退，并验证仍按 `dataType` 转换；证明这些字段属于运行期预编译契约。
- **真实表定位**：JOIN 两侧别名精确绑定、schema 表别名、元数据顺序不影响结果、裸字段唯一命中/多表同名歧义、内外层同名别名作用域、编号 18 多 CTE/派生表/JOIN/UNION/标量子查询综合验证，以及解析类型向运行期绑定值类型传递。

2026-10-13 最近一次验证：参数专项测试 37 个通过；`AllTestsSuite` 114 个通过；core 与依赖模块全量测试 118 个通过。

后续需增加：

- 基础：单变量、多变量、重复变量、必填缺值、`'${x}'` 字符串、无变量；
- **绑定**：字段=变量、表达式多字段、别名/子查询/CTE 别名、多表同名字段、无字段场景（ORDER BY/LIMIT）；
- 条件：`=`/`IN`/`NOT IN`/`LIKE`、未赋值非必填条件删除、`AND/OR` 混合、括号；
- 结构：`ORDER BY`（含 asc/desc 变量）、`GROUP BY`、`LIMIT/OFFSET` 变量、最外层常量 `LIMIT`；
- 出参：别名、`*` 展开（依赖 `tableColumnsMap`）、函数列、`CASE WHEN` 列；
- 操作符：`ParamOperatorEnum.ofSymbol/ofDruid` 映射正确；
- 类型：各数据源典型类型经 `ParamDataTypeTransfer` 转换；
- 一致性断言：`beautifulSql` 中第 N 个 `?` ↔ `inputFields` 第 N 个值。

### 6.5 后续完善项

| 阶段 | 内容 | 重点复用 |
|---|---|---|
| 已完成（第一阶段） | 参数字段绑定支持普通表、schema/catalog 表、表别名、JOIN 和裸字段唯一性消歧 | parameter 内 `ParameterColumnResolver`、`TableSourceUtil` |
| 下一步 | 参数字段绑定支持 CTE、派生表、UNION 输出列穿透和复杂表达式字段提取 | 参考 `SelectColumnSpecialLineageService`，不修改字段血缘行为 |
| 下一步 | 用 Druid 输出 visitor 在渲染时同步收集每个 `?` 的绑定值 | `SQLASTOutputVisitor` |
| 下一步 | 输出字段 `*` 展开、主体 SELECT 输出边界、tables 结果 | select / lineage 引擎 |
| 下一步 | 方言分页改写、LIMIT 元数据标记、非法变量位置预处理 | datasource-specific rewriter / preprocessor |
| 持续扩展 | 新数据源在自己的 `parser/<datasource>/parameter` 目录增加 Client 与 Transfer | `ParamDataTypeTransfer` |

---

## 七、变更记录

| 日期 | 变更 |
|---|---|
| 2026-10-10 | 初版：基于 Druid 1.2.28 实测（`${x}` → `SQLVariantRefExpr`），给出纯 Druid 实现蓝本、可复用组件与分期计划 |
| 2026-10-10 | 明确遍历方案：改用与血缘解析同构的「递归分派 Service」（不用通用 AST visitor）；有序值用 Druid 输出访问者收集；新增 `ParamOperatorEnum` |
| 2026-10-10 | 细化「别名.字段 → 真实表.字段 → 类型」解析：from 别名映射、真实表 vs 派生表两种字段引用、元数据查询、逐层示例 |
| 2026-10-10 | 将「参数与表字段绑定解析」并入配置期方法的字段绑定小节（现 4.1.5）；明确核心复用它 `SelectTableSpecialLineageService.queryFromTable` |
| 2026-10-10 | 澄清数据源类型传递：区分**业务数据源类型 `SqlParserDataSourceType`**（类型转换/分页）与 **Druid `DbType`**（仅渲染）；Client 由 `fromDbType(getDbType())` 获得业务类型，不改接口 |
| 2026-10-12 | 补充初步实现记录：Base Client 编排、SELECT 解析与嵌套递归、MySQL/Doris 方言类型转换注入、当前边界及测试资源组织 |
| 2026-10-12 | 参数能力收敛为**仅支持 SELECT 查询**：移除 INSERT/UPDATE/DELETE 解析分支，运行期对非 SELECT 直接拒绝；同步测试资源与文档 |
| 2026-10-12 | 补充 `BETWEEN`/`NOT BETWEEN` 与 `SQLUnionQuery` 顶层 `ORDER BY`/`LIMIT` 的变量识别；测试资源改为纯查询并扩充到 14 条、22 个场景，覆盖全部输出字段与多层嵌套 |
| 2026-10-12 | 同名变量合并：必填优先 + 绑定信息补齐；测试补充多标量子查询/CTE+UNION 的 `?` 顺序断言与同名参数合并用例，扩充到 17 条、26 个场景 |
| 2026-10-12 | `ApiParamSpan` 移除未赋值字段 `isOutLayer`/`isConst`/`constValue`；`ParamInfo` 增加 `sqlPart`（参数块来源片段）；4.1/4.2 各补充「全流程细节步骤」子章节 |
| 2026-10-12 | 转换器注入点收敛为可覆盖的 `BaseParseParameterClient#getPrepareSqlRewriter(dbType)`；占位符统一为 `Constant.PREPARED_PLACEHOLDER`；核心方法补充流程序号注释 |
| 2026-10-12 | 文档重构为「背景/可行性/整体架构设计/核心实现方法设计/类包结构/实现范围边界情况」六大章；核心方法一二补充细节流程，冗余的独立实现说明章节并入正文 |
| 2026-10-13 | 补充参数字段绑定的确定性问题、参数模块内 `ParameterColumnResolver`/查询作用域设计，以及可暴露别名与多表同名字段误绑定的定向测试方案；明确不修改现有字段血缘行为 |
| 2026-10-13 | 落地字段绑定第一阶段：新增 `ParameterColumnResolver`，支持普通表别名、JOIN、schema/catalog 表及裸字段歧义保护；增加 9 个回归测试（含编号 18 复杂嵌套查询），参数专项 35 个通过 |
| 2026-10-13 | 修复嵌套查询输出污染：CTE、派生表、标量子查询使用临时输出容器，UNION 只取第一分支结果结构；复杂编号 18 输出由错误的 30 列收敛为最外层真实 7 列，并为全部嵌套结构用例补充输出投影断言 |
| 2026-10-13 | 完善配置期解析测试契约：审计测试类全部 27 次 `parseParameterSql` 调用，统一补充输入参数数量与最终输出字段数量断言 |
| 2026-10-13 | 精简结果模型：`ApiParamSpan` 仅保留输出字段属性，参数块状态改为服务内部 `ParameterSpan`；`ParamInfo` 删除始终无有效值的 `constant/groupId` 和与 `imperative` 重复的 `required`，保留预编译读取的 `value/defaultValue/dataType/imperative` |
