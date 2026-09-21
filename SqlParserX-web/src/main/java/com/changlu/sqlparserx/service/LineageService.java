package com.changlu.sqlparserx.service;

import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;
import com.changlu.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult.ColumnRelation;
import com.changlu.sqlparserx.enums.DataSourceType2DbType;

import com.alibaba.druid.DbType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LineageService {

    public Map<String, Object> getLineage(String sql, Integer datasourceType) {
        // 无匹配的数据源则走
        DbType dbType = DataSourceType2DbType.getDbType(datasourceType);
        ISqlParser sqlParser = SqlParserFactory.getSqlParser(dbType);
        ColumnLineageResult columnLineageResult = sqlParser.parseColumnLineages(sql, new HashMap<>());

        Set<String> allTables = new HashSet<>();
        Map<String, Set<String>> tableFields = new HashMap<>();
        List<Map<String, String>> relationData = new ArrayList<>();

        String targetTable = columnLineageResult.getTargetTable();
        Set<String> targetFields = new HashSet<>();

        columnLineageResult.getColumnRelations().forEach((targetColumn, relations) -> {
            // Split on last dot only to handle cases like "digua_ceshi.result_table.id"
            int lastDotIndex = targetColumn.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String columnName = targetColumn.substring(lastDotIndex + 1);
                targetFields.add(columnName);
            }

            if (relations != null) {
                for (ColumnRelation relation : relations) {
                    String source = relation.getSource();
                    String target = relation.getTarget();

                    // JDK8兼容写法：使用HashMap创建关系Map
                    Map<String, String> relationMap = new HashMap<>();
                    relationMap.put("from", source);
                    relationMap.put("to", target);
                    relationData.add(relationMap);

                    // Handle source with same logic (split on last dot)
                    int sourceLastDotIndex = source.lastIndexOf('.');
                    if (sourceLastDotIndex > 0) {
                        String tableName = source.substring(0, sourceLastDotIndex);
                        String sourceColumnName = source.substring(sourceLastDotIndex + 1);

                        allTables.add(tableName);
                        if (!tableFields.containsKey(tableName)) {
                            tableFields.put(tableName, new HashSet<>());
                        }
                        tableFields.get(tableName).add(sourceColumnName);
                    }
                }
            }
        });

        allTables.add(targetTable);
        tableFields.put(targetTable, targetFields);

        // JDK8兼容写法：构建tableData
        List<Map<String, Object>> tableData = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : tableFields.entrySet()) {
            Map<String, Object> tableInfo = new HashMap<>();
            tableInfo.put("tableName", entry.getKey());
            tableInfo.put("fields", new ArrayList<>(entry.getValue()));
            tableData.add(tableInfo);
        }

        // JDK8兼容写法：构建最终返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("tableData", tableData);
        result.put("relationData", relationData);
        return result;
    }
}