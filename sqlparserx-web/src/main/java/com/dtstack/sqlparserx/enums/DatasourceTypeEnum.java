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

public enum DatasourceTypeEnum {

    ORACLE(1, SqlParserDataSourceType.ORACLE), OCEANBASE_ORACLE(2, SqlParserDataSourceType.OCEANBASE_ORACLE), MYSQL(3,
            SqlParserDataSourceType.MYSQL), TIDB(4, SqlParserDataSourceType.TIDB), OCEANBASE(7,
                    SqlParserDataSourceType.OCEANBASE), POSTGRESQL(11, SqlParserDataSourceType.POSTGRESQL), GREENPLUM(
                            12, SqlParserDataSourceType.GREENPLUM), SQLSERVER(17,
                                    SqlParserDataSourceType.SQLSERVER), HIVE(25, SqlParserDataSourceType.HIVE), TRINO(
                                            27, SqlParserDataSourceType.TRINO), SPARK(31,
                                                    SqlParserDataSourceType.SPARK), CLICKHOUSE(32,
                                                            SqlParserDataSourceType.CLICKHOUSE), STARROCKS(33,
                                                                    SqlParserDataSourceType.STARROCKS), DORIS(36,
                                                                            SqlParserDataSourceType.DORIS), DM(39,
                                                                                    SqlParserDataSourceType.DM);

    private final int code;
    private final SqlParserDataSourceType sqlParserDataSourceType;

    DatasourceTypeEnum(int code, SqlParserDataSourceType sqlParserDataSourceType) {
        this.code = code;
        this.sqlParserDataSourceType = sqlParserDataSourceType;
    }

    public int getCode() {
        return code;
    }

    public SqlParserDataSourceType getSqlParserDataSourceType() {
        return sqlParserDataSourceType;
    }
}
