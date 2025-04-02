package com.changlu.lineage.column;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.alibaba.druid.stat.TableStat;
import com.changlu.lineage.domain.Column;
import com.changlu.lineage.utils.TableSourceUtil;

import java.util.*;

public class ColumnLineageVisitor extends SQLASTVisitorAdapter {

    private String targetTable;
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

    /**
     * 获取表名
     * @param tableSource 表名source
     * @return 表名
     */
    private String extractTableName(SQLTableSource tableSource) {
        if (tableSource instanceof SQLExprTableSource) {
            SQLExpr expr = ((SQLExprTableSource) tableSource).getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                return ((SQLIdentifierExpr) expr).getName();
            } else if (expr instanceof SQLPropertyExpr) {
                return ((SQLPropertyExpr) expr).getName();
            }
        }
        return tableSource.toString();
    }

    @Override
    public boolean visit(SQLCreateTableStatement x) {
        // 获取到创建的目标表名
        targetTable = x.getName().getSimpleName();
        contextStack.push(x);

        // 处理CREATE TABLE AS SELECT 其他场景无血缘情况
        SQLSelect select = x.getSelect();
        if (select != null) {
            SQLSelectQuery selectQuery = select.getQuery();
            // 收集所有的表名映射情况
            fromTables = TableSourceUtil.getAllTables(selectQuery);

            // 区分不同select 类型场景
            // 场景1：普通select xxx from、select xxx from xxx left join
            if (selectQuery instanceof SQLSelectQueryBlock) {
                // 目标targetColumns（考虑一层即可）
                SQLSelectQueryBlock selectQueryBlock = (SQLSelectQueryBlock) selectQuery;
                List<String> targetColumns = getTargetColumns(selectQueryBlock);

                // 处理一组 select xxx 来源字段
                processQueryBlockLineage(selectQueryBlock, targetColumns);
            }
            // 场景2：select xxx from table1 union select xxx from table2
            else if (selectQuery instanceof SQLUnionQuery){
                // 获取到union 所有组
                // select xxx from table1 union 场景
//                throw new RuntimeException("暂不支持该场景");
                List<SQLSelectQuery> relations = ((SQLUnionQuery) selectQuery).getRelations();
                // 获取到union所有组的关系
                if (relations != null && !relations.isEmpty())  {
                    // 目标targets字段
                    SQLSelectQueryBlock selectQueryBlock = findFirstSelectQueryBlock(selectQuery);
                    List<String> targetColumns = getTargetColumns(selectQueryBlock);

                    for (SQLSelectQuery relationSelectQuery : relations) {
                        if (relationSelectQuery instanceof SQLSelectQueryBlock) {
                            SQLSelectQueryBlock relationSelectQueryBlock = (SQLSelectQueryBlock) relationSelectQuery;
                            // 处理每一组的select血缘关系
                            processQueryBlockLineage(relationSelectQueryBlock, targetColumns);
                        }else {
                            throw new RuntimeException("selectQuery is SQLUnionQuery，match relations error，selectQuery type: " + selectQuery.getClass().getName() + "暂无相应处理");
                        }
                    }
                }

//                for (SQLSelectQuery relation : relations) {
//                    // 选择第一组 select xxx 来源字段作为目标写入端
//
//
//                    if (relation instanceof SQLSelectQueryBlock) {
//                        // 处理select xxx 来源字段
//                        processQueryBlockLineage((SQLSelectQueryBlock) relation, targetColumns);
//                    }else {
//                        throw new RuntimeException("暂不支持该场景");
//                    }
//                }
            }else {
                throw new RuntimeException("暂不支持该场景");
            }
        }
        return false;
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


    private void processQueryBlockLineage(SQLSelectQueryBlock queryBlock, List<String> targetColumns) {
        // 获取SELECT项列表
        SelectSpecialLineageService selectSpecialLineageService = new SelectSpecialLineageService();
        List<SelectColumn> selectColumns = selectSpecialLineageService.queryColumnsBySqlSelectQueryBlock(queryBlock);
        if (selectColumns == null || selectColumns.isEmpty()) return;

        // 遍历select 列表项 & targetColumns 来实现血缘关联
        for (int i = 0; i < selectColumns.size(); i++) {
            SelectColumn selectColumn = selectColumns.get(i);
            String sourceTableName = selectColumn.getTableName();
            String sourceColumnName = selectColumn.getColumnName();
            String aliasName = selectColumn.getAliasName();

            if (aliasName != null) {
                // 有别名情况 （选择指定sql语句中带有别名的表）
                if (targetColumns.contains(aliasName)) {
                    addRelation(fromTables.get(sourceTableName), sourceColumnName, targetTable, aliasName);
                }
            }else {
                // 无别名情况（多表连接场景下，则需要去比对真实传入表 & 字段来选择相应的表）
                addRelation(findSourceTableForColumn(sourceColumnName), sourceColumnName, targetTable, targetColumns.get(i));
            }
        }
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
     * 处理多种场景
     *   场景1: create table (id, name, xxx) as select xx, xx, xx 【无该场景】
     *   场景2：create table as select
     * @param createStmt
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
        return new ColumnLineageResult(targetTable, columnRelations);
    }
}
