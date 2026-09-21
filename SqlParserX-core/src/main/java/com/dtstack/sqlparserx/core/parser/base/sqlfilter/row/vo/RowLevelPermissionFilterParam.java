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
package com.dtstack.sqlparserx.core.parser.base.sqlfilter.row.vo;
import java.util.List;

public class RowLevelPermissionFilterParam {

    /**
     * 表名称,这个是包含了catalog和schema
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段值
     */
    private List<Object> fieldValue;

    /**
     * 符号
     */
    private OperatorEnum operatorEnum;

    /**
     * 链接符
     */
    private LinkOperatorEnum linkOperatorEnum;

    /**
     * 字段类型
     */
    private String fieldType;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<Object> getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(List<Object> fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public OperatorEnum getOperatorEnum() {
        return operatorEnum;
    }

    public void setOperatorEnum(OperatorEnum operatorEnum) {
        this.operatorEnum = operatorEnum;
    }

    public LinkOperatorEnum getLinkOperatorEnum() {
        return linkOperatorEnum;
    }

    public void setLinkOperatorEnum(LinkOperatorEnum linkOperatorEnum) {
        this.linkOperatorEnum = linkOperatorEnum;
    }

    public enum OperatorEnum {
        IN(1, " IN "), NOT_IN(2, " NOT IN "), EQ(3, " = "), NOT_EQ(4, " != "), NULL(5, " is null");

        final int code;

        final String opera;

        OperatorEnum(int code, String opera) {
            this.code = code;
            this.opera = opera;
        }

        public int getCode() {
            return code;
        }

        public String getOpera() {
            return opera;
        }
    }

    public enum LinkOperatorEnum {
        AND(1, " AND "), OR(2, " OR ");

        final int code;

        final String opera;

        LinkOperatorEnum(int code, String opera) {
            this.code = code;
            this.opera = opera;
        }

        public int getCode() {
            return code;
        }

        public String getOpera() {
            return opera;
        }
    }

}
