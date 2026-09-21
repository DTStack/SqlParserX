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

import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.builder.BaseExprBuilder;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;

import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;

public interface RowLevelPermissionHandler {

    /**
     * 行级权限过滤
     * @param sqlStatement SQL语法树
     * @param permissionParams 权限过滤参数列表
     * @return 返回结果中的standardSql为生成的行级权限过滤的SQL语句
     * @throws Exception 如果发生异常
     */
    String getRowLevelPermissionSql(SQLStatement sqlStatement, List<RowLevelPermissionFilterParam> permissionParams);

    /**
     * 获取Expr构建器
     * @return BaseExprBuilder
     */
    default BaseExprBuilder getExprBuilder() {
        return new BaseExprBuilder();
    }

}
