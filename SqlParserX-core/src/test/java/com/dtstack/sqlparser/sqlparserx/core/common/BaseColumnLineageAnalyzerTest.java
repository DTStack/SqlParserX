package com.dtstack.sqlparser.sqlparserx.core.common;

import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.column.ColumnLineageResult;

import java.util.*;

import static org.junit.Assert.*;

public class BaseColumnLineageAnalyzerTest extends AbstractSqlParserTest {

    /**
     * 验证关联关系
     * @param relations
     * @param target 目标端字段 table.column
     * @param source 来源字段 table.column
     */
    public void assertRelation(Map<String, List<ColumnLineageResult.ColumnRelation>> relations,
                              String target, String source) {
        List<ColumnLineageResult.ColumnRelation> rels = relations.get(target);
        if (rels == null) assertTrue(false);
        boolean flag = false;
        // check指定来源字段中的关联关系中包含有期望来源：expectedSource
        for (ColumnLineageResult.ColumnRelation rel : rels) {
            if (rel.getSource().equals(source)) {
                flag = true;
                break;
            }
        }
        assertTrue(flag);
    }
}