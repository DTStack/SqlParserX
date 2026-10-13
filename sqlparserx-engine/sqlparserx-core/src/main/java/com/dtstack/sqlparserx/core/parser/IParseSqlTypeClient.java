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

import com.dtstack.sqlparserx.core.enums.SqlType;

import com.alibaba.druid.sql.ast.SQLStatement;

/**
 * @description SQL 类型解析 Client，基于 Druid 语法树判定业务类型
 * @author changlu
 * @date 2026/10/12 17:40
 */
public interface IParseSqlTypeClient {

    /**
     * 根据 Druid 语法树解析 SQL 类型
     * @param stmt Druid 语法树
     * @return 业务 SQL 类型，无法识别时返回 {@link SqlType#OTHER}
     */
    SqlType parseSqlType(SQLStatement stmt);
}
