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

/**
 * @description SQL 业务操作类型。
 *              仅收录可由 {@code ISqlParser#parseSqlType} 基于 Druid 语法树判定出的类型；
 *              无法识别的一律返回 {@link #OTHER}。{@code type} 为对外业务标识串，{@code value} 为整型编码。
 * @author changlu
 * @date 2026/10/12 17:40
 */
public enum SqlType {

    // 查询
    QUERY("select", 44), WITH_QUERY("with_query", 49), QUERY_NO_FROM("xxx", 48),

    // 数据操作
    INSERT("insert", 41), UPDATE("update", 45), DELETE("delete", 46), TRUNCATE("truncate", 43), MERGE_INTO(
            "merge_into", 50), BEGIN_END("begin_end", 51),

    // 结构操作
    CREATE("create", 101), CREATE_AS("create_as", 103), CREATE_LIKE("create_like", 102), CREATE_TEMP("create_temp",
            200), DROP("drop", 104), ALTER("alter", 105), ALTERTABLE_RENAME("altertable_rename",
                    106), ALTERTABLE_RENAMECOL("altertable_renamecol", 113), ALTERTABLE_ADDCOLS("altertable_addcols",
                            114), ALTERTABLE_REPLACECOLS("altertable_replacecols",
                                    115), ALTERTABLE_ADDPARTS("altertable_addparts",
                                            109), ALTERTABLE_DROPPARTS("altertable_dropparts",
                                                    111), ALTERTABLE_RENAMEPART("altertable_renamepart",
                                                            110), COMMENT_ON("comment_on", 200),

    // 描述 / SHOW
    DESC_TABLE("desc_table", 176), DATABASE_DESC("databases_desc", 13), SHOW("show", 180), SHOW_TABLES("show_tables",
            171), SHOW_CREATETABLE("show_createtable", 172), SHOW_COLUMNS("show_columns", 175), SHOW_FUNCTION(
                    "show_function", 25), SHOW_MATERIALIZED_VIEW("show_materialized_view",
                            406), SHOW_CREATE_MATERIALIZED_VIEW(
                                    "show_create_materialized_view", 407), EXPLAIN("explain", 177),

    // 视图 / 物化视图
    CREATE_VIEW("create_view", 401), CREATE_MATERIALIZED_VIEW("create_materialized_view",
            403), DROP_MATERIALIZED_VIEW("drop_materialized_view", 404), ALTER_MATERIALIZED_VIEW(
                    "alter_materialized_view", 405), REFRESH_MATERIALIZED_VIEW("refresh_materialized_view", 408),

    // 函数 / 过程
    CREATE_FUNCTION("create_function", 22), DROP_FUNCTION("drop_function", 23), PROCEDURE_EXEC("procedure_exec", 410),

    // 数据库
    DATABASE_CREATE("databases_create", 11), DATABASE_DROP("databases_drop", 12), DATABASE_SHOW("databases_show", 14),

    // 兜底
    OTHER("other", 500);

    private final String type;
    private final Integer value;

    SqlType(String type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Integer getValue() {
        return value;
    }

    /**
     * 按业务标识串解析（忽略大小写）
     */
    public static SqlType getByType(String type) {
        if (type != null) {
            for (SqlType sqlType : values()) {
                if (sqlType.type.equalsIgnoreCase(type.trim())) {
                    return sqlType;
                }
            }
        }
        return OTHER;
    }

    /**
     * 按整型编码解析
     */
    public static SqlType getByValue(Integer value) {
        if (value != null) {
            for (SqlType sqlType : values()) {
                if (sqlType.value.equals(value)) {
                    return sqlType;
                }
            }
        }
        return OTHER;
    }
}
