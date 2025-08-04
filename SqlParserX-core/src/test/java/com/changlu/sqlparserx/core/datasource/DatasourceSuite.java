package com.changlu.sqlparserx.core.datasource;

import com.changlu.sqlparserx.core.datasource.doris.DorisColumnLineageTest;
import com.changlu.sqlparserx.core.datasource.doris.DorisCreateTableTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // doris
        DorisColumnLineageTest.class,
        DorisCreateTableTest.class
})
public class DatasourceSuite {
}
