package com.changlu.sqlparserx.core.parser.base.table.domain;

import com.changlu.sqlparserx.core.parser.base.lineage.table.Table;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description  解析表结果
 * @author changlu
 * @date 2025/4/30 17:16
 */
public class ParseTableResult {
    private Table mainTable;
    private Set<Table> allTables;

    public ParseTableResult() {
    }

    public ParseTableResult(Table mainTable, Set<Table> allTables) {
        this.mainTable = mainTable;
        this.allTables = allTables;
        ;
    }

    public Table getMainTable() {
        return mainTable;
    }

    public void setMainTable(Table mainTable) {
        this.mainTable = mainTable;
    }

    public Set<Table> getAllTables() {
        return allTables;
    }

    public void setAllTables(Set<Table> allTables) {
        this.allTables = allTables;
    }

    // 对外暴露
    public Set<String> getAllTableName() {
        Set<String> tables = this.allTables.stream().map((table -> Table.TableTransfer.transfer(table))).collect(Collectors.toSet());
        return tables;
    }

    @Override
    public String toString() {
        return "ParseTableResult{" +
                "mainTable=" + mainTable +
                ", allTables=" + allTables +
                '}';
    }
}