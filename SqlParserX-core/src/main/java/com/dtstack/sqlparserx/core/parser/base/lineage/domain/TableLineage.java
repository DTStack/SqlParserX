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
package com.dtstack.sqlparserx.core.parser.base.lineage.domain;

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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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