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
package com.dtstack.sqlparserx.syntax;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;

/**
 * SqlParserX 语法解析层统一契约。
 *
 * <p>该接口只负责 SQL 文本到 Druid AST 的语法处理，不包含表解析、血缘等业务语义。</p>
 */
public interface SqlSyntaxParser {

    DbType getDbType();

    SQLStatement parseStatement(String sql);

    List<SQLStatement> parseStatements(String sql);

    List<String> split(String sql);

    List<String> splitAndFormat(String sql);

    List<String> splitByDruid(String sql);
}
