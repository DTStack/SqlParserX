package com.changlu.sqlparserx.core.parser.doris.table;

import com.changlu.sqlparserx.core.parser.starrocks.table.StarrocksParseCreateTableClient;

import java.util.Map;

// 继承 StarrocksParseTableClient 以复用其逻辑
public class DorisParseCreateTableClient extends StarrocksParseCreateTableClient {

    @Override
    protected boolean isDynamicPartitionEnabled() {
        return true;
    }

    @Override
    protected boolean analyzeHasDynamicPartitionTypeProperties(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return false;
        }
        // 是否是否有分析识别到时动态分区属性
        String dynamicPartition = properties.get("dynamic_partition.enable");
        return "true".equalsIgnoreCase(dynamicPartition);
    }
}
