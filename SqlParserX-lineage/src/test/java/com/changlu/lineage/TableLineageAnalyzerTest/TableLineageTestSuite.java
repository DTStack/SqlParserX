package com.changlu.lineage.TableLineageAnalyzerTest;


import com.changlu.lineage.TableLineageAnalyzerTest.basic.CreateTableAsSelectTest;
import com.changlu.lineage.TableLineageAnalyzerTest.basic.InsertSelectTest;
import com.changlu.lineage.TableLineageAnalyzerTest.basic.WithAsInsertTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @description 表级血缘聚合单测
 * @author changlu
 * @date 2025/4/23 10:25
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CreateTableAsSelectTest.class,
        InsertSelectTest.class,
        WithAsInsertTest.class
})
public class TableLineageTestSuite {
}
