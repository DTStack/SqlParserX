package com.dtstack.sqlparser.sqlparserx.core.base.sqlfilter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.dtstack.sqlparser.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparser.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparser.sqlparserx.core.parser.SqlParserFactory;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseFilterSortSelectFieldTest extends AbstractSqlParserTest {

    private String sqlTxtName = "BaseFilterSortSelectFieldTest.txt";
    private ISqlParser sqlParser = SqlParserFactory.getSqlParser(DbType.mysql);

    @Override
    public String getSqlTestPath() {
        return "/core/base/sqlfilter/";
    }

    @Test
    public void demo() {
        // 简单SELECT
        String sql = "SELECT * FROM orders WHERE region = 'beijing';";
        SQLStatementParser sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        SQLStatement sqlStatement = sqlStatementParser.parseStatement();

        // SELECT带复杂WHERE约束
        sql = "SELECT * FROM orders WHERE (price > 45.0 OR customer_name = 'John') AND region = 'beijing';";
        sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        sqlStatement = sqlStatementParser.parseStatement();

        // 两表JOIN且含子查询
        sql = "SELECT\n" +
                "    o.*,\n" +
                "    p.name,\n" +
                "    p.description\n" +
                "FROM \n" +
                "    (SELECT\n" +
                "        *\n" +
                "     FROM \n" +
                "        orders\n" +
                "     WHERE \n" +
                "        order_status = FALSE AND region = 'beijing'\n" +
                "    ) AS o\n" +
                "LEFT JOIN products AS p ON o.product_id = p.id\n" +
                "WHERE\n" +
                "    o.price > 45.0 OR o.customer_name = 'John' ";
        sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        sqlStatement = sqlStatementParser.parseStatement();

        // 三表JOIN
        sql = "SELECT\n" +
                "  o.*,\n" +
                "  p.name,\n" +
                "  p.description,\n" +
                "  s.shipment_id,\n" +
                "  s.origin,\n" +
                "  s.destination,\n" +
                "  s.is_arrived\n" +
                "FROM\n" +
                "  orders AS o\n" +
                "  LEFT JOIN products AS p ON o.product_id=p.id\n" +
                "  LEFT JOIN shipments AS s ON o.order_id=s.order_id\n" +
                "WHERE\n" +
                "  o.region='beijing'\n" +
                "  AND p.name='hammer'\n" +
                "  AND s.is_arrived=FALSE;";
        sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        sqlStatement = sqlStatementParser.parseStatement();

        // INSERT来自带子查询的SELECT
        sql = "INSERT INTO print_sink (SELECT * FROM (SELECT * FROM orders WHERE region = 'beijing'));";
        sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, DbType.mysql);
        sqlStatement = sqlStatementParser.parseStatement();

        System.out.println(sqlStatement);
    }

    @Test
    public void test_01_select_table() {
        List<RowLevelPermissionFilterParam> permissionParams = new ArrayList<>();
        RowLevelPermissionFilterParam param1 = new RowLevelPermissionFilterParam();
        param1.setTableName("orders");
        param1.setFieldName("region");
        param1.setFieldValue(Arrays.asList("beijing"));
        param1.setFieldType("varchar");
        permissionParams.add(param1);

        // 测试单表带有别名
        String sql = readSqlByIndex(sqlTxtName, 1);
        String rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 2));

        // 测试单表不带有别名
        sql = readSqlByIndex(sqlTxtName, 3);
        rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 4));
    }

    @Test
    public void test_02_select_multi_params() {
        List<RowLevelPermissionFilterParam> permissionParams = new ArrayList<>();
        RowLevelPermissionFilterParam param1 = new RowLevelPermissionFilterParam();
        param1.setTableName("orders");
        param1.setFieldName("region");
        param1.setFieldValue(Arrays.asList("beijing"));
        param1.setFieldType("varchar");
        permissionParams.add(param1);

        RowLevelPermissionFilterParam param2 = new RowLevelPermissionFilterParam();
        param2.setTableName("orders");
        param2.setFieldName("name");
        param2.setFieldValue(Arrays.asList("xiaoming", "xiaohua"));
        param2.setFieldType("varchar");
        permissionParams.add(param2);

        // 测试同一张表有多个字段增加权限限制
        String sql = readSqlByIndex(sqlTxtName, 5);
        String rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 6));
    }

    @Test
    public void test_03_where_subquery() {
        List<RowLevelPermissionFilterParam> permissionParams = new ArrayList<>();
        RowLevelPermissionFilterParam param1 = new RowLevelPermissionFilterParam();
        param1.setTableName("orders");
        param1.setFieldName("region");
        param1.setFieldValue(Arrays.asList("beijing"));
        param1.setFieldType("varchar");
        permissionParams.add(param1);

        RowLevelPermissionFilterParam param2 = new RowLevelPermissionFilterParam();
        param2.setTableName("orders");
        param2.setFieldName("name");
        param2.setFieldValue(Arrays.asList("xiaoming", "xiaohua"));
        param2.setFieldType("varchar");
        permissionParams.add(param2);

        // 测试单表where中有or条件
        String sql = readSqlByIndex(sqlTxtName, 7);
        String rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 8));
//        System.out.println(rowLevelPermissionSql);

        // 测试from后面跟子查询
        sql = readSqlByIndex(sqlTxtName, 9);
        rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 10));
//        System.out.println(rowLevelPermissionSql);
    }

    @Test
    public void test_04_join() {
        List<RowLevelPermissionFilterParam> permissionFilterParams = new ArrayList<>();
        RowLevelPermissionFilterParam param1 = new RowLevelPermissionFilterParam();
        param1.setTableName("orders");
        param1.setFieldName("region");
        param1.setFieldValue(Arrays.asList("beijing"));
        param1.setFieldType("varchar");
        permissionFilterParams.add(param1);

        RowLevelPermissionFilterParam param2 = new RowLevelPermissionFilterParam();
        param2.setTableName("products");
        param2.setFieldName("name");
        param2.setFieldValue(Arrays.asList("hammer"));
        param2.setFieldType("VARCHAR");
        permissionFilterParams.add(param2);

        RowLevelPermissionFilterParam param3 = new RowLevelPermissionFilterParam();
        param3.setTableName("shipments");
        param3.setFieldName("is_arrived");
        param3.setFieldValue(Arrays.asList("FALSE"));
        param3.setFieldType("boolean");
        permissionFilterParams.add(param3);

        // 测试join
        String sql = readSqlByIndex(sqlTxtName, 11);
        String rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionFilterParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 12));
//        System.out.println(rowLevelPermissionSql);
    }

    @Test
    public void test_05_update() {
        List<RowLevelPermissionFilterParam> permissionFilterParams = new ArrayList<>();
        RowLevelPermissionFilterParam param1 = new RowLevelPermissionFilterParam();
        param1.setTableName("orders");
        param1.setFieldName("region");
        param1.setFieldValue(Arrays.asList("beijing"));
        param1.setFieldType("varchar");
        permissionFilterParams.add(param1);

        // 测试join
        String sql = readSqlByIndex(sqlTxtName, 13);
        String rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionFilterParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 14));
//        System.out.println(rowLevelPermissionSql);
    }

    @Test
    public void test_06_join() {
        List<RowLevelPermissionFilterParam> permissionFilterParams = new ArrayList<>();
        RowLevelPermissionFilterParam param1 = new RowLevelPermissionFilterParam();
        param1.setTableName("source_table");
        param1.setFieldName("key");
        param1.setFieldValue(Arrays.asList("beijing", "hangzhou"));
        param1.setFieldType("varchar");
        permissionFilterParams.add(param1);

        // 测试join
        String sql = readSqlByIndex(sqlTxtName, 15);
        String rowLevelPermissionSql = sqlParser.getRowLevelPermissionSql(sql, permissionFilterParams);
        Assert.assertEquals(rowLevelPermissionSql, readSqlByIndex(sqlTxtName, 16));
//        System.out.println(rowLevelPermissionSql);
    }



}
