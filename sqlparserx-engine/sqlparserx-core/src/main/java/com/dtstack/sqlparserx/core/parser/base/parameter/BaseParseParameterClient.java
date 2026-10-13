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
package com.dtstack.sqlparserx.core.parser.base.parameter;

import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;
import com.dtstack.sqlparserx.core.parser.IParseParameterClient;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparserx.core.parser.base.parameter.rewrite.PrepareSqlRewriter;
import com.dtstack.sqlparserx.core.parser.base.parameter.service.ParamParseService;
import com.dtstack.sqlparserx.core.parser.base.parameter.transfer.BaseParamDataTypeTransfer;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;
import java.util.Map;

/**
 * @description 变量参数解析与预编译客户端
 * @author changlu
 * @date 2026/10/12 15:20
 */
public class BaseParseParameterClient implements IParseParameterClient {
    private final ParamParseService parseService;
    private final PrepareSqlRewriter prepareSqlRewriter;

    public BaseParseParameterClient() {
        this(null);
    }

    public BaseParseParameterClient(SqlParserDataSourceType dataSourceType) {
        DbType dbType = dataSourceType == null ? null : dataSourceType.getDbType();
        this.parseService = getParamParseService();
        this.prepareSqlRewriter = getPrepareSqlRewriter(dbType);
    }

    /**
     * 默认参数解析服务实现，子类可覆盖
     */
    protected ParamParseService getParamParseService() {
        return new ParamParseService();
    }

    /**
     * 默认预编译改写器实现，子类可覆盖并注入数据源方言类型转换
     */
    protected PrepareSqlRewriter getPrepareSqlRewriter(DbType dbType) {
        return new PrepareSqlRewriter(dbType, new BaseParamDataTypeTransfer());
    }

    @Override
    public ParseParameterResult parseParameterSql(SQLStatement statement, Boolean isIgnoreSyntaxCheck,
            Map<String, List<DataTypeColumn>> tableColumnsMap) {
        return parseService.parse(statement, isIgnoreSyntaxCheck, tableColumnsMap);
    }

    @Override
    public PrepareCompileResult buildSqlByPrepareCompile(SQLStatement statement, Map<String, Object> inputParams,
            Map<String, ParamInfo> columns) {
        return prepareSqlRewriter.rewrite(statement, inputParams, columns);
    }
}
