package com.changlu.lineage.column;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.utils.TableSourceUtil;

import java.util.ArrayList;
import java.util.List;

public class SelectSpecialLineageService {

    /**
     * 获取到SQLSelect中所有的字段【不关注别名】
     * @param selectQueryBlock select语句
     * @return 关联到的所有字段(只关注获取到字段来源表名 & 字段名)
     */
    public List<SelectColumn> queryColumnsBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        if (selectQueryBlock == null) return null;
        List<SelectColumn> selectColumns = new ArrayList<>();
        // 遍历SELECT项并处理
        List<SQLSelectItem> selectItems = selectQueryBlock.getSelectList();
        // 获取到当前select一组的表名
//        String tableName = TableSourceUtil.extractTableName(selectQueryBlock.getFrom());
        for (int i = 0; i < selectItems.size(); i++) {
            SQLSelectItem selectItem = selectItems.get(i);
            List<SelectColumn> columns = queryColumnsBySelectItem(selectItem);
//            columns.forEach(selectColumn -> selectColumn.setTableName(tableName));
            // 处理selectItem
            selectColumns.addAll(columns);
        }
        return selectColumns;
    }

    /**
     * 获取到SQLSelectItem所带有的字段【不关注别名】
     * @param selectItem select选项
     * @return 关联到的所有字段(只关注获取到字段来源表名 & 字段名)
     */
    private List<SelectColumn> queryColumnsBySelectItem(SQLSelectItem selectItem) {
        SQLExpr expr = selectItem.getExpr();
        String alias = selectItem.getAlias();
        List<SelectColumn> selectColumns = queryColumnsByExpr(expr);
        for (SelectColumn selectColumn : selectColumns) {
            selectColumn.setAliasName(alias);
        }
        return selectColumns;
    }

    /**
     * 处理一个expr表达式中所带有的所有字段【不关注别名】
     * @param expr 表达式
     * @return 关联到的所有字段(只关注获取到字段来源表名 & 字段名)
     */
    private List<SelectColumn> queryColumnsByExpr(SQLExpr expr) {
        // 构造结果集
        List<SelectColumn> selectColumns = new ArrayList<>();

        String sourceTable = null;
        String sourceColumn = null;
        if (expr instanceof SQLIdentifierExpr) {
            // 简单字段引用：从FROM子句查找表名
            sourceColumn = ((SQLIdentifierExpr) expr).getName();
            selectColumns.add(new SelectColumn(sourceTable, sourceColumn));
        }else if (expr instanceof SQLPropertyExpr) {
            // 带表名的字段引用：SELECT t.a AS col1
            SQLPropertyExpr propExpr = (SQLPropertyExpr) expr;
            sourceTable = propExpr.getOwnerName();
            sourceColumn = propExpr.getName();
            selectColumns.add(new SelectColumn(sourceTable, sourceColumn));
        }else if (expr instanceof SQLAggregateExpr) {
            // 聚合表达式场景：如sum、count、avg等
            SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) expr;
            List<SQLExpr> arguments = aggregateExpr.getArguments();
            for (SQLExpr argumentExpr : arguments) {
                selectColumns.addAll(queryColumnsByExpr(argumentExpr));
            }
        }
        return selectColumns;
    }

    public static void main(String[] args) {
        String sql = "SELECT \n" +
                "    u.id as user_id, \n" +
                "    u.name,\n" +
                "    SUM(o.amount) as total_amount,\n" +
                "    COUNT(o.id) as order_count\n" +
                "FROM users u JOIN orders o ON u.id = o.user_id\n" +
                "GROUP BY u.id, u.name";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        SQLSelect select = stmt.getSelect();
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        SelectSpecialLineageService selectSpecialLineageService = new SelectSpecialLineageService();
        List<SelectColumn> selectColumns = selectSpecialLineageService.queryColumnsBySqlSelectQueryBlock(queryBlock);
        System.out.println(selectColumns);
    }

}

class SelectColumn {

    private String tableName;
    private String columnName;
    private String aliasName;

    public SelectColumn(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "SelectColumn{" +
                "columnName='" + columnName + '\'' +
                ", tableName='" + tableName + '\'' +
                '}';
    }
}
