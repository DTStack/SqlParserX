package com.changlu.visitor;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.changlu.lineage.core.select.table.SelectTable;
import com.changlu.lineage.core.select.table.SelectTableSpecialLineageService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 解析真实表
 * @author changlu
 * @date 2025/4/22 19:20
 */
public class ParseTableVisitor extends SQLASTVisitorAdapter {

    private final DbType dbType;
    private Set<String> sourceTables = new HashSet<>();
    private SelectTableSpecialLineageService selectTableSpecialLineageService;

    public ParseTableVisitor(DbType dbType) {
        this.dbType = dbType;
        this.selectTableSpecialLineageService = new SelectTableSpecialLineageService();
    }

    @Override
    public boolean visit(SQLSelectStatement x) {
        return false;
    }

    @Override
    public boolean visit(SQLUpdateStatement x) {
        return false;
    }

    @Override
    public boolean visit(SQLDeleteStatement x) {
        return false;
    }

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public boolean visit(SQLCreateTableStatement x) {
        // 处理CREATE TABLE AS SELECT 其他场景无血缘情况
        SQLSelect select = x.getSelect();
        if (select != null) {
            SQLSelectQuery selectQuery = select.getQuery();
            // select结构中的来源表
            List<SelectTable> selectTables = selectTableSpecialLineageService.queryTablesBySqlSelectQuery(selectQuery);
            // 添加到原始表中
            addSourceTable(selectTables);
        }
        return false;
    }

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public boolean visit(SQLInsertStatement x) {
        // 2、获取到主体查询的一组tables
        SQLSelect sqlSelect = x.getQuery();
        List<SelectTable> selectTables = selectTableSpecialLineageService.queryTablesBySqlSelect(sqlSelect);
        // 3、with as场景处理
        Map<String, List<SelectTable>> tableInfoByTableMap = new HashMap<>();
        if (x.getWith() != null) {
            SQLWithSubqueryClause with = x.getWith();
            List<SQLWithSubqueryClause.Entry> entries = with.getEntries();
            if (!entries.isEmpty()) {
                // 遍历每一组with as结构
                for (SQLWithSubqueryClause.Entry entry : entries) {
                    // as 的别名
                    String aliasTableName = entry.getAlias();
                    SQLSelect withSubquerySQLSelect = entry.getSubQuery();
                    List<SelectTable> withSubqueryTables = selectTableSpecialLineageService.queryTablesBySqlSelect(withSubquerySQLSelect);
                    tableInfoByTableMap.put(aliasTableName, withSubqueryTables);
                }
            }

            // 处理with as场景 & 主体selct字段情况 过滤掉主体中引用with as 别名表字段情况
            selectTables = matchWithAsAndSelectTables(selectTables, tableInfoByTableMap);
        }

        // 添加到原始表中
        addSourceTable(selectTables);
        return false;
    }

    /**
     * 添加到原始来源表
     */
    public void addSourceTable(List<SelectTable> selectTables) {
        for (SelectTable selectTable : selectTables) {
            String[] sourceTableArr = selectTable.getTableInfo().split(",");
            Collections.addAll(this.sourceTables, sourceTableArr);
        }
    }

    /**
     * with as 场景处理：当前只考虑完成selectTables中具有的别名替换
     *  注意：这块逻辑表血缘 与 表解析有一些小区别（表解析应当将with as中的所有表字段全部解析拿到的）
     */
    private List<SelectTable> matchWithAsAndSelectTables(List<SelectTable> selectTables, Map<String, List<SelectTable>> tableInfoByTableMap) {
        Map<String, String> aliasNameBySourceTables = tableInfoByTableMap.entrySet().stream()
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream()
                                        .map(SelectTable::getTableInfo)
                                        .collect(Collectors.joining(","))
                        )
                );

        // 重新构建匹配select主体得到的表
        List<SelectTable> newSelectTables = new ArrayList<>();
        for (SelectTable selectTable : selectTables) {
            List<String> tableGroup = new ArrayList<>();
            String[] selectTableArr = selectTable.getTableInfo().split(",");
            for (String tableName : selectTableArr) {
                tableGroup.add(aliasNameBySourceTables.getOrDefault(tableName, tableName));
            }
            newSelectTables.add(new SelectTable(String.join(",", tableGroup), selectTable.getAliasName()));
        }
        return newSelectTables;
    }

    public DbType getDbType() {
        return dbType;
    }

    public Set<String> getSourceTables() {
        return sourceTables;
    }

    public void setSourceTables(Set<String> sourceTables) {
        this.sourceTables = sourceTables;
    }

    public SelectTableSpecialLineageService getSelectTableSpecialLineageService() {
        return selectTableSpecialLineageService;
    }

    public void setSelectTableSpecialLineageService(SelectTableSpecialLineageService selectTableSpecialLineageService) {
        this.selectTableSpecialLineageService = selectTableSpecialLineageService;
    }
}
