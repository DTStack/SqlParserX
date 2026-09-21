package com.changlu.sqlparserx.core.base;

import com.changlu.sqlparserx.core.base.function.BaseParseFunctionTest;
import com.changlu.sqlparserx.core.base.lineage.BaseLineageSuite;
import com.changlu.sqlparserx.core.base.sqlfilter.BaseFilterSortSelectFieldTest;
import com.changlu.sqlparserx.core.base.tables.BaseParseTablesTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // 血缘测试集合 包含字段、表血缘
        BaseLineageSuite.class,
        // 解析表单测
        BaseParseTablesTest.class,
        // 解析函数单测
        BaseParseFunctionTest.class,
        // 解析函数单测
        BaseFilterSortSelectFieldTest.class
})
public class BaseSuite {
}
