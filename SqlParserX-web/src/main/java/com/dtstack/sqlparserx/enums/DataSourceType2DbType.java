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
package com.dtstack.sqlparserx.enums;

import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;

import com.alibaba.druid.DbType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将前端传入的数字值映射到 SqlParserX Core 数据源类型。
 *
 * <p>这里只注册 Core 已有独立解析器实现的数据源，Druid DbType 仅由 Core
 * 数据源类型派生，不作为前端支持范围的判断依据。</p>
 */
public enum DataSourceType2DbType {
    MYSQL(SqlParserDataSourceType.MYSQL), HIVE(SqlParserDataSourceType.HIVE), TRINO(
            SqlParserDataSourceType.TRINO), ORACLE(SqlParserDataSourceType.ORACLE), GREENPLUM(
                    SqlParserDataSourceType.GREENPLUM), TIDB(SqlParserDataSourceType.TIDB), POSTGRESQL(
                            SqlParserDataSourceType.POSTGRESQL), SQLSERVER(
                                    SqlParserDataSourceType.SQLSERVER), STARROCKS(
                                            SqlParserDataSourceType.STARROCKS), DORIS(
                                                    SqlParserDataSourceType.DORIS), OCEANBASE(
                                                            SqlParserDataSourceType.OCEANBASE), OCEANBASE_ORACLE(
                                                                    SqlParserDataSourceType.OCEANBASE_ORACLE), DM(
                                                                            SqlParserDataSourceType.DM), CLICKHOUSE(
                                                                                    SqlParserDataSourceType.CLICKHOUSE), SPARK(
                                                                                            SqlParserDataSourceType.SPARK);

    private final Set<Integer> codes;
    private final SqlParserDataSourceType sqlParserDataSourceType;

    DataSourceType2DbType(SqlParserDataSourceType dataSourceType) {
        this.sqlParserDataSourceType = dataSourceType;
        this.codes = Collections.singleton(dataSourceType.getValue());
    }

    public Set<Integer> getCodes() {
        return codes;
    }

    public DbType getDbType() {
        return sqlParserDataSourceType.getDbType();
    }

    public SqlParserDataSourceType getSqlParserDataSourceType() {
        return sqlParserDataSourceType;
    }

    /** Returns true only when the code maps one-to-one to a parser in core. */
    public static boolean isCoreSupported(int dataSourceType) {
        return findByCode(dataSourceType) != null;
    }

    /** The Web option list is derived from the core parser registry. */
    public static Set<Integer> getCoreSupportedCodes() {
        return Collections.unmodifiableSet(Arrays.stream(values())
                .flatMap(type -> type.codes.stream())
                .collect(Collectors.toSet()));
    }

    /** Returns the mapped DbType, preserving the historical MySQL fallback. */
    public static DbType getDbType(int dataSourceType) {
        DataSourceType2DbType mapping = findByCode(dataSourceType);
        return mapping == null ? DbType.mysql : mapping.getDbType();
    }

    public static SqlParserDataSourceType getSqlParserDataSourceType(int dataSourceType) {
        DataSourceType2DbType mapping = findByCode(dataSourceType);
        return mapping == null ? SqlParserDataSourceType.MYSQL : mapping.sqlParserDataSourceType;
    }

    public static DataSourceType2DbType getByCode(int dataSourceType) {
        DataSourceType2DbType mapping = findByCode(dataSourceType);
        return mapping == null ? MYSQL : mapping;
    }

    private static DataSourceType2DbType findByCode(int dataSourceType) {
        return Arrays.stream(values())
                .filter(type -> type.codes.contains(dataSourceType))
                .findFirst()
                .orElse(null);
    }
}
