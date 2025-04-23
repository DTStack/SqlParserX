package com.changlu.lineage.ColumnLineageAnalyzerTest;


import com.changlu.lineage.ColumnLineageAnalyzerTest.basic.CreateTableAsSelectTest;
import com.changlu.lineage.ColumnLineageAnalyzerTest.basic.InsertSelectTest;
import com.changlu.lineage.ColumnLineageAnalyzerTest.basic.WithAsInsertTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @description 字段血缘聚合单测
 * @author changlu
 * @date 2025/4/23 10:25
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CreateTableAsSelectTest.class,
        InsertSelectTest.class,
        WithAsInsertTest.class
})
public class ColumnLineageTestSuite {
}
