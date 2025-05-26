package com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter;

import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlFilterClient;

import java.util.LinkedHashSet;

public class BaseSqlFilterClient implements ISqlFilterClient {

    @Override
    public String filterSortSelectField(String sql, LinkedHashSet<String> outputParams) {

        return "";
    }

}
