import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.lineage.core.select.SelectColumn;
import com.changlu.lineage.core.select.SelectSpecialLineageService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SelectSpecialLineageServiceTest {

    @Test
    public void test_01() {
        String sql = "SELECT \n" +
                "    u.id as user_id, \n" +
                "    u.name,\n" +
                "    concat(o.amount, '', o.ttt) as total_amount,\n" +
                "    COUNT(o.id) as order_count\n" +
                "FROM users u JOIN orders o ON u.id = o.user_id\n" +
                "GROUP BY u.id, u.name";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        SQLSelect select = stmt.getSelect();
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        SelectSpecialLineageService selectSpecialLineageService = new SelectSpecialLineageService();
        List<SelectColumn> selectColumns = selectSpecialLineageService.queryColumnsBySqlSelect(select);
        Assert.assertEquals(4, selectColumns.size());
        Assert.assertEquals(selectColumns.contains(new SelectColumn("users", "id", "user_id")), true);
        Assert.assertEquals(selectColumns.contains(new SelectColumn("users", "name", "name")), true);
        Assert.assertEquals(selectColumns.contains(new SelectColumn("orders,orders", "amount,ttt", "total_amount")), true);
        Assert.assertEquals(selectColumns.contains(new SelectColumn("orders", "id", "order_count")), true);

    }

}
