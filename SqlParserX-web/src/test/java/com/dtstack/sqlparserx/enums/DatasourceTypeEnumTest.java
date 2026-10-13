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
package com.dtstack.sqlparserx.enums;

import com.dtstack.sqlparserx.core.enums.SqlParserDataSourceType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

class DatasourceTypeEnumTest {

    @Test
    void shouldMapEveryCoreDataSourceToOneWebEnumValue() {
        Set<SqlParserDataSourceType> webTypes = Arrays.stream(DatasourceTypeEnum.values())
                .map(DatasourceTypeEnum::getSqlParserDataSourceType)
                .collect(Collectors.toSet());

        Assertions.assertEquals(SqlParserDataSourceType.values().length, DatasourceTypeEnum.values().length);
        Assertions.assertEquals(SqlParserDataSourceType.values().length, webTypes.size());
        Assertions.assertEquals(Arrays.stream(SqlParserDataSourceType.values()).collect(Collectors.toSet()), webTypes);
    }
}
