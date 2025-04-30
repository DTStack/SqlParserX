package com.changlu.core.parser.base.lineage.domain;

import java.util.Objects;

/**
 * 字段血缘
 */
public class ColumnLineage{
    private String fromCatalog;
    private String fromDb;
    private String fromTable;
    private String fromColumn;

    private String toCatalog;
    private String toDb;
    private String toTable;
    private String toColumn;

    public ColumnLineage() {
    }

    public ColumnLineage(String fromCatalog, String fromDb, String fromTable, String fromColumn, String toCatalog, String toDb, String toTable, String toColumn) {
        this.fromCatalog = fromCatalog;
        this.fromDb = fromDb;
        this.fromTable = fromTable;
        this.fromColumn = fromColumn;
        this.toCatalog = toCatalog;
        this.toDb = toDb;
        this.toTable = toTable;
        this.toColumn = toColumn;
    }

    public String getFromCatalog() {
        return fromCatalog;
    }

    public void setFromCatalog(String fromCatalog) {
        this.fromCatalog = fromCatalog;
    }

    public String getFromDb() {
        return fromDb;
    }

    public void setFromDb(String fromDb) {
        this.fromDb = fromDb;
    }

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable(String fromTable) {
        this.fromTable = fromTable;
    }

    public String getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(String fromColumn) {
        this.fromColumn = fromColumn;
    }

    public String getToCatalog() {
        return toCatalog;
    }

    public void setToCatalog(String toCatalog) {
        this.toCatalog = toCatalog;
    }

    public String getToDb() {
        return toDb;
    }

    public void setToDb(String toDb) {
        this.toDb = toDb;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable(String toTable) {
        this.toTable = toTable;
    }

    public String getToColumn() {
        return toColumn;
    }

    public void setToColumn(String toColumn) {
        this.toColumn = toColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnLineage that = (ColumnLineage) o;
        return Objects.equals(fromCatalog, that.fromCatalog) && Objects.equals(fromDb, that.fromDb) && Objects.equals(fromTable, that.fromTable) && Objects.equals(fromColumn, that.fromColumn) && Objects.equals(toCatalog, that.toCatalog) && Objects.equals(toDb, that.toDb) && Objects.equals(toTable, that.toTable) && Objects.equals(toColumn, that.toColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCatalog, fromDb, fromTable, fromColumn, toCatalog, toDb, toTable, toColumn);
    }

    @Override
    public String toString() {
        return "ColumnLineage{" +
                "fromCatalog='" + fromCatalog + '\'' +
                ", fromDb='" + fromDb + '\'' +
                ", fromTable='" + fromTable + '\'' +
                ", fromColumn='" + fromColumn + '\'' +
                ", toCatalog='" + toCatalog + '\'' +
                ", toDb='" + toDb + '\'' +
                ", toTable='" + toTable + '\'' +
                ", toColumn='" + toColumn + '\'' +
                '}';
    }
}