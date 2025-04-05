package com.changlu.lineage.column;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.domain.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnLineageAnalyzer {

    public ColumnLineageResult analyze(String sql) {
        return analyze(sql, new HashMap<>());
    }
    
    public ColumnLineageResult analyze(String sql, Map<String, List<Column>> tableColumnsMap) {
        return analyze(sql, tableColumnsMap, DbType.mysql);
    }

    public ColumnLineageResult analyze(String sql, Map<String, List<Column>> tableColumnsMap, DbType dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement stmt = parser.parseStatement();

        ColumnLineageVisitor visitor = new ColumnLineageVisitor(tableColumnsMap);
        stmt.accept(visitor);

        return visitor.getResult();
    }
}