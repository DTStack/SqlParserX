package com.changlu.sqlparserx.service;

import com.alibaba.druid.DbType;
import com.changlu.lineage.column.ColumnLineageAnalyzer;
import com.changlu.lineage.column.ColumnLineageResult;
import com.changlu.lineage.column.ColumnLineageResult.ColumnRelation;
import com.changlu.sqlparserx.enums.DataSourceType2DbType;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LineageService {

    public Map<String, Object> getLineage(String sql, Integer datasourceType) {
        ColumnLineageAnalyzer analyzer = new ColumnLineageAnalyzer();
        DbType dbType = DataSourceType2DbType.getDbType(datasourceType);
        ColumnLineageResult columnLineageResult = analyzer.analyze(sql, null, dbType);

        Set<String> allTables = new HashSet<>();
        Map<String, Set<String>> tableFields = new HashMap<>();
        List<Map<String, String>> relationData = new ArrayList<>();

        String targetTable = columnLineageResult.getTargetTable();
        Set<String> targetFields = new HashSet<>();

        columnLineageResult.getColumnRelations().forEach((targetColumn, relations) -> {
            String[] targetParts = targetColumn.split("\\.");
            if (targetParts.length == 2) {
                targetFields.add(targetParts[1]);
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

                    String[] sourceParts = source.split("\\.");
                    if (sourceParts.length == 2) {
                        allTables.add(sourceParts[0]);
                        if (!tableFields.containsKey(sourceParts[0])) {
                            tableFields.put(sourceParts[0], new HashSet<>());
                        }
                        tableFields.get(sourceParts[0]).add(sourceParts[1]);
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