package com.changlu.sqlparserx.core.parser.base.table;

import com.changlu.sqlparserx.core.domain.Pair;
import com.changlu.sqlparserx.core.parser.IParseTableClient;
import com.changlu.sqlparserx.core.parser.base.lineage.table.Table;
import com.changlu.sqlparserx.core.parser.base.table.createtable.domain.*;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.DistributedByTypeEnum;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TableKeyEnum;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TablePartitionEnum;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseCreateTableResult;
import com.changlu.sqlparserx.core.utils.CollectionUtil;
import com.changlu.sqlparserx.core.utils.CommonUtil;
import com.changlu.sqlparserx.core.utils.TableSourceUtil;

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.statement.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description  解析create table 类型client
 * @author changlu
 * @date 2025/8/4 11:20
 */
public class ParseCreateTableClient implements IParseTableClient {

    @Override
    public ParseCreateTableResult parseCreateTable(SQLStatement stmt) {
        if (stmt instanceof SQLCreateTableStatement) {
            return parseCreateTableInfo((SQLCreateTableStatement) stmt);
        } else {
            throw new UnsupportedOperationException("该方言暂未支持");
        }
    }

    protected ParseCreateTableResult parseCreateTableInfo(SQLCreateTableStatement stmt) {
        ParseCreateTableResult result = new ParseCreateTableResult();

        // 是否存在if not exist判断
        boolean ifNotExists = stmt.isIfNotExists();
        result.setNotExists(ifNotExists);

        // 获取到建表基本信息
        SQLExprTableSource tableSource = stmt.getTableSource();
        Table table = TableSourceUtil.extractTable(tableSource);
        result.setDb(table.getDb());
        result.setTableName(table.getName());

        // 获取到表注释信息
        SQLExpr tableComment = stmt.getComment();
        if (tableComment != null) {
            result.setTableComment(CommonUtil.removeQuotes(tableComment.toString()));
        }

        // 获取到表的建表字段信息
        result.setTableElementList(parseCreateTableElement(stmt.getTableElementList()));
        // 获取到表的分区信息
        result.setPartitionedByList(parseColumnDefinition(stmt.getPartitionColumns()));
        // 获取到表的指定类型键信息
        result.setTableKey(parseTableKey(stmt.getUnique()));
        // 获取到分桶信息
        result.setTableDistributedBuckets(parseTableDistributedBuckets(stmt));
        // 获取表的特定分区
        result.setTablePartition(parseTablePartition(stmt.getPartitioning()));
        // 获取表的properties参数
        result.setProperties(parseTableProperties(stmt.getTableOptions()));

        // 是否开启识别动态分区
        if (isDynamicPartitionEnabled()) {
            TablePartition tablePartition = result.getTablePartition();
            Map<String, String> properties = result.getProperties();
            if (analyzeHasDynamicPartitionTypeProperties(properties)) {
                tablePartition.setTablePartitionEnum(TablePartitionEnum.DYNAMIC);
            }
        }
        return result;
    }

    /**
     * 分析配置参数中是否有动态分区
     * @param properties Map<String>
     * @return boolean
     * @author changlu
     */
    protected boolean analyzeHasDynamicPartitionTypeProperties(Map<String, String> properties) {
        return false;
    }

    /**
     * 是否开启识别动态分区
     * @param
     * @return boolean
     */
    protected boolean isDynamicPartitionEnabled() {
        return false;
    }

    /**
     * 解析表的properties参数
     * @param tableOptions 表的properties参数
     * @return Map<String, String>
     */
    protected Map<String, String> parseTableProperties(List<SQLAssignItem> tableOptions) {
        Map<String, String> properties = new HashMap<>();
        if (CollectionUtil.isEmpty(tableOptions))
            return properties;
        for (SQLAssignItem tableOption : tableOptions) {
            String target = CommonUtil.sqlExpr2Value(tableOption.getTarget());
            String value = CommonUtil.sqlExpr2Value(tableOption.getValue());
            properties.put(target, value);
        }
        return properties;
    }

    /**
     * 解析表的分桶信息
     * @param stmt SQLCreateTableStatement
     * @return TableDistributedBuckets
     */
    protected TableDistributedBuckets parseTableDistributedBuckets(SQLCreateTableStatement stmt) {
        if (stmt == null)
            return new TableDistributedBuckets();
        // 分桶数
        int buckets = stmt.getBuckets();
        // 获取到分桶类型 & 分桶字段
        Pair<DistributedByTypeEnum, List<String>> distributedByTypeEnumListPair = dealDistributeByColumns(stmt);
        DistributedByTypeEnum distributedByTypeEnum = distributedByTypeEnumListPair.getFirst();
        List<String> columnNames = distributedByTypeEnumListPair.getSecond();

        // 设置分桶结果类
        TableDistributedBuckets tableDistributedBuckets = new TableDistributedBuckets();
        tableDistributedBuckets.setBucketNum(buckets);
        tableDistributedBuckets.setDistributedByTypeEnum(distributedByTypeEnum);
        tableDistributedBuckets.setColumnNames(columnNames);
        return tableDistributedBuckets;
    }

    /**
     * 获取到key：DistributedByType，value为字段名
     * @param stmt SQLCreateTableStatement
     * @return Pair<DistributedByTypeEnum,List<String>>
     */
    protected Pair<DistributedByTypeEnum, List<String>> dealDistributeByColumns(SQLCreateTableStatement stmt) {
        // 暂无实现
        return new Pair<>(null, new ArrayList<>());
    }

    protected TablePartition parseTablePartition(SQLPartitionBy partitionBy) {
        if (partitionBy == null)
            return new TablePartition();
        TablePartition tablePartition = new TablePartition();
        List<SQLExpr> columns = partitionBy.getColumns();
        List<SQLPartition> partitions = partitionBy.getPartitions();

        // 设置分区字段
        if (CollectionUtil.isNotEmpty(columns)) {
            tablePartition.setColumnName(CommonUtil.sqlExprs2Values(columns));
        }
        // 设置分区元素
        tablePartition.setTablePartitionElement(dealTablePartitionElement(partitions));
        // 设置分区类型枚举
        tablePartition.setTablePartitionEnum(dealTablePartitionEnum(partitionBy));
        return tablePartition;
    }

    /**
     * 处理表分区元素
     * @param partitions SQLPartition
     * @return List<TablePartitionElement>
     */
    protected List<TablePartitionElement> dealTablePartitionElement(List<SQLPartition> partitions) {
        List<TablePartitionElement> partitionElements = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(partitions)) {
            partitionElements = partitions.stream()
                    .map(partition -> {
                        TablePartitionElement tablePartitionElement = new TablePartitionElement();
                        if (partition instanceof SQLPartitionSingle) {
                            SQLPartitionSingle sqlPartitionSingle = (SQLPartitionSingle) partition;
                            SQLName name = sqlPartitionSingle.getName();
                            SQLPartitionValue values = sqlPartitionSingle.getValues();
                            List<SQLExpr> items = values.getItems();

                            tablePartitionElement.setName(name.getSimpleName());
                            tablePartitionElement.setValues(CommonUtil.sqlExprs2Values((items)));
                        }
                        return tablePartitionElement;
                    })
                    .collect(Collectors.toList());
        }
        return partitionElements;
    }

    /**
     * 处理表分区枚举
     * @param partitionBy SQLPartitionBy
     * @return TablePartitionEnum
     */
    protected TablePartitionEnum dealTablePartitionEnum(SQLPartitionBy partitionBy) {
        TablePartitionEnum tablePartitionEnum = null;
        if (partitionBy instanceof SQLPartitionByRange) {
            tablePartitionEnum = TablePartitionEnum.RANGER;
        } else if (partitionBy instanceof SQLPartitionByList) {
            tablePartitionEnum = TablePartitionEnum.LIST;
        }
        return tablePartitionEnum;
    }

    /**
     * 解析建表指定类型的键值
     * @param unique SQLUnique
     * @return TableKey
     */
    protected TableKey parseTableKey(SQLUnique unique) {
        if (unique == null)
            return new TableKey();
        TableKey tableKey = new TableKey();
        // 处理指定类型键值的类型信息（默认unique）
        tableKey.setTableKeyEnum(dealTableKey(unique));

        // 处理指定类型键值的列信息
        List<SQLSelectOrderByItem> columns = unique.getColumns();
        List<String> columnNames = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(columns)) {
            columnNames = columns.stream().map((column) -> {
                String columnName = column.getExpr().toString();
                return CommonUtil.removeQuotes(columnName);
            }).collect(Collectors.toList());
        }
        tableKey.setColumnName(columnNames);

        return tableKey;
    }

    protected TableKeyEnum dealTableKey(SQLUnique unique) {
        return TableKeyEnum.UNIQUE;
    }

    protected List<TableElement> parseCreateTableElement(List<SQLTableElement> tableElementList) {
        if (CollectionUtil.isEmpty(tableElementList)) {
            return new ArrayList<>();
        }
        List<TableElement> resultList = new ArrayList<>();
        for (SQLTableElement sqlTableElement : tableElementList) {
            if (sqlTableElement instanceof SQLColumnDefinition) {
                resultList.addAll(parseColumnDefinition(Collections.singletonList(
                        (SQLColumnDefinition) sqlTableElement)));
            }
        }
        return resultList;
    }

    private List<TableElement> parseColumnDefinition(List<SQLColumnDefinition> columnDefinitions) {
        if (CollectionUtil.isEmpty(columnDefinitions)) {
            return new ArrayList<>();
        }
        List<TableElement> resultList = new ArrayList<>();
        for (SQLColumnDefinition columnDefinition : columnDefinitions) {
            TableElement element = new TableElement();
            String name = columnDefinition.getName().getSimpleName();
            String typeName = columnDefinition.getDataType().getName();
            SQLExpr comment = columnDefinition.getComment();
            SQLCharExpr aggType = columnDefinition.getAggType();

            element.setName(CommonUtil.removeQuotes(name));
            element.setType(CommonUtil.removeQuotes(typeName));
            if (Objects.nonNull(comment)) {
                element.setComment(CommonUtil.removeQuotes(comment.toString()));
            }
            element.setArguments(columnDefinition.getDataType().getArguments().stream().map(x -> x.toString()).collect(
                    Collectors.toList()));
            element.setAggType(CommonUtil.sqlExpr2Value(aggType));
            resultList.add(element);
        }
        return resultList;
    }

}
