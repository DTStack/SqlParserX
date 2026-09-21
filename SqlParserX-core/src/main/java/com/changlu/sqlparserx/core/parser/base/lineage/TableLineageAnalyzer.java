package com.changlu.sqlparserx.core.parser.base.lineage;

import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.ITableLineageClient;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableLineageResult;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

public class TableLineageAnalyzer {
    private DbType dbType;

    public TableLineageAnalyzer(DbType dbType) {
        this.dbType = dbType;
    }

    public TableLineageResult analyze(String sql) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement stmt = parser.parseStatement();
        return analyze(stmt, dbType);
    }

    /**
     * 传入druid的statement
     */
    public static TableLineageResult analyze(SQLStatement stmt, DbType dbType) {
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        ITableLineageClient tableLineageClient = sqlParser.getTableLineageClient();
        return tableLineageClient.parseTableLineages(stmt);
    }

}