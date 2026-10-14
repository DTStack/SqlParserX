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
package com.dtstack.sqlparserx.core.enums;

import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.clickhouse.ClickHouseSqlParser;
import com.dtstack.sqlparserx.core.parser.dm.DmSqlParser;
import com.dtstack.sqlparserx.core.parser.doris.DorisSqlParser;
import com.dtstack.sqlparserx.core.parser.greenplum.GreenplumSqlParser;
import com.dtstack.sqlparserx.core.parser.hive.HiveSqlParser;
import com.dtstack.sqlparserx.core.parser.mysql.MySqlParser;
import com.dtstack.sqlparserx.core.parser.oceanbase.OceanBaseSqlParser;
import com.dtstack.sqlparserx.core.parser.oceanbase_oracle.OceanBaseOracleSqlParser;
import com.dtstack.sqlparserx.core.parser.oracle.OracleSqlParser;
import com.dtstack.sqlparserx.core.parser.postgresql.PostgresqlSqlParser;
import com.dtstack.sqlparserx.core.parser.spark.SparkSqlParser;
import com.dtstack.sqlparserx.core.parser.sqlserver.SqlServerSqlParser;
import com.dtstack.sqlparserx.core.parser.starrocks.StarrocksSqlParser;
import com.dtstack.sqlparserx.core.parser.tidb.TidbSqlParser;
import com.dtstack.sqlparserx.core.parser.trino.TrinoSqlParser;

import com.alibaba.druid.DbType;

import java.util.function.Supplier;

/**
 * SqlParserX Core 业务解析层支持的数据源类型。
 *
 * <p>该枚举是 Core 支持范围及解析器初始化方式的唯一来源。Druid {@link DbType}
 * 仅作为底层 SQL 方言标识，不再决定 SqlParserX 支持哪些数据源。</p>
 */
public enum SqlParserDataSourceType {

    MYSQL(1, DbType.mysql, MySqlParser::new), HIVE(2, DbType.hive, HiveSqlParser::new), TRINO(3, DbType.trino,
            TrinoSqlParser::new), ORACLE(4, DbType.oracle, OracleSqlParser::new), GREENPLUM(5, DbType.greenplum,
                    GreenplumSqlParser::new), TIDB(6, DbType.tidb, TidbSqlParser::new), POSTGRESQL(7, DbType.postgresql,
                            PostgresqlSqlParser::new), SQLSERVER(8, DbType.sqlserver,
                                    SqlServerSqlParser::new), STARROCKS(9, DbType.starrocks,
                                            StarrocksSqlParser::new), DORIS(10, DbType.doris,
                                                    DorisSqlParser::new), OCEANBASE(11, DbType.oceanbase,
                                                            OceanBaseSqlParser::new), OCEANBASE_ORACLE(12,
                                                                    DbType.oceanbase_oracle,
                                                                    OceanBaseOracleSqlParser::new), DM(13, DbType.dm,
                                                                            DmSqlParser::new), CLICKHOUSE(14,
                                                                                    DbType.clickhouse,
                                                                                    ClickHouseSqlParser::new), SPARK(15,
                                                                                            DbType.spark,
                                                                                            SparkSqlParser::new);

    private final int value;
    private final DbType dbType;
    private final Supplier<ISqlParser> parserSupplier;

    SqlParserDataSourceType(int value, DbType dbType, Supplier<ISqlParser> parserSupplier) {
        this.value = value;
        this.dbType = dbType;
        this.parserSupplier = parserSupplier;
    }

    public int getValue() {
        return value;
    }

    public DbType getDbType() {
        return dbType;
    }

    public ISqlParser createSqlParser() {
        return parserSupplier.get();
    }

    /**
     * 将底层 Druid 方言转换为 Core 支持的数据源类型。
     *
     * @return 匹配的数据源类型；Core 不支持或参数为空时返回 null
     */
    public static SqlParserDataSourceType fromDbType(DbType dbType) {
        if (dbType == null) {
            return null;
        }
        for (SqlParserDataSourceType dataSourceType : values()) {
            if (dataSourceType.dbType == dbType) {
                return dataSourceType;
            }
        }
        return null;
    }

    /**
     * Find the core data source type by its stable numeric value.
     *
     * @return matching data source type; {@code null} when the value is unknown
     */
    public static SqlParserDataSourceType fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (SqlParserDataSourceType dataSourceType : values()) {
            if (dataSourceType.value == value) {
                return dataSourceType;
            }
        }
        return null;
    }
}
