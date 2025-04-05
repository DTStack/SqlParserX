package com.changlu.sqlparserx.enums;
import com.alibaba.druid.DbType;

public enum DataSourceType2DbType {
    MYSQL(1, DbType.mysql),
    POSTGRESQL(2, DbType.postgresql),
    ORACLE(3, DbType.oracle),
    SQLSERVER(4, DbType.sqlserver),
    HIVE(5, DbType.hive);

    private final int code;
    private final DbType dbType;

    DataSourceType2DbType(int code, DbType dbType) {
        this.code = code;
        this.dbType = dbType;
    }

    public int getCode() {
        return code;
    }

    public DbType getDbType() {
        return dbType;
    }

    /**
     * 根据数据源类型代码获取对应的DbType
     * @param dataSourceType 数据源类型代码
     * @return 对应的DbType，如果找不到则返回null
     */
    public static DbType getDbType(int dataSourceType) {
        for (DataSourceType2DbType type : values()) {
            if (type.getCode() == dataSourceType) {
                return type.getDbType();
            }
        }
        return DbType.mysql;
    }

    /**
     * 根据数据源类型代码获取对应的枚举实例
     * @param dataSourceType 数据源类型代码
     * @return 对应的枚举实例，如果找不到则返回null
     */
    public static DataSourceType2DbType getByCode(int dataSourceType) {
        for (DataSourceType2DbType type : values()) {
            if (type.getCode() == dataSourceType) {
                return type;
            }
        }
        return DataSourceType2DbType.MYSQL;
    }
}
