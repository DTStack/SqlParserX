package com.changlu.sqlparserx.core.parser.base.sqlfilter;

import com.changlu.sqlparserx.core.parser.ISqlFilterClient;
import com.changlu.sqlparserx.core.parser.base.sqlfilter.row.BaseRowLevelPermissionHandler;
import com.changlu.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

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
        return getRowLevelPermissionHandler().getRowLevelPermissionSql(sqlStatement, permissionParams);
    }

    @Override
    public RowLevelPermissionHandler getRowLevelPermissionHandler() {
        return new BaseRowLevelPermissionHandler();
    }
}