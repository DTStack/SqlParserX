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
package com.dtstack.sqlparserx.core.utils;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 通用工具类 SQLParser解析器过程中使用
 * @author changlu
 * @date 2025/7/24 16:11
 */
public class CommonUtil {

    /**
     * 去除字符串中的反引号(`)和单引号(')
     * @param input 输入字符串
     * @return 处理后的字符串，如果输入为null则返回null
     */
    public static String removeQuotes(String input) {
        if (input == null) {
            return null;
        }
        // 去除字符串开头和结尾的反引号或单引号
        return input.replaceAll("`", "")
                .replaceAll("'", "")
                .replaceAll("\"", "");
    }

    /**
     * 将SQLExpr类型的列名转换为值
     * @param sqlExprs SQLExpr类型的列名列表
     * @return 字符串列表，如果输入为空则返回null
     */
    public static List<String> sqlExprs2Values(List<SQLExpr> sqlExprs) {
        if (CollectionUtil.isEmpty(sqlExprs)) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        for (SQLExpr sqlExpr : sqlExprs) {
            values.add(sqlExpr2Value(sqlExpr));
        }
        return values;
    }

    /**
     * 将SQLExpr类型的列名转换为值
     * @param sqlExpr sqlExpr
     * @return 字符串列表，如果输入为空则返回null
     */
    public static String sqlExpr2Value(SQLExpr sqlExpr) {
        if (sqlExpr == null)
            return null;
        String value = sqlExpr.toString();
        if (sqlExpr instanceof SQLCharExpr) {
            SQLCharExpr sqlCharExpr = (SQLCharExpr) sqlExpr;
            value = sqlCharExpr.getText();
        }
        return ColumnUtil.removeQuotes(value);
    }

    /**
     * 将SQLSelectOrderByItem类型的列名转换为值
     * @param sqlSelectOrderByItems SQLSelectOrderByItem类型的列名列表
     * @return 字符串列表，如果输入为空则返回null
     */
    public static List<String> sqlSelectOrderByItems2Values(List<SQLSelectOrderByItem> sqlSelectOrderByItems) {
        if (CollectionUtil.isEmpty(sqlSelectOrderByItems)) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        for (SQLSelectOrderByItem sqlSelectOrderByItem : sqlSelectOrderByItems) {
            String value = sqlSelectOrderByItem.getExpr().toString();
            values.add(ColumnUtil.removeQuotes(value));
        }
        return values;
    }

}
