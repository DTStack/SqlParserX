package com.changlu.sqlparserx.core.enums;

/**
 * SQL解析策略配置（包含列名匹配、表名匹配等可扩展策略）通用场景
 */
public enum SQLParseStrategy {
    // ===== 列名匹配策略 =====
    /**
     * 列名忽略大小写匹配
     */
    COLUMN_IGNORE_CASE,
    /**
     * 特性：子查询字段忽略大小写匹配
     *  示范：select的字段匹配子查询的字段时忽略大小写【doris支持这种写法】
     *      insert into new_table
     *      select id, name from (
     *         select ID, NAME
     *         from table1
     *      ) t1
     */
    SUBQUERY_COLUMN_IGNORE_CASE,

    // ===== CTE 相关策略 =====
    /**
     * 特性：CTE 支持向前引用（Forward Reference）
     *  当前支持CTE 支持向前引用数据源类型：Doris。
     *
     *  示范：WITH 子句中允许后面的 CTE 引用前面未定义的 CTE
     *      WITH
     *      t1 AS (SELECT * FROM t2),  -- 引用了后面定义的 t2
     *      t2 AS (SELECT * FROM table)
     *      SELECT * FROM t1
     *
     *  注：某些数据库（如 SQL Server）支持这种写法，但标准的 SQL 和大多数数据库（如 PostgreSQL、MySQL）不支持。
     *  开启此策略后，解析器会自动处理 CTE 的依赖关系。
     */
    CTE_FORWARD_REFERENCE,
    ;

    SQLParseStrategy() {
        mask = (1 << ordinal());
    }

    public final int mask;

    /**
     * 检查指定策略是否启用
     * @param strategies 当前策略组合值（位掩码）
     * @param strategy 要检查的策略枚举
     * @return 当策略已启用时返回true
     * @throws IllegalArgumentException 如果strategy为null
     */
    public static boolean isEnabled(int strategies, SQLParseStrategy strategy) {
        return (strategies & strategy.mask) != 0;
    }

    /**
     * 修改策略状态
     * @param strategies 原始策略组合值
     * @param strategy 要修改的策略
     * @param enable 是否启用该策略
     * @return 更新后的策略组合值
     * @throws IllegalArgumentException 如果strategy为null
     */
    public static int config(int strategies, SQLParseStrategy strategy, boolean enable) {
        return enable ?
            strategies | strategy.mask : 
            strategies & ~strategy.mask;
    }

    /**
     * 创建策略组合
     * @param features 要组合的策略数组
     * @return 组合后的策略位掩码
     *         （空输入返回DEFAULT_STRATEGIES，null输入抛出异常）
     * @throws IllegalArgumentException 如果strategies数组为null
     */
    public static int of(SQLParseStrategy... features) {
        if (features == null) return 0;
        
        int value = 0;
        for (SQLParseStrategy s : features) {
            value |= s.mask;
        }
        return value;
    }
}