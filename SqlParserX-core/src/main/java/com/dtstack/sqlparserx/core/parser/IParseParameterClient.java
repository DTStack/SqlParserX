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
package com.dtstack.sqlparserx.core.parser;

import com.dtstack.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;

import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;
import java.util.Map;

/**
 * 参数相关sql解析client器
 *  历史来源：特定针对api场景提供下面两个场景
 *      预编译场景：buildSqlByPrepareCompile
 *      非预编译场景（解析sql中带有变量场景）：parseParameterSql
 */
public interface IParseParameterClient {

    /**
     * 解析带变量的sql场景
     * @param stmt sql语法树
     * @param isIgnoreSyntaxCheck Boolean
     * @param tableColumnsMap Map<List<Column>>
     * @return ParseParameterResult
     */
    ParseParameterResult parseParameterSql(SQLStatement stmt, Boolean isIgnoreSyntaxCheck,
            Map<String, List<DataTypeColumn>> tableColumnsMap);

    /**
     * 预编译场景处理带变量sql
     * @param stmt sql语法树
     * @param inParams Map<Object> 客户输入参数值map
     * @param columns Map<String,ParamInfo> 参数信息
     * @return PrepareCompileResult 构建预编译处理结果集合
     */
    PrepareCompileResult buildSqlByPrepareCompile(SQLStatement stmt, Map<String, Object> inParams,
            Map<String, ParamInfo> columns);

}
