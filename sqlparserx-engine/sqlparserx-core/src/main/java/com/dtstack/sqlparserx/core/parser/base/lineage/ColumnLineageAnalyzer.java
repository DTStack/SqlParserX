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
package com.dtstack.sqlparserx.core.parser.base.lineage;

import com.dtstack.sqlparserx.core.parser.IColumnLineageClient;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparserx.syntax.SqlSyntaxParserFactory;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;
import java.util.Map;

public class ColumnLineageAnalyzer {

    public ColumnLineageResult analyze(String sql) {
        return analyze(sql, null);
    }

    public ColumnLineageResult analyze(String sql, Map<String, List<Column>> tableColumnsMap) {
        return analyze(sql, tableColumnsMap, DbType.mysql);
    }

    public ColumnLineageResult analyze(String sql, Map<String, List<Column>> tableColumnsMap, DbType dbType) {
        SQLStatement stmt = SqlSyntaxParserFactory.getParser(dbType).parseStatement(sql);
        return this.analyze(stmt, dbType, tableColumnsMap);
    }

    /**
     * 传入druid的statement & 字段map集合
     */
    public ColumnLineageResult analyze(SQLStatement stmt, DbType dbType, Map<String, List<Column>> tableColumnsMap) {
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        IColumnLineageClient columnLineageClient = sqlParser.getColumnLineageClient(tableColumnsMap);
        return columnLineageClient.parseColumnLineages(stmt);
    }
}
