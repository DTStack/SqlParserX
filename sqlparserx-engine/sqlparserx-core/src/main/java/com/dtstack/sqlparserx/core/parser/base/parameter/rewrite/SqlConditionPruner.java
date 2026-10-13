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

import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparserx.core.parser.base.parameter.support.ParameterAstUtils;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description 缺省可选参数SQL条件剪枝器
 * @author changlu
 * @date 2026/10/12 15:30
 */
public class SqlConditionPruner {
    public SQLExpr prune(SQLExpr expression, Map<String, Object> values, Map<String, ParamInfo> definitions) {
        if (expression == null)
            return null;
        if (expression instanceof SQLBinaryOpExpr
                && ParameterAstUtils.isLogical(((SQLBinaryOpExpr) expression).getOperator())) {
            SQLBinaryOpExpr binary = (SQLBinaryOpExpr) expression;
            SQLExpr left = prune(binary.getLeft(), values, definitions);
            SQLExpr right = prune(binary.getRight(), values, definitions);
            if (left == null)
                return right;
            if (right == null)
                return left;
            binary.setLeft(left);
            binary.setRight(right);
            return binary;
        }
        for (String name : variableNames(expression))
            if (ParameterAstUtils.value(values, definitions, name) == null) {
                ParamInfo definition = definitions.get(name);
                if (definition != null && definition.isImperative())
                    definition.hasValue();
                return null;
            }
        return expression;
    }

    private List<String> variableNames(SQLExpr expression) {
        final List<String> names = new ArrayList<>();
        expression.accept(new SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLVariantRefExpr x) {
                if (ParameterAstUtils.isVariable(x))
                    names.add(ParameterAstUtils.variableName(x));
                return true;
            }
        });
        return names;
    }
}
