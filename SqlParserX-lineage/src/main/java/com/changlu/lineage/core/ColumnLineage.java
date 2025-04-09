package com.changlu.lineage.core;

import java.util.Objects;

/**
 * 字段血缘
 */
public class ColumnLineage extends TableLineage {
    private String fromColumn;
    private String fromColumnType;
    private String toColumn;
    private String toColumnType;

    // Getters and Setters
    public String getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(String fromColumn) {
        this.fromColumn = fromColumn;
    }

    public String getFromColumnType() {
        return fromColumnType;
    }

    public void setFromColumnType(String fromColumnType) {
        this.fromColumnType = fromColumnType;
    }

    public String getToColumn() {
        return toColumn;
    }

    public void setToColumn(String toColumn) {
        this.toColumn = toColumn;
    }

    public String getToColumnType() {
        return toColumnType;
    }

    public void setToColumnType(String toColumnType) {
        this.toColumnType = toColumnType;
    }

    // toString()
    @Override
    public String toString() {
        return "ColumnLineage{" +
                "fromCatalog='" + getFromCatalog() + '\'' +
                ", fromDb='" + getFromDb() + '\'' +
                ", fromTable='" + getFromTable() + '\'' +
                ", fromColumn='" + fromColumn + '\'' +
                ", fromColumnType='" + fromColumnType + '\'' +
                ", toCatalog='" + getToCatalog() + '\'' +
                ", toDb='" + getToDb() + '\'' +
                ", toTable='" + getToTable() + '\'' +
                ", toColumn='" + toColumn + '\'' +
                ", toColumnType='" + toColumnType + '\'' +
                '}';
    }

    // equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ColumnLineage that = (ColumnLineage) o;
        return Objects.equals(fromColumn, that.fromColumn) &&
                Objects.equals(fromColumnType, that.fromColumnType) &&
                Objects.equals(toColumn, that.toColumn) &&
                Objects.equals(toColumnType, that.toColumnType);
    }

    // hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromColumn, fromColumnType, toColumn, toColumnType);
    }
}