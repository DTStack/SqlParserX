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

import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;
import com.dtstack.sqlparserx.core.parser.base.BaseSqlParser;

import com.alibaba.druid.DbType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可根据不同数据源类型来完成不同组件的使用
 */
public class SqlParserFactory {

    /**
     * 当前核心库实际支持的数据源集合。
     */
    private static final Set<SqlParserDataSourceType> SUPPORTED_DATA_SOURCE_TYPES = Collections.unmodifiableSet(EnumSet
            .allOf(SqlParserDataSourceType.class));

    private static final Set<DbType> SUPPORTED_DB_TYPES = buildSupportedDbTypes();

    /** 每种数据源对应一个解析器实例，首次获取时再创建。 */
    private static final Map<SqlParserDataSourceType, ISqlParser> SQL_PARSER_CACHE = new ConcurrentHashMap<>();

    private static Set<DbType> buildSupportedDbTypes() {
        Set<DbType> dbTypes = new HashSet<>();
        for (SqlParserDataSourceType dataSourceType : SUPPORTED_DATA_SOURCE_TYPES) {
            dbTypes.add(dataSourceType.getDbType());
        }
        return Collections.unmodifiableSet(dbTypes);
    }

    /**
     * 根据 SqlParserX Core 自有的数据源类型创建业务解析器。
     *
     * @param dataSourceType SqlParserX 支持的数据源类型
     * @return 对应数据源的业务解析器
     * @throws IllegalArgumentException 数据源类型为空时抛出
     */
    public static ISqlParser getSqlParser(SqlParserDataSourceType dataSourceType) {
        if (dataSourceType == null) {
            throw new IllegalArgumentException("SqlParser data source type must not be null");
        }
        return SQL_PARSER_CACHE.computeIfAbsent(dataSourceType, SqlParserDataSourceType::createSqlParser);
    }

    public static boolean supports(SqlParserDataSourceType dataSourceType) {
        return dataSourceType != null && SUPPORTED_DATA_SOURCE_TYPES.contains(dataSourceType);
    }

    public static Set<SqlParserDataSourceType> getSupportedDataSourceTypes() {
        return SUPPORTED_DATA_SOURCE_TYPES;
    }

    /**
     * 判断指定的 Druid DbType 是否为核心库实际支持的方言（有独立解析器实现，非回退默认解析器）
     * @param dbType DbType
     * @return true=支持
     */
    @Deprecated
    public static boolean supports(DbType dbType) {
        return SqlParserDataSourceType.fromDbType(dbType) != null;
    }

    /**
     * 获取核心库实际支持的方言集合（不可变）
     * @return Set<DbType>
     */
    @Deprecated
    public static Set<DbType> getSupportedDbTypes() {
        return SUPPORTED_DB_TYPES;
    }

    /**
     * 基于 Druid DbType 的兼容入口。新代码请使用
     * {@link #getSqlParser(SqlParserDataSourceType)}。
     * @param dbType DbType
     * @return ISqlParser
     */
    @Deprecated
    public static ISqlParser getSqlParser(DbType dbType) {
        SqlParserDataSourceType dataSourceType = SqlParserDataSourceType.fromDbType(dbType);
        return dataSourceType == null ? new BaseSqlParser() : getSqlParser(dataSourceType);
    }

    // 根据可接入不同场景去自行提供转换

}
