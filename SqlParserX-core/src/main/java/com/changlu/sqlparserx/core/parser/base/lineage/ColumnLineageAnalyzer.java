package com.changlu.sqlparserx.core.parser.base.lineage;

import com.changlu.sqlparserx.core.parser.IColumnLineageClient;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.column.Column;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

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
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement stmt = parser.parseStatement();
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