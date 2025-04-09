package com.changlu.lineage.column;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.changlu.lineage.core.select.column.SelectColumn;
import com.changlu.lineage.core.select.column.SelectColumnSpecialLineageService;
import com.changlu.lineage.domain.Table;
import com.changlu.lineage.utils.TableSourceUtil;

import java.util.*;

public class ColumnLineageVisitor extends SQLASTVisitorAdapter {

    // 目标表：主体表
    private Table targetTable;
    // key：写入目标table.column  value：一组来源column
    private final Map<String, List<ColumnLineageResult.ColumnRelation>> columnRelations = new LinkedHashMap<>();
    // 跟踪当前解析上下文的关键数据结构，用于记录遍历过程中当前语法上下文的栈
    private final Deque<SQLObject> contextStack = new ArrayDeque<>();
    private Map<String, String> fromTables = new HashMap<>(); // 存储FROM子句的表别名映射

    // 真实表&字段关联关系
    private Map<String, List<Column>> tableColumnsMap;

    public ColumnLineageVisitor(Map<String, List<Column>> tableColumnsMap) {
        this.tableColumnsMap = tableColumnsMap;
    }

    @Override
    public boolean visit(SQLInsertStatement x) {
        targetTable = TableSourceUtil.extractTable(x.getTableSource());

        // 1、获取到目标表的columns信息
        List<SQLExpr> insertTableColumns = x.getColumns();
        List<SelectColumn> targetColumns = new ArrayList<>();
        if (insertTableColumns == null) {
            // 写法1：无插入指定列场景 INSERT INTO target_table SELECT column1, column2, column3
            SQLSelect select = x.getQuery();
            if (select != null) {
                SQLSelectQuery selectQuery = select.getQuery();
                // 获取到目标字段集合
                targetColumns = matchFirstSelectColumns(selectQuery);
            }
        }else {
            // 写法2：插入指定列场景 insert into table values(xxx, xxx) select xxx, xxx
            targetColumns = SelectColumnSpecialLineageService.buildSelectColumnsBySQLExprs(insertTableColumns);
        }
        // 初始化目标字段的表信息
        for (SelectColumn targetColumn : targetColumns) {
            targetColumn.setTableInfo(Table.TableTransfer.transfer(targetTable));
        }

        // 2、获取到主体查询的一组columns
        SQLSelect query = x.getQuery();
        List<SelectColumn> selectColumns = SelectColumnSpecialLineageService.queryColumnsBySqlSelect(query);

        // 3、with as场景处理
        // check是否是with as结构
        Map<String, List<SelectColumn>> columnInfoByTableMap = new HashMap<>();
        if (x.getWith() != null) {
            SQLWithSubqueryClause with = x.getWith();
            List<SQLWithSubqueryClause.Entry> entries = with.getEntries();
            if (!entries.isEmpty()) {
                // 遍历每一组with as结构
                for (SQLWithSubqueryClause.Entry entry : entries) {
                    // as 的别名
                    String aliasTableName = entry.getAlias();
                    // select 主体过滤得到的字段记录含追溯真实表记录
                    List<SelectColumn> withAsTableSelectColumns = SelectColumnSpecialLineageService.queryColumnsBySqlSelect(entry.getSubQuery());
                    List<SelectColumn> filterSelectColumns = new ArrayList<>();
                    // with as 递归CTE场景 去除tableInfo中相对应匹配别名table相对应的字段记录信息
                    for (SelectColumn selectColumn : withAsTableSelectColumns) {
                        List<String> selectColumnNames = new ArrayList<>();
                        List<String> seletTableNames = new ArrayList<>();
                        String[] selectColumnNameArr = selectColumn.getColumnName().split(",");
                        String[] selectTableNameArr = selectColumn.getTableInfo().split(",");
                        for (int i = 0; i < selectColumnNameArr.length; i++) {
                            // 核心过滤掉递归CTE场景的字段依赖别名情况
                            if (aliasTableName.equals(selectTableNameArr[i])) continue;
                            selectColumnNames.add(selectColumnNameArr[i]);
                            seletTableNames.add(selectTableNameArr[i]);
                        }
                        // 若是去除引入别名字段依赖情况后，数组数量为空情况，则跳过（一般不会出现该问题）
                        if (selectColumnNames.isEmpty()) continue;
                        selectColumn.setColumnName(String.join(",", selectColumnNames));
                        selectColumn.setTableInfo(String.join(",", seletTableNames));
                        filterSelectColumns.add(selectColumn);
                    }

                    columnInfoByTableMap.put(aliasTableName, filterSelectColumns);
                }
            }

            // 处理with as场景 & 主体selct字段情况 过滤掉主体中引用with as 别名字段依赖
            selectColumns = matchWithAsAndSelectColumns(selectColumns, columnInfoByTableMap);
        }
        System.out.println(columnInfoByTableMap);

        if (targetColumns.size() > selectColumns.size()) {
            throw new RuntimeException("血缘解析异常，sql写法问题");
        }

        // 4、构建字段血缘信息
        addColumnRelations(selectColumns, targetColumns);
        return false;
    }

    /**
     * 处理with as场景 & 主体selct字段情况  消除掉主体select引用with as别名问题
     * @param selectColumns 主体select字段
     * @param columnInfoByTableMap with as map场景
     * @return 消除掉主体select引用with as别名后得到的一组selectColumns
     */
    private List<SelectColumn> matchWithAsAndSelectColumns(List<SelectColumn> selectColumns, Map<String, List<SelectColumn>> columnInfoByTableMap) {
        List<SelectColumn> result = new ArrayList<>();

        // 遍历每个SELECT列
        for (SelectColumn selectColumn : selectColumns) {
            // 拆分表信息和列名(可能包含多个以逗号分隔的值)
            String[] tableInfos = selectColumn.getTableInfo().split(",");
            String[] columnNames = selectColumn.getColumnName().split(",");
            String aliasName = selectColumn.getAliasName();

            // 验证表信息和列名数量是否匹配
            if (tableInfos.length != columnNames.length) {
                throw new IllegalArgumentException("SELECT列中的表信息和列名数量不匹配");
            }

            List<String> newTableInfos = new ArrayList<>();
            List<String> newColumnNames = new ArrayList<>();

            // 处理每个表/列对
            for (int i = 0; i < tableInfos.length; i++) {
                String tableInfo = tableInfos[i];
                String columnName = columnNames[i];

                // 检查别名是否是WITH AS表(存在于map中)
                if (columnInfoByTableMap.containsKey(tableInfo)) {
                    boolean found = false;
                    // 在WITH AS表的列中搜索匹配项
                    for (SelectColumn withColumn : columnInfoByTableMap.get(tableInfo)) {
                        // 匹配列名与WITH AS表中的别名
                        if (columnName.equals(withColumn.getAliasName())) {
                            newTableInfos.add(withColumn.getTableInfo());
                            newColumnNames.add(withColumn.getColumnName());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // 未在WITH AS表中找到匹配项，保留原始值
                        newTableInfos.add(tableInfo);
                        newColumnNames.add(columnName);
                    }
                }else {
                    // 无匹配到，表示该表名非with as中，保留原始值
                    newTableInfos.add(tableInfo);
                    newColumnNames.add(columnName);
                }
            }

            // 创建新的SelectColumn对象(使用解析后的引用)
            SelectColumn newColumn = new SelectColumn(
                    String.join(",", newTableInfos),
                    String.join(",", newColumnNames),
                    aliasName
            );
            result.add(newColumn);
        }

        return result;
    }

    @Override
    public boolean visit(SQLCreateTableStatement x) {
        // 获取到目标表
        // 留口子 可以根据实际业务方法调用传入指定catalog、db情况完成赋值
        targetTable = TableSourceUtil.extractTable(x.getTableSource());
        contextStack.push(x);

        // 处理CREATE TABLE AS SELECT 其他场景无血缘情况
        SQLSelect select = x.getSelect();
        if (select != null) {
            SQLSelectQuery selectQuery = select.getQuery();

            // 1、获取到目标字段集合
            List<SelectColumn> targetColumns = matchFirstSelectColumns(selectQuery);
            // 初始化目标表
            for (SelectColumn targetColumn : targetColumns) {
                targetColumn.setTableInfo(Table.TableTransfer.transfer(targetTable));
            }

            // 提前剪枝
            if (targetColumns.isEmpty()) return false;

            // 2、收集以当前select主体结构所追溯汇总得到的一组来源字段
            List<SelectColumn> selectColumns = SelectColumnSpecialLineageService.queryColumnsBySqlSelectQuery(selectQuery);

            // 3、完成CREATE TABLE AS SELECT的血缘分析
            if (targetColumns.size() > selectColumns.size()) {
                throw new RuntimeException("血缘解析异常，sql写法问题");
            }
            addColumnRelations(selectColumns, targetColumns);
        }
        return false;
    }

    /**
     * 针对来源columns & 目标columns 来构件字段级血缘
     * @param selectColumns 主体select压缩合并的字段集合
     * @param targetColumns 目标表字段集合
     */
    private void addColumnRelations(List<SelectColumn> selectColumns, List<SelectColumn> targetColumns) {
        for (int i = 0; i < targetColumns.size(); i++) {
            // #feat5：字段血缘核心匹配逻辑
            SelectColumn targetColumn = targetColumns.get(i);
            SelectColumn selectColumn = selectColumns.get(i);
            // 确认select的字段是否为聚合字段、union字段场景，会出现血缘匹配多条情况
            String[] selectColumnNameArr = selectColumn.getColumnName().split(",");
            String[] selectTableNameArr = selectColumn.getTableInfo().split(",");
            int lineageRelationsCount = selectColumnNameArr.length;
            for (int j = 0; j < lineageRelationsCount; j++) {
                // 添加血缘关系
                // 目标端一组列表中的真实表名 => 目标表、目标端一组列表中的别名字段 => 目标字段
                // 来源端的真实表名 => 来源表名、来源端的真实字段 => 来源字段
                addRelation(selectTableNameArr[j], selectColumnNameArr[j], targetColumn.getTableInfo(), targetColumn.getAliasName());
            }
        }
    }

    /**
     * 获取到union的第一个select
     * @param selectQuery
     * @return
     */
    private SQLSelectQueryBlock findFirstSelectQueryBlock(SQLSelectQuery selectQuery) {
        if (selectQuery instanceof SQLSelectQueryBlock) {
            return (SQLSelectQueryBlock) selectQuery;
        } else if (selectQuery instanceof SQLUnionQuery) {
            SQLUnionQuery unionQuery = (SQLUnionQuery) selectQuery;
            List<SQLSelectQuery> relations = unionQuery.getRelations();
            if (!relations.isEmpty()) {
                return findFirstSelectQueryBlock(relations.get(0));
            }
        }
        throw new RuntimeException("findFirstSelectQueryBlock error，selectQuery type: " + selectQuery.getClass().getName() + "暂无相应处理");
    }

    @Override
    public void endVisit(SQLCreateTableStatement x) {
        contextStack.pop();
    }

    private String findSourceTableForColumn(String columnName) {
        List<String> possibleTables = new ArrayList<>();

        // 收集所有包含该字段的表
        for (Map.Entry<String, String> entry : fromTables.entrySet()) {
            String realTable = entry.getValue();

            // 后续需要check是否有对应真实表及字段名
            if (hasColumn(realTable, columnName)) {
                possibleTables.add(realTable);
                break;
            }
        }

        // 处理查询结果
        if (possibleTables.isEmpty()) {
            return columnName; // 字段不属于任何已知表
        } else if (possibleTables.size() == 1) {
            return possibleTables.get(0); // 唯一匹配
        } else {
            // 多表包含相同字段名，需要表名限定 （可以理解为最终的sql执行不过）
            throw new IllegalStateException("Ambiguous column '" + columnName +
                    "' in tables: " + possibleTables);
        }
    }

    // 模拟元数据服务检查表是否包含某字段
    private boolean hasColumn(String tableName, String columnName) {
        List<Column> columns = this.tableColumnsMap.get(tableName);
        for (Column column : columns) {
            if (column.getColumnName().contains(columnName)) return true;
        }
        return false;
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
        if (sourceTable == null && sourceColumn == null) {
            columnRelations.computeIfAbsent(relationKey, k -> new ArrayList<>());
            return;
        }
        // 来源不为空情况
        ColumnLineageResult.ColumnRelation relation = new ColumnLineageResult.ColumnRelation(
                sourceTable != null ? sourceTable + "." + sourceColumn : sourceColumn,
                targetTable + "." + targetColumn
        );
        columnRelations.computeIfAbsent(relationKey, k -> new ArrayList<>())
                .add(relation);
    }

    /**
     * 匹配获取到select中的第一组columns
     * @param sqlSelectQuery
     * @return
     */
    private List<SelectColumn> matchFirstSelectColumns(SQLSelectQuery sqlSelectQuery) {
        List<SelectColumn> selectColumns = new ArrayList<>();
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            List<SQLSelectItem> selectList = sqlSelectQueryBlock.getSelectList();
            selectColumns = SelectColumnSpecialLineageService.buildSelectColumnsBySelectItems(selectList);
        }else if (sqlSelectQuery instanceof SQLUnionQuery) {
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            // 优先匹配到左侧的
            SQLSelectQuery left = sqlUnionQuery.getLeft();
            return matchFirstSelectColumns(left);
        }
        return selectColumns;
    }

    /**
     * 处理多种场景
     *   场景1: create table (id, name, xxx) as select xx, xx, xx 【无该场景】
     *   场景2：create table as select
     * @param selectQueryBlock
     * @return
     */
    private List<String> getTargetColumns(SQLSelectQueryBlock selectQueryBlock) {
        List<String> columns = new ArrayList<>();
        // 方式2：如果没有显式定义列，则使用SELECT项的别名（直接create table as select 这种形式，无列名场景）
        if (selectQueryBlock != null) {
            List<SQLSelectItem> selectList = selectQueryBlock.getSelectList();
            for (int i = 0; i < selectList.size(); i++) {
                String columnName = extractRealColumnName(selectList.get(i), i);
                columns.add(columnName);
            }
        }
        return columns;
    }

    private String extractRealColumnName(SQLSelectItem selectItem, int index) {
        // 优先使用别名
        if (selectItem.getAlias() != null) {
            return selectItem.getAlias();
        }

        // 没有别名则从表达式中提取
        SQLExpr expr = selectItem.getExpr();
        if (expr instanceof SQLIdentifierExpr) {
            return ((SQLIdentifierExpr) expr).getName();
        } else if (expr instanceof SQLPropertyExpr) {
            return ((SQLPropertyExpr) expr).getName();
        }

        // 复杂表达式默认使用列位置
        return "column_" + (index + 1);
    }

    public ColumnLineageResult getResult() {
        return new ColumnLineageResult(Table.TableTransfer.transfer(targetTable), columnRelations);
    }
}
