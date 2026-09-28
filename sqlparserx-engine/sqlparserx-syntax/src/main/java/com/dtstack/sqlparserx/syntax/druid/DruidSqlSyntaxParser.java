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
package com.dtstack.sqlparserx.syntax.druid;

import com.dtstack.sqlparserx.syntax.SqlSyntaxParser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** 基于 Druid 的默认语法解析实现。 */
public class DruidSqlSyntaxParser implements SqlSyntaxParser {

    private final DbType dbType;

    public DruidSqlSyntaxParser(DbType dbType) {
        this.dbType = dbType;
    }

    @Override
    public DbType getDbType() {
        return dbType;
    }

    @Override
    public SQLStatement parseStatement(String sql) {
        return createParser(sql).parseStatement();
    }

    @Override
    public List<SQLStatement> parseStatements(String sql) {
        return createParser(sql).parseStatementList();
    }

    @Override
    public List<String> split(String sql) {
        List<String> result = new ArrayList<>();
        SQLStatementParser parser = createParser(sql);
        Lexer lexer = parser.getLexer();
        int startIndex = 0;
        while (true) {
            SQLStatement statement = parser.parseStatement();
            if (statement == null) {
                break;
            }
            int backLength = lexer.token().name != null ? lexer.token().name.length() : 0;
            int endIndex = lexer.pos() - backLength;
            result.add(sql.substring(startIndex, endIndex).trim());
            startIndex = endIndex;
            if (lexer.token() == Token.EOF) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<String> splitAndFormat(String sql) {
        return parseStatements(sql).stream().map(SQLStatement::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> splitByDruid(String sql) {
        return SQLParserUtils.split(sql, dbType);
    }

    private SQLStatementParser createParser(String sql) {
        return SQLParserUtils.createSQLStatementParser(sql, dbType);
    }
}
