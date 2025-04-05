package com.changlu.lineage.table;

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

    public TableLineageResult(String targetTable, Set<String> sourceTables, Set<TableRelation> relations) {
        this.targetTable = targetTable;
        this.sourceTables = sourceTables;
        this.relations = relations;
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

    @Override
    public String toString() {
        return "TableLineageResult{" +
                "targetTable='" + targetTable + '\'' +
                ", sourceTables=" + sourceTables +
                ", relations=" + relations +
                '}';
    }
}
