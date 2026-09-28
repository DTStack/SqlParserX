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

import com.dtstack.sqlparserx.core.parser.SqlParserFactory;

import com.alibaba.druid.DbType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps the Web data-source code to Druid's dialect type.
 *
 * <p>Compatibility mappings are retained for old clients. Web APIs use
 * {@link #isCoreSupported(int)} so only independently implemented core
 * parsers are advertised and accepted as supported dialects.</p>
 */
public enum DataSourceType2DbType {
    ORACLE(DbType.oracle, DatasourceTypeEnum.ORACLE), OCEANBASE_ORACLE(DbType.oceanbase_oracle,
            DatasourceTypeEnum.OCEANBASE_ORACLE), MYSQL(DbType.mysql, DatasourceTypeEnum.MYSQL), TIDB(DbType.tidb,
                    DatasourceTypeEnum.TIDB), MARIADB(DbType.mariadb, DatasourceTypeEnum.MARIADB), GOLDENDB(
                            DbType.mysql, DatasourceTypeEnum.GOLDENDB), OCEANBASE(DbType.oceanbase,
                                    DatasourceTypeEnum.OCEANBASE), DRDS(DbType.drds, DatasourceTypeEnum.DRDS), POLARDBX(
                                            DbType.polardb, DatasourceTypeEnum.POLARDBX), ELASTIC_SEARCH(
                                                    DbType.elastic_search,
                                                    DatasourceTypeEnum.ELASTIC_SEARCH), POSTGRESQL(DbType.postgresql,
                                                            DatasourceTypeEnum.POSTGRESQL), GREENPLUM(DbType.greenplum,
                                                                    DatasourceTypeEnum.GREENPLUM), EDB(DbType.edb,
                                                                            DatasourceTypeEnum.EDB), GAUSSDB(
                                                                                    DbType.postgresql,
                                                                                    DatasourceTypeEnum.GAUSSDB), HOLOGRES(
                                                                                            DbType.postgresql,
                                                                                            DatasourceTypeEnum.HOLOGRES), REDSHIFT(
                                                                                                    DbType.postgresql,
                                                                                                    DatasourceTypeEnum.REDSHIFT), SQLSERVER(
                                                                                                            DbType.sqlserver,
                                                                                                            DatasourceTypeEnum.SQLSERVER), JTDS(
                                                                                                                    DbType.jtds,
                                                                                                                    DatasourceTypeEnum.JTDS), H2(
                                                                                                                            DbType.h2,
                                                                                                                            DatasourceTypeEnum.H2), LEALONE(
                                                                                                                                    DbType.lealone,
                                                                                                                                    DatasourceTypeEnum.LEALONE), BLINK(
                                                                                                                                            DbType.blink,
                                                                                                                                            DatasourceTypeEnum.BLINK), DB2(
                                                                                                                                                    DbType.db2,
                                                                                                                                                    DatasourceTypeEnum.DB2), ODPS(
                                                                                                                                                            DbType.odps,
                                                                                                                                                            DatasourceTypeEnum.ODPS), PHOENIX(
                                                                                                                                                                    DbType.phoenix,
                                                                                                                                                                    DatasourceTypeEnum.PHOENIX), HIVE(
                                                                                                                                                                            DbType.hive,
                                                                                                                                                                            DatasourceTypeEnum.HIVE), PRESTO(
                                                                                                                                                                                    DbType.presto,
                                                                                                                                                                                    DatasourceTypeEnum.PRESTO), TRINO(
                                                                                                                                                                                            DbType.trino,
                                                                                                                                                                                            DatasourceTypeEnum.TRINO), ATHENA(
                                                                                                                                                                                                    DbType.athena,
                                                                                                                                                                                                    DatasourceTypeEnum.ATHENA), BIGQUERY(
                                                                                                                                                                                                            DbType.bigquery,
                                                                                                                                                                                                            DatasourceTypeEnum.BIGQUERY), SNOWFLAKE(
                                                                                                                                                                                                                    DbType.snowflake,
                                                                                                                                                                                                                    DatasourceTypeEnum.SNOWFLAKE), SPARK(
                                                                                                                                                                                                                            DbType.spark,
                                                                                                                                                                                                                            DatasourceTypeEnum.SPARK), CLICKHOUSE(
                                                                                                                                                                                                                                    DbType.clickhouse,
                                                                                                                                                                                                                                    DatasourceTypeEnum.CLICKHOUSE), STARROCKS(
                                                                                                                                                                                                                                            DbType.starrocks,
                                                                                                                                                                                                                                            DatasourceTypeEnum.STARROCKS), INFORMIX(
                                                                                                                                                                                                                                                    DbType.informix,
                                                                                                                                                                                                                                                    DatasourceTypeEnum.INFORMIX), IMPALA(
                                                                                                                                                                                                                                                            DbType.impala,
                                                                                                                                                                                                                                                            DatasourceTypeEnum.IMPALA), DORIS(
                                                                                                                                                                                                                                                                    DbType.doris,
                                                                                                                                                                                                                                                                    DatasourceTypeEnum.DORIS), OSCAR(
                                                                                                                                                                                                                                                                            DbType.oscar,
                                                                                                                                                                                                                                                                            DatasourceTypeEnum.OSCAR), TERADATA(
                                                                                                                                                                                                                                                                                    DbType.teradata,
                                                                                                                                                                                                                                                                                    DatasourceTypeEnum.TERADATA), DM(
                                                                                                                                                                                                                                                                                            DbType.dm,
                                                                                                                                                                                                                                                                                            DatasourceTypeEnum.DM);

    private final Set<Integer> codes;
    private final DbType dbType;

    DataSourceType2DbType(DbType dbType, DatasourceTypeEnum dataSourceType) {
        this.dbType = dbType;
        this.codes = Collections.singleton(dataSourceType.getCode());
    }

    public Set<Integer> getCodes() {
        return codes;
    }

    public DbType getDbType() {
        return dbType;
    }

    /** Returns true only when the code maps one-to-one to a parser in core. */
    public static boolean isCoreSupported(int dataSourceType) {
        DataSourceType2DbType mapping = findByCode(dataSourceType);
        return mapping != null && SqlParserFactory.supports(mapping.dbType)
                && isIndependentCoreDialect(dataSourceType);
    }

    /** The Web option list is derived from the core parser registry. */
    public static Set<Integer> getCoreSupportedCodes() {
        Set<Integer> result = new HashSet<>();
        for (DatasourceTypeEnum type : DatasourceTypeEnum.values()) {
            if (isCoreSupported(type.getCode())) {
                result.add(type.getCode());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static boolean isIndependentCoreDialect(int code) {
        return code == DatasourceTypeEnum.ORACLE.getCode()
                || code == DatasourceTypeEnum.OCEANBASE_ORACLE.getCode()
                || code == DatasourceTypeEnum.MYSQL.getCode()
                || code == DatasourceTypeEnum.TIDB.getCode()
                || code == DatasourceTypeEnum.OCEANBASE.getCode()
                || code == DatasourceTypeEnum.POSTGRESQL.getCode()
                || code == DatasourceTypeEnum.GREENPLUM.getCode()
                || code == DatasourceTypeEnum.SQLSERVER.getCode()
                || code == DatasourceTypeEnum.HIVE.getCode()
                || code == DatasourceTypeEnum.TRINO.getCode()
                || code == DatasourceTypeEnum.SPARK.getCode()
                || code == DatasourceTypeEnum.CLICKHOUSE.getCode()
                || code == DatasourceTypeEnum.STARROCKS.getCode()
                || code == DatasourceTypeEnum.DORIS.getCode()
                || code == DatasourceTypeEnum.DM.getCode();
    }

    /** Returns the mapped DbType, preserving the historical MySQL fallback. */
    public static DbType getDbType(int dataSourceType) {
        DataSourceType2DbType mapping = findByCode(dataSourceType);
        return mapping == null ? DbType.mysql : mapping.dbType;
    }

    public static DataSourceType2DbType getByCode(int dataSourceType) {
        DataSourceType2DbType mapping = findByCode(dataSourceType);
        return mapping == null ? MYSQL : mapping;
    }

    private static DataSourceType2DbType findByCode(int dataSourceType) {
        return Arrays.stream(values())
                .filter(type -> type.codes.contains(dataSourceType))
                .findFirst()
                .orElse(null);
    }
}
