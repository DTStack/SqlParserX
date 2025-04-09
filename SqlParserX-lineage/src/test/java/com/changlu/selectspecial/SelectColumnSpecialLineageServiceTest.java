package com.changlu.selectspecial;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.core.select.column.SelectColumn;
import com.changlu.lineage.core.select.column.SelectColumnSpecialLineageService;
import com.changlu.AbstractSqlParserTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SelectColumnSpecialLineageServiceTest extends AbstractSqlParserTest {

    @Test
    public void test_01() {
        String sql = readSqlFile("1.txt");
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        SQLSelect select = stmt.getSelect();
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        SelectColumnSpecialLineageService selectColumnSpecialLineageService = new SelectColumnSpecialLineageService();
        List<SelectColumn> selectColumns = selectColumnSpecialLineageService.queryColumnsBySqlSelect(select);
        Assert.assertEquals(4, selectColumns.size());
        Assert.assertEquals(selectColumns.contains(new SelectColumn("users", "id", "user_id")), true);
        Assert.assertEquals(selectColumns.contains(new SelectColumn("users", "name", "name")), true);
        Assert.assertEquals(selectColumns.contains(new SelectColumn("orders,orders", "amount,ttt", "total_amount")), true);
        Assert.assertEquals(selectColumns.contains(new SelectColumn("orders", "id", "order_count")), true);
    }

    @Override
    public String getSqlTestPath() {
        return "/selectspecial/";
    }

}
