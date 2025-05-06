package com.changlu.core;

import com.changlu.core.base.BaseSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @description 全局聚合单测套件
 * @author changlu
 * @date 2025/4/23
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // 基础通用模板测试
        BaseSuite.class,
        // 数据源集合测试
})
public class AllTestsSuite {
}