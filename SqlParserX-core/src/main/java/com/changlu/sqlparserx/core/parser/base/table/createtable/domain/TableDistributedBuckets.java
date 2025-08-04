package com.changlu.sqlparserx.core.parser.base.table.createtable.domain;

import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.DistributedByTypeEnum;

import java.util.List;

public class TableDistributedBuckets {

    private DistributedByTypeEnum distributedByTypeEnum;

    private Integer bucketNum;

    private List<String> columnNames;

    public DistributedByTypeEnum getDistributedByTypeEnum() {
        return distributedByTypeEnum;
    }

    public void setDistributedByTypeEnum(DistributedByTypeEnum distributedByTypeEnum) {
        this.distributedByTypeEnum = distributedByTypeEnum;
    }

    public Integer getBucketNum() {
        return bucketNum;
    }

    public void setBucketNum(Integer bucketNum) {
        this.bucketNum = bucketNum;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
}