package com.changlu.lineage.column;

import com.alibaba.druid.sql.ast.statement.SQLSelect;

import java.util.List;

public class SelectSpecialLineageService {

    public List<SelectColumn> getSelectColumns(SQLSelect select) {
        return null;
    }

}

class SelectColumn {

    private String columnName;
    private String columnAlias;
    private String tableName;

}
