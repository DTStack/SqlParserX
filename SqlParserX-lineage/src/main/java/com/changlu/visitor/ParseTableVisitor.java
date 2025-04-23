package com.changlu.visitor;

import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.changlu.lineage.core.select.table.SelectTable;
import com.changlu.lineage.core.select.table.SelectTableSpecialLineageService;
import com.changlu.lineage.domain.Table;
import com.changlu.lineage.table.TableLineageResult;
import com.changlu.lineage.table.TableLineageVisitor;
import com.changlu.utils.TableSourceUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 解析真实表
 * @author changlu
 * @date 2025/4/22 19:20
 */
public class ParseTableVisitor extends SQLASTVisitorAdapter {

    private TableLineageVisitor tableLineageVisitor;
    private Set<String> allTables = new HashSet<>();
    private final SelectTableSpecialLineageService selectTableSpecialLineageService = new SelectTableSpecialLineageService();

    public ParseTableVisitor() {
        this.tableLineageVisitor = new TableLineageVisitor();
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
        // 复用表血缘
        this.tableLineageVisitor.visit(x);

        TableLineageResult result = this.tableLineageVisitor.getResult();
        Set<String> tables = result.getSourceTables();
        String targetTable = result.getTargetTable();
        this.allTables.addAll(tables);
        this.allTables.add(targetTable);
        return false;
    }

    /**
     * 处理create 类型sql
     * @param x druid node sql
     * @return
     */
    @Override
    public boolean visit(SQLInsertStatement x) {
        // 基本采用表血缘SQLInsertStatement逻辑，但是with as的match匹配应当重新完成匹配
        // 1、目标表
        Table targetTable = TableSourceUtil.extractTable(x.getTableSource());
        // 2、获取到主体查询的一组tables
        SQLSelect sqlSelect = x.getQuery();
        List<SelectTable> selectTables = selectTableSpecialLineageService.queryTablesBySqlSelect(sqlSelect);
        // 3、with as场景处理
        if (x.getWith() != null) {
            // 3.1、构建with map
            Map<String, List<SelectTable>> tableInfoByTableMap = selectTableSpecialLineageService.buildTableInfoByTableMap(x.getWith());

            // 3.2、处理with as场景 & 主体select字段情况 过滤掉主体中引用with as 别名表字段情况
            selectTables = matchWithAsAndAddAnotherTables(selectTables, tableInfoByTableMap);
        }

        // 处理来源表
        for (SelectTable selectTable : selectTables) {
            String[] sourceTableArr = selectTable.getTableInfo().split(",");
            Collections.addAll(this.allTables, sourceTableArr);
        }
        // 处理目标表
        this.allTables.add(Table.TableTransfer.transfer(targetTable));
        return false;
    }

    /**
     * with as 场景处理：考虑完成selectTables中具有的别名替换 & 其他未引用别名场景的tables也要添加进来
     */
    private List<SelectTable> matchWithAsAndAddAnotherTables(List<SelectTable> selectTables, Map<String, List<SelectTable>> tableInfoByTableMap) {
        // 用于记录后续替换过的with as 的别名
        Set<String> usedAliases = new HashSet<>();
        // 1、提前转换map
        /**
         * filter逻辑：针对with as递归cte场景单独处理，过滤掉一组中同样引用别名情况
         * 转换效果如下：
         * t5 -> [{tableInfo:"t5, table1"}, {tableInfo:"table2"}]
         *      => 只过滤得到最终为
         *      t5->table1,table2
         */
        Map<String, String> aliasNameBySourceTables = tableInfoByTableMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(SelectTable::getTableInfo) // 获取每个 TableInfo
                                .flatMap(tableInfo -> Arrays.stream(tableInfo.split(","))) // 将每个 TableInfo 按逗号分割为多个 tableName
                                .map(String::trim) // 去除多余的空格
                                .filter(tableName -> !tableName.equals(entry.getKey())) // 过滤掉与 entryKey 相同的 tableName
                                .collect(Collectors.joining(",")) // 将剩余的 tableName 拼接为字符串
                ));

        // 2、重新构建匹配替换select主体引用的别名表
        List<SelectTable> newSelectTables = new ArrayList<>();
        for (SelectTable selectTable : selectTables) {
            List<String> tableGroup = new ArrayList<>();
            String[] selectTableArr = selectTable.getTableInfo().split(",");
            for (String tableName : selectTableArr) {
                if (aliasNameBySourceTables.containsKey(tableName)) {
                    tableGroup.add(aliasNameBySourceTables.get(tableName));
                    usedAliases.add(tableName);
                }else {
                    tableGroup.add(tableName);
                }

            }
            newSelectTables.add(new SelectTable(String.join(",", tableGroup), selectTable.getAliasName()));
        }

        // 筛选出来未进行替换的所有key、value
        // 3. 添加未在主查询中使用但定义了的CTE对应的表
        for (Map.Entry<String, String> entry : aliasNameBySourceTables.entrySet()) {
            String alias = entry.getKey();
            String sourceTables = entry.getValue();

            if (!usedAliases.contains(alias) && !sourceTables.isEmpty()) {
                // 添加未使用的CTE对应的实际表
                newSelectTables.add(new SelectTable(sourceTables, alias));
            }
        }

        return newSelectTables;
    }

    public Set<String> getSourceTables() {
        return allTables;
    }
}
