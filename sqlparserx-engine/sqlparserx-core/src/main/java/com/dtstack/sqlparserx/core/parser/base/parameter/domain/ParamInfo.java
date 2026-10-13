/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.core.parser.base.parameter.domain;

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
     * 参数块对应的 SQL 片段（标明该参数来自源 SQL 的哪一部分）
     */
    private String sqlPart;

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

    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public String getOperator() {
        return operator;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }
    public String getSqlPart() {
        return sqlPart;
    }
    public void setSqlPart(String sqlPart) {
        this.sqlPart = sqlPart;
    }
}
