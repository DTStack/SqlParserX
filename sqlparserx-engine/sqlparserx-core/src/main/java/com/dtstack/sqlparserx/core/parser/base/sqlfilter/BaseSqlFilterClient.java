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
package com.dtstack.sqlparserx.core.parser.base.sqlfilter;

import com.dtstack.sqlparserx.core.parser.ISqlFilterClient;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.BaseRowLevelPermissionHandler;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;
import com.dtstack.sqlparserx.syntax.SqlSyntaxParserFactory;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * @description  sql过滤相关client，如行级、列级权限sql
 * @author changlu
 * @date 2025/7/1 00:42
 */
public class BaseSqlFilterClient implements ISqlFilterClient {

    private DbType dbType;

    public BaseSqlFilterClient(DbType dbType) {
        this.dbType = dbType;
    }

    private SQLStatement parseSqlStatement(String sql) {
        return SqlSyntaxParserFactory.getParser(this.dbType).parseStatement(sql);
    }

    @Override
    public String filterSortSelectField(String sql, LinkedHashSet<String> outputParams, List<String> filterFiled) {
        return "";
    }

    @Override
    public String getRowLevelPermissionSql(String sql, List<RowLevelPermissionFilterParam> permissionParams) {
        if (permissionParams == null || permissionParams.isEmpty()) {
            return sql;
        }
        SQLStatement sqlStatement = this.parseSqlStatement(sql);
        return getRowLevelPermissionHandler().getRowLevelPermissionSql(sqlStatement, permissionParams);
    }

    @Override
    public RowLevelPermissionHandler getRowLevelPermissionHandler() {
        return new BaseRowLevelPermissionHandler();
    }
}
