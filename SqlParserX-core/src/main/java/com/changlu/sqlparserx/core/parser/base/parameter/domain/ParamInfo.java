package com.changlu.sqlparserx.core.parser.base.parameter.domain;

import java.util.Objects;

/**
 * @description  参数键值对
 * @author changlu
 * @date 2025/5/15 17:32
 */
public class ParamInfo {
    /**
     * 参数名
     */
    private String name;

    /**
     * 参数值
     */
    private Object value;

    /**
     * 是否必填
     */
    private boolean imperative;

    /**
     * 参数类型
     */
    private String dataType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否是常量
     */
    private boolean constant;

    /**
     * 是否必须要
     */
    private boolean required;

    /**
     * 字段描述
     */
    private String desc;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 操作类型
     */
    private String operator;

    /**
     * 组id
     */
    private Integer groupId;

    public ParamInfo() {
    }

    public ParamInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isImperative() {
        return imperative;
    }

    public void setImperative(boolean imperative) {
        this.imperative = imperative;
    }

    /**
     * 检查参数是否是必填参数且没有填写值
     */
    private void checkValueOmit() {
        if (this.imperative && Objects.isNull(this.value)) {
            throw new RuntimeException("必填参数" + this.name + "未没有值");
        }
    }

    public boolean hasValue() {
        checkValueOmit();
        return Objects.nonNull(this.value);
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

}
