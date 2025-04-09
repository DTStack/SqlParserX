package com.changlu.lineage.column;

import com.changlu.lineage.core.select.column.SelectColumn;
import org.junit.Assert;
import org.junit.Test;

public class SelectColumnTest {

    @Test
    public void test_01_filterDuplicateRealColumns() {
        SelectColumn selectColumn = new SelectColumn();
        selectColumn.setAliasName("cc1");
        selectColumn.setTableInfo("table1,table4,table1");
        selectColumn.setColumnName("col1,col1,col1");
        // 过滤重复列
        SelectColumn dealSelectColumn = SelectColumn.filterDuplicateRealColumns(selectColumn);
        Assert.assertEquals(dealSelectColumn.getAliasName(), "cc1");
        Assert.assertEquals(dealSelectColumn.getTableInfo(), "table1,table4");
        Assert.assertEquals(dealSelectColumn.getColumnName(), "col1,col1");
    }

}
