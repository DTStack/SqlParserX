# 变量参数解析与预编译（纯 Druid）实现方案

> 状态：实现方案（待评审）
> 能力入口：`ISqlParser#parseParameterSql` / `ISqlParser#buildSqlByPrepareCompile`
> 底座：**仅 Druid 1.2.28**（`sqlparserx-syntax` 已提供 `String → SQLStatement`）
> 关联文档：[变量参数解析与预编译支持情况](../../能力支持情况/通用解析能力/变量参数解析与预编译支持情况.md)
> 说明：本文合并「老实现（SQLParser）分析」与「纯 Druid 落地方案」，作为该能力的唯一设计文档。

---

## 一、背景与目标

「SQL 转 API」场景下，用户手写带 `${变量}` 的 SQL，平台需要：

- **配置期**：解析出有哪些 `${变量}`、绑定哪个字段、操作符、是否必填，以及 `SELECT` 输出字段 → `parseParameterSql`；
- **运行期**：用请求值把 `${变量}` 改写为 JDBC `?`，并按顺序给出绑定值 → `buildSqlByPrepareCompile`。

老项目 `SQLParser` 用 **Calcite 为主 + Druid 兜底** 实现；本项目重构为 **纯 Druid 实现**。本文先梳理老实现关键机制，再给出 Druid-only 落地方案。

```text
配置期：SQL 里的 ${变量}  ->  解析成 API 入参配置（变量名/绑定字段/操作符/是否必填）
运行期：调用方传入参数值   ->  ${变量} 改写成 ? 并按序绑定执行  ->  返回标准结果
```

---

## 二、老实现（SQLParser）关键梳理

### 2.1 双模式入口

老入口 `ISqlParserClient#parseApiSql(..., prepareCompile, priorityCalcite)`，`prepareCompile` 是同一方法内的开关：

| 模式 | 核心产物 | 说明 |
|---|---|---|
| `false` | `inputParams` / `outputParams` | 只分析，不生成可执行 SQL |
| `true` | `beautifulSql` + `orderedParam`（并保留 `inputParams`） | 在 `false` 基础上继续改写 |

> 本项目已将其**拆成两个语义清晰的方法**：`parseParameterSql`（对应 `false`）、`buildSqlByPrepareCompile`（对应 `true`）。

### 2.2 变量机制：`${name}` → `αριname`（等长替换）

老实现（`ApiParserImpl#handleApiSql`）：

- 正则 `\$\{([a-zA-Z_0-9]+)\}` 匹配变量；
- **单引号包裹的 `'${name}'` 视为字符串，不替换**；
- 把 `${name}` 替换为等长占位 `αριname`（`α`/`ρ`/`ι` 各 1 个 `char`，长度与原串一致），使 Druid/Calcite 能按标识符解析；
- 遍历时把变量按首现顺序记入 `ORDER_PARAM`；
- 产出时由 `restoreApiParamPart` 把 `αριname` 还原为 `${name}`。

**为什么等长**：老实现依赖 Calcite AST 的 `SqlPos.columnNumber/endColumnNumber` 去切片原文、按位置替换；等长保证替换后坐标不漂移。

### 2.3 输入 / 输出参数提取

- 输入：遍历 `WHERE/HAVING/ON` 条件、函数、`IN/NOT IN`、`ORDER BY`、`GROUP BY`、`LIMIT/OFFSET`、`CASE WHEN`、`INSERT VALUES/UPDATE SET`，产出 `ApiParamSpan`（`sqlPart`/`params`/`bindColumns`/`operator`/`mustFlag`/`isOutLayer`/`isConst`/`constValue`）。
- 输出：取主体 `SELECT` 列表，产出字段别名与绑定字段。
- 绑定字段类型：`fillColumnFiled` 按「表名+字段名」匹配 `tableColumnsMap`，**原样回填** `type/comment`（不做类型识别）。

### 2.4 预编译改写（`true`）要做的四件事

1. `${变量}` → `?`（普通变量一个 `?`）；
2. `IN/NOT IN` 集合值展开为多个 `?`；
3. 删除「非必填且未赋值」的参数条件（连带清理 `and/or`、括号）；
4. `ORDER BY / GROUP BY` 属于结构、不能 bind，直接内联值；`LIMIT/OFFSET` 按方言改写分页。

### 2.5 字段类别转换扩展（重点）

这是老实现里**参数值 → JDBC 绑定值**的核心扩展，位于 **Druid 链路**（`SQLBuilder.DataType`）：

```java
public interface DataType {
    Object getData(String dataType, Object value);
}
// SQLBuilder 子类各自实现（MySQL/Oracle/PostgreSQL/...）
```

- 调用点：`SQLParserUtil#doDealSQLVariantRefExpr`（普通变量）、`#doDealSQLInListExprExpr`（IN 集合逐元素）、`#doDealSQLVariantRefExprOfSelect`（select 变量）；
- 输入：`ParamInfo.dataType`（来自 `ApiParam.dataType`，缺省 `"String"`）；
- 输出：`Integer/Long/Float/Double/BigDecimal/Boolean/java.sql.Date/java.util.Date/Timestamp/String` 等真实绑定值；
- 规则：`null → null`；类型**前缀匹配**（如 `DECIMAL(10,2)` 命中 `DECIMAL`）；未知/异常回退 `String`；自定义类型 `OBJECT/CUSTOM` 走 `isNumeric ? DecimalFormat : String`；
- 方言差异：每种数据源一套类型分类表（详见第八节）。

> 结论：**这是必须移植的能力**，且天然可做成按数据源可扩展的组件（零 Calcite 依赖）。

### 2.6 去 Calcite 后必须重做的部分

老实现大量逻辑建立在 Calcite 的 `Node/SqlPos` 模型上。切到 Druid-only 后：

| 老能力 | Druid-only 替代 |
|---|---|
| Calcite AST + `SqlPos` 字符坐标 | Druid `SQLStatement` + **AST 结构改写 + `SQLUtils.toSQLString`**，不依赖坐标 |
| `replacePart`/`removeToken` 按位置替换 | 直接改 AST 节点（`setName("?")`、剪枝 `SQLBinaryOpExpr`） |
| `handleApiSql` 等长占位 | **不需要**：Druid 原生把 `${x}` 解析为 `SQLVariantRefExpr` |
| `prepareInSql`（Calcite 修 `in ${x}`） | Druid 原生解析 `in (${x})` |
| `computeExpression`（limit 算术表达式） | 一期不支持，二期轻量求值 |
| 输出参数绑定（Calcite `SelectSpecialParser`） | 复用本项目 `select` 引擎 / Druid 遍历 select 列表 |

> 老实现**已有一条纯 Druid 链路**（`parseApiSqlByDruid` → `SQLBuilder#buildCustomizedSqlFromDruid` → `SQLParserUtil#dealNullParam/dealPlaceHolder`），本方案即以它为蓝本补齐覆盖度。

---

## 三、纯 Druid 可行性（基于 Druid 1.2.28 实测）

**核心结论：纯 Druid 完全可行，且比老实现（Calcite + `αρι` 等长替换 + 字符坐标）简单得多。**

> Druid 1.2.28 **原生**把 `${name}` 解析为 `SQLVariantRefExpr`（name = `${name}`），绝大多数位置**无需任何预处理**。

### 3.1 解析实测

| 输入 SQL（片段） | Druid 解析结果 | 是否可用 |
|---|---|---|
| `WHERE tenant_id = ${tenantId}` | `SQLBinaryOpExpr`，右值 `SQLVariantRefExpr("${tenantId}")` | ✅ |
| `WHERE id in (${ids})` | `SQLInListExpr`，`targetList=[SQLVariantRefExpr]` | ✅ |
| `SELECT ${col} AS c` | select 项为 `SQLVariantRefExpr` | ✅ |
| `concat(name, ${suf})` | 函数参数为 `SQLVariantRefExpr` | ✅ |
| `LIMIT ${offset}, ${rows}` | `SQLLimit.offset/rowCount` 均为 `SQLVariantRefExpr` | ✅ |
| `ORDER BY ${col}` / `ORDER BY ${col} asc` | `SQLSelectOrderByItem.expr` 为 `SQLVariantRefExpr` | ✅ |
| `INSERT INTO t(...) VALUES (${id}, ${name})` | values 中为 `SQLVariantRefExpr` | ✅ |
| `WHERE name = '${x}'` | 右值是 `SQLCharExpr`（**字符串字面量，非变量**） | ✅ 天然排除 |
| `ORDER BY ${col} ${dir}` | **解析失败**（方向位 VARIANT 非法） | ⚠️ 需兜底 |
| `LIMIT ${a}*10` | **解析失败**（illegal variable） | ⚠️ 需兜底 |

### 3.2 改写能力实测

| 操作 | 手法 | 结果 |
|---|---|---|
| 变量 → `?` | `SQLVariantRefExpr.setName("?")` | `tenant_id = ?` |
| IN 展开 | `SQLInListExpr.setTargetList([? × N])` | `id IN (?, ?, ?)` |
| 条件删除 | 结构剪枝 `SQLBinaryOpExpr`（`setWhere(right)` 等） | `a=${x} AND b=${y}` → `b=${y}` |
| ORDER BY 内联 | `SQLSelectOrderByItem.setExpr(new SQLIdentifierExpr(value))` | `ORDER BY create_time ASC` |
| LIMIT → `?` | `SQLLimit.offset/rowCount.setName("?")` | `LIMIT ?, ?` |

### 3.3 结论

以 **Druid 原生解析为主，AST 结构改写 + `SQLUtils.toSQLString` 渲染**，仅在「ORDER BY 方向变量 / LIMIT 算术表达式」两个非法位置做轻量兜底。**不需要** `αρι` 等长替换、**不需要**字符坐标。

---

## 四、可复用组件（优先复用，不重复造轮子）

### 4.1 Druid 内置（直接复用）

| 环节 | Druid 组件 | 说明 |
|---|---|---|
| 解析 | `SQLParserUtils.createSQLStatementParser(sql, DbType)` | 已被 `DruidSqlSyntaxParser` 封装 |
| 遍历 | `com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter` | 访问者模式，无侵入 |
| 变量节点 | `com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr` | `${x}` 的载体，`getName()` 原样保留 |
| 表达式节点 | `SQLBinaryOpExpr` / `SQLInListExpr` / `SQLMethodInvokeExpr` / `SQLCaseExpr` / `SQLAggregateExpr` | 条件/IN/函数/CASE |
| 结构节点 | `SQLSelectQueryBlock` / `SQLSelectItem` / `SQLOrderBy` / `SQLSelectOrderByItem` / `SQLLimit` / `SQLSelectGroupByClause` | SELECT/ORDER/LIMIT/GROUP |
| 渲染 | `com.alibaba.druid.sql.SQLUtils#toSQLString(SQLObject, DbType)` | AST → SQL，方言感知 |
| 字面量 | `SQLIdentifierExpr` / `SQLCharExpr` / `SQLIntegerExpr` | 内联值/字符串 |

### 4.2 本项目现有（直接复用）

| 环节 | 现有组件 |
|---|---|
| `String → SQLStatement` | `sqlparserx-syntax/DruidSqlSyntaxParser` + `BaseSqlParser#createStatement` |
| 两个核心方法入口 | `ISqlParser` / `BaseSqlParser`（已转交 Client） |
| 扩展点 | `IParseParameterClient` / `BaseParseParameterClient` / `ISqlParser#getParseParameterClient` |
| 输出/绑定字段 | `select` 引擎 `SelectColumnSpecialLineageService` / `SelectColumnItem` |
| 字段/字面量标记 | `ColumnTypeEnum`（`BELONGS_TO_FIELD` / `LITERAL_VALUE`） |
| 数据模型 | `ApiParamSpan` / `DataTypeColumn` / `ParamInfo` / `ParseParameterResult` / `PrepareCompileResult` |
| 方言清单 | `SqlParserDataSourceType`（15 种，含 `DbType`） |

### 4.3 可从老项目 Druid 链**直接移植**（本就无 Calcite 依赖）

| 环节 | 老实现类/方法 | 处理 |
|---|---|---|
| 字段类别转换 | `SQLBuilder.DataType#getData` + 各方言 `getDataTypeObj` | 直接移植为 `ParamDataTypeTransfer` |
| 变量改写蓝本 | `SQLParserUtil#doDealSQLVariantRefExpr` / `#doDealSQLInListExprExpr` | 移植思路（本项目改为遍历即产出有序值） |
| 变量收集蓝本 | `SQLParserUtil#dealNullParam` / `#dealWhereParam` / `#dealOrderByParam` | 移植为 `PrepareSqlRewriter` |
| 输入参数遍历蓝本 | `DruidParserUtil#parseWhereItems` / `#handleLimitInputParams` | 移植为 `InputParamVisitor` |
| DTO 映射 | `TransferUtil#getApiSpanParam` / `#getInParams` / `#getColumns` | 移植为模型装配 |

### 4.4 方言分页矩阵（直接移植）

老实现按数据源分组（`outputlimitDataType` / `outputOffsetFetchDataType` / `outputLimitOffsetDataType` / `outputOffsetLimitDataType`），可直接移植为 `PageSqlRewriter` 的方言策略。

---

## 五、整体架构（纯 Druid）

```text
调用方
 └─ ISqlParser#parseParameterSql / buildSqlByPrepareCompile        (String 入口)
      └─ BaseSqlParser → createStatement(sql)                     (syntax 层 Druid)
           └─ BaseParseParameterClient                            (业务编排)
                ├─ InputParamVisitor      (SQLASTVisitorAdapter；识别 SQLVariantRefExpr 等)
                ├─ OutputParamVisitor     (复用 select 引擎)
                ├─ ParamMetadataResolver  (按表名+字段名回填 DataTypeColumn)
                ├─ PrepareSqlRewriter     (变量→? / IN 展开 / order·group 内联 / limit)
                │    ├─ SqlConditionPruner(结构剪枝)
                │    └─ PageSqlRewriter   (方言分页)
                ├─ ParamDataTypeTransfer  (字段类别转换，按方言)
                ├─ PreprocessFallback     (仅 ORDER BY 方向 / LIMIT 算术兜底)
                └─ Renderer              (SQLUtils.toSQLString)
```

- 变量识别**不需要** `αρι` 等长替换、**不需要**字符坐标：直接认 `SQLVariantRefExpr` 且 `name` 形如 `${...}`。
- 结果为「AST 重新渲染」，SQL 大小写/换行可能变化（可接受）；`?` 与绑定值顺序严格对应。

### 5.1 核心数据模型（沿用现有 VO/Domain）

```text
ParseParameterResult
├── inputParams   List<ParamInfo>      // 入参定义
├── outputParams  List<ApiParamSpan>   // 出参定义
└── tables        List<Table>

PrepareCompileResult
├── beautifulSql  String               // ${变量} 已改写为 ?
└── inputFields   List<Object>         // 与每个 ? 一一对应的有序绑定值

DataTypeColumn { name, type, comment }        // 绑定字段的方言原始类型
ApiParamSpan   { sqlPart, alias, bindColumns, params, operator, mustFlag, isOutLayer, isConst, constValue }
ParamInfo      { name, value, imperative, dataType, defaultValue, constant, required, desc, fieldName, operator, groupId }
```

> `PrepareCompileResult.inputFields` 保持「有序绑定值」。若需保留每个 `?` 的变量名/类型（老实现 `orderedParam: List<ApiParam>`），可新增 `List<ParamInfo> orderedParams` 辅助字段，二者按下标对应。

---

## 六、核心方法一：`parseParameterSql`（配置期）

```java
ParseParameterResult parseParameterSql(SQLStatement stmt,
        Boolean isIgnoreSyntaxCheck,
        Map<String, List<DataTypeColumn>> tableColumnsMap);
```

### 6.1 流程

```text
1. [可选] PreprocessFallback：仅处理 ORDER BY 方向变量 / LIMIT 算术（默认透明）
2. InputParamVisitor 遍历 stmt，收集变量所在的条件/表达式
3. 组装 ApiParamSpan -> inputParams（operator / params / mustFlag / isOutLayer / isConst / constValue）
4. ParamMetadataResolver 回填 bindColumns 的 type/comment
5. OutputParamVisitor 解析 SELECT 列表 -> outputParams
6. 组装 ParseParameterResult(inputParams, outputParams, tables)
```

### 6.2 变量识别（复用 Druid 原生行为）

```java
private static boolean isApiVariable(SQLExpr e) {
    if (!(e instanceof SQLVariantRefExpr)) return false;   // '${x}' 已是 SQLCharExpr，天然排除
    String n = ((SQLVariantRefExpr) e).getName();
    return n != null && n.startsWith("${") && n.endsWith("}");
}
private static String varName(SQLExpr e) {                 // ${tenantId} -> tenantId
    String n = ((SQLVariantRefExpr) e).getName();
    return n.substring(2, n.length() - 1);
}
```

### 6.3 输入参数提取（节点矩阵）

| 结构 | Druid 节点 | 产出 |
|---|---|---|
| 比较条件 | `SQLBinaryOpExpr`（`getOperator()`） | operator=`=`/`>`/…、字段、变量 |
| `IN/NOT IN` | `SQLInListExpr`（`isNot()`、`getTargetList()`） | operator=`in`/`not in` |
| `LIKE` | `SQLBinaryOpExpr` | operator=`like` |
| 函数参数 | `SQLMethodInvokeExpr` | 函数内变量 |
| `ORDER BY` | `SQLOrderBy`/`SQLSelectOrderByItem` | operator=`CUSTOM` |
| `GROUP BY` | `SQLSelectGroupByClause` | 分组变量 |
| `LIMIT/OFFSET` | `SQLLimit`（`getOffset/getRowCount`） | `isOutLayer/isConst/constValue` |
| `CASE WHEN` | `SQLCaseExpr` | `mustFlag=true` |
| `INSERT VALUES / UPDATE SET` | `SQLInsertStatement`/`SQLUpdateStatement` | `mustFlag=true` |

### 6.4 代码骨架

```java
final List<ApiParamSpan> inputs = new ArrayList<>();
stmt.accept(new SQLASTVisitorAdapter() {
    @Override public boolean visit(SQLBinaryOpExpr x) {
        if (isApiVariable(x.getRight()) && x.getLeft() instanceof SQLIdentifierExpr) {
            ApiParamSpan s = new ApiParamSpan();
            s.setSqlPart(SQLUtils.toSQLString(x, dbType));
            s.setOperator(x.getOperator().getName().toLowerCase());
            s.setParams(Collections.singletonList(varName(x.getRight())));
            s.setBindColumns(Collections.singletonList(
                    new DataTypeColumn(((SQLIdentifierExpr) x.getLeft()).getName(), null, null)));
            inputs.add(s);
        }
        return true;
    }
    @Override public boolean visit(SQLInListExpr x) {
        // targetList 中收集变量；operator = isNot ? "not in" : "in"
        return true;
    }
    // ... SQLLimit / SQLSelectOrderByItem / SQLMethodInvokeExpr 等
});
```

### 6.5 输出字段与元数据

- 输出字段：复用 `select` 引擎（`SelectColumnSpecialLineageService`）解析 `SELECT` 列表的真实字段归属，产出 `outputParams`（`ApiParamSpan`）。
- 元数据回填：`ParamMetadataResolver` 按「表名+字段名」匹配 `tableColumnsMap`，命中则覆盖 `DataTypeColumn.type/comment`；未命中按字段名（忽略大小写）兜底；`type` 保持方言原始字符串。

### 6.6 LIMIT / 分页

- 记录最外层 `LIMIT` 是否常量、常量值（`isOutLayer/isConst/constValue`），供上层做分页上限校验；
- 子查询内 `LIMIT` 不作为最外层分页判定。

---

## 七、核心方法二：`buildSqlByPrepareCompile`（运行期）

```java
PrepareCompileResult buildSqlByPrepareCompile(SQLStatement stmt,
        Map<String, Object> inParams,
        Map<String, ParamInfo> columns);
```

### 7.1 流程

```text
1. [可选] PreprocessFallback（同上）
2. PrepareSqlRewriter 递归遍历改写（一次遍历同时产出有序绑定值）：
     a. 变量 -> ?            (SQLVariantRefExpr.setName("?"))
     b. IN/NOT IN 展开        (SQLInListExpr.setTargetList([? × N]))
     c. 未赋值非必填条件       (SqlConditionPruner 结构剪枝)
     d. ORDER BY/GROUP BY 内联 (setExpr(new SQLIdentifierExpr(value)))
     e. LIMIT/OFFSET          (PageSqlRewriter 方言改写)
3. beautifulSql = SQLUtils.toSQLString(stmt, dbType)
4. return new PrepareCompileResult(beautifulSql, inputFields)
```

### 7.2 变量 → `?`（含有序值产出）

```java
Object value = inParams.get(name);
if (value == null && !required) { prune(); }        // 见 7.4
else if (value == null && required) { throw ...; }
else {
    Object bind = transfer.transfer(columns.get(name).getDataType(), value);
    ref.setName("?");                                // AST 改写
    inputFields.add(bind);                           // 与 ? 顺序一一对应
}
```

### 7.3 `IN/NOT IN` 展开

```java
List<Object> vals = toList(value);                   // List 直接用；逗号串拆分
List<SQLExpr> targets = new ArrayList<>();
for (Object v : vals) {
    targets.add(new SQLVariantRefExpr("?"));
    inputFields.add(transfer.transfer(dataType, v)); // 逐元素转换，顺序即 ? 顺序
}
inExpr.setTargetList(targets);                       // id IN (?, ?, ?)
```

### 7.4 未赋值非必填条件删除（结构剪枝）

在 `SQLBinaryOpExpr` 树上摘除「变量无值且非必填」的条件，并整理父节点：

```text
- AND 一侧为空 -> 上提另一侧
- OR  一侧为空 -> 上提另一侧（谨慎，保持语义）
- WHERE 无剩余条件 -> setWhere(null)
- 括号/嵌套：递归至根，避免悬空 and/or
```

> 相比老实现的文本删除（`deleteSingleCondition` + 正则清理），AST 剪枝更稳，且完全不依赖字符坐标。

### 7.5 `ORDER BY / GROUP BY` 内联

```java
// ORDER BY ${col} ASC  ->  ORDER BY create_time ASC
item.setExpr(new SQLIdentifierExpr(String.valueOf(value)));
// GROUP BY ${x}        ->  写入实际值
```

- 这些位置不能使用 `?`（会改变语义），值内联；对应变量不计入 `inputFields`。

### 7.6 `LIMIT / OFFSET` 分页（方言）

- `SQLLimit.getOffset()/getRowCount()` 是变量则 `setName("?")` 并把值入 `inputFields`；是常量则写字面量。
- 方言差异（直接移植老实现分组）：
  - `LIMIT offset, rowCount`（MySQL 系）；
  - `LIMIT rowCount OFFSET offset`（PostgreSQL 系）；
  - `OFFSET ... FETCH FIRST ... ROWS ONLY`（Oracle/DB2）；
  - `OFFSET ... LIMIT ...`（Trino）。
- 注意两个 `?`（offset/rowCount）与方言顺序的对应关系。

### 7.7 渲染与有序参数保证

```java
String beautifulSql = SQLUtils.toSQLString(stmt, dbType);
```

`inputFields` 在**同一次遍历**中按写入 `?` 的顺序产出，天然满足：

```text
beautifulSql 中第 N 个 ?  <->  inputFields 第 N 个值
```

无需老实现 `orderDruidParamsBySql` 的事后按值回配（对重复值/IN 展开不可靠）。

---

## 八、字段类别转换：`ParamDataTypeTransfer`（直接移植）

老实现 `SQLBuilder.DataType#getData(dataType, value)` 本就零 Calcite 依赖，**直接移植**并按数据源拆分为组件：

```java
public interface ParamDataTypeTransfer {
    Object transfer(String dataType, Object value);   // 方言原始类型 -> JDBC 绑定值
    DbType dbType();
}
```

- 选择：`ParamDataTypeTransferRegistry.get(SqlParserDataSourceType)`；
- 默认：`BaseParamDataTypeTransfer`；
- 规则：`null → null`；类型 `trim + upperCase` + **前缀匹配**（`DECIMAL(10,2)` 命中 `DECIMAL`）；未知回退 `String`；异常回退 `value.toString()`；
- 与 `inParams`/`columns` 的配合：

```text
columns:  Map<String, ParamInfo>    // name -> { dataType, required, ... }
inParams: Map<String, Object>       // name -> 请求原始值
transfer(paramInfo.dataType, inParams.get(name)) -> 绑定值
```

- 方言实现矩阵：

| 方言 | 整数 | 浮点/精确 | 布尔 | 时间 | 字符串 |
|---|---|---|---|---|---|
| MySQL | TINYINT/SMALLINT/INT/BIGINT → `Integer` | FLOAT→`Float`、DOUBLE→`Double`、DECIMAL→`BigDecimal` | BIT/BOOL→`Integer` | DATETIME/DATE/TIMESTAMP→`Date` | CHAR/VARCHAR/TEXT→`String` |
| Oracle | INTEGER→`Integer` | NUMBER→`BigDecimal`、BINARY_FLOAT→`Float`、BINARY_DOUBLE→`Double` | — | DATE/TIMESTAMP→`Date` | CHAR/VARCHAR/VARCHAR2→`String` |
| PostgreSQL | INT2/INT4/INTEGER→`Integer`、INT8/BIGINT→`Long` | FLOAT4/FLOAT8/NUMERIC/DECIMAL→`Double` | BOOLEAN→`Boolean` | TIMESTAMP→`Timestamp`，其他→`Date` | 各类文本→`String` |
| Hive/Spark/Doris/StarRocks | TINYINT/SMALLINT/INT→`Integer`、BIGINT→`Long` | FLOAT→`Float`、DOUBLE→`Double`、DECIMAL→`BigDecimal` | BOOLEAN→`Boolean` | DATE/TIMESTAMP→`Date` | STRING/VARCHAR/CHAR→`String` |
| Trino | 整数族→`Integer/Long` | FLOAT→`Float`、DOUBLE→`Double`、DECIMAL→`BigDecimal` | BOOLEAN→`Boolean` | TIMESTAMP→`Timestamp` | VARCHAR→`String` |
| ClickHouse | 有符号整数→`Integer/Long` | FLOAT32/64→`Float/Double`、DECIMAL→`BigDecimal` | — | DATE/DATETIME→`Date` | String/FixedString→`String` |

> Base 提供通用分类，方言仅覆盖差异项；具体清单实现时补齐。

---

## 九、兜底预处理（仅少数非法位置）

只有两个位置 Druid 原生无法解析，`PreprocessFallback` 做最小处理：

| 位置 | 现象 | 兜底 |
|---|---|---|
| `ORDER BY ${col} ${dir}` | 方向位 `${dir}` 解析失败 | 解析前把方向变量替换为合法占位，改写在 AST 外按变量处理 |
| `LIMIT ${a}*10` | `illegal variable`（变量参与算术） | 一期不支持；二期用轻量表达式求值（老实现用 JS `ScriptEngine#computeExpression`） |

其余位置**零预处理**。`'${x}'` 无需处理（Druid 已是字符串字面量）。

---

## 十、端到端示例

### 10.1 配置期

输入：

```sql
SELECT id, name FROM user_info WHERE tenant_id = ${tenantId} AND status = ${status}
```

产出：

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

### 10.2 运行期

输入 `inParams={tenantId:1001, status:"OPEN"}`，`columns` 给出类型：

```json
{
  "beautifulSql": "SELECT id, name FROM user_info WHERE tenant_id = ? AND status = ?",
  "inputFields": [1001, "OPEN"]
}
```

`tenant_id = ?` ↔ `inputFields[0]`，`status = ?` ↔ `inputFields[1]`。

### 10.3 关键流程时序

```text
配置期：
Caller -> ISqlParser.parseParameterSql(sql, ...)
  -> BaseSqlParser.createStatement(sql)                 (syntax 层 Druid)
  -> BaseParseParameterClient.parseParameterSql(stmt, ...)
       -> InputParamVisitor.visit(stmt)                 (inputParams)
       -> OutputParamVisitor.visit(stmt)                (outputParams)
       -> ParamMetadataResolver.resolve(bindColumns)    (类型回填)
       -> 组装 ParseParameterResult 返回

运行期：
Caller -> ISqlParser.buildSqlByPrepareCompile(sql, inParams, columns)
  -> BaseSqlParser.createStatement(sql)
  -> BaseParseParameterClient.buildSqlByPrepareCompile(stmt, inParams, columns)
       -> PrepareSqlRewriter.rewrite(stmt, inParams, columns)
            -> 变量 -> ?            (ParamDataTypeTransfer.transfer -> inputFields)
            -> IN/NOT IN 展开        -> 多个 ?
            -> SqlConditionPruner    删除未赋值非必填条件
            -> ORDER BY/GROUP BY 内联
            -> PageSqlRewriter       方言分页
       -> ParamSqlRenderer.render(stmt, dbType)         (beautifulSql)
       -> 组装 PrepareCompileResult(beautifulSql, inputFields) 返回
```

---

## 十一、类与包结构

```text
core/parser/base/parameter/
├── BaseParseParameterClient          (实现两个核心方法)
├── domain/
│   ├── ApiParamSpan                  (已有)
│   ├── DataTypeColumn                (已有)
│   └── ParamInfo                     (已有)
├── vo/
│   ├── ParseParameterResult          (已有)
│   └── PrepareCompileResult          (已有)
├── visitor/
│   ├── InputParamVisitor              (SQLASTVisitorAdapter)
│   └── OutputParamVisitor             (复用 select 引擎)
├── resolve/
│   └── ParamMetadataResolver
├── rewrite/
│   ├── PrepareSqlRewriter
│   ├── SqlConditionPruner
│   └── PageSqlRewriter
├── transfer/
│   ├── ParamDataTypeTransfer
│   ├── BaseParamDataTypeTransfer
│   ├── ParamDataTypeTransferRegistry
│   └── (Mysql|Oracle|Postgresql|...)ParamDataTypeTransfer
├── preprocess/
│   └── PreprocessFallback             (ORDER BY 方向 / LIMIT 算术)
└── render/
    └── ParamSqlRenderer               (SQLUtils.toSQLString)
```

方言侧（按需）：`core/parser/mysql/MysqlParseParameterClient` 等。

---

## 十二、边界与风险

| 项 | 说明 | 处置 |
|---|---|---|
| 变量语法 | 仅 `${name}`（`[a-zA-Z_0-9]+`） | 正则约束；不支持 `${bdp.system.bizdate}` |
| 引号内变量 | `'${x}'` | Druid 天然识别为 `SQLCharExpr`，无需处理 |
| SQL 重新渲染 | 大小写/换行可能变化 | 已声明可接受；顺序不受影响 |
| ORDER BY 方向变量 | 原生解析失败 | `PreprocessFallback` 兜底 |
| LIMIT 算术 | 原生解析失败 | 一期不支持，二期表达式求值 |
| 条件剪枝 | AND/OR/括号/空 WHERE | `SqlConditionPruner` 统一处理 + 用例覆盖 |
| 重复变量 | 同一变量多次出现 | 每次出现各产生一个 `?`，值同源 |
| 类型转换 | 方言类型繁多 | 前缀匹配 + 未知回退 `String` + 方言覆盖 |
| `toSQLString` 性能 | 大 SQL 渲染 | 视为普通字符串生成，可接受 |

---

## 十三、分期落地

| 阶段 | 内容 | 重点复用 |
|---|---|---|
| P0 | `parseParameterSql`：Visitor + 元数据回填 + 输出字段 | Druid `SQLASTVisitorAdapter`、`select` 引擎 |
| P1 | `buildSqlByPrepareCompile`：变量→`?` + 有序值 + 渲染 | `SQLUtils.toSQLString` |
| P2 | IN 展开、条件剪枝 | AST `setTargetList` / `SQLBinaryOpExpr` 剪枝 |
| P3 | ORDER BY/GROUP BY 内联、LIMIT 方言分页 | 移植老实现分页矩阵 |
| P4 | `ParamDataTypeTransfer` 各方言实现 | 直接移植 `SQLBuilder.DataType` |
| P5 | `PreprocessFallback`（方向/算术）+ 方言 Client + 回归 | — |

---

## 十四、测试建议

- 基础：单变量、多变量、重复变量、必填缺值、`'${x}'` 字符串、无变量；
- 条件：`=`/`IN`/`NOT IN`/`LIKE`、未赋值非必填条件删除、`AND/OR` 混合、括号；
- 结构：`ORDER BY`（含 asc/desc 变量）、`GROUP BY`、`LIMIT/OFFSET` 变量、最外层常量 `LIMIT`；
- 输出：别名、`*` 展开（依赖 `tableColumnsMap`）、函数列、`CASE WHEN` 列；
- 类型：各数据源典型类型经 `ParamDataTypeTransfer` 转换；
- 一致性断言：`beautifulSql` 中第 N 个 `?` ↔ `inputFields` 第 N 个值。

---

## 十五、变更记录

| 日期 | 变更 |
|---|---|
| 2026-10-10 | 初版：合并「老实现分析」与「纯 Druid 实现方案」；基于 Druid 1.2.28 实测（`${x}` → `SQLVariantRefExpr`），给出实现蓝本、可复用组件与分期计划 |
