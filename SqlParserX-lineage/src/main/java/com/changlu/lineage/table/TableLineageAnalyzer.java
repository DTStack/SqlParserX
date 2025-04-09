package com.changlu.lineage.table;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

public class TableLineageAnalyzer {
    private final DbType dbType;
    
    public TableLineageAnalyzer(DbType dbType) {
        this.dbType = dbType;
    }
    
    public TableLineageResult analyze(String sql) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement stmt = parser.parseStatement();
        
        TableLineageVisitor visitor = new TableLineageVisitor();
        stmt.accept(visitor);
        return visitor.getResult();
    }

}