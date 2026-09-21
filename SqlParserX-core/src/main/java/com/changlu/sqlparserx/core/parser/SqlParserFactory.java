package com.changlu.sqlparserx.core.parser;

import com.changlu.sqlparserx.core.parser.base.BaseSqlParser;
import com.changlu.sqlparserx.core.parser.clickhouse.ClickHouseSqlParser;
import com.changlu.sqlparserx.core.parser.dm.DmSqlParser;
import com.changlu.sqlparserx.core.parser.doris.DorisSqlParser;
import com.changlu.sqlparserx.core.parser.greenplum.GreenplumSqlParser;
import com.changlu.sqlparserx.core.parser.hive.HiveSqlParser;
import com.changlu.sqlparserx.core.parser.mysql.MySqlParser;
import com.changlu.sqlparserx.core.parser.oceanbase.OceanBaseSqlParser;
import com.changlu.sqlparserx.core.parser.oceanbase_oracle.OceanBaseOracleSqlParser;
import com.changlu.sqlparserx.core.parser.oracle.OracleSqlParser;
import com.changlu.sqlparserx.core.parser.postgresql.PostgresqlSqlParser;
import com.changlu.sqlparserx.core.parser.spark.SparkSqlParser;
import com.changlu.sqlparserx.core.parser.sqlserver.SqlServerSqlParser;
import com.changlu.sqlparserx.core.parser.starrocks.StarrocksSqlParser;
import com.changlu.sqlparserx.core.parser.tidb.TidbSqlParser;
import com.changlu.sqlparserx.core.parser.trino.TrinoSqlParser;

import com.alibaba.druid.DbType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 可根据不同数据源类型来完成不同组件的使用
 */
public class SqlParserFactory {

    /**
     * 当前核心库实际实现了解析器的方言集合（与 {@link #getSqlParser(DbType)} 保持一致）
     */
    private static final Set<DbType> SUPPORTED_DB_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            DbType.mysql,
            DbType.hive,
            DbType.trino,
            DbType.oracle,
            DbType.greenplum,
            DbType.tidb,
            DbType.postgresql,
            DbType.sqlserver,
            DbType.starrocks,
            DbType.doris,
            DbType.oceanbase,
            DbType.oceanbase_oracle,
            DbType.dm,
            DbType.clickhouse,
            DbType.spark)));

    /**
     * 判断指定的 Druid DbType 是否为核心库实际支持的方言（有独立解析器实现，非回退默认解析器）
     * @param dbType DbType
     * @return true=支持
     */
    public static boolean supports(DbType dbType) {
        return dbType != null && SUPPORTED_DB_TYPES.contains(dbType);
    }

    /**
     * 获取核心库实际支持的方言集合（不可变）
     * @return Set<DbType>
     */
    public static Set<DbType> getSupportedDbTypes() {
        return SUPPORTED_DB_TYPES;
    }

    /**
     * 基于Druid的dbType来实现
     * @param dbType DbType
     * @return ISqlParser
     */
    public static ISqlParser getSqlParser(DbType dbType) {
        if (dbType == DbType.mysql) {
            return new MySqlParser();
        } else if (dbType == DbType.hive) {
            return new HiveSqlParser();
        } else if (dbType == DbType.trino) {
            return new TrinoSqlParser();
        } else if (dbType == DbType.oracle) {
            return new OracleSqlParser();
        } else if (dbType == DbType.greenplum) {
            return new GreenplumSqlParser();
        } else if (dbType == DbType.tidb) {
            return new TidbSqlParser();
        } else if (dbType == DbType.postgresql) {
            return new PostgresqlSqlParser();
        } else if (dbType == DbType.sqlserver) {
            return new SqlServerSqlParser();
        } else if (dbType == DbType.starrocks) {
            return new StarrocksSqlParser();
        } else if (dbType == DbType.doris) {
            return new DorisSqlParser();
        } else if (dbType == DbType.oceanbase) {
            return new OceanBaseSqlParser();
        } else if (dbType == DbType.oceanbase_oracle) {
            return new OceanBaseOracleSqlParser();
        } else if (dbType == DbType.dm) {
            return new DmSqlParser();
        } else if (dbType == DbType.clickhouse) {
            return new ClickHouseSqlParser();
        } else if (dbType == DbType.spark) {
            return new SparkSqlParser();
        }
        return new BaseSqlParser();
    }

    // 根据可接入不同场景去自行提供转换

}
