package com.changlu.parser.common.table;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.changlu.lineage.core.select.table.SelectTable;
import com.changlu.lineage.core.select.table.SelectTableSpecialLineageService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @description  真实表解析select结构组件
 *  1、select 字段、from 表逻辑有追溯真实表的逻辑过程 【SelectTableSpecialLineageService中代码实现】
 *  2、针对于select结构中的额外场景情况，如【where in 中带有表】，这里就进行单独处理，不与血缘中追溯逻辑相耦合。
 * @author changlu
 * @date 2025/4/27 14:28
 */
public class ParseSelectTableService extends SelectTableSpecialLineageService {

    @Override
    public List<SelectTable> queryTableBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        List<SelectTable> tables = super.queryTableBySqlSelectQueryBlock(selectQueryBlock);
        // 针对 where in 场景中的真实表解析
        tables.addAll(queryTableByWhere(selectQueryBlock.getWhere()));
        return tables;
    }


    private List<SelectTable> queryTableByWhere(SQLExpr where) {
        List<SelectTable> tables = new ArrayList<>();
        // 举例：where id in (select * from table)
        if (where instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) where;
            // 即为select * from table，查询到表
            SQLSelect sqlSelect = sqlInSubQueryExpr.getSubQuery();
            List<SelectTable> selectTables = super.queryTablesBySqlSelect(sqlSelect);
            tables.addAll(selectTables);
        }
        return tables;
    }


}
