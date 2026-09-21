/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.core.select.table;

import com.dtstack.sqlparserx.core.enums.SQLParseStrategy;
import com.dtstack.sqlparserx.core.utils.CollectionUtil;
import com.dtstack.sqlparserx.core.utils.TableSourceUtil;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SelectTableSpecialLineageService {

    private int features;

    public SelectTableSpecialLineageService() {
    }

    public SelectTableSpecialLineageService(SQLParseStrategy... features) {
        this.features = SQLParseStrategy.of(features);
    }

    public List<SelectTable> queryTablesBySqlSelect(SQLSelect sqlSelect) {
        if (sqlSelect == null) {
            return new ArrayList<>();
        }
        SQLSelectQuery sqlSelectQuery = sqlSelect.getQuery();
        return this.queryTablesBySqlSelectQuery(sqlSelectQuery);
    }

    public List<SelectTable> queryTablesBySqlSelectQuery(SQLSelectQuery sqlSelectQuery) {
        List<SelectTable> selectTables = new ArrayList<>();
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            selectTables = this.queryTableBySqlSelectQueryBlock((SQLSelectQueryBlock) sqlSelectQuery);
        } else if (sqlSelectQuery instanceof SQLUnionQuery) {
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            List<SQLSelectQuery> relations = sqlUnionQuery.getRelations();
            if (relations == null || relations.isEmpty())
                return selectTables;
            for (int i = 0; i < relations.size(); i++) {
                List<SelectTable> curSelectTables = queryTablesBySqlSelectQuery(relations.get(i));
                selectTables.addAll(curSelectTables);
            }
        }

        // 去除无真实表的记录
        if (CollectionUtil.isNotEmpty(selectTables)) {
            selectTables = selectTables.stream()
                    .filter(table -> table != null && !StringUtils.isEmpty(table.getTableInfo()))
                    .collect(Collectors.toList());
        }
        return selectTables;
    }

    protected List<SelectTable> queryTableBySqlSelectQueryBlock(SQLSelectQueryBlock selectQueryBlock) {
        if (selectQueryBlock == null)
            return null;
        // 从表字段中筛选得到表
        List<SelectTable> tables = this.queryTableBySQLSelectItems(selectQueryBlock.getSelectList());
        // 表来源拆分
        SQLTableSource fromTable = selectQueryBlock.getFrom();
        // 基于from、select选项完成构造表字段记录逻辑
        tables.addAll(queryFromTable(fromTable));
        return tables;
    }

    /**
     * 查询出来一组字段中的表
     */
    protected List<SelectTable> queryTableBySQLSelectItems(List<SQLSelectItem> selectList) {
        List<SelectTable> selectTables = new ArrayList<>();
        for (SQLSelectItem sqlSelectItem : selectList) {
            SQLExpr expr = sqlSelectItem.getExpr();
            selectTables.addAll(this.queryFromTableBySQLExpr(expr));
        }
        return selectTables;
    }

    /**
     * 查询字段表达式中是否带有真实表场景
     */
    private List<SelectTable> queryFromTableBySQLExpr(SQLExpr expr) {
        List<SelectTable> selectTables = new ArrayList<>();
        if (expr instanceof SQLQueryExpr) {
            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) expr;
            // select (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count
            SQLSelect sqlSelect = sqlQueryExpr.getSubQuery();
            selectTables = this.queryTablesBySqlSelect(sqlSelect);
        }
        return selectTables;
    }

    protected List<SelectTable> queryFromTable(SQLTableSource fromTable) {
        List<SelectTable> tables = new ArrayList<>();
        // 不同场景
        // 场景1：from 普通表场景（SQLExprTableSource） 如：from table1  或者 from table1 as t1
        if (fromTable instanceof SQLExprTableSource) {
            // #feat2：结合select & from 结合转换字段处理
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) fromTable;
            // 真实表名 表别名
            String sourceTableName = TableSourceUtil.extractTableName((SQLExprTableSource) fromTable);
            String aliasTableName = sqlExprTableSource.getAlias() == null
                    ? sourceTableName
                    : sqlExprTableSource.getAlias();
            tables.add(new SelectTable(sourceTableName, aliasTableName));
        }// 场景2：子查询表（SQLSubqueryTableSource） 如：from (select xxx, xxx from table 1)
        else if (fromTable instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) fromTable;
            SQLSelect select = sqlSubqueryTableSource.getSelect();
            String aliasTableName = sqlSubqueryTableSource.getAlias();
            // 查询到主体select中的多个表
            // 如：select * from table3 as tt3 left join table4 as tt4 on tt3.id = tt4.id
            // 得到多条表记录
            //    真实表      表别名
            //    table3      tt3
            //    table4      tt4
            List<SelectTable> subQuerySelectTables = this.queryTablesBySqlSelect(select);

            SelectTable selectTable = this.doMergeSubqueryTables(subQuerySelectTables, aliasTableName);
            tables.add(selectTable);
        } // 场景3：join表 如：from table1 left|inner|right join table2 on table1.id = table2.id
        else if (fromTable instanceof SQLJoinTableSource) {
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) fromTable;
            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
//            // 左表匹配扫描
            tables.addAll(queryFromTable(left));
//            // 右表匹配扫描
            tables.addAll(queryFromTable(right));
        } // 场景5：子查询+union结构（SQLUnionQueryTableSource） 与场景2基本一致
        else if (fromTable instanceof SQLUnionQueryTableSource) {
            SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) fromTable;
            SQLUnionQuery sqlUnionQuery = sqlUnionQueryTableSource.getUnion();
            String aliasTableName = sqlUnionQueryTableSource.getAlias();

            List<SelectTable> subQuerySelectTables = this.queryTablesBySqlSelectQuery(sqlUnionQuery);

            SelectTable selectTable = this.doMergeSubqueryTables(subQuerySelectTables, aliasTableName);
            tables.add(selectTable);
        } // 场景6：LATERAL VIEW表源（SQLLateralViewTableSource），内部有一个tableSource，复用方法逻辑即可
        else if (fromTable instanceof SQLLateralViewTableSource) {
            SQLLateralViewTableSource sqlLateralViewTableSource = (SQLLateralViewTableSource) fromTable;
            SQLTableSource tableSource = sqlLateralViewTableSource.getTableSource();
            tables.addAll(this.queryFromTable(tableSource));
        }
        return tables;
    }

    /**
     * 将子查询中的一组表 合并 为一个SelectTable
     * @param subQuerySelectTables List<SelectTable>
     * @return SelectTable
     */
    public SelectTable doMergeSubqueryTables(List<SelectTable> subQuerySelectTables, String aliasTableName) {
        // 合并为一条表记录
        //      真实表          表别名
        // table3, table4        t34
        List<String> tableGroup = new ArrayList<>();
        for (SelectTable subQuerySelectTable : subQuerySelectTables) {
            tableGroup.add(subQuerySelectTable.getTableInfo());
        }
        return new SelectTable(String.join(",", tableGroup), aliasTableName);
    }

    /**
     * 构建with map
     *    key：with as别名   value：List<SelectTable>
     * @param with SQLWithSubqueryClause
     * @return Map<String,List<SelectTable>>
     */
    public Map<String, List<SelectTable>> buildTableInfoByTableMap(SQLWithSubqueryClause with) {
        // 为了支持后续的cte向前推断，这里的map就应该是有序的
        Map<String, List<SelectTable>> tableInfoByTableMap = new LinkedHashMap<>();
        if (with != null) {
            List<SQLWithSubqueryClause.Entry> entries = with.getEntries();
            if (!entries.isEmpty()) {
                // 遍历每一组with as结构
                for (SQLWithSubqueryClause.Entry entry : entries) {
                    // as 的别名
                    String aliasTableName = entry.getAlias();
                    SQLSelect withSubquerySQLSelect = entry.getSubQuery();
                    List<SelectTable> withSubqueryTables = this.queryTablesBySqlSelect(withSubquerySQLSelect);
                    tableInfoByTableMap.put(aliasTableName, withSubqueryTables);
                }
            }
        }
        // 单独处理cte向前引用场景
        boolean enableCteForwardReference = SQLParseStrategy.isEnabled(
                this.features,
                SQLParseStrategy.CTE_FORWARD_REFERENCE);

        // 开启cte向前引用场景
        if (enableCteForwardReference) {
            // 按照with as cte机制 从前往后去顺序依次替换with as的别名表
            tableInfoByTableMap = findRealCteForwardReferenceTableMap(tableInfoByTableMap);
        }
        return tableInfoByTableMap;
    }

    /**
     * 按照with as cte机制 从前往后去顺序依次替换with as的表
     * @param tableInfoByTableMap Map<List<SelectTable>>
     * @return Map<String,List<SelectTable>>
     */
    private Map<String, List<SelectTable>> findRealCteForwardReferenceTableMap(
            Map<String, List<SelectTable>> tableInfoByTableMap) {
        Map<String, List<SelectTable>> tempRealWithasMap = new LinkedHashMap<>();
        if (tableInfoByTableMap != null && !tableInfoByTableMap.isEmpty()) {
            // 遍历每一个现有可能存在临时with as 别名表的map
            for (Map.Entry<String, List<SelectTable>> entry : tableInfoByTableMap.entrySet()) {
                String withAsKey = entry.getKey();
                List<SelectTable> curTables = entry.getValue();

                // 需要去拿到当前pos前的一组map 去比对其中的key是否为当前的curTables的，若是则需要替换进来
                if (CollectionUtil.isNotEmpty(curTables)) {
                    // 查询到真实的一组with as tables表
                    curTables = findRealWithasTables(curTables, tempRealWithasMap);
                }

                //  每次暂存最新的with as map
                tempRealWithasMap.put(withAsKey, curTables);
            }
        }
        return tempRealWithasMap;
    }

    /**
     * 对当前的withas 真实表去寻找真实的一组集合
     * @param curTables List<SelectTable>
     * @param tempRealWithasMap Map<List<SelectTable>>
     * @return List<SelectTable>
     */
    private List<SelectTable> findRealWithasTables(List<SelectTable> curTables,
            Map<String, List<SelectTable>> tempRealWithasMap) {
        if (tempRealWithasMap.isEmpty())
            return curTables;

        List<SelectTable> realTables = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(curTables)) {
            for (SelectTable curSelectTable : curTables) {

                Set<String> realTableNameList = new HashSet<>();
                // 会有多个表同时存在在一个字段中
                String[] selectTableNameArr = curSelectTable.getTableInfo().split(",", -1);
                int realSelectTableLength = selectTableNameArr.length;
                for (int i = 0; i < realSelectTableLength; i++) {
                    String tableName = selectTableNameArr[i];
                    // 寻找with as别名情况
                    boolean exists = tempRealWithasMap.containsKey(tableName);
                    if (exists) {
                        List<SelectTable> selectTables = tempRealWithasMap.get(tableName);
                        // 收集最新的真实表名
                        if (CollectionUtil.isNotEmpty(selectTables)) {
                            for (SelectTable selectTable : selectTables) {
                                realTableNameList.add(selectTable.getTableInfo());
                            }
                        }
                    } else {
                        realTableNameList.add(tableName);
                    }
                }

                // 变更最新的tableInfo
                curSelectTable.setTableInfo(String.join(",", realTableNameList));
                realTables.add(curSelectTable);
            }
        }
        return realTables;
    }

    /**
     * with as 场景处理：当前只考虑完成selectTables中具有的别名替换
     *  注意：这块逻辑表血缘 与 表解析有一些小区别（表解析应当将with as中的所有表字段全部解析拿到的）
     */
    public List<SelectTable> matchWithAsAndSelectTables(List<SelectTable> selectTables,
            Map<String, List<SelectTable>> tableInfoByTableMap) {
        /**
         * filter逻辑：针对with as递归cte场景单独处理
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

        // 重新构建匹配替换select主体引用的别名表
        List<SelectTable> newSelectTables = new ArrayList<>();
        for (SelectTable selectTable : selectTables) {
            List<String> tableGroup = new ArrayList<>();
            String[] selectTableArr = selectTable.getTableInfo().split(",");
            for (String tableName : selectTableArr) {
                tableGroup.add(aliasNameBySourceTables.getOrDefault(tableName, tableName));
            }
            newSelectTables.add(new SelectTable(String.join(",", tableGroup), selectTable.getAliasName()));
        }
        return newSelectTables;
    }

}
