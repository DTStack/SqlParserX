package com.changlu.sqlparserx.core.parser.base.lineage.table;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.changlu.sqlparserx.core.enums.SQLParseStrategy;
import com.changlu.sqlparserx.core.parser.ITableLineageClient;
import com.changlu.sqlparserx.core.select.table.SelectTable;
import com.changlu.sqlparserx.core.select.table.SelectTableSpecialLineageService;
import com.changlu.sqlparserx.core.utils.TableSourceUtil;

import java.util.*;
import java.util.stream.Collectors;

public class TableLineageClient implements ITableLineageClient {

    // 目标表 即 主体表
    private Table targetTable;
    // 来源表
    private Set<String> sourceTables = new HashSet<>();
    private Set<TableRelation> relations = new HashSet<>();
    private final SelectTableSpecialLineageService selectTableSpecialLineageService;

    // sql语法特征
    protected int features;

    public TableLineageClient() {
        this.selectTableSpecialLineageService = new SelectTableSpecialLineageService();
    }

    public TableLineageClient(SQLParseStrategy... features) {
        this.features = SQLParseStrategy.of(features);
        this.selectTableSpecialLineageService = new SelectTableSpecialLineageService(features);
    }

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
        SQLSelect query = x.getQuery();
        List<SelectTable> selectTables = selectTableSpecialLineageService.queryTablesBySqlSelect(query);
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
            Map<String, List<SelectTable>> tableInfoByTableMap = selectTableSpecialLineageService.buildTableInfoByTableMap(withSubqueryClause);

            // 3.2、处理with as场景 & 主体select字段情况 过滤掉主体中引用with as 别名表字段情况
            selectTables = selectTableSpecialLineageService.matchWithAsAndSelectTables(selectTables, tableInfoByTableMap);
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
