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
package com.dtstack.sqlparserx.core.parser.base.parameter.rewrite;

import com.dtstack.sqlparserx.core.config.Constant;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparserx.core.parser.base.parameter.support.ParameterAstUtils;
import com.dtstack.sqlparserx.core.parser.base.parameter.transfer.ParamDataTypeTransfer;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import java.util.*;

/**
 * @description 变量参数预编译SQL改写器
 * @author changlu
 * @date 2026/10/12 15:30
 */
public class PrepareSqlRewriter {
    private final DbType dbType;
    private final ParamDataTypeTransfer transfer;
    private final SqlConditionPruner conditionPruner;

    public PrepareSqlRewriter(DbType dbType, ParamDataTypeTransfer transfer) {
        this.dbType = dbType;
        this.transfer = transfer;
        this.conditionPruner = new SqlConditionPruner();
    }

    public PrepareCompileResult rewrite(SQLStatement statement, Map<String, Object> input,
            Map<String, ParamInfo> columns) {
        // 0) 预编译改写仅面向「SQL 转 API」的 SELECT 查询
        if (!(statement instanceof SQLSelectStatement))
            throw new IllegalArgumentException("预编译改写仅支持 SELECT 查询: " + statement.getClass().getSimpleName());

        // 1) 克隆 AST，避免改写调用方传入的语法树；规整入参值与参数定义
        final SQLStatement sql = statement.clone();
        final Map<String, Object> values = input == null ? Collections.emptyMap() : input;
        final Map<String, ParamInfo> definitions = columns == null ? Collections.emptyMap() : columns;

        // 2) 条件剪枝：可选条件缺值则从 WHERE / HAVING 中移除；必填缺值则触发异常
        sql.accept(new SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLSelectQueryBlock x) {
                x.setWhere(conditionPruner.prune(x.getWhere(), values, definitions));
                if (x.getGroupBy() != null)
                    x.getGroupBy().setHaving(
                            conditionPruner.prune(x.getGroupBy().getHaving(), values, definitions));
                return true;
            }
        });

        // 3) 变量改写：IN 展开、ORDER BY/GROUP BY 内联、其余 ${变量} 改为占位符并记录绑定值
        final IdentityHashMap<SQLVariantRefExpr, Object> pairs = new IdentityHashMap<>();
        sql.accept(new SQLASTVisitorAdapter() {
            // 3.1) IN / NOT IN：变量值按集合展开为多个占位符
            @Override
            public boolean visit(SQLInListExpr x) {
                List<SQLExpr> targets = new ArrayList<>();
                for (SQLExpr target : x.getTargetList()) {
                    if (!ParameterAstUtils.isVariable(target)) {
                        targets.add(target);
                        continue;
                    }
                    String name = ParameterAstUtils.variableName(target);
                    for (Object value : ParameterAstUtils.asList(ParameterAstUtils.value(values, definitions, name))) {
                        SQLVariantRefExpr marker = new SQLVariantRefExpr(Constant.PREPARED_PLACEHOLDER);
                        pairs.put(marker, transfer.transfer(type(definitions, name), value));
                        targets.add(marker);
                    }
                }
                x.setTargetList(targets);
                return true;
            }
            // 3.2) ORDER BY：变量值直接内联为标识符，不产生占位符
            @Override
            public boolean visit(SQLSelectOrderByItem x) {
                if (ParameterAstUtils.isVariable(x.getExpr()))
                    x.setExpr(identifierValue(x.getExpr()));
                return true;
            }
            // 3.3) GROUP BY：变量值直接内联为标识符，不产生占位符
            @Override
            public boolean visit(SQLSelectGroupByClause x) {
                for (int i = 0; i < x.getItems().size(); i++)
                    if (ParameterAstUtils.isVariable(x.getItems().get(i)))
                        x.getItems().set(i, identifierValue(x.getItems().get(i)));
                return true;
            }
            private SQLIdentifierExpr identifierValue(SQLExpr expression) {
                String name = ParameterAstUtils.variableName(expression);
                return new SQLIdentifierExpr(String.valueOf(ParameterAstUtils.value(values, definitions, name)));
            }
            // 3.4) 其余 ${变量}：改为占位符，并按参数定义的字段类型转换绑定值
            @Override
            public boolean visit(SQLVariantRefExpr x) {
                if (ParameterAstUtils.isVariable(x)) {
                    String name = ParameterAstUtils.variableName(x);
                    pairs.put(x, transfer.transfer(type(definitions, name),
                            ParameterAstUtils.value(values, definitions, name)));
                    x.setName(Constant.PREPARED_PLACEHOLDER);
                }
                return true;
            }
        });

        // 4) 按改写后 AST 的遍历顺序收集绑定值，保证与最终渲染出的占位符顺序一致
        final List<Object> ordered = new ArrayList<>();
        sql.accept(new SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLVariantRefExpr x) {
                if (Constant.PREPARED_PLACEHOLDER.equals(x.getName()) && pairs.containsKey(x))
                    ordered.add(pairs.get(x));
                return true;
            }
        });

        // 5) 渲染预编译 SQL，返回「占位符 SQL + 有序绑定值」
        String rendered = dbType == null ? SQLUtils.toSQLString(sql) : SQLUtils.toSQLString(sql, dbType);
        return new PrepareCompileResult(ordered, rendered);
    }

    private String type(Map<String, ParamInfo> definitions, String name) {
        ParamInfo definition = definitions.get(name);
        return definition == null ? null : definition.getDataType();
    }
}
