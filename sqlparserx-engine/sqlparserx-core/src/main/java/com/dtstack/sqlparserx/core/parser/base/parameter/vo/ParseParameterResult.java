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

import com.dtstack.sqlparserx.core.parser.base.lineage.table.Table;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ApiParamSpan;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;

import java.util.List;

public class ParseParameterResult {

    // 输出参数集合
    private List<ApiParamSpan> outputParams;

    // 输入参数集合
    private List<ParamInfo> inputParams;

    // 表集合
    private List<Table> tables;

    public ParseParameterResult() {
    }

    public ParseParameterResult(List<ParamInfo> inputParams, List<ApiParamSpan> outputParams, List<Table> tables) {
        this.inputParams = inputParams;
        this.outputParams = outputParams;
        this.tables = tables;
    }

    public List<ApiParamSpan> getOutputParams() {
        return outputParams;
    }
    public void setOutputParams(List<ApiParamSpan> outputParams) {
        this.outputParams = outputParams;
    }
    public List<ParamInfo> getInputParams() {
        return inputParams;
    }
    public void setInputParams(List<ParamInfo> inputParams) {
        this.inputParams = inputParams;
    }
    public List<Table> getTables() {
        return tables;
    }
    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

}
