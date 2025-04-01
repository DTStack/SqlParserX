package com.changlu.lineage.column;

import java.util.*;

public class ColumnLineageResult {
    private final String targetTable;
    private final Map<String, List<ColumnRelation>> columnRelations;
    
    public ColumnLineageResult(String targetTable, 
                             Map<String, List<ColumnRelation>> columnRelations) {
        this.targetTable = targetTable;
        this.columnRelations = Collections.unmodifiableMap(columnRelations);
    }
    
    public String getTargetTable() {
        return targetTable;
    }
    
    public Map<String, List<ColumnRelation>> getColumnRelations() {
        return columnRelations;
    }
    
    public static class ColumnRelation {
        private final String source;
        private final String target;
        
        public ColumnRelation(String source, String target) {
            this.source = source;
            this.target = target;
        }
        
        public String getSource() {
            return source;
        }
        
        public String getTarget() {
            return target;
        }
        
        @Override
        public String toString() {
            return source + " -> " + target;
        }
    }

    /**
     * 打印字段血缘关系
     */
    public void printColumnLineage() {
        Map<String, List<ColumnLineageResult.ColumnRelation>> columnRelations = this.columnRelations;
        // 遍历每一个目标字段的血缘情况
        for (Map.Entry<String, List<ColumnLineageResult.ColumnRelation>> entry : columnRelations.entrySet()) {
            String targetColumn = entry.getKey();
            List<ColumnLineageResult.ColumnRelation> value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                for (ColumnLineageResult.ColumnRelation relation : value) {
                    System.out.println(relation);
                }
            }else {
                System.out.println("null -> " + targetColumn);
            }
        }
    }
}