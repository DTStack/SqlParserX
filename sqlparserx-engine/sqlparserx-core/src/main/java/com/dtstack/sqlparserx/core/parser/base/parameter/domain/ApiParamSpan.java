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

import java.util.List;

/**
 * @author chener
 * @Classname ApiParamSpan
 * @Description API 输出字段定义
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

    public ApiParamSpan() {
    }

    public ApiParamSpan(String sqlPart) {
        this.sqlPart = sqlPart;
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

}
