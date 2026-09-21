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
package com.dtstack.sqlparserx.core;

import com.dtstack.sqlparserx.core.base.BaseSuite;
import com.dtstack.sqlparserx.core.datasource.DatasourceSuite;
import com.dtstack.sqlparserx.core.util.SQLParseStrategySimpleTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @description 全局聚合单测套件
 * @author changlu
 * @date 2025/4/23
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // 基础通用模板测试
        BaseSuite.class,
        // 数据源集合测试
        DatasourceSuite.class,
        // 工具类集合测试
        SQLParseStrategySimpleTest.class
})
public class AllTestsSuite {
}