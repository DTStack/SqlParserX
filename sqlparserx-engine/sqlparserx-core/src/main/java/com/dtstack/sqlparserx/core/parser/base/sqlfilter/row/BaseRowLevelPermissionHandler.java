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
package com.dtstack.sqlparserx.core.parser.base.sqlfilter.row;

import com.dtstack.sqlparserx.core.parser.base.sqlfilter.RowLevelPermissionHandler;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;
import com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo.TableInfo;
import com.dtstack.sqlparserx.core.utils.TableSourceUtil;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BaseRowLevelPermissionHandler implements RowLevelPermissionHandler {

    @Override
    public String getRowLevelPermissionSql(SQLStatement sqlStatement,
            List<RowLevelPermissionFilterParam> permissionParams) {
        if (sqlStatement instanceof SQLInsertStatement) {
            handleInsertStatement((SQLInsertStatement) sqlStatement, permissionParams);
        } else if (sqlStatement instanceof SQLSelectStatement) {
            handleSelectStatement((SQLSelectStatement) sqlStatement, permissionParams);
        } else if (sqlStatement instanceof SQLUpdateStatement) {
            handleUpdateStatement((SQLUpdateStatement) sqlStatement, permissionParams);
        } else if (sqlStatement instanceof SQLDeleteStatement) {
            handleDeleteStatement((SQLDeleteStatement) sqlStatement, permissionParams);
        }
        return sqlStatement.toString();
    }

    /**
     * 通用处理方法 - 适用于带有WHERE子句的语句(UPDATE/DELETE)
     */
    protected void handleStatementWithWhereClause(SQLTableSource from,
            SQLExpr existingWhere,
            List<RowLevelPermissionFilterParam> permissionParams,
            Consumer<SQLExpr> whereSetter) {
        List<TableInfo> tableInfos = handleInSQLTableSource(from, permissionParams);

        SQLExpr combinedCondition = buildCombinedConditionForTables(tableInfos, permissionParams);

        if (combinedCondition != null) {
            whereSetter.accept(mergeConditions(existingWhere, combinedCondition));
        }
    }

    /**
     * 为多个表构建组合条件
     */
    protected SQLExpr buildCombinedConditionForTables(List<TableInfo> tableInfos,
            List<RowLevelPermissionFilterParam> permissionParams) {
        boolean paramNotNeedUseTable = tableInfos.size() == 1;
        SQLExpr combinedCondition = null;

        for (TableInfo tableInfo : tableInfos) {
            SQLExpr tableCondition = buildCombinedCondition(permissionParams, tableInfo, paramNotNeedUseTable);
            if (tableCondition != null) {
                if (combinedCondition == null) {
                    combinedCondition = tableCondition;
                } else {
                    combinedCondition = new SQLBinaryOpExpr(combinedCondition, SQLBinaryOperator.BooleanAnd,
                            tableCondition);
                }
            }
        }

        return combinedCondition;
    }

    /**
     * 合并现有条件和新增条件
     */
    private SQLExpr mergeConditions(SQLExpr existingWhere, SQLExpr newCondition) {
        if (existingWhere == null) {
            return newCondition;
        }

        if (existingWhere instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) existingWhere;
            if (SQLBinaryOperator.BooleanOr.equals(sqlBinaryOpExpr.getOperator())) {
                sqlBinaryOpExpr.setParenthesized(true);
            }
        }

        return new SQLBinaryOpExpr(existingWhere, SQLBinaryOperator.BooleanAnd, newCondition);
    }

    private void handleSelectStatement(SQLSelectStatement selectStatement,
            List<RowLevelPermissionFilterParam> permissionParams) {
        SQLSelect select = selectStatement.getSelect();
        if (select != null) {
            handleSelectQuery(select.getQuery(), permissionParams);
        }
    }

    private void handleInsertStatement(SQLInsertStatement insertStatement,
            List<RowLevelPermissionFilterParam> permissionParams) {
        SQLSelect query = insertStatement.getQuery();
        if (query != null) {
            handleSelectQuery(query.getQuery(), permissionParams);
        }
    }

    private void handleSelectQuery(SQLSelectQuery selectQuery, List<RowLevelPermissionFilterParam> permissionParams) {
        if (selectQuery instanceof SQLSelectQueryBlock) {
            handleSelectQueryBlock((SQLSelectQueryBlock) selectQuery, permissionParams);
        } else if (selectQuery instanceof SQLUnionQuery) {
            SQLUnionQuery unionQuery = (SQLUnionQuery) selectQuery;
            handleSelectQuery(unionQuery.getLeft(), permissionParams);
            handleSelectQuery(unionQuery.getRight(), permissionParams);
        }
    }

    // 处理select结构
    private void handleSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock,
            List<RowLevelPermissionFilterParam> permissionParams) {
        SQLTableSource from = selectQueryBlock.getFrom();

        List<TableInfo> tableInfos = handleInSQLTableSource(from, permissionParams);

        boolean paramNotNeedUseTable = tableInfos.size() == 1;
        for (TableInfo tableInfo : tableInfos) {
            // 根据当前的table表名信息来进行构建条件expr节点
            SQLExpr combinedCondition = buildCombinedCondition(permissionParams, tableInfo, paramNotNeedUseTable);
            // 合并  新构建的 & 原始select结构的条件到一起
            if (combinedCondition != null) {
                SQLExpr existingWhere = selectQueryBlock.getWhere();
                if (existingWhere == null) {
                    selectQueryBlock.setWhere(combinedCondition);
                } else {
                    // 若是原始存在的为or条件，如：WHERE price > 45.0 OR customer_name = 'John'，新条件为newCondition，则需要将条件进行(price > 45.0 OR customer_name = 'John')
                    if (existingWhere instanceof SQLBinaryOpExpr) {
                        SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) existingWhere;
                        if (SQLBinaryOperator.BooleanOr.equals(sqlBinaryOpExpr.getOperator())) {
                            sqlBinaryOpExpr.setParenthesized(true);
                        }
                    }
                    selectQueryBlock.setWhere(new SQLBinaryOpExpr(existingWhere, SQLBinaryOperator.BooleanAnd,
                            combinedCondition));
                }
            }
        }
    }

    // 处理update结构
    protected void handleUpdateStatement(SQLUpdateStatement sqlStatement,
            List<RowLevelPermissionFilterParam> permissionParams) {
        handleStatementWithWhereClause(
                // SQLUpdateStatement中的from表通过tablesource获取
                (sqlStatement).getTableSource(),
                (sqlStatement).getWhere(),
                permissionParams,
                (sqlStatement)::setWhere);
    }

    // 处理delete结构
    private void handleDeleteStatement(SQLDeleteStatement sqlStatement,
            List<RowLevelPermissionFilterParam> permissionParams) {
        handleStatementWithWhereClause(
                (sqlStatement).getFrom(),
                (sqlStatement).getWhere(),
                permissionParams,
                (sqlStatement)::setWhere);
    }

    public List<TableInfo> handleInSQLTableSource(SQLTableSource from,
            List<RowLevelPermissionFilterParam> permissionParams) {
        List<TableInfo> collectSelectTables = new ArrayList<>();

        if (from instanceof SQLExprTableSource) {
            SQLExprTableSource tableSource = (SQLExprTableSource) from;
            String fullTableName = TableSourceUtil.extractTableName(tableSource);
            String alias = tableSource.getAlias();
            TableInfo tableInfo = new TableInfo(fullTableName, alias);
            collectSelectTables.add(tableInfo);
        } else if (from instanceof SQLSubqueryTableSource) {
            // 子查询无需收集
            handleSubqueryTableSource((SQLSubqueryTableSource) from, permissionParams);
        } else if (from instanceof SQLJoinTableSource) {
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) from;
            collectSelectTables.addAll(handleInSQLTableSource(sqlJoinTableSource.getLeft(), permissionParams));
            collectSelectTables.addAll(handleInSQLTableSource(sqlJoinTableSource.getRight(), permissionParams));
        }
        return collectSelectTables;
    }

    private void handleSubqueryTableSource(SQLSubqueryTableSource subqueryTableSource,
            List<RowLevelPermissionFilterParam> permissionParams) {
        handleSelectQuery(subqueryTableSource.getSelect().getQuery(), permissionParams);
    }

    /**
     * 一个表 & 多个权限参数合并为一个条件
     * @param permissionParams List<RowLevelPermissionFilterParam>
     * @param tableInfo TableInfo
     * @param paramNotNeedUseTable boolean 构建的条件中是否属性不需要带表名，若是例如 select * from order，单表就无需带上表名 如：select * from table where region in ('abc')
     */
    private SQLExpr buildCombinedCondition(List<RowLevelPermissionFilterParam> permissionParams,
            TableInfo tableInfo, boolean paramNotNeedUseTable) {
        SQLExpr combinedCondition = null;

        for (RowLevelPermissionFilterParam param : permissionParams) {
            if (!isParamApplicable(param, tableInfo.getTableName())) {
                continue;
            }

            SQLExpr condition = createSingleCondition(param, tableInfo, paramNotNeedUseTable);
            if (condition != null) {
                combinedCondition = combineConditions(combinedCondition, condition, param);
            }
        }

        return combinedCondition;
    }

    private boolean isParamApplicable(RowLevelPermissionFilterParam param, String tableName) {
        return tableName == null || tableName.endsWith(param.getTableName());
    }

    private SQLExpr createSingleCondition(RowLevelPermissionFilterParam param, TableInfo tableInfo,
            boolean paramNotNeedUseTable) {
        if (param.getFieldValue() == null || param.getFieldValue().isEmpty()) {
            return null;
        }
        return getExprBuilder().buildCondition(param, tableInfo, paramNotNeedUseTable);
    }

    private SQLExpr combineConditions(SQLExpr existing, SQLExpr newCondition,
            RowLevelPermissionFilterParam param) {
        if (existing == null)
            return newCondition;

        SQLBinaryOperator operator = param.getLinkOperatorEnum() == RowLevelPermissionFilterParam.LinkOperatorEnum.OR
                ? SQLBinaryOperator.BooleanOr
                : SQLBinaryOperator.BooleanAnd;
        return new SQLBinaryOpExpr(existing, operator, newCondition);
    }

}
