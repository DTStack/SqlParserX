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
     *  处理表数据源
     * @param tableSource
     */
    private void processTableSource(SQLTableSource tableSource) {
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
            String alias = exprTableSource.getAlias();
            String tableName = extractTableName(exprTableSource);

            if (alias != null) {
                fromTables.put(alias, tableName);
            }
            fromTables.put(tableName, tableName);
        }
    }

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
            // 收集所有的表名映射情况
            SQLTableSource fromNode = x.getSelect().getQueryBlock().getFrom();
            fromNode.accept(new SQLASTVisitorAdapter() {
                @Override
                public boolean visit(SQLExprTableSource tableSource) {
                    String alias = tableSource.getAlias();
                    String tableName = extractTableName(tableSource);
                    if (alias != null) {
                        fromTables.put(alias, tableName);
                    }else {
                        fromTables.put(tableName, tableName);
                    }
                    return true;
                }
            });

            // 处理select xxx 来源字段
            processCreateTableAsSelect(x);
        }
        return false;
    }

    @Override
    public void endVisit(SQLCreateTableStatement x) {
        contextStack.pop();
    }


    private void processCreateTableAsSelect(SQLCreateTableStatement createStmt) {
        // 获取到目标表的字段名选项
        List<String> targetColumns = getTargetColumns(createStmt);

        // 获取SELECT项列表
        SQLSelect select = createStmt.getSelect();
        List<SQLSelectItem> selectItems = select.getQueryBlock().getSelectList();

        // 遍历SELECT项并处理
        for (int i = 0; i < selectItems.size(); i++) {
            SQLSelectItem selectItem = selectItems.get(i);
            String targetColumn = i < targetColumns.size() ?
                    targetColumns.get(i) :
                    "column_" + (i + 1);
            processSelectItem(selectItem, targetColumn);
        }

    }

    private void processSelectItem(SQLSelectItem selectItem, String targetColumn) {
        SQLExpr expr = selectItem.getExpr();
        if (expr instanceof SQLIdentifierExpr) {
            // 简单字段引用：从FROM子句查找表名
            String sourceColumn = ((SQLIdentifierExpr) expr).getName();
            String sourceTable = findSourceTableForColumn(sourceColumn);
            addRelation(sourceTable, sourceColumn, targetTable, targetColumn);
        }else if (expr instanceof SQLPropertyExpr) {
            // 带表名的字段引用：SELECT t.a AS col1 【注意：t可能是别名场景】
            SQLPropertyExpr propExpr = (SQLPropertyExpr) expr;
            String sourceTable = propExpr.getOwnerName();
            String sourceColumn = propExpr.getName();
            addRelation(fromTables.get(sourceTable), sourceColumn, targetTable, targetColumn);
        }
        // 支持聚合表达式，例如sum、count、avg类型
        else if (expr instanceof SQLAggregateExpr){
            SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) expr;
            List<SQLExpr> arguments = aggregateExpr.getArguments();
            // 目前需要简单check 一下，后续需要完善
            for (SQLExpr argument : arguments) {
                if (argument instanceof SQLPropertyExpr) {
                    // 带表名的字段引用：SELECT t.a AS col1 【注意：t可能是别名场景】
                    SQLPropertyExpr propExpr = (SQLPropertyExpr) argument;
                    String sourceTable = propExpr.getOwnerName();
                    String sourceColumn = propExpr.getName();
                    addRelation(fromTables.get(sourceTable), sourceColumn, targetTable, targetColumn);
                }
                else if (argument instanceof SQLIdentifierExpr) {
                    // 简单字段引用：从FROM子句查找表名
                    String sourceColumn = ((SQLIdentifierExpr) argument).getName();
                    String sourceTable = findSourceTableForColumn(sourceColumn);
                    addRelation(sourceTable, sourceColumn, targetTable, targetColumn);
                }
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
     * 处理表达式中的字段引用
     * @param expr 表达式node
     * @param targetTable 目标表
     * @param targetColumn 目标字段
     */
//    private void processExpression(SQLBinaryOpExpr expr, String targetTable, String targetColumn) {
//        // 递归处理表达式中的字段引用
//        expr.accept(new SQLASTVisitorAdapter() {
//            @Override
//            public boolean visit(SQLIdentifierExpr x) {
//                addRelation(null, x.getName(), targetTable, targetColumn);
//                return true;
//            }
//
//            @Override
//            public boolean visit(SQLPropertyExpr x) {
//                addRelation(x.getOwnerName(), x.getName(), targetTable, targetColumn);
//                return true;
//            }
//        });
//    }

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
    private List<String> getTargetColumns(SQLCreateTableStatement createStmt) {
        List<String> columns = new ArrayList<>();

        // 方式1：从表定义中获取列名（自定义指定列名场景）
//        for (SQLTableElement element : createStmt.getTableElementList()) {
//            if (element instanceof SQLColumnDefinition) {
//                columns.add(((SQLColumnDefinition) element).getColumnName());
//            }
//        }
        // 方式2：如果没有显式定义列，则使用SELECT项的别名（直接create table as select 这种形式，无列名场景）
        if (columns.isEmpty() && createStmt.getSelect() != null) {
            List<SQLSelectItem> selectList = createStmt.getSelect().getQueryBlock().getSelectList();
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
