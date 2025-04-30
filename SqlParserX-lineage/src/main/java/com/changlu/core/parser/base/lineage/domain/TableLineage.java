package com.changlu.core.parser.base.lineage.domain;

import java.util.Objects;

public class TableLineage {
    private String fromCatalog;
    private String fromDb;
    private String fromTable;
    private String toCatalog;
    private String toDb;
    private String toTable;

    // Getters and Setters
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

    // toString()
    @Override
    public String toString() {
        return "TableLineage{" +
                "fromCatalog='" + fromCatalog + '\'' +
                ", fromDb='" + fromDb + '\'' +
                ", fromTable='" + fromTable + '\'' +
                ", toCatalog='" + toCatalog + '\'' +
                ", toDb='" + toDb + '\'' +
                ", toTable='" + toTable + '\'' +
                '}';
    }

    // equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableLineage that = (TableLineage) o;
        return Objects.equals(fromCatalog, that.fromCatalog) &&
                Objects.equals(fromDb, that.fromDb) &&
                Objects.equals(fromTable, that.fromTable) &&
                Objects.equals(toCatalog, that.toCatalog) &&
                Objects.equals(toDb, that.toDb) &&
                Objects.equals(toTable, that.toTable);
    }

    // hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(fromCatalog, fromDb, fromTable, toCatalog, toDb, toTable);
    }
}