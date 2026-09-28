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
package com.dtstack.sqlparserx.core.common;

import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import java.util.*;

import static org.junit.Assert.*;

public class BaseColumnLineageAnalyzerTest extends AbstractSqlParserTest {

    /**
     * 验证关联关系
     * @param relations
     * @param target 目标端字段 table.column
     * @param source 来源字段 table.column
     */
    public void assertRelation(Map<String, List<ColumnLineageResult.ColumnRelation>> relations,
            String target, String source) {
        List<ColumnLineageResult.ColumnRelation> rels = relations.get(target);
        if (rels == null)
            assertTrue(false);
        boolean flag = false;
        // check指定来源字段中的关联关系中包含有期望来源：expectedSource
        for (ColumnLineageResult.ColumnRelation rel : rels) {
            if (rel.getSource().equals(source)) {
                flag = true;
                break;
            }
        }
        assertTrue(flag);
    }
}