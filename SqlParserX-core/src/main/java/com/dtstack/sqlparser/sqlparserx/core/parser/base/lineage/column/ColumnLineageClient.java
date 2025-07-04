package com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.dtstack.sqlparser.sqlparserx.core.config.Constant;
import com.dtstack.sqlparser.sqlparserx.core.parser.IColumnLineageClient;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.domain.ColumnLineage;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.table.Table;
import com.dtstack.sqlparser.sqlparserx.core.select.column.SelectColumn;
import com.dtstack.sqlparser.sqlparserx.core.select.column.SelectColumnItem;
import com.dtstack.sqlparser.sqlparserx.core.select.column.SelectColumnSpecialLineageService;
import com.dtstack.sqlparser.sqlparserx.core.utils.TableSourceUtil;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.*;

public class ColumnLineageClient implements IColumnLineageClient {

    private Logger log = LoggerFactory.getLogger(ColumnLineageClient.class);

    // 目标表：主体表
    private Table targetTable;
    // key：写入目标table.column  value：一组来源column
    private final Map<String, List<ColumnLineageResult.ColumnRelation>> columnRelations = new LinkedHashMap<>();
    // 记录了每一组血缘的的目标表及关联的来源表及字段 包含有catalog、db、table、column
    private final List<ColumnLineage> columnLineages = new ArrayList<>();
    // 跟踪当前解析上下文的关键数据结构，用于记录遍历过程中当前语法上下文的栈
    private final Deque<SQLObject> contextStack = new ArrayDeque<>();
    private Map<String, String> fromTables = new HashMap<>(); // 存储FROM子句的表别名映射

    // 真实表&字段关联关系（外部查询元数据）
    private Map<String, List<Column>> tableColumnsMap;
    private SelectColumnSpecialLineageService selectColumnSpecialLineageService;

    public ColumnLineageClient() {
        this.tableColumnsMap = new HashMap<>();
        this.selectColumnSpecialLineageService = new SelectColumnSpecialLineageService();
    }

    public ColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        this.tableColumnsMap = tableColumnsMap;
        if (tableColumnsMap != null && !tableColumnsMap.isEmpty()) {
            this.selectColumnSpecialLineageService = new SelectColumnSpecialLineageService(tableColumnsMap);
        }else {
            this.selectColumnSpecialLineageService = new SelectColumnSpecialLineageService();
        }
    }

    /**
     * 字段血缘解析：只有insert、create sql有血缘
     */
    @Override
    public ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLInsertStatement) {
            this.parseInsertSQL((SQLInsertStatement) sqlStatement);
        }else if (sqlStatement instanceof SQLCreateTableStatement) {
            this.parseCreateSQL((SQLCreateTableStatement) sqlStatement);
        }
        return getResult();
    }

    /**
     * 解析insert sql
     */
    @Override
    public void parseInsertSQL(SQLInsertStatement x) {
        targetTable = TableSourceUtil.extractTable(x.getTableSource());

        // 1、获取到目标表的columns信息
        List<SQLExpr> insertTableColumns = x.getColumns();
        List<SelectColumnItem> targetColumns = new ArrayList<>();
//        if (insertTableColumns == null || insertTableColumns.isEmpty()) {
//            // 写法1：无插入指定列场景 INSERT INTO target_table SELECT column1, column2, column3
//            SQLSelect select = x.getQuery();
//            if (select != null) {
//                SQLSelectQuery selectQuery = select.getQuery();
//                // 获取到目标字段集合
//                targetColumns = matchFirstSelectColumns(selectQuery);
//            }
//        }else
        // 若是insert sql指定字段写入
        if (insertTableColumns != null && insertTableColumns.isEmpty()){
            // 写法2：插入指定列场景 insert into table values(xxx, xxx) select xxx, xxx
            targetColumns = selectColumnSpecialLineageService.buildSelectColumnsBySQLExprs(insertTableColumns);
        }

        // 2、优先with as场景处理
        SQLSelect query = x.getQuery();
        // check是否是with as结构
        // 情况1：with 在 insert 上场景 【x.getWith()】 => 大部分数据源
        SQLWithSubqueryClause withSubqueryClause = x.getWith();
        // 场景2：with 在 insert 下场景 【x.getQuery().getWithSubQuery()】=> doris
        if (withSubqueryClause == null && query != null) {
            withSubqueryClause = query.getWithSubQuery();
        }
        // check是否满足with as场景
        Map<String, List<SelectColumn>> columnInfoByTableMap = null;
        Set<String> withSqlAliasTableNames = new HashSet<>();
        if (withSubqueryClause != null) {
            // 2.1、构建with map 别名与一组字段关系
            columnInfoByTableMap = selectColumnSpecialLineageService.buildColumnInfoByTableMap(withSubqueryClause);
            withSqlAliasTableNames = columnInfoByTableMap.keySet();
        }

        // 3、获取到主体查询的一组columns
        List<SelectColumn> selectColumns = selectColumnSpecialLineageService.queryColumnsBySqlSelect(query, withSqlAliasTableNames);

        // 处理含有with as场景 & 主体selct字段情况 过滤掉主体中引用with as 别名字段依赖
        if (columnInfoByTableMap != null) {
            selectColumns = selectColumnSpecialLineageService.matchWithAsAndSelectColumns(selectColumns, columnInfoByTableMap);
        }


        // 4、若是没有指定insert的字段，那么此时targetColumns为空，此时需要从主体select中获取字段信息
        if (insertTableColumns.isEmpty() || targetColumns.isEmpty()) {
            targetColumns = queryTargetTableColumns(selectColumns, targetTable);
        }

        if (targetColumns.size() > selectColumns.size()) {
            throw new RuntimeException("血缘解析异常，sql写法问题");
        }

        // 4、构建字段血缘信息
        addColumnRelations(selectColumns, targetColumns);
    }

    /**
     * 查询获取insert目标字段集合 从主体selectColumns中获取目标字段集合
     * @param selectColumns 主体select字段
     * @return List<SelectColumnItem>
     */
    private List<SelectColumnItem> queryTargetTableColumns(List<SelectColumn> selectColumns, Table targetTable) {
        List<SelectColumnItem> insertTableColumns = new ArrayList<>();
        for (SelectColumn selectColumn : selectColumns) {
            insertTableColumns.add(new SelectColumnItem(Table.TableTransfer.transfer(targetTable), selectColumn.getAliasName()));
        }
        return insertTableColumns;
    }

    /**
     * 解析create sql
     */
    @Override
    public void parseCreateSQL(SQLCreateTableStatement x) {
        // 获取到目标表
        // 留口子 可以根据实际业务方法调用传入指定catalog、db情况完成赋值
        targetTable = TableSourceUtil.extractTable(x.getTableSource());
        contextStack.push(x);

        // 处理CREATE TABLE AS SELECT 其他场景无血缘情况
        SQLSelect select = x.getSelect();
        if (select != null) {
            SQLSelectQuery selectQuery = select.getQuery();

            // 1、获取到目标字段集合
//            List<SelectColumn> targetColumns = matchFirstSelectColumns(selectQuery);
            // 初始化目标表
//            for (SelectColumn targetColumn : targetColumns) {
//                targetColumn.setTableInfo(Table.TableTransfer.transfer(targetTable));
//            }

            // 提前剪枝
//            if (targetColumns.isEmpty()) return false;

            // 2、收集以当前select主体结构所追溯汇总得到的一组来源字段
            List<SelectColumn> selectColumns = selectColumnSpecialLineageService.queryColumnsBySqlSelectQuery(selectQuery);

            List<SelectColumnItem> targetColumns = this.queryTargetTableColumns(selectColumns, targetTable);

            // 3、完成CREATE TABLE AS SELECT的血缘分析
            if (targetColumns.size() > selectColumns.size()) {
                throw new RuntimeException("血缘解析异常，sql写法问题");
            }
            addColumnRelations(selectColumns, targetColumns);
        }
    }

    /**
     * 针对来源columns & 目标columns 来构件字段级血缘
     * @param selectColumns 主体select压缩合并的字段集合
     * @param targetColumns 目标表字段集合
     */
    private void addColumnRelations(List<SelectColumn> selectColumns, List<SelectColumnItem> targetColumns) {
        for (int i = 0; i < targetColumns.size(); i++) {
            // #feat5：字段血缘核心匹配逻辑
            SelectColumnItem targetColumn = targetColumns.get(i);
            SelectColumn selectColumn = selectColumns.get(i);
            // 确认select的字段是否为聚合字段、union字段场景，会出现血缘匹配多条情况
            String[] selectColumnNameArr = selectColumn.getColumnName().split(",", -1);
            String[] selectTableNameArr = selectColumn.getTableInfo().split(",", -1);
            int lineageRelationsCount = selectColumnNameArr.length;
            for (int j = 0; j < lineageRelationsCount; j++) {
                try {
                    // 针对最终无匹配表场景情况，应当过滤 举例：select '0' as a from table 这类并不能作为血缘
                    if (selectTableNameArr[j] == null
                            || selectColumnNameArr[j].isEmpty()
                            || Constant.NO_RELATION_TABLE.equals(selectTableNameArr[j])) continue;
                    // 添加血缘关系
                    // 目标端一组列表中的真实表名 => 目标表、目标端一组列表中的别名字段 => 目标字段
                    // 来源端的真实表名 => 来源表名、来源端的真实字段 => 来源字段
                    addRelation(selectTableNameArr[j], selectColumnNameArr[j], targetColumn.getTableInfo(), targetColumn.getColumnName());
                }catch (Exception e) {
                    String msgInfo = String.format("addColumnRelations error! columnName => %s, tableInfo => %s", selectColumn.getColumnName(), selectColumn.getTableInfo());
                    log.error(msgInfo, e);
                    throw new RuntimeException(msgInfo);
                }
            }
        }
    }

    /**
     * 添加关联关系
     * @param sourceTable 原始表
     * @param sourceColumn 原始字段
     * @param targetTable 目标表
     * @param targetColumn 目标字段
     */
    private void addRelation(String sourceTable, String sourceColumn,
                             String targetTable, String targetColumn) {
        String relationKey = targetTable + "." + targetColumn;
        // 无来源表 & 无来源字段场景情况
        if (sourceTable == null && sourceColumn == null
            || sourceTable != null && sourceTable.isEmpty()  // 无来源表场景（一般是来源字段为一个字面量值，而非一个表的字段,不算作血缘）
        ) {
            columnRelations.computeIfAbsent(relationKey, k -> new ArrayList<>());
            return;
        }
        // 1、构建血缘关系map组
        // 来源不为空情况
        ColumnLineageResult.ColumnRelation relation = new ColumnLineageResult.ColumnRelation(
                sourceTable != null ? sourceTable + "." + sourceColumn : sourceColumn,
                targetTable + "." + targetColumn
        );
        columnRelations.computeIfAbsent(relationKey, k -> new ArrayList<>())
                .add(relation);

        // 2、构建血缘关系记录集合
        ColumnLineage columnLineage = new ColumnLineage();
        Table sourceTableObj = Table.TableTransfer.parse(sourceTable);
        Table targetTableObj = Table.TableTransfer.parse(targetTable);

        columnLineage.setFromCatalog(sourceTableObj.getCatalog());
        columnLineage.setFromDb(sourceTableObj.getDb());
        columnLineage.setFromTable(sourceTableObj.getName());
        columnLineage.setFromColumn(sourceColumn);

        columnLineage.setToCatalog(targetTableObj.getCatalog());
        columnLineage.setToDb(targetTableObj.getDb());
        columnLineage.setToTable(targetTableObj.getName());
        columnLineage.setToColumn(targetColumn);
        columnLineages.add(columnLineage);
    }

    public ColumnLineageResult getResult() {
        return new ColumnLineageResult(Table.TableTransfer.transfer(targetTable), columnRelations, columnLineages);
    }
}
