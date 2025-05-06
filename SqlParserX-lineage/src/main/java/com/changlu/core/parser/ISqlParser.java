package com.changlu.core.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.changlu.core.parser.base.lineage.column.Column;
import com.changlu.core.parser.base.lineage.column.ColumnLineageClient;
import com.changlu.core.parser.base.lineage.column.ColumnLineageResult;
import com.changlu.core.parser.base.lineage.table.TableLineageClient;
import com.changlu.core.parser.base.lineage.table.TableLineageResult;
import com.changlu.core.parser.base.table.ParseTableClient;
import com.changlu.core.parser.base.table.domain.ParseTableResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISqlParser {

    /**
     * 解析列血缘
     * @param sql sql sql
     * @param tableColumnsMap 元数据表及字段映射关系
     * @return 字段血缘结果
     */
    ColumnLineageResult parseColumnLineages(String sql, Map<String, List<Column>> tableColumnsMap);

    /**
     * 解析列血缘
     * @param sqlStatement druid语法树
     * @param tableColumnsMap 元数据表及字段映射关系
     * @return 字段血缘结果
     */
    ColumnLineageResult parseColumnLineages(SQLStatement sqlStatement, Map<String, List<Column>> tableColumnsMap);

    /**
     * 解析表血缘
     * @param sql sql sql
     * @return 表血缘结果
     */
    TableLineageResult parseTableLineages(String sql);

    /**
     * 解析表血缘
     * @param sqlStatement druid语法树
     * @return 表血缘结果
     */
    TableLineageResult parseTableLineages(SQLStatement sqlStatement);

    /**
     * 解析真实表
     * @param sql String
     * @return ParseTableResult 解析表结果
     */
    ParseTableResult parseTables(String sql);

    /**
     * 解析真实表
     * @param sqlStatement druid语法树
     * @return ParseTableResult 解析表结果
     */
    ParseTableResult parseTables(SQLStatement sqlStatement);

    /**
     * 解析sql中的函数
     * @param sql String sql
     * @return Set<String> 函数集合
     */
    Set<String> parseFunction(String sql);


    default IParseTableClient getParseTableClient() {
        return new ParseTableClient();
    }

    default IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        return new ColumnLineageClient(tableColumnsMap);
    }

    default ITableLineageClient getTableLineageClient() {
        return new TableLineageClient();
    }


}
