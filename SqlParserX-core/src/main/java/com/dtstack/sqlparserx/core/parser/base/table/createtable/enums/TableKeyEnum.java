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
package com.dtstack.sqlparserx.core.parser.base.table.createtable.enums;

public enum TableKeyEnum {

    // DUPLICATE(1)重复键
    // AGGREGATE(2)聚合键
    // UNIQUE(3)唯一键
    // PRIMARY(4)主键
    DUPLICATE(1), AGGREGATE(2), UNIQUE(3), PRIMARY(4);

    private final Integer code;

    TableKeyEnum(int code) {
        this.code = code;
    }

    public static TableKeyEnum getByCode(int type) {
        TableKeyEnum[] values = TableKeyEnum.values();

        for (TableKeyEnum value : values) {
            if (value.code == type) {
                return value;
            }
        }
        return null;
    }
}
