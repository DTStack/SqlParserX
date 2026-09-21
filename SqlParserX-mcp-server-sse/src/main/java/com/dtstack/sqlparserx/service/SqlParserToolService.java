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
package com.dtstack.sqlparserx.service;

import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.dtstack.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import com.dtstack.sqlparserx.enums.DataSourceType2DbType;
import com.dtstack.sqlparserx.enums.DatasourceTypeEnum;

import com.alibaba.druid.DbType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class SqlParserToolService {

    /**
     * 获取当前支持的所有数据源类型
     * @return 支持的数据源类型描述字符串
     */
    @Tool(description = "获取当前支持的所有数据源类型，包含数据源类型名称 & datasourceType（int数字）")
    public String getSupportedDataSourceTypes() {
        log.info("查询getSupportedDataSourceTypes");
        StringBuilder sb = new StringBuilder();
        for (DatasourceTypeEnum type : DatasourceTypeEnum.values()) {
            sb.append(type.name())
                    .append(" (dataSourceType: ").append(type.getCode())
                    .append(", 数据源名称: ").append(type.name())
                    .append(")\n");
        }
        return sb.toString();
    }

    /**
     * 解析SQL中的真实表
     * @param dataSourceType 数据源类型代码
     * @param sql 需要解析的SQL语句
     * @return 解析表结果对象
     */
    @Tool(description = "解析SQL中的所有真实表名，解析调用之前确认datasourceType真实数字")
    public String parseTables(
            @ToolParam(description = "数据源类型是数字，需要调用tools工具getSupportedDataSourceTypes获取数据源的dataSourceType") int dataSourceType,
            @ToolParam(description = "需要解析的SQL语句") String sql) {
        log.info(String.format("数据源类型：%s，sql为：%s", dataSourceType, sql));
        DbType dbType = DataSourceType2DbType.getDbType(dataSourceType);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        ParseTableResult parseTableResult = sqlParser.parseTables(sql);
        String res = String.format("主表为：%s, 解析到的真实表为：%s",
                parseTableResult.getMainTable().toString(),
                parseTableResult.getAllTableName().toString());
        log.info("解析表结果为：{}", res);
        return res;
    }

    /**
     * 解析SQL中的字段血缘关系
     * @param dataSourceType 数据源类型代码
     * @param sql 需要解析的SQL语句
     * @return 字段血缘解析结果
     */
    @Tool(description = "解析SQL中的真实字段血缘关系，解析调用之前确认datasourceType真实数字")
    public String parseColumnLineages(
            @ToolParam(description = "数据源类型是数字，需要调用tools工具getSupportedDataSourceTypes获取数据源的dataSourceType") int dataSourceType,
            @ToolParam(description = "需要解析的SQL语句") String sql) {
        log.info(String.format("数据源类型：%s，sql为：%s", dataSourceType, sql));
        DbType dbType = DataSourceType2DbType.getDbType(dataSourceType);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        ColumnLineageResult columnLineageResult = sqlParser.parseColumnLineages(sql, null);
        String res = columnLineageResult.getColumnLineages().toString();
        log.info("解析字段血缘结果为：{}", res);
        return res;
    }

    /**
     * 解析SQL中的表血缘关系
     * @param dataSourceType 数据源类型代码
     * @param sql 需要解析的SQL语句
     * @return 表血缘解析结果
     */
    @Tool(description = "解析SQL中的真实表血缘关系，解析调用之前确认datasourceType真实数字")
    public String parseTableLineages(
            @ToolParam(description = "数据源类型是数字，需要调用tools工具getSupportedDataSourceTypes获取数据源的dataSourceType") int dataSourceType,
            @ToolParam(description = "需要解析的SQL语句") String sql) {
        log.info(String.format("数据源类型：%s，sql为：%s", dataSourceType, sql));
        DbType dbType = DataSourceType2DbType.getDbType(dataSourceType);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        TableLineageResult tableLineageResult = sqlParser.parseTableLineages(sql);
        String res = tableLineageResult.getTableLineages().toString();
        log.info("解析表血缘结果为：{}", res);
        return res;
    }

    /**
     * 解析SQL中的函数
     * @param dataSourceType 数据源类型代码
     * @param sql 需要解析的SQL语句
     * @return 函数名称集合
     */
    @Tool(description = "解析SQL中的所有函数名，解析调用之前确认datasourceType真实数字")
    public String parseFunction(
            @ToolParam(description = "数据源类型是数字，需要调用tools工具getSupportedDataSourceTypes获取数据源的dataSourceType") int dataSourceType,
            @ToolParam(description = "需要解析的SQL语句") String sql) {
        log.info(String.format("数据源类型：%s，sql为：%s", dataSourceType, sql));
        DbType dbType = DataSourceType2DbType.getDbType(dataSourceType);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        Set<String> functions = sqlParser.parseFunction(sql);
        String res = functions.toString();
        log.info("解析真实函数结果为：{}", res);
        return res;
    }
}