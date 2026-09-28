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
package com.dtstack.sqlparserx.core.parser.base.table.component;

import com.dtstack.sqlparserx.core.select.table.SelectTable;
import com.dtstack.sqlparserx.core.select.table.SelectTableSpecialLineageService;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @description  真实表解析select结构组件
 *  1、select 字段、from 表逻辑有追溯真实表的逻辑过程 【SelectTableSpecialLineageService中代码实现】
 *  2、针对于select结构中的额外场景情况，如【where in 中带有表】，这里就进行单独处理，不与血缘中追溯逻辑相耦合。
 * @author changlu
 * @date 2025/4/27 14:28
 */
public class ParseSelectTableService extends SelectTableSpecialLineageService {

    @Override
    public List<SelectTable> queryTableBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        List<SelectTable> tables = super.queryTableBySqlSelectQueryBlock(selectQueryBlock);
        // 针对 where in 场景中的真实表解析
        tables.addAll(queryTableBySQLExpr(selectQueryBlock.getWhere()));
        return tables;
    }

    private List<SelectTable> queryTableBySQLExpr(SQLExpr sqlExpr) {
        List<SelectTable> tables = new ArrayList<>();
        // 举例：id in (select * from table)
        if (sqlExpr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) sqlExpr;
            // 即为select * from table，查询到表
            SQLSelect sqlSelect = sqlInSubQueryExpr.getSubQuery();
            List<SelectTable> selectTables = super.queryTablesBySqlSelect(sqlSelect);
            tables.addAll(selectTables);
        } else if (sqlExpr instanceof SQLBinaryOpExpr) {
            // 举例：id IN ( SELECT id FROM tz2) AND name IN ( SELECT name FROM tz3)
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
            SQLExpr left = sqlBinaryOpExpr.getLeft();
            SQLExpr right = sqlBinaryOpExpr.getRight();
            tables.addAll(this.queryTableBySQLExpr(left));
            tables.addAll(this.queryTableBySQLExpr(right));
        }
        return tables;
    }

}
