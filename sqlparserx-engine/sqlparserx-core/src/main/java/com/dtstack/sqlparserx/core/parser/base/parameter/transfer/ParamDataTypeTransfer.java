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
package com.dtstack.sqlparserx.core.parser.base.parameter.transfer;

/**
 * @description 参数字段类型转换接口，用于「SQL 转 API」预编译场景下的参数值类型适配。
 *              <p>
 *              <b>用途场景</b>：用户手写带 {@code ${变量}} 的 SQL，平台分两步处理，类型转换发生在运行期：
 *              <ol>
 *              <li>配置期 {@code parseParameterSql}：解析变量绑定的真实表字段，把字段的方言类型写入
 *              {@code ParamInfo.dataType} 随 API 配置落库；</li>
 *              <li>运行期 {@code buildSqlByPrepareCompile}：依据 {@code columns[param].dataType} 决定每个
 *              {@code ?} 的绑定类型，调用本接口把请求值转成 JDBC 绑定对象，按顺序写入
 *              {@code PrepareCompileResult.inputFields} 供绑定执行。</li>
 *              </ol>
 *              <p>
 *              <b>使用位置</b>：由 {@code PrepareSqlRewriter} 持有，在把 {@code ${变量}} 改写为 {@code ?} 时对每个
 *              参数值调用；具体实现通过对应 Parser 注入，默认实现为 {@code BaseParamDataTypeTransfer}，各数据源可在
 *              自身 {@code parser/<datasource>/parameter} 包下提供方言转换实现。
 *              <p>
 *              <b>约定</b>：{@code dataType} 或 {@code value} 为空时原样返回；无法识别的非空类型按字符串处理；
 *              转换异常应回退为原始值而不是抛出，保证预编译流程不中断。
 * @author changlu
 * @date 2026/10/12 15:30
 */
public interface ParamDataTypeTransfer {

    /**
     * 将参数原始值转换为预编译绑定对象
     * @param dataType 数据源方言原始类型字符串（如 BIGINT / VARCHAR(32) / DECIMAL(10,2)），可为空
     * @param value 调用方传入的原始值，可为空
     * @return 绑定到 PreparedStatement 的 Java 对象
     */
    Object transfer(String dataType, Object value);
}
