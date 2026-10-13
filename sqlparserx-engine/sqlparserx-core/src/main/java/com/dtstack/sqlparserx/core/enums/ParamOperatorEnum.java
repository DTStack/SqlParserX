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
package com.dtstack.sqlparserx.core.enums;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;

/**
 * @description 参数解析操作符枚举
 * @author changlu
 * @date 2026/10/12 15:20
 */
public enum ParamOperatorEnum {
    EQ("="), NE("<>"), GT(">"), GTE(">="), LT("<"), LTE("<="), IS_NULL("is null"), IS_NOT_NULL("is not null"), LIKE(
            "like"), NOT_LIKE("not like"), IN("in"), NOT_IN("not in"), BETWEEN("between"), NOT_BETWEEN(
                    "not between"), CUSTOM("CUSTOM"), CASE_WHEN("CASE_WHEN"), ORDER_BY_CASE_WHEN(
                            "ORDER_BY_CASE_WHEN"), SELECT_LIST("API_PARAM_IN_SELECT_LIST"), UNKNOWN("UNKNOWN");

    private final String symbol;

    ParamOperatorEnum(String symbol) {
        this.symbol = symbol;
    }
    public String getSymbol() {
        return symbol;
    }

    public static ParamOperatorEnum ofSymbol(String symbol) {
        if (symbol != null) {
            for (ParamOperatorEnum value : values()) {
                if (value.symbol.equalsIgnoreCase(symbol.trim()))
                    return value;
            }
        }
        return UNKNOWN;
    }

    public static ParamOperatorEnum ofDruid(SQLBinaryOperator operator) {
        return operator == null ? UNKNOWN : ofSymbol(operator.getName());
    }
}
