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
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.dtstack.sqlparserx.core.utils.TableSourceUtil;
import com.dtstack.sqlparserx.domain.ResponseResult;
import com.dtstack.sqlparserx.enums.DataSourceType2DbType;
import com.dtstack.sqlparserx.enums.DatasourceTypeEnum;
import com.dtstack.sqlparserx.vo.TableParseRequest;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    private static final Logger log = LoggerFactory.getLogger(DataSourceController.class);

    @GetMapping("/types")
    public ResponseResult getDataSourceTypes() {
        // 仅返回核心库（SqlParserX-core）实际实现解析器的方言对应的数据源类型，与后端支持保持同步
        List<DataSourceOption> dataSourceOptions = Arrays.stream(DatasourceTypeEnum.values())
                .filter(type -> SqlParserFactory.supports(DataSourceType2DbType.getDbType(type.getCode())))
                .map(type -> new DataSourceOption(type.getCode(), type.name()))
                .collect(Collectors.toList());
        return ResponseResult.success(dataSourceOptions);
    }

    @PostMapping("/grammarCheck")
    public ResponseResult grammarCheck(@RequestParam Integer datasourceType, @RequestBody String sql) {
        DbType dbType = DataSourceType2DbType.getDbType(datasourceType);
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
            SQLStatement stmt = parser.parseStatement();
            log.info("stmt: \n" + stmt.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.info("grammarCheck error!", e);
            return ResponseResult.error(msg, stackTrace);
        }
        return ResponseResult.success("成功!");
    }

    @PostMapping("/parseCreateTable")
    public ResponseResult parseCreateTable(@RequestBody TableParseRequest request) {
        DbType dbType = DataSourceType2DbType.getDbType(request.getDatasourceType());
        try {
            ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
            ParseCreateTableResult result = sqlParser.parseCreateTable(request.getSql());
            return ResponseResult.success(result);
        } catch (Exception e) {
            String msg = e.getMessage();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.error("parseCreateTable error!", e);
            return ResponseResult.error(msg, stackTrace);
        }
    }

    @PostMapping("/parseTables")
    public ResponseResult parseTables(@RequestBody TableParseRequest request) {
        DbType dbType = DataSourceType2DbType.getDbType(request.getDatasourceType());
        try {
            List<String> tables = TableSourceUtil.parseTables(request.getSql(), dbType);
            return ResponseResult.success(tables);
        } catch (Exception e) {
            String msg = e.getMessage();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.info("parseTables error!", e);
            return ResponseResult.error(msg, stackTrace);
        }
    }

    public static class DataSourceOption {
        private final int value;
        private final String label;

        public DataSourceOption(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}