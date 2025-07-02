package com.dtstack.sqlparser.sqlparserx.core.parser.base;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.dtstack.sqlparser.sqlparserx.core.parser.*;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

import java.util.*;


public class BaseSqlParser implements ISqlParser {

    private SQLStatement createStatement(String sql) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, this.getDbType());
        return parser.parseStatement();
    }

    private List<SQLStatement> createStatements(String sql) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, this.getDbType());
        return parser.parseStatementList();
    }

    @Override
    public ColumnLineageResult parseColumnLineages(String sql, Map<String, List<Column>> tableColumnsMap) {
        SQLStatement sqlStatement = this.createStatement(sql);
        return this.parseColumnLineages(sqlStatement, tableColumnsMap);
    }

    @Override
    public ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement, Map<String, List<Column>> tableColumnsMap) {
        IColumnLineageClient columnLineageClient = this.getColumnLineageClient(tableColumnsMap);
        return columnLineageClient.parseColumnLineages(sqlStatement);
    }

    @Override
    public TableLineageResult parseTableLineages(String sql) {
        SQLStatement sqlStatement = this.createStatement(sql);
        return this.parseTableLineages(sqlStatement);
    }

    @Override
    public TableLineageResult parseTableLineages(SQLStatement sqlStatement) {
        ITableLineageClient tableLineageClient = this.getTableLineageClient();
        return tableLineageClient.parseTableLineages(sqlStatement);
    }

    @Override
    public ParseTableResult parseTables(String sql) {
        SQLStatement sqlStatement = this.createStatement(sql);
        return this.parseTables(sqlStatement);
    }

    @Override
    public ParseTableResult parseTables(SQLStatement sqlStatement) {
        IParseTableClient parseTableClient = this.getParseTableClient();
        return parseTableClient.parseTables(sqlStatement);
    }

    @Override
    public Set<String> parseFunction(String sql) {
        Set<String> res = new HashSet<>();
        List<SQLStatement> statements = this.createStatements(sql);
        for (SQLStatement sqlStatement : statements) {
            sqlStatement.accept(new SQLASTVisitorAdapter(){
                @Override
                public boolean visit(SQLMethodInvokeExpr x) {
                    res.add(x.getMethodName());
                    return super.visit(x);
                }

                @Override
                public boolean visit(SQLAggregateExpr x) {
                    res.add(x.getMethodName());
                    return super.visit(x);
                }
            });
        }
        return res;
    }

    @Override
    public ParseParameterResult parseParameterSql(String sql, Boolean isIgnoreSyntaxCheck, Map<String, List<DataTypeColumn>> tableColumnsMap) {
        SQLStatement statement = this.createStatement(sql);
        return getParseParameterClient().parseParameterSql(statement, isIgnoreSyntaxCheck, tableColumnsMap);
    }

    @Override
    public PrepareCompileResult buildSqlByPrepareCompile(String sql, Map<String, Object> inParams, Map<String, ParamInfo> columns) {
        SQLStatement statement = this.createStatement(sql);
        return getParseParameterClient().buildSqlByPrepareCompile(statement, inParams, columns);
    }

    @Override
    public String getRowLevelPermissionSql(String sql, List<RowLevelPermissionFilterParam> permissionParams) {
        ISqlFilterClient sqlFilterClient = this.getSqlFilterClient();
        return sqlFilterClient.getRowLevelPermissionSql(sql, permissionParams);
    }
}
