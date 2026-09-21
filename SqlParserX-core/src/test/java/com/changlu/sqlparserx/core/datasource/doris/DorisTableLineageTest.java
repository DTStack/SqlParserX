package com.changlu.sqlparserx.core.datasource.doris;

import com.changlu.sqlparserx.core.common.AbstractSqlParserTest;
import com.changlu.sqlparserx.core.parser.ISqlParser;
import com.changlu.sqlparserx.core.parser.SqlParserFactory;
import com.changlu.sqlparserx.core.parser.base.lineage.domain.TableLineage;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableLineageResult;
import com.changlu.sqlparserx.core.parser.base.lineage.table.TableRelation;

import com.alibaba.druid.DbType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @description  doris特殊表血缘解析
 * @author changlu
 * @date 2026/1/31 18:59
 */
public class DorisTableLineageTest extends AbstractSqlParserTest {

    private final ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.doris);
    private String sqlTxtName = "DorisTableLineageTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/datasource/doris/";
    }

    /**
     * 支持CTE_FORWARD_REFERENCE 特性，cte表引用向前表追溯
     */
    @Test
    public void test_1_withas_cte_forward() {
        String sql = readSqlByIndex(sqlTxtName, 1);
        TableLineageResult result = sqlParser.parseTableLineages(sql);
        List<TableLineage> tableLineages = result.getTableLineages();
        assertEquals(2, tableLineages.size());
        assertTrue(result.getRelations().contains(new TableRelation("demo_table2", "new_table")));
        assertTrue(result.getRelations().contains(new TableRelation("demo_table", "new_table")));
    }

}
