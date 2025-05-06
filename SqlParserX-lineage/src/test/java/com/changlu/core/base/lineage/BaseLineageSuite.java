package com.changlu.core.base.lineage;

import com.changlu.core.base.lineage.column.BaseColumnLineageCreateTableAsSelectTest;
import com.changlu.core.base.lineage.column.BaseColumnLineageInsertSelectTest;
import com.changlu.core.base.lineage.column.BaseColumnLineageWithAsInsertTest;
import com.changlu.core.base.lineage.table.BaseTableLineageCreateTableAsSelectTest;
import com.changlu.core.base.lineage.table.BaseTableLineageInsertSelectTest;
import com.changlu.core.base.lineage.table.BaseTableLineageWithAsInsertTest;
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
