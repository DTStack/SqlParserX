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
package com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.builder;

import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.TableInfo;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BaseExprBuilder {

    public BaseExprBuilder() {
    }

    public SQLExpr buildCondition(RowLevelPermissionFilterParam param, TableInfo tableInfo,
            boolean paramNotNeedUseTable) {
        SQLExpr fieldExpr = createFieldExpression(param.getFieldName(), tableInfo, paramNotNeedUseTable);
        String dataType = param.getFieldType();

        if (param.getOperatorEnum() == null) {
            return buildFallbackCondition(fieldExpr, param, dataType);
        }
        return buildConditionByOperator(fieldExpr, param, dataType);
    }

    private SQLExpr buildConditionByOperator(SQLExpr fieldExpr,
            RowLevelPermissionFilterParam param,
            String dataType) {
        switch (param.getOperatorEnum()) {
            case IN :
            case NOT_IN :
                return buildInCondition(fieldExpr, param, dataType);
            case EQ :
            case NOT_EQ :
                return buildComparisonCondition(fieldExpr, param, dataType);
            case NULL :
                return createNullCondition(fieldExpr);
            default :
                return buildFallbackCondition(fieldExpr, param, dataType);
        }
    }

    private SQLExpr buildInCondition(SQLExpr fieldExpr,
            RowLevelPermissionFilterParam param,
            String dataType) {
        SQLInListExpr inExpr = new SQLInListExpr(fieldExpr);
        param.getFieldValue().forEach(value -> inExpr.getTargetList().add(createValueExpression(value, dataType)));
        inExpr.setNot(param.getOperatorEnum() == RowLevelPermissionFilterParam.OperatorEnum.NOT_IN);
        return inExpr;
    }

    private SQLExpr buildComparisonCondition(SQLExpr fieldExpr,
            RowLevelPermissionFilterParam param,
            String dataType) {
        return new SQLBinaryOpExpr(
                fieldExpr,
                getComparisonOperator(param.getOperatorEnum()),
                createValueExpression(param.getFieldValue().get(0), dataType));
    }

    private SQLExpr buildFallbackCondition(SQLExpr fieldExpr,
            RowLevelPermissionFilterParam param,
            String dataType) {
        List<Object> fieldValues = param.getFieldValue();
        if (fieldValues == null || fieldValues.isEmpty())
            return null;
        // 若是有一个元素默认就是eq，多个元素就是in
        if (fieldValues.size() == 1) {
            return new SQLBinaryOpExpr(
                    fieldExpr,
                    SQLBinaryOperator.Equality,
                    createValueExpression(param.getFieldValue().get(0), dataType));
        } else {
            SQLInListExpr inExpr = new SQLInListExpr(fieldExpr);
            fieldValues.forEach(value -> inExpr.getTargetList().add(createValueExpression(value, dataType)));
            return inExpr;
        }
    }

    private SQLBinaryOperator getComparisonOperator(RowLevelPermissionFilterParam.OperatorEnum operator) {
        return operator == RowLevelPermissionFilterParam.OperatorEnum.EQ
                ? SQLBinaryOperator.Equality
                : SQLBinaryOperator.NotEqual;
    }

    // 后续可扩展各个supportBuild方法 根据传递具体filed类型来判断创建expr
    private SQLExpr createValueExpression(Object value, String fieldType) {
        if (value == null)
            return new SQLNullExpr();

        if (fieldType == null) {
            return new SQLCharExpr(value.toString());
        }

        String upperFieldType = fieldType.toUpperCase();

        // 根据类型匹配去创建expr
        if (getSupportBuildNumberExprDataTypes().contains(upperFieldType)) {
            return createNumberExpr(value);
        }
        if (getSupportBuildBooleanExprDataTypes().contains(upperFieldType)) {
            return createBooleanExpr(value);
        }

        return new SQLCharExpr(value.toString());
    }

    private SQLExpr createNullCondition(SQLExpr fieldExpr) {
        return new SQLBinaryOpExpr(fieldExpr, SQLBinaryOperator.Is, new SQLNullExpr());
    }

    private SQLExpr createFieldExpression(String fieldName, TableInfo tableInfo, boolean paramNotNeedUseTable) {
        // 1、优先走别名
        String alias = tableInfo.getAlias();
        if (alias != null)
            return new SQLPropertyExpr(alias, fieldName);

        // 2、若是没有别名且当前一组select关联表只有一个时，直接使用属性，无需带table
        if (paramNotNeedUseTable)
            return new SQLIdentifierExpr(fieldName);

        // 3、若是无别名，有table名则带上table名
        String tableName = tableInfo.getTableName();
        if (tableName != null)
            return new SQLPropertyExpr(tableName, fieldName);

        // 4、无表、别名 不带任何属性
        return new SQLIdentifierExpr(fieldName);
    }

    protected List<String> getSupportBuildNumberExprDataTypes() {
        return Arrays.asList(
                "BIT", "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "BIGINT",
                "FLOAT", "DOUBLE", "DECIMAL");
    }

    protected List<String> getSupportBuildBooleanExprDataTypes() {
        return Collections.singletonList("BOOLEAN");
    }

    private SQLExpr createNumberExpr(Object value) {
        if (value instanceof Number) {
            return new SQLNumberExpr((Number) value);
        }
        try {
            return new SQLNumberExpr(Double.parseDouble(value.toString()));
        } catch (NumberFormatException e) {
            return new SQLCharExpr(value.toString());
        }
    }

    private SQLExpr createBooleanExpr(Object value) {
        if (value instanceof Boolean) {
            return new SQLBooleanExpr((Boolean) value);
        }
        String strValue = value.toString().toLowerCase();
        if ("true".equals(strValue) || "1".equals(strValue)) {
            return new SQLBooleanExpr(true);
        }
        if ("false".equals(strValue) || "0".equals(strValue)) {
            return new SQLBooleanExpr(false);
        }
        return new SQLCharExpr(value.toString());
    }
}