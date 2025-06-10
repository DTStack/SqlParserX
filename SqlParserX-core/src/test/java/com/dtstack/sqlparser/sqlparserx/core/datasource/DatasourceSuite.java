package com.dtstack.sqlparser.sqlparserx.core.datasource;

import com.dtstack.sqlparser.sqlparserx.core.datasource.doris.DorisColumnLineageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // doris
        DorisColumnLineageTest.class
})
public class DatasourceSuite {
}
