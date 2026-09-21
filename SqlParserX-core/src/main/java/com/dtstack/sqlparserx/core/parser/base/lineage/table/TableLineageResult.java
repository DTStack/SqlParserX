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
package com.dtstack.sqlparserx.core.parser.base.lineage.table;

import com.dtstack.sqlparserx.core.parser.base.lineage.domain.TableLineage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @ClassName TableLineageResult
 * @Author ChangLu
 * @Date 2025/3/30 21:08
 * @Description 表血缘结果
 */
public class TableLineageResult {
    private final String targetTable;
    private final Set<String> sourceTables;
    private final Set<TableRelation> relations;
    private final List<TableLineage> tableLineages;

    public TableLineageResult(String targetTable, Set<String> sourceTables, Set<TableRelation> relations) {
        this.targetTable = targetTable;
        this.sourceTables = sourceTables;
        this.relations = relations;
        this.tableLineages = buildTableLineages(relations);
    }

    public String getTargetTable() {
        return targetTable;
    }

    public Set<String> getSourceTables() {
        return sourceTables;
    }

    public Set<TableRelation> getRelations() {
        return relations;
    }

    /**
     * 对外提供的table血缘集合
     */
    private List<TableLineage> buildTableLineages(Set<TableRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<TableLineage> tableLineages = new ArrayList<>();
        for (TableRelation relation : relations) {
            Table sourceTable = relation.getSourceTableObj();
            Table targetTable = relation.getTargetTableObj();
            // 创建关联关系
            TableLineage tableLineage = new TableLineage();
            // 来源表
            tableLineage.setFromCatalog(sourceTable.getCatalog());
            tableLineage.setFromDb(sourceTable.getDb());
            tableLineage.setFromTable(sourceTable.getName());
            // 目标表
            tableLineage.setToCatalog(targetTable.getCatalog());
            tableLineage.setToDb(targetTable.getDb());
            tableLineage.setToTable(targetTable.getName());

            tableLineages.add(tableLineage);
        }
        return tableLineages;
    }

    public List<TableLineage> getTableLineages() {
        return tableLineages;
    }

    @Override
    public String toString() {
        return "TableLineageResult{" +
                "targetTable='" + targetTable + '\'' +
                ", sourceTables=" + sourceTables +
                ", relations=" + relations +
                '}';
    }
}
