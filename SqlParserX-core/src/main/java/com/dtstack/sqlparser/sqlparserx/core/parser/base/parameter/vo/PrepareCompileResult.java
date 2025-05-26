package com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.vo;

import java.util.List;

public class PrepareCompileResult {

    // 预编译结果集合
    private List<Object> inputFields;
    // 预编译之后的sql
    private String beautifulSql;

    public PrepareCompileResult() {
    }

    public PrepareCompileResult(List<Object> inputFields, String beautifulSql) {
        this.inputFields = inputFields;
        this.beautifulSql = beautifulSql;
    }

    public String getBeautifulSql() {
        return beautifulSql;
    }

    public void setBeautifulSql(String beautifulSql) {
        this.beautifulSql = beautifulSql;
    }

    public List<Object> getInputFields() {
        return inputFields;
    }

    public void setInputFields(List<Object> inputFields) {
        this.inputFields = inputFields;
    }

    @Override
    public String toString() {
        return "PrepareCompileResult{" +
                "inputFields=" + inputFields +
                ", beautifulSql='" + beautifulSql + '\'' +
                '}';
    }
}
