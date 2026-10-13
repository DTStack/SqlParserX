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
package com.dtstack.sqlparserx.core.parser.doris.parameter;

import com.dtstack.sqlparserx.core.parser.base.parameter.transfer.BaseParamDataTypeTransfer;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Locale;

/**
 * @description Doris参数字段类型转换器
 * @author changlu
 * @date 2026/10/12 16:20
 */
public class DorisParamDataTypeTransfer extends BaseParamDataTypeTransfer {
    @Override
    public Object transfer(String dataType, Object value) {
        if (value == null || dataType == null)
            return value;
        String type = dataType.trim().toUpperCase(Locale.ROOT);
        try {
            if (type.startsWith("BOOLEAN"))
                return value instanceof Boolean ? value : Boolean.valueOf(value.toString());
            if (type.startsWith("DATETIME"))
                return value instanceof Timestamp ? value : Timestamp.valueOf(value.toString());
            if (type.startsWith("DATE"))
                return value instanceof Date ? value : Date.valueOf(value.toString());
            if (type.startsWith("LARGEINT"))
                return value.toString();
        } catch (RuntimeException exception) {
            return value.toString();
        }
        return super.transfer(dataType, value);
    }
}
