package com.dtstack.sqlparser.sqlparserx.core.tmp;

import java.util.HashSet;
import java.util.Set;

public class ParameterHandler implements TokenHandler{

    Set<String> sets = new HashSet<>();

    @Override
    public String handleToken(String content) {
        sets.add(content);
        return "?";
    }
}
