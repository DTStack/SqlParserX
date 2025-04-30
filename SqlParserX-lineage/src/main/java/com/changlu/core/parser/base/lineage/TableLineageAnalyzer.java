package com.changlu.core.parser.base.lineage;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.core.parser.ISqlParser;
import com.changlu.core.parser.ITableLineageClient;
import com.changlu.core.parser.SqlParserFactory;
import com.changlu.core.parser.base.lineage.table.TableLineageResult;

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
    public TableLineageResult analyze(SQLStatement stmt, DbType dbType) {
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        ITableLineageClient tableLineageClient = sqlParser.getTableLineageClient();
        return tableLineageClient.parseTableLineages(stmt);
    }

}