package com.changlu.core.parser.base.lineage.table;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.core.parser.ITableLineageClient;
import com.changlu.core.service.select.table.SelectTable;
import com.changlu.core.service.select.table.SelectTableSpecialLineageService;
import com.changlu.core.utils.TableSourceUtil;

import java.util.*;
import java.util.stream.Collectors;

public class TableLineageClient implements ITableLineageClient {

    // 目标表 即 主体表
    private Table targetTable;
    // 来源表
    private Set<String> sourceTables = new HashSet<>();
    private Set<TableRelation> relations = new HashSet<>();
    private final SelectTableSpecialLineageService selectTableSpecialLineageService = new SelectTableSpecialLineageService();

    private void backInit() {
        this.targetTable = null;
        this.sourceTables = new HashSet<>();
        this.relations = new HashSet<>();
    }

    /**
     * 表血缘解析：只有insert、create sql有血缘
     */
    @Override
    public TableLineageResult parseTableLineages(SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLInsertStatement) {
            this.parseInsertSQL((SQLInsertStatement) sqlStatement);
        }else if (sqlStatement instanceof SQLCreateTableStatement) {
            this.parseCreateSQL((SQLCreateTableStatement) sqlStatement);
        }
        return getResult();
    }

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public void parseCreateSQL(SQLCreateTableStatement x) {
        this.backInit();

        targetTable = TableSourceUtil.extractTable(x.getTableSource());
        // 处理CREATE TABLE AS SELECT 其他场景无血缘情况
        SQLSelect select = x.getSelect();
        if (select != null) {
            SQLSelectQuery selectQuery = select.getQuery();
            // select结构中的来源表
            List<SelectTable> selectTables = selectTableSpecialLineageService.queryTablesBySqlSelectQuery(selectQuery);
            // 添加到原始表中
            addSourceTable(selectTables);
        }
    }

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public void parseInsertSQL(SQLInsertStatement x) {
        this.backInit();

        // 1、目标表
        targetTable = TableSourceUtil.extractTable(x.getTableSource());
        // 2、获取到主体查询的一组tables
        SQLSelect sqlSelect = x.getQuery();
        List<SelectTable> selectTables = selectTableSpecialLineageService.queryTablesBySqlSelect(sqlSelect);
        // 3、with as场景处理
        if (x.getWith() != null) {
            // 3.1、构建with map
            Map<String, List<SelectTable>> tableInfoByTableMap = selectTableSpecialLineageService.buildTableInfoByTableMap(x.getWith());

            // 3.2、处理with as场景 & 主体select字段情况 过滤掉主体中引用with as 别名表字段情况
            selectTables = matchWithAsAndSelectTables(selectTables, tableInfoByTableMap);
        }

        // 添加到原始表中
        addSourceTable(selectTables);
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
        /**
         * filter逻辑：针对with as递归cte场景单独处理
         * t5 -> [{tableInfo:"t5, table1"}, {tableInfo:"table2"}]
         *      => 只过滤得到最终为
         *      t5->table1,table2
         */
        Map<String, String> aliasNameBySourceTables = tableInfoByTableMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(SelectTable::getTableInfo) // 获取每个 TableInfo
                                .flatMap(tableInfo -> Arrays.stream(tableInfo.split(",", -1))) // 将每个 TableInfo 按逗号分割为多个 tableName
                                .map(String::trim) // 去除多余的空格
                                .filter(tableName -> !tableName.equals(entry.getKey())) // 过滤掉与 entryKey 相同的 tableName
                                .collect(Collectors.joining(",")) // 将剩余的 tableName 拼接为字符串
                ));

        // 重新构建匹配替换select主体引用的别名表
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

    /**
     * 获取到血缘解析及结果
     * @return 表血缘结果
     */
    public TableLineageResult getResult() {
        // 初始化血缘解析结果
        if (!sourceTables.isEmpty()) {
            for (String sourceTable : sourceTables) {
                relations.add(new TableRelation(sourceTable, Table.TableTransfer.transfer(targetTable)));
            }
        }
        return new TableLineageResult(Table.TableTransfer.transfer(targetTable), sourceTables, relations);
    }

}
