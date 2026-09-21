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
package com.dtstack.sqlparserx.core.parser.base.parameter.vo;

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
