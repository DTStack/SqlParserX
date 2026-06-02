package com.changlu.sqlparserx.core.parser.base.table;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.util.StringUtils;
import com.changlu.sqlparserx.core.parser.base.table.component.ParseSelectTableService;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;
import com.changlu.sqlparserx.core.select.column.SelectColumnItem;
import com.changlu.sqlparserx.core.select.column.SelectColumnSpecialLineageService;
import com.changlu.sqlparserx.core.select.table.SelectTable;
import com.changlu.sqlparserx.core.parser.base.lineage.table.Table;
import com.changlu.sqlparserx.core.parser.IParseTableClient;
import com.changlu.sqlparserx.core.utils.CollectionUtil;
import com.changlu.sqlparserx.core.utils.TableSourceUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description  解析真实表Client
 * @author changlu
 * @date 2025/8/4 14:35
 */
public class ParseTablesClient implements IParseTableClient {

    protected Set<Table> allTables = new HashSet<>();
    protected Table mainTable;
    private final ParseSelectTableService parseSelectTableService = new ParseSelectTableService();

    public ParseTablesClient() {
    }

    /**
     * 对外解析表入口
     */
    @Override
    public ParseTableResult parseTables(SQLStatement stmt) {
        if (stmt instanceof SQLSelectStatement) {
            parseSelectTables((SQLSelectStatement) stmt);
        }else if (stmt instanceof SQLUpdateStatement) {
            parseUpdateTables((SQLUpdateStatement) stmt);
        }else if (stmt instanceof SQLDeleteStatement) {
            parseDeleteTables((SQLDeleteStatement) stmt);
        }else if (stmt instanceof SQLDropTableStatement) {
            parseDropTables((SQLDropTableStatement) stmt);
        }else if (stmt instanceof SQLTruncateStatement) {
            parseTruncateTables((SQLTruncateStatement) stmt);
        }else if (stmt instanceof SQLCommentStatement) {
            parseCommentTables((SQLCommentStatement) stmt);
        }else if (stmt instanceof SQLCreateTableStatement) {
            parseCreateTables((SQLCreateTableStatement) stmt);
        }else if (stmt instanceof SQLInsertStatement) {
            parseInsertTables((SQLInsertStatement) stmt);
        }
        return getParseTableResult();
    }

    protected void parseSelectTables(SQLSelectStatement x) {
        SQLSelect select = x.getSelect();
        if (select != null) {
            List<SelectTable> selectTables = parseSelectTableService.queryTablesBySqlSelect(select);
            if (!selectTables.isEmpty()) {
                this.mainTable = transferTable(selectTables.get(0));

                Set<Table> tables = this.transferTables(selectTables);
                this.allTables.addAll(tables);
            }
        }
    }

    protected void parseUpdateTables(SQLUpdateStatement x) {
        Table table = TableSourceUtil.extractTable(x.getTableSource());
        if (table != null) {
            this.mainTable = table;
            this.allTables.add(table);
        }
    }

    protected void parseDeleteTables(SQLDeleteStatement x) {
        Table table = TableSourceUtil.extractTable(x.getTableSource());
        if (table != null) {
            this.mainTable = table;
            this.allTables.add(table);
        }
    }

    protected void parseDropTables(SQLDropTableStatement x) {
        parseCommonStatement(x.getTableSources());
    }

    protected void parseTruncateTables(SQLTruncateStatement x) {
        parseCommonStatement(x.getTableSources());
    }

    protected void parseCommentTables(SQLCommentStatement x) {
        SQLExprTableSource exprTableSource = x.getOn();
        SQLCommentStatement.Type type = x.getType();
        // 示范场景：COMMENT ON COLUMN batch_601.employees2.name IS '姓名'
        if (type == SQLCommentStatement.Type.COLUMN) {
            SelectColumnSpecialLineageService selectColumnSpecialLineageService = new SelectColumnSpecialLineageService();
            // expr表达式为
            SelectColumnItem selectColumnItem = selectColumnSpecialLineageService.buildSelectColumnItem(exprTableSource.getExpr());
            String tableName = selectColumnItem.getTableInfo();
            mainTable = Table.TableTransfer.parse(tableName);
        }else {
            mainTable = TableSourceUtil.extractTable(exprTableSource);
        }
        this.allTables.add(mainTable);
    }

    protected void parseCreateTables(SQLCreateTableStatement x) {
        // 主表
        mainTable = TableSourceUtil.extractTable(x.getTableSource());

        // 处理CREATE TABLE AS SELECT 其他场景无血缘情况
        SQLSelect select = x.getSelect();
        if (select != null) {
            SQLSelectQuery selectQuery = select.getQuery();
            // select结构中的来源表
            List<SelectTable> selectTables = parseSelectTableService.queryTablesBySqlSelectQuery(selectQuery);
            this.allTables.addAll(transferTables(selectTables));
        }
        this.allTables.add(mainTable);
    }

    protected void parseInsertTables(SQLInsertStatement x) {
        // 逻辑步骤与表血缘一致
        // 1、目标表
        mainTable = TableSourceUtil.extractTable(x.getTableSource());
        // 2、获取到主体查询的一组tables
        SQLSelect query = x.getQuery();
        List<SelectTable> selectTables = parseSelectTableService.queryTablesBySqlSelect(query);
        // 3、with as场景处理
        // check是否是with as结构
        // 情况1：with 在 insert 上场景 【x.getWith()】 => 大部分数据源
        SQLWithSubqueryClause withSubqueryClause = x.getWith();
        // 场景2：with 在 insert 下场景 【x.getQuery().getWithSubQuery()】=> doris
        if (withSubqueryClause == null && query != null) {
            withSubqueryClause = query.getWithSubQuery();
        }
        // check是否满足with as场景
        if (withSubqueryClause != null) {
            // 3.1、构建with map
            Map<String, List<SelectTable>> tableInfoByTableMap = parseSelectTableService.buildTableInfoByTableMap(withSubqueryClause);

            // 3.2、处理with as场景 & 主体select字段情况 过滤掉主体中引用with as 别名表字段情况
            selectTables = matchWithAsAndAddAnotherTables(selectTables, tableInfoByTableMap);
        }

        // 处理来源表
        this.allTables.addAll(transferTables(selectTables));
        // 处理目标表
        this.allTables.add(mainTable);
    }

    public void parseCommonStatement(List<SQLExprTableSource> tableSources) {
        mainTable = TableSourceUtil.extractTable(tableSources.get(0));

        for (SQLExprTableSource tableSource : tableSources) {
            this.allTables.add(TableSourceUtil.extractTable(tableSource));
        }
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
                                .flatMap(tableInfo -> Arrays.stream(tableInfo.split(",", -1))) // 将每个 TableInfo 按逗号分割为多个 tableName
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
                    String tableInfo = aliasNameBySourceTables.get(tableName);
                    // 如果with as中的某个别名表中实际无真实引用表，如 with t1 as (select 1 as id, "name1" as name)，无真实表则无需添加
                    if (!StringUtils.isEmpty(tableInfo)) {
                        tableGroup.add(tableInfo);
                        usedAliases.add(tableName);
                    }
                }else {
                    tableGroup.add(tableName);
                }

            }
            if (CollectionUtil.isNotEmpty(tableGroup)) {
                newSelectTables.add(new SelectTable(String.join(",", tableGroup), selectTable.getAliasName()));
            }
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

    private Table transferTable(SelectTable selectTable) {
        String tableInfo = selectTable.getTableInfo();
        if (tableInfo.contains(",")) {
            tableInfo = tableInfo.split(",", -1)[0];
        }
        return Table.TableTransfer.parse(tableInfo);
    }

    private Set<Table> transferTables(List<SelectTable> selectTables) {
        Set<Table> tables = new HashSet<>();
        for (SelectTable selectTable : selectTables) {
            String[] sourceTableArr = selectTable.getTableInfo().split(",");
            for (String tableName : sourceTableArr) {
                tables.add(Table.TableTransfer.parse(tableName));
            }
        }
        return tables;
    }

    public Set<Table> getTables() {
        return allTables;
    }

    public Table getMainTable() {
        return mainTable;
    }

    public ParseTableResult getParseTableResult() {
        return new ParseTableResult(mainTable, allTables);
    }

}
