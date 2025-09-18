package com.changlu.sqlparserx.core.datasource.doris;

import com.alibaba.druid.DbType;
import com.changlu.sqlparserx.core.common.BaseColumnLineageAnalyzerTest;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.changlu.sqlparserx.core.parser.base.table.createtable.domain.TableElement;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.DistributedByTypeEnum;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TableKeyEnum;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TablePartitionEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;

public class DorisCreateTableTest  extends BaseColumnLineageAnalyzerTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.doris);
    private String sqlTxtName = "DorisCreateTableTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/datasource/doris/";
    }

    @Test
    public void test_01_creatable_commonTable() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        ParseCreateTableResult parseCreateTableResult = sqlParser.parseCreateTable(sql);
//        System.out.println(parseCreateTableResult);

        assertEquals(null, parseCreateTableResult.getDb());
        assertEquals("ods_gate_common_rebate_ref_code_df", parseCreateTableResult.getTableName());
        assertEquals("用户邀请码表", parseCreateTableResult.getTableComment());
        assertEquals(8, parseCreateTableResult.getTableElementList().size());
        assertEquals(new TableElement("part_date", "date", new ArrayList<>(), null, "分区字段 按日分区", null, null, null), parseCreateTableResult.getTableElementList().get(0));
        // 校验表字段指定类型
        assertEquals(TableKeyEnum.UNIQUE, parseCreateTableResult.getTableKey().getTableKeyEnum());
        assertEquals(Arrays.asList("part_date", "id"), parseCreateTableResult.getTableKey().getColumnName());
        // 校验分桶字段
        assertEquals(DistributedByTypeEnum.Hash, parseCreateTableResult.getTableDistributedBuckets().getDistributedByTypeEnum());
        assertEquals(3, parseCreateTableResult.getTableDistributedBuckets().getBucketNum().intValue());
        assertEquals(Arrays.asList("id"), parseCreateTableResult.getTableDistributedBuckets().getColumnNames());
        // 校验分区类型
        assertEquals(TablePartitionEnum.DYNAMIC, parseCreateTableResult.getTablePartition().getTablePartitionEnum());
        assertEquals(1, parseCreateTableResult.getTablePartition().getColumnName().size());
        assertEquals(9, parseCreateTableResult.getTablePartition().getTablePartitionElement().size());
        assertEquals("p20250624", parseCreateTableResult.getTablePartition().getTablePartitionElement().get(0).getName());
        assertEquals(Arrays.asList("2025-06-24", "2025-06-25"), parseCreateTableResult.getTablePartition().getTablePartitionElement().get(0).getValues());
        // 校验配置项
        assertEquals(26, parseCreateTableResult.getProperties().size());
        assertEquals(true, parseCreateTableResult.getProperties().containsKey("min_load_replica_num"));
        assertEquals("-1", parseCreateTableResult.getProperties().get("min_load_replica_num"));
    }

    // 明细表
    @Test
    public void test_02_creatable_mingxi() {
        String sql = readSqlByIndex(sqlTxtName, 2);
        ParseCreateTableResult parseCreateTableResult = sqlParser.parseCreateTable(sql);
        assertEquals(TableKeyEnum.DUPLICATE, parseCreateTableResult.getTableKey().getTableKeyEnum());
    }

    // 聚合表
    @Test
    public void test_03_creatable_aggregate() {
        String sql = readSqlByIndex(sqlTxtName, 3);
        ParseCreateTableResult parseCreateTableResult = sqlParser.parseCreateTable(sql);
        assertEquals(TableKeyEnum.AGGREGATE, parseCreateTableResult.getTableKey().getTableKeyEnum());
    }

    // 分区类型 PARTITION BY List
    @Test
    public void test_04_creatable_list() {
        String sql = readSqlByIndex(sqlTxtName, 4);
        ParseCreateTableResult parseCreateTableResult = sqlParser.parseCreateTable(sql);
        assertEquals(TablePartitionEnum.LIST, parseCreateTableResult.getTablePartition().getTablePartitionEnum());
        assertEquals("REPLACE", parseCreateTableResult.getTableElementList().get(3).getAggType());
    }


    // 分区类型 为DYNAMIC
    @Test
    public void test_05_creatable_DYNAMIC() {
        String sql = readSqlByIndex(sqlTxtName, 5);
        ParseCreateTableResult parseCreateTableResult = sqlParser.parseCreateTable(sql);
        assertEquals(TablePartitionEnum.DYNAMIC, parseCreateTableResult.getTablePartition().getTablePartitionEnum());
    }

}
