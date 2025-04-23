package com.changlu;

import com.changlu.lineage.ColumnLineageAnalyzerTest.ColumnLineageTestSuite;
import com.changlu.lineage.TableLineageAnalyzerTest.TableLineageTestSuite;
import com.changlu.parts.TableSourceUtilTest.ParseTablesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @description 全局聚合单测套件
 * @author changlu
 * @date 2025/4/23
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ParseTablesTest.class,          // 数据源工具类单测
        TableLineageTestSuite.class,        // 表级血缘分析测试套件
        ColumnLineageTestSuite.class       // 字段级血缘分析测试套件
})
public class AllTestsSuite {
    // 这个类不需要任何实现，仅作为测试套件的容器
}