package com.changlu.core.parser.base;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.core.parser.IColumnLineageClient;
import com.changlu.core.parser.IParseTableClient;
import com.changlu.core.parser.ISqlParser;
import com.changlu.core.parser.ITableLineageClient;
import com.changlu.core.parser.base.lineage.column.Column;
import com.changlu.core.parser.base.lineage.column.ColumnLineageResult;
import com.changlu.core.parser.base.lineage.table.TableLineageResult;
import com.changlu.core.parser.base.table.domain.ParseTableResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BaseSqlParser implements ISqlParser {

    public DbType getDbType() {
        return null;
    }

    private SQLStatement createStatement(String sql, DbType dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        return parser.parseStatement();
    }

    private List<SQLStatement> createStatements(String sql, DbType dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        return parser.parseStatementList();
    }

    @Override
    public ColumnLineageResult parseColumnLineages(String sql, Map<String, List<Column>> tableColumnsMap) {
        SQLStatement sqlStatement = this.createStatement(sql, getDbType());
        return this.parseColumnLineages(sqlStatement, tableColumnsMap);
    }

    @Override
    public ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement, Map<String, List<Column>> tableColumnsMap) {
        IColumnLineageClient columnLineageClient = this.getColumnLineageClient(tableColumnsMap);
        return columnLineageClient.parseColumnLineages(sqlStatement);
    }

    @Override
    public TableLineageResult parseTableLineages(String sql) {
        SQLStatement sqlStatement = this.createStatement(sql, getDbType());
        return this.parseTableLineages(sqlStatement);
    }

    @Override
    public TableLineageResult parseTableLineages(SQLStatement sqlStatement) {
        ITableLineageClient tableLineageClient = this.getTableLineageClient();
        return tableLineageClient.parseTableLineages(sqlStatement);
    }

    @Override
    public ParseTableResult parseTables(String sql) {
        SQLStatement sqlStatement = this.createStatement(sql, getDbType());
        return this.parseTables(sqlStatement);
    }

    @Override
    public ParseTableResult parseTables(SQLStatement sqlStatement) {
        IParseTableClient parseTableClient = this.getParseTableClient();
        return parseTableClient.parseTables(sqlStatement);
    }

    @Override
    public Set<String> parseFunction(String sql) {
        return Collections.emptySet();
    }
}
