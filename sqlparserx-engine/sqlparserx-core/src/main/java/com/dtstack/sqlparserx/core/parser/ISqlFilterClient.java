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

import com.dtstack.sqlparserx.core.parser.base.sqlfilter.RowLevelPermissionHandler;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * SQL过滤相关的服务接口
 *  按照输出参数排序过滤：filterSortSelectField
 *  列级权限过滤：getColumnLevelPermissionSql
 *  行级权限管控：getRowLevelPermissionSql
 */
public interface ISqlFilterClient {

    /**
     * 列级权限过滤
     * 注意：过滤select的输出参数以及按照outputParams顺序进行排序。
     *  入参：select name1, name, id from table, outputparams = [id, name]
     *  输出：select id, name from table
     * @param sql sql
     * @param outputParams 应当输出的参数名
     * @param filterFiled 过滤属性
     * @return sql保留outputParams中指定的参数名返回，没有的参数名则从sql中剔除
     */
    String filterSortSelectField(String sql, LinkedHashSet<String> outputParams, List<String> filterFiled);

    /**
     * 行级权限过滤
     * @param sql 原始SQL语句
     * @param permissionParams 权限过滤参数列表
     * @return 返回结果中的standardSql为生成的行级权限过滤的SQL语句
     * @throws Exception 如果发生异常
     */
    String getRowLevelPermissionSql(String sql, List<RowLevelPermissionFilterParam> permissionParams);

    /**
     * 获取行级权限执行器
     * @param
     * @return RowLevelPermissionHandler
     */
    RowLevelPermissionHandler getRowLevelPermissionHandler();

}
