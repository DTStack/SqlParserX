package com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlFilterClient;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter.vo.RowLevelPermissionFilterParam;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter.vo.TableInfo;
import com.dtstack.sqlparser.sqlparserx.core.utils.TableSourceUtil;

import java.util.ArrayList;
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
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, this.dbType);
        return parser.parseStatement();
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

        if (sqlStatement instanceof SQLInsertStatement) {
            handleInsertStatement((SQLInsertStatement) sqlStatement, permissionParams);
        } else if (sqlStatement instanceof SQLSelectStatement) {
            handleSelectStatement((SQLSelectStatement) sqlStatement, permissionParams);
        } else if (sqlStatement instanceof SQLUpdateStatement) {
            handleUpdateStatement((SQLUpdateStatement) sqlStatement, permissionParams);
        }

        return sqlStatement.toString();
    }

    private void handleUpdateStatement(SQLUpdateStatement sqlUpdateStatement, List<RowLevelPermissionFilterParam> permissionParams) {
        SQLTableSource from = sqlUpdateStatement.getFrom();
//        sqlUpdateStatement.setWhere();
    }

    private void handleSelectStatement(SQLSelectStatement selectStatement, List<RowLevelPermissionFilterParam> permissionParams) {
        SQLSelect select = selectStatement.getSelect();
        if (select != null) {
            handleSelectQuery(select.getQuery(), permissionParams);
        }
    }

    private void handleInsertStatement(SQLInsertStatement insertStatement, List<RowLevelPermissionFilterParam> permissionParams) {
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

    private void handleSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock,
                                        List<RowLevelPermissionFilterParam> permissionParams) {
        SQLTableSource from = selectQueryBlock.getFrom();
        List<TableInfo> tableInfos = handleInSQLTableSource(from, permissionParams);
        addPermissionConditions(selectQueryBlock, tableInfos, permissionParams);
    }

    public List<TableInfo> handleInSQLTableSource(SQLTableSource from, List<RowLevelPermissionFilterParam> permissionParams) {
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

    private void addPermissionConditions(SQLSelectQueryBlock selectQueryBlock, List<TableInfo> tableInfos,
                                         List<RowLevelPermissionFilterParam> permissionParams) {
        boolean paramNotNeedUseTable = tableInfos.size() == 1;
        for (TableInfo tableInfo : tableInfos) {
            // 根据当前的table表名信息来进行构建条件expr节点
            SQLExpr combinedCondition = buildCombinedCondition(permissionParams, tableInfo, paramNotNeedUseTable);
            // 合并  新构建的 & 原始select结构的条件到一起
            if (combinedCondition != null) {
                applyConditionsToQuery(selectQueryBlock, combinedCondition);
            }
        }
    }

    /**
     *
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

    private SQLExpr createSingleCondition(RowLevelPermissionFilterParam param, TableInfo tableInfo, boolean paramNotNeedUseTable) {
        if (param.getFieldValue() == null || param.getFieldValue().isEmpty()) {
            return null;
        }
        return getExprBuilder().buildCondition(param, tableInfo, paramNotNeedUseTable);
    }

    private SQLExpr combineConditions(SQLExpr existing, SQLExpr newCondition,
                                      RowLevelPermissionFilterParam param) {
        if (existing == null) return newCondition;

        SQLBinaryOperator operator = param.getLinkOperatorEnum() == RowLevelPermissionFilterParam.LinkOperatorEnum.OR ?
                SQLBinaryOperator.BooleanOr : SQLBinaryOperator.BooleanAnd;
        return new SQLBinaryOpExpr(existing, operator, newCondition);
    }

    private void applyConditionsToQuery(SQLSelectQueryBlock queryBlock, SQLExpr newCondition) {
        SQLExpr existingWhere = queryBlock.getWhere();
        if (existingWhere == null) {
            queryBlock.setWhere(newCondition);
        }else {
            // 若是原始存在的为or条件，如：WHERE price > 45.0 OR customer_name = 'John'，新条件为newCondition，则需要将条件进行(price > 45.0 OR customer_name = 'John')
            if (existingWhere instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) existingWhere;
                if (SQLBinaryOperator.BooleanOr.equals(sqlBinaryOpExpr.getOperator())) {
                    sqlBinaryOpExpr.setParenthesized(true);
                }
            }
            queryBlock.setWhere(new SQLBinaryOpExpr(existingWhere, SQLBinaryOperator.BooleanAnd, newCondition));
        }

    }

}