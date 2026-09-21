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
package com.dtstack.sqlparserx.core.parser.base.table.createtable.domain;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TableElement {
    private String name;
    private String type;
    private List<String> arguments;
    private String polymerizeType;
    private String comment;
    /**
     * 字段策略：0 可以 null, 1 不可以 null
     */
    private Integer columnStrategy;

    /**
     * 字段精度
     */
    private Integer scale;

    /**
     * 字段精度
     */
    private Integer precision;

    /**
     * 聚合类型
     */
    private String aggType;

    public TableElement() {
    }

    public TableElement(String name, String type, List<String> arguments, String polymerizeType, String comment,
            Integer columnStrategy, Integer scale, Integer precision) {
        this.name = name;
        this.type = type;
        this.arguments = arguments;
        this.polymerizeType = polymerizeType;
        this.comment = comment;
        this.columnStrategy = columnStrategy;
        this.scale = scale;
        this.precision = precision;
    }
}