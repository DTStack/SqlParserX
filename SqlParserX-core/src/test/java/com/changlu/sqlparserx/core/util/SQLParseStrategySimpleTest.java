package com.changlu.sqlparserx.core.util;

import com.changlu.sqlparserx.core.enums.SQLParseStrategy;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @description 解析策略枚举测试
 * @author changlu
 * @date 2025/7/24 11:17
 */
public class SQLParseStrategySimpleTest {

    // 测试构造函数和mask赋值
    @Test
    public void test_ConstructorAndMask() {
        assertEquals(1, SQLParseStrategy.COLUMN_IGNORE_CASE.mask);
    }

    // 测试isEnabled方法
    @Test
    public void testIsEnabled() {
        // 启用COLUMN_IGNORE_CASE策略
        int strategies = SQLParseStrategy.COLUMN_IGNORE_CASE.mask;
        assertTrue(SQLParseStrategy.isEnabled(strategies, SQLParseStrategy.COLUMN_IGNORE_CASE));
        
        // 没有启用任何策略
        assertFalse(SQLParseStrategy.isEnabled(0, SQLParseStrategy.COLUMN_IGNORE_CASE));
    }

    // 测试config方法 - 启用策略
    @Test
    public void testConfigEnable() {
        int original = 0;
        int updated = SQLParseStrategy.config(original, SQLParseStrategy.COLUMN_IGNORE_CASE, true);
        assertTrue(SQLParseStrategy.isEnabled(updated, SQLParseStrategy.COLUMN_IGNORE_CASE));
    }

    // 测试config方法 - 禁用策略
    @Test
    public void testConfigDisable() {
        int original = SQLParseStrategy.COLUMN_IGNORE_CASE.mask;
        int updated = SQLParseStrategy.config(original, SQLParseStrategy.COLUMN_IGNORE_CASE, false);
        assertFalse(SQLParseStrategy.isEnabled(updated, SQLParseStrategy.COLUMN_IGNORE_CASE));
    }

    // 测试of方法 - 单个策略
    @Test
    public void testOfSingleStrategy() {
        int result = SQLParseStrategy.of(SQLParseStrategy.COLUMN_IGNORE_CASE);
        assertEquals(SQLParseStrategy.COLUMN_IGNORE_CASE.mask, result);
    }

    // 测试of方法 - 空数组
    @Test
    public void testOfEmptyArray() {
        int result = SQLParseStrategy.of();
        assertEquals(0, result);
    }
}