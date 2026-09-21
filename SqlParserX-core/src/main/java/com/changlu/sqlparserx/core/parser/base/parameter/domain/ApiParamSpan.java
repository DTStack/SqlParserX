package com.changlu.sqlparserx.core.parser.base.parameter.domain;

import java.util.List;

/**
 * @author chener
 * @Classname ApiParamSpan
 * @Description 该类代码一个api的参数块。例如
 *      1. where id = ${id}中，参数块是id = ${id}；
 *      2. where id = ${id} and concat(name,${suf}) like ${name}中，有两个参数块：id = ${id}和concat(name,${suf} like ${name}
 * @Date 2020/12/15 15:54
 * @Created chener@dtstack.com
 */
public class ApiParamSpan {

    /**
     * 参数块全名，也就是sql片段
     */
    private String sqlPart;

    /**
     * 作为出参时，代表别名
     */
    private String alias;

    /**
     * 参数块中使用的表字段【实际场景 api只会取其中的第一个】
     */
    private List<DataTypeColumn> bindColumns;

    /**
     * 参数块中使用的参数
     * 如果参数中任意一个参数为非必填且没有填写，则该块失效
     */
    private List<String> params;

    /**
     * 操作符
     */
    private String operator;

    private boolean mustFlag;

    /**limit是否为最外层**/
    private Boolean isOutLayer = false;

    /**是否为常量**/
    private Boolean isConst = false;

    /**常量值**/
    private String constValue;

    public ApiParamSpan() {
    }

    public ApiParamSpan(String sqlPart, String operator) {
        this.sqlPart = sqlPart;
        this.operator = operator;
    }

    public String getSqlPart() {
        return sqlPart;
    }

    public void setSqlPart(String sqlPart) {
        this.sqlPart = sqlPart;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<DataTypeColumn> getBindColumns() {
        return bindColumns;
    }

    public void setBindColumns(List<DataTypeColumn> bindColumns) {
        this.bindColumns = bindColumns;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setMustFlag(boolean mustFlag) {
        this.mustFlag = mustFlag;
    }

    public boolean getMustFlag() {
        return this.mustFlag;
    }

    public Boolean getOutLayer() {
        return isOutLayer;
    }

    public void setOutLayer(Boolean outLayer) {
        isOutLayer = outLayer;
    }

    public boolean isMustFlag() {
        return mustFlag;
    }

    public Boolean getConst() {
        return isConst;
    }

    public void setConst(Boolean aConst) {
        isConst = aConst;
    }

    public String getConstValue() {
        return constValue;
    }

    public void setConstValue(String constValue) {
        this.constValue = constValue;
    }
}
