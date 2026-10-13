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
package com.dtstack.sqlparserx.core.parser.base.parameter.support;

import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @description 变量参数AST处理工具类
 * @author changlu
 * @date 2026/10/12 15:30
 */
public final class ParameterAstUtils {
    private ParameterAstUtils() {
    }

    public static boolean isVariable(SQLExpr expr) {
        if (!(expr instanceof SQLVariantRefExpr))
            return false;
        String name = ((SQLVariantRefExpr) expr).getName();
        return name != null && name.matches("\\$\\{[a-zA-Z_0-9]+}");
    }

    public static String variableName(SQLExpr expr) {
        String name = ((SQLVariantRefExpr) expr).getName();
        return name.substring(2, name.length() - 1);
    }

    public static boolean isLogical(SQLBinaryOperator operator) {
        return operator == SQLBinaryOperator.BooleanAnd || operator == SQLBinaryOperator.BooleanOr;
    }

    public static Object value(Map<String, Object> values, Map<String, ParamInfo> definitions, String name) {
        if (values.containsKey(name))
            return values.get(name);
        ParamInfo definition = definitions.get(name);
        if (definition == null)
            return null;
        return definition.getValue() != null ? definition.getValue() : definition.getDefaultValue();
    }

    public static List<Object> asList(Object value) {
        if (value == null)
            return Collections.emptyList();
        List<Object> result = new ArrayList<>();
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value)
                result.add(item);
        } else if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++)
                result.add(Array.get(value, i));
        } else
            result.add(value);
        return result;
    }
}
