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
package com.dtstack.sqlparserx.core.config;

public class Constant {

    public static final String ALL = "*";

    /**
     * JDBC 预编译占位符：变量参数改写为 ? 后统一使用该符号
     */
    public static final String PREPARED_PLACEHOLDER = "?";

    /**
     * 若是某个字段无来自某个表，此时该字段的table则为" "，最终血缘会完成过滤
     * 举例：create table new_table select id, '0' as num from table
     */
    public static final String NO_RELATION_TABLE = " ";

}
