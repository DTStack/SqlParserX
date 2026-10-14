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
package com.dtstack.sqlparserx.controller;

import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;
import com.dtstack.sqlparserx.domain.ResponseResult;
import com.dtstack.sqlparserx.enums.DataSourceType2DbType;
import com.dtstack.sqlparserx.vo.ParameterParseRequest;
import com.dtstack.sqlparserx.vo.PrepareCompileRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/parameter")
public class ParameterController {

    private static final Logger log = LoggerFactory.getLogger(ParameterController.class);

    @PostMapping("/parse")
    public ResponseResult parseParameters(@RequestBody ParameterParseRequest request) {
        if (request == null || !isValid(request.getDatasourceType()) || isBlank(request.getSql())) {
            return ResponseResult.error("请提供有效的数据源类型和 SQL 语句");
        }
        try {
            ISqlParser parser = getParser(request.getDatasourceType());
            ParseParameterResult result = parser.parseParameterSql(request.getSql(),
                    request.getIgnoreSyntaxCheck(), request.getTableColumnsMap() == null
                            ? Collections.emptyMap() : request.getTableColumnsMap());
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("parseParameters error", e);
            return ResponseResult.error(errorMessage(e));
        }
    }

    @PostMapping("/prepareCompile")
    public ResponseResult prepareCompile(@RequestBody PrepareCompileRequest request) {
        if (request == null || !isValid(request.getDatasourceType()) || isBlank(request.getSql())) {
            return ResponseResult.error("请提供有效的数据源类型和 SQL 语句");
        }
        try {
            ISqlParser parser = getParser(request.getDatasourceType());
            PrepareCompileResult result = parser.buildSqlByPrepareCompile(request.getSql(), request.getInParams(),
                    request.getColumns());
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("prepareCompile error", e);
            return ResponseResult.error(errorMessage(e));
        }
    }

    private ISqlParser getParser(Integer datasourceType) {
        return SqlParserFactory.getSqlParser(DataSourceType2DbType.getSqlParserDataSourceType(datasourceType));
    }

    private boolean isValid(Integer datasourceType) {
        return datasourceType != null && DataSourceType2DbType.isCoreSupported(datasourceType);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String errorMessage(Exception exception) {
        return exception.getMessage() == null ? "SQL 参数处理失败" : exception.getMessage();
    }
}
