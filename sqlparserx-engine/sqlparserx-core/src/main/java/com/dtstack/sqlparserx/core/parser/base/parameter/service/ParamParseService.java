/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.core.parser.base.parameter.service;

import com.dtstack.sqlparserx.core.enums.ParamOperatorEnum;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.*;
import com.dtstack.sqlparserx.core.parser.base.parameter.resolve.ParameterColumnResolver;
import com.dtstack.sqlparserx.core.parser.base.parameter.resolve.ResolvedParameterColumn;
import com.dtstack.sqlparserx.core.parser.base.parameter.support.ParameterAstUtils;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import java.util.*;

/**
 * @description 变量参数配置期解析服务，按照SQL节点类型递归分派
 * @author changlu
 * @date 2026/10/12 15:30
 */
public class ParamParseService {
    private final ParameterColumnResolver columnResolver = new ParameterColumnResolver();

    public ParseParameterResult parse(SQLStatement statement, Boolean isIgnoreSyntaxCheck,
            Map<String, List<DataTypeColumn>> metadata) {
        // 1) 初始化结果容器：入参按首次出现顺序（LinkedHashMap）、出参 span、表
        Map<String, ParamInfo> params = new LinkedHashMap<>();
        List<ApiParamSpan> outputs = new ArrayList<>();

        // 2) 「SQL 转 API」变量参数解析仅支持 SELECT 查询；其他语句在开启语法校验时抛异常，否则忽略
        if (statement instanceof SQLSelectStatement)
            parseSelectStatement((SQLSelectStatement) statement, params, outputs, metadata);
        else if (!Boolean.TRUE.equals(isIgnoreSyntaxCheck))
            throw new IllegalArgumentException("变量参数解析仅支持 SELECT 查询: " + statement.getClass().getSimpleName());

        // 3) 组装结果（tables 当前暂为空）
        return new ParseParameterResult(new ArrayList<>(params.values()), outputs, Collections.emptyList());
    }

    private void parseSelectStatement(SQLSelectStatement selectStatement, Map<String, ParamInfo> params,
            List<ApiParamSpan> outputs, Map<String, List<DataTypeColumn>> metadata) {
        walkSelect(selectStatement.getSelect(), params, outputs, metadata);
    }

    private void walkSelect(SQLSelect select, Map<String, ParamInfo> params, List<ApiParamSpan> outputs,
            Map<String, List<DataTypeColumn>> meta) {
        if (select == null)
            return;
        // 1) 先递归 WITH ... AS ... 中的每个 CTE 子查询。
        // CTE 内的变量仍写入同一个 params，但 CTE 投影列只是中间结果，不能写入最终 API outputs。
        if (select.getWithSubQuery() != null)
            for (SQLWithSubqueryClause.Entry e : select.getWithSubQuery().getEntries())
                walkSelect(e.getSubQuery(), params, new ArrayList<ApiParamSpan>(), meta);
        // 2) 再解析主体查询（query block 或 union）
        walkQuery(select.getQuery(), params, outputs, meta);
        // 3) SELECT 级别的 ORDER BY / LIMIT（简单查询在 query block 上，此处兼容极端形态）
        walkOrderBy(select.getOrderBy(), params, meta);
        walkLimit(select.getLimit(), params, meta);
    }
    private void walkQuery(SQLSelectQuery query, Map<String, ParamInfo> params, List<ApiParamSpan> outputs,
            Map<String, List<DataTypeColumn>> meta) {
        if (query instanceof SQLSelectQueryBlock)
            walkBlock((SQLSelectQueryBlock) query, params, outputs, meta);
        else if (query instanceof SQLUnionQuery) {
            SQLUnionQuery union = (SQLUnionQuery) query;
            // UNION 各分支输出列按位置形成同一套结果结构，因此最终 API 输出只取第一分支；
            // 后续分支仍递归解析变量，但使用临时 outputs，避免重复追加相同位置的输出列。
            for (int i = 0; i < union.getRelations().size(); i++)
                walkQuery(union.getRelations().get(i), params,
                        i == 0 ? outputs : new ArrayList<ApiParamSpan>(), meta);
            // union 级别的 ORDER BY / LIMIT 挂在 SQLUnionQuery 上
            walkOrderBy(union.getOrderBy(), params, meta);
            walkLimit(union.getLimit(), params, meta);
        }
    }
    private void walkOrderBy(SQLOrderBy orderBy, Map<String, ParamInfo> params,
            Map<String, List<DataTypeColumn>> meta) {
        if (orderBy != null)
            for (SQLSelectOrderByItem item : orderBy.getItems())
                walkExpr(item.getExpr(), params, meta);
    }
    private void walkLimit(SQLLimit limit, Map<String, ParamInfo> params, Map<String, List<DataTypeColumn>> meta) {
        if (limit != null) {
            walkExpr(limit.getOffset(), params, meta);
            walkExpr(limit.getRowCount(), params, meta);
        }
    }
    private void walkBlock(SQLSelectQueryBlock block, Map<String, ParamInfo> params, List<ApiParamSpan> outputs,
            Map<String, List<DataTypeColumn>> meta) {
        // 按 SQL 结构顺序递归：FROM -> WHERE -> GROUP BY/HAVING -> ORDER BY -> LIMIT -> SELECT 列表
        walkTable(block.getFrom(), params, outputs, meta);
        walkExpr(block.getWhere(), params, meta);
        if (block.getGroupBy() != null) {
            for (SQLExpr e : block.getGroupBy().getItems())
                walkExpr(e, params, meta);
            walkExpr(block.getGroupBy().getHaving(), params, meta);
        }
        walkOrderBy(block.getOrderBy(), params, meta);
        walkLimit(block.getLimit(), params, meta);
        for (SQLSelectItem item : block.getSelectList()) {
            if (ParameterAstUtils.isVariable(item.getExpr())) {
                ParameterSpan s = span(item, null, Collections.singletonList(item.getExpr()),
                        ParamOperatorEnum.SELECT_LIST.getSymbol(), true, meta);
                register(params, s);
            } else {
                ApiParamSpan out = new ApiParamSpan(item.getExpr().toString());
                out.setAlias(item.getAlias());
                out.setBindColumns(bind(item.getExpr(), meta));
                outputs.add(out);
                walkExpr(item.getExpr(), params, meta);
            }
        }
    }
    private void walkTable(SQLTableSource source, Map<String, ParamInfo> params, List<ApiParamSpan> outputs,
            Map<String, List<DataTypeColumn>> meta) {
        // 按表来源类型递归：派生表 / JOIN / UNION 子查询 / LATERAL VIEW。
        // FROM 内的查询只提供中间表，其投影列不属于当前最外层 API 输出，统一使用临时 outputs。
        if (source instanceof SQLSubqueryTableSource)
            walkSelect(((SQLSubqueryTableSource) source).getSelect(), params, new ArrayList<ApiParamSpan>(), meta);
        else if (source instanceof SQLJoinTableSource) {
            SQLJoinTableSource join = (SQLJoinTableSource) source;
            walkTable(join.getLeft(), params, outputs, meta);
            walkTable(join.getRight(), params, outputs, meta);
            walkExpr(join.getCondition(), params, meta);
        } else if (source instanceof SQLUnionQueryTableSource)
            walkQuery(((SQLUnionQueryTableSource) source).getUnion(), params, new ArrayList<ApiParamSpan>(), meta);
        else if (source instanceof SQLLateralViewTableSource)
            walkTable(((SQLLateralViewTableSource) source).getTableSource(), params, outputs, meta);
    }
    private void walkExpr(SQLExpr expr, Map<String, ParamInfo> params, Map<String, List<DataTypeColumn>> meta) {
        if (expr == null)
            return;
        // 1) 二元比较条件：右侧为变量则登记参数，并把左侧表达式作为待绑定的真实字段。
        // 例如 u.tenant_id = ${tenantId} 中：field=u.tenant_id、variable=tenantId；
        // 后续 span -> bind 会使用 field 的 owner/column 去 tableColumnsMap 查字段类型与注释。
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr b = (SQLBinaryOpExpr) expr;
            if (ParameterAstUtils.isVariable(b.getRight()) && !ParameterAstUtils.isLogical(b.getOperator())) {
                register(params, span(b, b.getLeft(), Collections.singletonList(b.getRight()), ParamOperatorEnum
                        .ofDruid(b.getOperator()).getSymbol(), false, meta));
                walkExpr(b.getLeft(), params, meta);
            } else {
                walkExpr(b.getLeft(), params, meta);
                walkExpr(b.getRight(), params, meta);
            }
            return;
        }
        // 2) IN / NOT IN：列表中的变量归到同一参数定义，左值为绑定字段
        if (expr instanceof SQLInListExpr) {
            SQLInListExpr in = (SQLInListExpr) expr;
            List<SQLExpr> vars = new ArrayList<>();
            for (SQLExpr target : in.getTargetList())
                if (ParameterAstUtils.isVariable(target))
                    vars.add(target);
            if (!vars.isEmpty())
                register(params, span(in, in.getExpr(), vars, in.isNot()
                        ? ParamOperatorEnum.NOT_IN.getSymbol()
                        : ParamOperatorEnum.IN.getSymbol(), false, meta));
            walkExpr(in.getExpr(), params, meta);
            for (SQLExpr target : in.getTargetList())
                if (!ParameterAstUtils.isVariable(target))
                    walkExpr(target, params, meta);
            return;
        }
        // 2.1) BETWEEN / NOT BETWEEN：区间两端变量归到同一参数定义，被比较值为绑定字段
        if (expr instanceof SQLBetweenExpr) {
            SQLBetweenExpr between = (SQLBetweenExpr) expr;
            List<SQLExpr> vars = new ArrayList<>();
            if (ParameterAstUtils.isVariable(between.getBeginExpr()))
                vars.add(between.getBeginExpr());
            if (ParameterAstUtils.isVariable(between.getEndExpr()))
                vars.add(between.getEndExpr());
            if (!vars.isEmpty())
                register(params, span(between, between.getTestExpr(), vars, (between.isNot()
                        ? ParamOperatorEnum.NOT_BETWEEN
                        : ParamOperatorEnum.BETWEEN).getSymbol(), false, meta));
            walkExpr(between.getTestExpr(), params, meta);
            if (!ParameterAstUtils.isVariable(between.getBeginExpr()))
                walkExpr(between.getBeginExpr(), params, meta);
            if (!ParameterAstUtils.isVariable(between.getEndExpr()))
                walkExpr(between.getEndExpr(), params, meta);
            return;
        }
        // 3) 子查询表达式：递归进入内部 select
        if (expr instanceof SQLQueryExpr) {
            walkSelect(((SQLQueryExpr) expr).getSubQuery(), params, new ArrayList<ApiParamSpan>(), meta);
            return;
        }
        // 4) EXISTS 子查询：递归进入内部 select
        if (expr instanceof SQLExistsExpr) {
            walkSelect(((SQLExistsExpr) expr).getSubQuery(), params, new ArrayList<ApiParamSpan>(), meta);
            return;
        }
        // 5) IN 子查询：左表达式与内部 select 分别递归
        if (expr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr inSubQuery = (SQLInSubQueryExpr) expr;
            walkExpr(inSubQuery.getExpr(), params, meta);
            walkSelect(inSubQuery.getSubQuery(), params, new ArrayList<ApiParamSpan>(), meta);
            return;
        }
        // 6) 变量本体：登记为 CUSTOM 参数
        if (ParameterAstUtils.isVariable(expr)) {
            register(params, span(expr, null, Collections.singletonList(expr), ParamOperatorEnum.CUSTOM.getSymbol(),
                    false, meta));
            return;
        }
        // 7) 其他复合表达式（函数/CASE 等）：Druid visitor 兜底收集其中变量
        walkChildren(expr, params, meta);
    }
    private void walkChildren(SQLObject object, Map<String, ParamInfo> params, Map<String, List<DataTypeColumn>> meta) {
        object.accept(new SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLVariantRefExpr variable) {
                if (ParameterAstUtils.isVariable(variable))
                    register(params, span(variable, null, Collections.singletonList(variable),
                            ParamOperatorEnum.CUSTOM.getSymbol(), false, meta));
                return true;
            }
        });
    }
    private ParameterSpan span(SQLObject part, SQLExpr field, List<SQLExpr> vars, String op, boolean must,
            Map<String, List<DataTypeColumn>> meta) {
        ParameterSpan s = new ParameterSpan(part.toString(), op, must);
        List<String> names = new ArrayList<>();
        for (SQLExpr v : vars)
            names.add(ParameterAstUtils.variableName(v));
        s.params = names;
        // 将变量所在条件的字段表达式绑定到外部传入的表字段元数据。
        // 绑定结果随后会由 register/applyBindInfo 写入 ParamInfo.fieldName/dataType/desc。
        s.bindColumns = bind(field, meta);
        return s;
    }
    private void register(Map<String, ParamInfo> result, ParameterSpan span) {
        for (String name : span.params) {
            ParamInfo existing = result.get(name);
            if (existing == null) {
                ParamInfo param = new ParamInfo(name);
                param.setImperative(span.must);
                param.setOperator(span.operator);
                param.setSqlPart(span.sqlPart);
                applyBindInfo(param, span);
                result.put(name, param);
            } else {
                // 同一变量多次出现：必填优先；首次缺失的字段绑定信息补齐，其余保留首次出现
                mergeParamInfo(existing, span);
            }
        }
    }

    private void mergeParamInfo(ParamInfo existing, ParameterSpan span) {
        if (span.must) {
            existing.setImperative(true);
        }
        if (existing.getSqlPart() == null)
            existing.setSqlPart(span.sqlPart);
        if (existing.getFieldName() == null && !span.bindColumns.isEmpty()) {
            DataTypeColumn column = span.bindColumns.get(0);
            existing.setFieldName(column.getName());
            if (existing.getDataType() == null)
                existing.setDataType(column.getType());
            if (existing.getDesc() == null)
                existing.setDesc(column.getComment());
        }
    }

    private void applyBindInfo(ParamInfo param, ParameterSpan span) {
        if (!span.bindColumns.isEmpty()) {
            DataTypeColumn column = span.bindColumns.get(0);
            param.setFieldName(column.getName());
            param.setDataType(column.getType());
            param.setDesc(column.getComment());
        }
    }

    /** 参数识别过程中的内部状态，不暴露到 ParseParameterResult.outputParams。 */
    private static class ParameterSpan {
        private final String sqlPart;
        private final String operator;
        private final boolean must;
        private List<String> params = Collections.emptyList();
        private List<DataTypeColumn> bindColumns = Collections.emptyList();

        private ParameterSpan(String sqlPart, String operator, boolean must) {
            this.sqlPart = sqlPart;
            this.operator = operator;
            this.must = must;
        }
    }
    private List<DataTypeColumn> bind(SQLExpr field, Map<String, List<DataTypeColumn>> meta) {
        if (field == null)
            return Collections.emptyList();

        // 参数字段绑定分成两步：
        // 1) 定位：基于字段所在 SQLSelectQueryBlock 的 FROM/JOIN 作用域，把 u.tenant_id
        //    确定性地解析为 user_info.tenant_id；
        // 2) 取元数据：只在解析出的真实表内查询 DataTypeColumn，并回填类型和注释。
        // 裸字段在当前作用域存在多个同名候选时，resolver 会返回 tableName=null；此时保留字段名，
        // 但不猜测 dataType/comment，避免类型结果随 tableColumnsMap 遍历顺序变化。
        ResolvedParameterColumn resolved = columnResolver.resolve(field, meta);
        if (resolved == null)
            return Collections.emptyList();
        DataTypeColumn hit = columnResolver.findColumn(resolved, meta);
        return Collections.singletonList(hit == null
                ? new DataTypeColumn(resolved.getColumnName(), null, null)
                : new DataTypeColumn(hit.getName(), hit.getType(), hit.getComment()));
    }
}
