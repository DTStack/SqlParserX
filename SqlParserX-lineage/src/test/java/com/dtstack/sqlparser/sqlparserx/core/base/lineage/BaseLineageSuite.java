package com.dtstack.sqlparser.sqlparserx.core.base.lineage;

import com.dtstack.sqlparser.sqlparserx.core.base.lineage.column.BaseColumnLineageCreateTableAsSelectTest;
import com.dtstack.sqlparser.sqlparserx.core.base.lineage.column.BaseColumnLineageInsertSelectTest;
import com.dtstack.sqlparser.sqlparserx.core.base.lineage.column.BaseColumnLineageWithAsInsertTest;
import com.dtstack.sqlparser.sqlparserx.core.base.lineage.table.BaseTableLineageCreateTableAsSelectTest;
import com.dtstack.sqlparser.sqlparserx.core.base.lineage.table.BaseTableLineageInsertSelectTest;
import com.dtstack.sqlparser.sqlparserx.core.base.lineage.table.BaseTableLineageWithAsInsertTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // base 字段血缘
        BaseColumnLineageCreateTableAsSelectTest.class,
        BaseColumnLineageInsertSelectTest.class,
        BaseColumnLineageWithAsInsertTest.class,
        // base 表血缘
        BaseTableLineageCreateTableAsSelectTest.class,
        BaseTableLineageInsertSelectTest.class,
        BaseTableLineageWithAsInsertTest.class
})
public class BaseLineageSuite {
}
