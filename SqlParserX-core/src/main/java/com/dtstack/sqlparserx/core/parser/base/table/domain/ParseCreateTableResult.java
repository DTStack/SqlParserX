/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.core.parser.base.table.domain;

import com.dtstack.sqlparserx.core.parser.base.table.createtable.domain.TableDistributedBuckets;
import com.dtstack.sqlparserx.core.parser.base.table.createtable.domain.TableElement;
import com.dtstack.sqlparserx.core.parser.base.table.createtable.domain.TableKey;
import com.dtstack.sqlparserx.core.parser.base.table.createtable.domain.TablePartition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description  解析create table表info 包含分区表、分桶表、普通表等
 * @author changlu
 * @date 2025/8/4 11:02
 */
public class ParseCreateTableResult {
    // 是否带有if not exists判断
    private boolean isNotExists;
    // 建表所属db
    private String db;
    // 建表名称
    private String tableName;
    // 建表注释
    private String tableComment;
    // 表字段属性
    List<TableElement> tableElementList;
    // 分区字段属性【无指定类型情况】
    List<TableElement> partitionedByList;
    // 所属表键分类 主键、唯一键、聚合键等
    private TableKey tableKey;
    // 表分桶
    private TableDistributedBuckets tableDistributedBuckets;
    // 表分区
    private TablePartition tablePartition;
    // 表properties参数
    private Map<String, String> properties;

    public ParseCreateTableResult() {
    }

    public boolean isNotExists() {
        return isNotExists;
    }

    public void setNotExists(boolean notExists) {
        isNotExists = notExists;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public List<TableElement> getTableElementList() {
        return tableElementList;
    }

    public void setTableElementList(List<TableElement> tableElementList) {
        this.tableElementList = tableElementList;
    }

    public List<TableElement> getPartitionedByList() {
        return partitionedByList;
    }

    public void setPartitionedByList(List<TableElement> partitionedByList) {
        this.partitionedByList = partitionedByList;
    }

    public TableKey getTableKey() {
        return tableKey;
    }

    public void setTableKey(TableKey tableKey) {
        this.tableKey = tableKey;
    }

    public TableDistributedBuckets getTableDistributedBuckets() {
        return tableDistributedBuckets;
    }

    public void setTableDistributedBuckets(TableDistributedBuckets tableDistributedBuckets) {
        this.tableDistributedBuckets = tableDistributedBuckets;
    }

    public TablePartition getTablePartition() {
        return tablePartition;
    }

    public void setTablePartition(TablePartition tablePartition) {
        this.tablePartition = tablePartition;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ParseCreateTableResult that = (ParseCreateTableResult) o;
        return isNotExists == that.isNotExists && Objects.equals(db, that.db) && Objects.equals(tableName,
                that.tableName) && Objects.equals(tableComment, that.tableComment) && Objects.equals(tableElementList,
                        that.tableElementList) && Objects.equals(partitionedByList, that.partitionedByList) && Objects
                                .equals(tableKey, that.tableKey) && Objects.equals(tableDistributedBuckets,
                                        that.tableDistributedBuckets) && Objects.equals(tablePartition,
                                                that.tablePartition) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isNotExists, db, tableName, tableComment, tableElementList, partitionedByList, tableKey,
                tableDistributedBuckets, tablePartition, properties);
    }

    @Override
    public String toString() {
        return "ParseCreateTableResult{" +
                "isNotExists=" + isNotExists +
                ", db='" + db + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableComment='" + tableComment + '\'' +
                ", tableElementList=" + tableElementList +
                ", partitionedByList=" + partitionedByList +
                ", tableKey=" + tableKey +
                ", tableDistributedBuckets=" + tableDistributedBuckets +
                ", tablePartition=" + tablePartition +
                ", properties=" + properties +
                '}';
    }
}
