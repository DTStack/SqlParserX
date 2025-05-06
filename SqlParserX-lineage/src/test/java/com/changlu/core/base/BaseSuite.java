package com.changlu.core.base;

import com.changlu.core.base.lineage.BaseLineageSuite;
import com.changlu.core.base.tables.BaseParseTablesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // 血缘测试集合 包含字段、表血缘
        BaseLineageSuite.class,
        // 解析表单测
        BaseParseTablesTest.class
})
public class BaseSuite {
}
