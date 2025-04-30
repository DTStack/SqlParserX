package com.changlu.core.parser.base.lineage.table;

import java.util.Objects;

public class TableRelation {
    private final String sourceTable;
    private final String targetTable;

    public TableRelation(String sourceTable, String targetTable) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public Table getSourceTableObj() {
        return Table.TableTransfer.parse(this.sourceTable);
    }

    public Table getTargetTableObj() {
        return Table.TableTransfer.parse(this.targetTable);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableRelation that = (TableRelation) o;
        return Objects.equals(sourceTable, that.sourceTable) && Objects.equals(targetTable, that.targetTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTable, targetTable);
    }
}