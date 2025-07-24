package com.changlu.sqlparserx.core;

import com.changlu.sqlparserx.core.base.BaseSuite;
import com.changlu.sqlparserx.core.datasource.DatasourceSuite;
import com.changlu.sqlparserx.core.util.SQLParseStrategySimpleTest;
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
        DatasourceSuite.class,
        // 工具类集合测试
        SQLParseStrategySimpleTest.class
})
public class AllTestsSuite {
}