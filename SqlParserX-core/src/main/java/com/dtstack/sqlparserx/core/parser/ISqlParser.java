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

import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageClient;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageClient;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.BaseParseParameterClient;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.BaseSqlFilterClient;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;
import com.dtstack.sqlparserx.core.parser.base.table.BaseParseTableClient;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISqlParser {

    /**
     * 解析列血缘
     * @param sql sql sql
     * @param tableColumnsMap 元数据表及字段映射关系
     * @return 字段血缘结果
     */
    ColumnLineageResult parseColumnLineages(String sql, Map<String, List<Column>> tableColumnsMap);

    /**
     * 解析列血缘
     * @param sqlStatement druid语法树
     * @param tableColumnsMap 元数据表及字段映射关系
     * @return 字段血缘结果
     */
    ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement, Map<String, List<Column>> tableColumnsMap);

    /**
     * 解析表血缘
     * @param sql sql sql
     * @return 表血缘结果
     */
    TableLineageResult parseTableLineages(String sql);

    /**
     * 解析表血缘
     * @param sqlStatement druid语法树
     * @return 表血缘结果
     */
    TableLineageResult parseTableLineages(SQLStatement sqlStatement);

    /**
     * 解析真实表
     * @param sql String
     * @return ParseTableResult 解析表结果
     */
    ParseTableResult parseTables(String sql);

    /**
     * 解析真实表
     * @param sqlStatement druid语法树
     * @return ParseTableResult 解析表结果
     */
    ParseTableResult parseTables(SQLStatement sqlStatement);

    /**
     * 解析create table语句
     * @param sql String sql
     * @return ParseCreateTableResult 解析结果
     */
    ParseCreateTableResult parseCreateTable(String sql);

    /**
     * 解析sql中的函数
     * @param sql String sql
     * @return Set<String> 函数集合
     */
    Set<String> parseFunction(String sql);

    /**
     * 解析带变量的sql场景
     * @param sql sql语法树
     * @param isIgnoreSyntaxCheck Boolean
     * @param tableColumnsMap Map<List<Column>>
     * @return ParseParameterResult
     */
    ParseParameterResult parseParameterSql(String sql, Boolean isIgnoreSyntaxCheck,
            Map<String, List<DataTypeColumn>> tableColumnsMap);

    /**
     * 预编译场景处理带变量sql
     * @param sql sql语法树
     * @param inParams Map<Object> 客户输入参数值map
     * @param columns Map<String,ParamInfo> 参数信息
     * @return PrepareCompileResult 构建预编译处理结果集合
     */
    PrepareCompileResult buildSqlByPrepareCompile(String sql, Map<String, Object> inParams,
            Map<String, ParamInfo> columns);

    /**
     * 行级权限过滤
     * @param sql 原始SQL语句
     * @param permissionParams 权限过滤参数列表
     * @return 返回结果中的standardSql为生成的行级权限过滤的SQL语句
     * @throws Exception 如果发生异常
     */
    String getRowLevelPermissionSql(String sql, List<RowLevelPermissionFilterParam> permissionParams);

    default IParseTableClient getParseTableClient() {
        return new BaseParseTableClient();
    }

    default IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        return new ColumnLineageClient(tableColumnsMap);
    }

    default ITableLineageClient getTableLineageClient() {
        return new TableLineageClient();
    }

    default ISqlFilterClient getSqlFilterClient() {
        return new BaseSqlFilterClient(getDbType());
    }

    default IParseParameterClient getParseParameterClient() {
        return new BaseParseParameterClient();
    }

    default DbType getDbType() {
        // null -> SQLStatementParser
        return null;
    }

}
