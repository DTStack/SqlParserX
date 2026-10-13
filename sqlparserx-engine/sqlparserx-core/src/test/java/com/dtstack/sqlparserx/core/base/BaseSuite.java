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
package com.dtstack.sqlparserx.core.base;

import com.dtstack.sqlparserx.core.base.function.BaseParseFunctionTest;
import com.dtstack.sqlparserx.core.base.lineage.BaseLineageSuite;
import com.dtstack.sqlparserx.core.base.parameter.BaseParseParameterClientTest;
import com.dtstack.sqlparserx.core.base.sqlfilter.BaseFilterSortSelectFieldTest;
import com.dtstack.sqlparserx.core.base.sqltype.BaseParseSqlTypeClientTest;
import com.dtstack.sqlparserx.core.base.tables.BaseParseTablesTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // 血缘测试集合 包含字段、表血缘
        BaseLineageSuite.class,
        // 解析表单测
        BaseParseTablesTest.class,
        // 解析函数单测
        BaseParseFunctionTest.class,
        // 变量参数解析与预编译单测
        BaseParseParameterClientTest.class,
        // SQL 语句类型解析单测
        BaseParseSqlTypeClientTest.class,
        // SELECT 字段排序单测
        BaseFilterSortSelectFieldTest.class
})
public class BaseSuite {
}
