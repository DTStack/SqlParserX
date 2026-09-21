package com.changlu.sqlparserx.enums;

public enum DatasourceTypeEnum {

    ORACLE(1), OCEANBASE_ORACLE(2), MYSQL(3), TIDB(4), MARIADB(5), GOLDENDB(6), OCEANBASE(7), DRDS(8), POLARDBX(
            9), ELASTIC_SEARCH(10), POSTGRESQL(11), GREENPLUM(12), EDB(13), GAUSSDB(14), HOLOGRES(15), REDSHIFT(
                    16), SQLSERVER(17), JTDS(18), H2(19), LEALONE(20), BLINK(21), DB2(22), ODPS(23), PHOENIX(24), HIVE(
                            25), PRESTO(26), TRINO(27), ATHENA(28), BIGQUERY(29), SNOWFLAKE(30), SPARK(31), CLICKHOUSE(
                                    32), STARROCKS(33), INFORMIX(34), IMPALA(35), DORIS(36), OSCAR(37), TERADATA(38);

    private final int code;

    DatasourceTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}