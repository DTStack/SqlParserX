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
package com.dtstack.sqlparserx.core.base.parameter;

import com.dtstack.sqlparserx.core.common.AbstractSqlParserTest;
import com.dtstack.sqlparserx.core.parser.ISqlParser;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ApiParamSpan;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;
import com.dtstack.sqlparserx.core.parser.doris.DorisSqlParser;
import com.dtstack.sqlparserx.core.parser.mysql.MySqlParser;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 变量参数配置期解析与运行期预编译回归测试。
 *
 * <p>配置期验证 `${变量}` 的发现顺序、操作符、必填属性、真实表字段及类型绑定，并同时锁定输入参数和最终
 * API 输出字段数量；复杂查询覆盖 CTE、派生表、JOIN、UNION、EXISTS 和标量子查询，确保内部投影不会污染
 * 最外层输出契约。运行期验证条件剪枝、占位符顺序、集合展开、方言类型转换，以及配置期字段类型向预编译值
 * 类型的完整传递。
 */
public class BaseParseParameterClientTest extends AbstractSqlParserTest {

    private final ISqlParser parser = new MySqlParser();
    private final String sqlTxtName = "BaseParseParameterClientTest.txt";

    @Override
    public String getSqlTestPath() {
        return "/core/base/parameter/";
    }

    // ---------- 基础：入参/出参全字段 ----------

    @Test
    public void shouldParseBasicVariablesAndCoverAllOutputFields() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 1), false, userInfo());

        assertParameterCounts(result, 2, 2);

        ParamInfo tenant = result.getInputParams().get(0);
        assertEquals("tenantId", tenant.getName());
        assertEquals("tenant_id", tenant.getFieldName());
        assertEquals("BIGINT", tenant.getDataType());
        assertEquals("=", tenant.getOperator());
        assertFalse(tenant.isImperative());
        assertEquals("租户", tenant.getDesc());
        assertNull(tenant.getValue());
        assertNull(tenant.getDefaultValue());
        // sqlPart 标明参数块来源片段
        assertTrue(tenant.getSqlPart().contains("u.tenant_id"));
        assertTrue(tenant.getSqlPart().contains("${tenantId}"));

        ParamInfo states = result.getInputParams().get(1);
        assertEquals("states", states.getName());
        assertEquals("status", states.getFieldName());
        assertEquals("VARCHAR(32)", states.getDataType());
        assertEquals("in", states.getOperator());
        assertEquals("状态", states.getDesc());
        assertTrue(states.getSqlPart().contains("u.status"));

        ApiParamSpan idSpan = result.getOutputParams().get(0);
        assertEquals("u.id", idSpan.getSqlPart());
        assertNull(idSpan.getAlias());
        assertEquals(1, idSpan.getBindColumns().size());
        assertEquals("id", idSpan.getBindColumns().get(0).getName());
        assertEquals("BIGINT", idSpan.getBindColumns().get(0).getType());
        assertEquals("主键", idSpan.getBindColumns().get(0).getComment());

        ApiParamSpan nameSpan = result.getOutputParams().get(1);
        assertEquals("u.name", nameSpan.getSqlPart());
        assertEquals("name", nameSpan.getBindColumns().get(0).getName());
        assertEquals("VARCHAR(64)", nameSpan.getBindColumns().get(0).getType());

        assertNotNull(result.getTables());
        assertTrue(result.getTables().isEmpty());
    }

    @Test
    public void shouldParseComparisonOperators() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 2), false,
                Collections.<String, List<DataTypeColumn>>emptyMap());

        assertParameterCounts(result, 6, 1);
        assertEquals(Arrays.asList("gt", "gte", "lt", "lte", "ne", "kw"), names(result));
        assertEquals(Arrays.asList(">", ">=", "<", "<=", "<>", "like"),
                result.getInputParams().stream().map(ParamInfo::getOperator).collect(Collectors.toList()));
    }

    @Test
    public void shouldTreatSelectListVariableAsRequired() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 3), false, userInfo());

        assertParameterCounts(result, 2, 2);
        assertEquals(Arrays.asList("tenantId", "dynamicColumn"), names(result));
        ParamInfo dynamic = result.getInputParams().get(1);
        assertEquals("API_PARAM_IN_SELECT_LIST", dynamic.getOperator());
        assertTrue(dynamic.isImperative());
        assertNull(dynamic.getFieldName());
        assertNull(dynamic.getDataType());

        assertEquals("id", result.getOutputParams().get(0).getSqlPart());
        assertEquals("user_id", result.getOutputParams().get(0).getAlias());
        assertEquals("name", result.getOutputParams().get(1).getSqlPart());
        assertNull(result.getOutputParams().get(1).getAlias());
    }

    @Test
    public void shouldIgnoreVariableInsideStringLiteral() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 4), false,
                Collections.<String, List<DataTypeColumn>>emptyMap());
        assertParameterCounts(result, 0, 1);
    }

    @Test
    public void shouldReturnSingleAllColumnOutput() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 14), false, userInfo());
        assertParameterCounts(result, 1, 1);
        assertEquals(Collections.singletonList("tenantId"), names(result));
        assertEquals("*", result.getOutputParams().get(0).getSqlPart());
        assertTrue(result.getOutputParams().get(0).getBindColumns().isEmpty());
    }

    // ---------- 结构：分组/排序/分页 ----------

    @Test
    public void shouldParseGroupHavingOrderLimitVariables() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 5), false, userInfo());

        assertParameterCounts(result, 6, 2);
        assertEquals(Arrays.asList("tenantId", "groupColumn", "minCount", "sortColumn", "offset", "rows"),
                names(result));
        assertEquals(Arrays.asList("=", "CUSTOM", ">", "CUSTOM", "CUSTOM", "CUSTOM"),
                result.getInputParams().stream().map(ParamInfo::getOperator).collect(Collectors.toList()));
    }

    @Test
    public void shouldPrepareCompileGroupHavingOrderLimit() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("tenantId", 1001L);
        values.put("groupColumn", "department_id");
        values.put("minCount", 5);
        values.put("sortColumn", "created_at");
        values.put("offset", 0);
        values.put("rows", 20);

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 5), values,
                columns(param("tenantId", "BIGINT"), param("minCount", "INT"), param("offset", "INT"), param("rows",
                        "INT"), param("groupColumn", "VARCHAR"), param("sortColumn", "VARCHAR")));

        assertEquals(Arrays.<Object>asList(1001L, 5, 0, 20), result.getInputFields());
        assertEquals(4, count(result.getBeautifulSql(), '?'));
        assertTrue(result.getBeautifulSql().contains("GROUP BY department_id"));
        assertTrue(result.getBeautifulSql().contains("ORDER BY created_at DESC"));
        assertTrue(result.getBeautifulSql().contains("HAVING count(*) > ?"));
        assertTrue(result.getBeautifulSql().contains("LIMIT ?, ?"));
    }

    // ---------- 嵌套：CTE / 派生表 / JOIN / EXISTS ----------

    @Test
    public void shouldParseVariablesFromNestedSqlStructures() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 6), false,
                nestedSqlMetadata());

        assertParameterCounts(result, 7, 1);
        assertEquals(Arrays.asList("cteTenant", "startTime", "states", "minAmount", "sortColumn", "offset", "rows"),
                names(result));
        assertEquals("DECIMAL(10,2)", result.getInputParams().get(3).getDataType());
        assertOutputProjection(result, "u.id");
    }

    @Test
    public void shouldPrepareCompileNestedSqlInRenderedOrder() {
        Map<String, Object> values = new HashMap<>();
        values.put("cteTenant", "9");
        values.put("startTime", "2026-01-01");
        values.put("states", Arrays.asList("OPEN", "CLOSED"));
        values.put("minAmount", "99.50");
        values.put("sortColumn", "u.id");
        values.put("offset", "0");
        values.put("rows", "20");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 6), values,
                columns(param("cteTenant", "BIGINT"), param("startTime", "VARCHAR"), param("states", "VARCHAR"), param(
                        "minAmount", "DECIMAL(10,2)"), param("offset", "INT"), param("rows", "INT")));

        assertEquals(Arrays.<Object>asList(9L, "2026-01-01", "OPEN", "CLOSED", new BigDecimal("99.50"), 0, 20),
                result.getInputFields());
        assertEquals(7, count(result.getBeautifulSql(), '?'));
        assertTrue(result.getBeautifulSql().contains("JOIN tenant_users tu ON tu.id = u.id"));
        assertTrue(result.getBeautifulSql().contains("ORDER BY u.id DESC"));
        assertFalse(result.getBeautifulSql().contains("${"));
    }

    // ---------- 嵌套：SELECT 列表标量子查询（顺序一致性） ----------

    @Test
    public void shouldParseScalarSubqueryAndOuterConditions() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 7), false, userInfo());

        assertParameterCounts(result, 3, 2);
        assertEquals(Arrays.asList("tenantId", "states", "minAmount"), names(result));
        assertOutputProjection(result, "rid", "max_amount");
    }

    @Test
    public void shouldPrepareCompileScalarSubqueryInRenderedOrder() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("minAmount", "99.50");
        values.put("tenantId", "9");
        values.put("states", Arrays.asList("OPEN", "CLOSED"));

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 7), values,
                columns(param("minAmount", "DECIMAL(10,2)"), param("tenantId", "BIGINT"), param("states", "VARCHAR")));

        assertEquals(Arrays.<Object>asList(new BigDecimal("99.50"), 9L, "OPEN", "CLOSED"), result.getInputFields());
        assertEquals(4, count(result.getBeautifulSql(), '?'));
    }

    // ---------- 嵌套：UNION ----------

    @Test
    public void shouldParseTopLevelUnionWithOrderLimitVariables() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 8), false, userInfo());

        assertParameterCounts(result, 3, 2);
        assertEquals(Arrays.asList("tenantIdA", "tenantIdB", "rows"), names(result));
        assertOutputProjection(result, "id", "name");
    }

    @Test
    public void shouldPrepareCompileTopLevelUnionOrderLimit() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("tenantIdA", "1");
        values.put("tenantIdB", "2");
        values.put("rows", "10");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 8), values,
                columns(param("tenantIdA", "BIGINT"), param("tenantIdB", "BIGINT"), param("rows", "INT")));

        assertEquals(Arrays.<Object>asList(1L, 2L, 10), result.getInputFields());
        assertEquals(3, count(result.getBeautifulSql(), '?'));
        assertTrue(result.getBeautifulSql().contains("ORDER BY id"));
        assertTrue(result.getBeautifulSql().contains("LIMIT ?"));
    }

    @Test
    public void shouldParseUnionInsideDerivedTable() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 9), false, userInfo());

        assertParameterCounts(result, 3, 1);
        assertEquals(Arrays.asList("tenantIdA", "tenantIdB", "minId"), names(result));
        assertOutputProjection(result, "t.id");
    }

    // ---------- 嵌套：多层 EXISTS / IN 子查询 ----------

    @Test
    public void shouldParseMultiLevelNestedSubqueries() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 10), false,
                Collections.<String, List<DataTypeColumn>>emptyMap());

        assertParameterCounts(result, 2, 1);
        assertEquals(Arrays.asList("flag", "minPay"), names(result));
        assertOutputProjection(result, "id");
    }

    // ---------- 重复变量 ----------

    @Test
    public void shouldDeduplicateRepeatedVariable() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 11), false, userInfo());
        assertParameterCounts(result, 1, 1);
        assertEquals(Collections.singletonList("tenantId"), names(result));
        assertEquals("tenant_id", result.getInputParams().get(0).getFieldName());
        assertEquals("=", result.getInputParams().get(0).getOperator());
    }

    @Test
    public void shouldBindSameValueForRepeatedVariable() {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", "5");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 11), values,
                columns(param("tenantId", "BIGINT")));

        assertEquals(Arrays.<Object>asList(5L, 5L), result.getInputFields());
        assertEquals(2, count(result.getBeautifulSql(), '?'));
    }

    // ---------- BETWEEN ----------

    @Test
    public void shouldParseBetweenAndNotBetween() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 13), false, userInfo());

        assertParameterCounts(result, 4, 3);
        assertEquals(Arrays.asList("minScore", "maxScore", "minAged", "maxAged"), names(result));
        assertEquals(Arrays.asList("between", "between", "not between", "not between"),
                result.getInputParams().stream().map(ParamInfo::getOperator).collect(Collectors.toList()));
        assertEquals("score", result.getInputParams().get(0).getFieldName());
        assertEquals("aged", result.getInputParams().get(2).getFieldName());
    }

    @Test
    public void shouldPrepareCompileBetween() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("minScore", "1");
        values.put("maxScore", "5");
        values.put("minAged", "10");
        values.put("maxAged", "20");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 13), values,
                columns(param("minScore", "INT"), param("maxScore", "INT"), param("minAged", "INT"), param("maxAged",
                        "INT")));

        assertEquals(Arrays.<Object>asList(1, 5, 10, 20), result.getInputFields());
        assertEquals(4, count(result.getBeautifulSql(), '?'));
        assertTrue(result.getBeautifulSql().contains("BETWEEN ? AND ?"));
        assertTrue(result.getBeautifulSql().contains("NOT BETWEEN ? AND ?"));
    }

    // ---------- 复杂嵌套：预编译值顺序 ----------

    @Test
    public void shouldOrderPlaceholdersForMultipleScalarSubqueries() {
        ParseParameterResult parseResult = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 15), false, userInfo());
        assertParameterCounts(parseResult, 4, 3);
        // 配置期按结构顺序（from -> where -> selectList）
        assertEquals(Arrays.asList("tenantId", "minId", "minAmount", "payStatus"), names(parseResult));
        assertOutputProjection(parseResult, "t.id", "max_amt", "cnt");

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("minAmount", "10.00");
        values.put("payStatus", "PAID");
        values.put("tenantId", "9");
        values.put("minId", "100");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 15), values,
                columns(param("minAmount", "DECIMAL(10,2)"), param("payStatus", "VARCHAR"), param("tenantId", "BIGINT"),
                        param("minId", "INT")));

        // 渲染顺序（selectList 标量子查询 -> from -> where）
        assertEquals(Arrays.<Object>asList(new BigDecimal("10.00"), "PAID", 9L, 100), result.getInputFields());
        assertEquals(4, count(result.getBeautifulSql(), '?'));

        String sql = result.getBeautifulSql();
        assertTrue(placeholderIndex(sql, "a.amount >") < placeholderIndex(sql, "p.status ="));
        assertTrue(placeholderIndex(sql, "p.status =") < placeholderIndex(sql, "tenant_id ="));
        assertTrue(placeholderIndex(sql, "tenant_id =") < placeholderIndex(sql, "t.id >"));
    }

    @Test
    public void shouldOrderPlaceholdersForCteUnionSubqueryLimit() {
        ParseParameterResult parseResult = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 16), false, userInfo());
        assertParameterCounts(parseResult, 4, 1);
        assertOutputProjection(parseResult, "b.id");

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("tenantId", "9");
        values.put("minAmount", "50");
        values.put("payStatus", "PAID");
        values.put("rows", "10");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 16), values,
                columns(param("tenantId", "BIGINT"), param("minAmount", "INT"), param("payStatus", "VARCHAR"), param(
                        "rows", "INT")));

        assertEquals(Arrays.<Object>asList(9L, 50, "PAID", 10), result.getInputFields());
        assertEquals(4, count(result.getBeautifulSql(), '?'));

        String sql = result.getBeautifulSql();
        assertTrue(placeholderIndex(sql, "tenant_id =") < placeholderIndex(sql, "o.amount >"));
        assertTrue(placeholderIndex(sql, "o.amount >") < placeholderIndex(sql, "p.status ="));
        assertTrue(placeholderIndex(sql, "p.status =") < sql.lastIndexOf('?'));
    }

    // ---------- 同名参数合并 ----------

    @Test
    public void shouldUpgradeRepeatedParamToRequired() {
        ParseParameterResult result = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 17), false, userInfo());

        assertParameterCounts(result, 1, 1);
        assertEquals(Collections.singletonList("dupParam"), names(result));
        ParamInfo dup = result.getInputParams().get(0);
        assertTrue(dup.isImperative());
        assertEquals("tenant_id", dup.getFieldName());
        assertEquals("BIGINT", dup.getDataType());
        assertEquals("=", dup.getOperator());
    }

    @Test
    public void shouldBindSameValueForRequiredRepeatedParam() {
        Map<String, Object> values = new HashMap<>();
        values.put("dupParam", "7");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 17), values,
                columns(param("dupParam", "BIGINT")));

        assertEquals(Arrays.<Object>asList(7L, 7L), result.getInputFields());
        assertEquals(2, count(result.getBeautifulSql(), '?'));
        assertTrue(result.getBeautifulSql().contains("AS dup_col"));
    }

    // ---------- 解析 → 预编译 组合 ----------

    @Test
    public void shouldParseThenPrepareCompileWithParsedParams() {
        String sql = readSqlByIndex(sqlTxtName, 6);
        ParseParameterResult parseResult = parser.parseParameterSql(sql, false, nestedSqlMetadata());
        assertParameterCounts(parseResult, 7, 1);
        Map<String, ParamInfo> columns = columnsFrom(parseResult);

        Map<String, Object> values = new HashMap<>();
        values.put("cteTenant", 9L);
        values.put("startTime", "2026-01-01");
        values.put("states", Arrays.asList("OPEN", "CLOSED"));
        values.put("minAmount", new BigDecimal("99.50"));
        values.put("sortColumn", "u.id");
        values.put("offset", 0);
        values.put("rows", 20);

        PrepareCompileResult compileResult = parser.buildSqlByPrepareCompile(sql, values, columns);

        assertEquals(Arrays.<Object>asList(9L, "2026-01-01", "OPEN", "CLOSED", new BigDecimal("99.50"), 0, 20),
                compileResult.getInputFields());
        assertFalse(compileResult.getBeautifulSql().contains("${"));
    }

    // ---------- 类型转换 ----------

    @Test
    public void shouldUseMysqlDataTypeTransferForPreparedValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("enabled", true);
        values.put("birthday", "2026-10-12");
        values.put("createdAt", "2026-10-12 16:20:30");

        PrepareCompileResult result = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 12), values,
                columns(param("enabled", "BIT(1)"), param("birthday", "DATE"), param("createdAt", "DATETIME")));

        assertEquals(Integer.class, result.getInputFields().get(0).getClass());
        assertEquals(java.sql.Date.class, result.getInputFields().get(1).getClass());
        assertEquals(java.sql.Timestamp.class, result.getInputFields().get(2).getClass());
        assertEquals(Arrays.<Object>asList(1, java.sql.Date.valueOf("2026-10-12"), java.sql.Timestamp.valueOf(
                "2026-10-12 16:20:30")), result.getInputFields());
    }

    @Test
    public void shouldUseDorisDataTypeTransferForPreparedValues() {
        ISqlParser dorisParser = new DorisSqlParser();
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("enabled", "true");
        values.put("birthday", "2026-10-12");
        values.put("createdAt", "2026-10-12 16:20:30");

        PrepareCompileResult result = dorisParser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 12), values,
                columns(param("enabled", "BOOLEAN"), param("birthday", "DATE"), param("createdAt", "DATETIME")));

        assertEquals(Arrays.<Object>asList(true, java.sql.Date.valueOf("2026-10-12"), java.sql.Timestamp.valueOf(
                "2026-10-12 16:20:30")), result.getInputFields());
    }

    // ---------- 参数字段真实表定位 ----------

    @Test
    public void shouldResolveQualifiedColumnToRealTableInJoin() {
        String sql = "select u.id from user_info u join audit_log a on a.user_id = u.id "
                + "where u.tenant_id = ${tenantId}";

        ParseParameterResult result = parser.parseParameterSql(sql, false, conflictingTenantMetadata(false));
        assertParameterCounts(result, 1, 1);
        ParamInfo tenant = result.getInputParams().get(0);

        assertEquals("tenant_id", tenant.getFieldName());
        assertEquals("BIGINT", tenant.getDataType());
        assertEquals("用户租户", tenant.getDesc());
    }

    @Test
    public void shouldResolveQualifiedColumnIndependentlyOfMetadataOrder() {
        String sql = "select u.id from user_info u join audit_log a on a.user_id = u.id "
                + "where u.tenant_id = ${tenantId}";

        ParseParameterResult userFirstResult = parser.parseParameterSql(sql, false, conflictingTenantMetadata(false));
        ParseParameterResult auditFirstResult = parser.parseParameterSql(sql, false, conflictingTenantMetadata(true));
        assertParameterCounts(userFirstResult, 1, 1);
        assertParameterCounts(auditFirstResult, 1, 1);
        ParamInfo userFirst = userFirstResult.getInputParams().get(0);
        ParamInfo auditFirst = auditFirstResult.getInputParams().get(0);

        assertEquals("BIGINT", userFirst.getDataType());
        assertEquals(userFirst.getDataType(), auditFirst.getDataType());
        assertEquals(userFirst.getDesc(), auditFirst.getDesc());
    }

    @Test
    public void shouldNotGuessAmbiguousUnqualifiedColumnInJoin() {
        String sql = "select u.id from user_info u join audit_log a on a.user_id = u.id "
                + "where tenant_id = ${tenantId}";

        ParseParameterResult result = parser.parseParameterSql(sql, false, conflictingTenantMetadata(false));
        assertParameterCounts(result, 1, 1);
        ParamInfo tenant = result.getInputParams().get(0);

        assertEquals("tenant_id", tenant.getFieldName());
        assertNull(tenant.getDataType());
        assertNull(tenant.getDesc());
    }

    @Test
    public void shouldResolveEachJoinAliasToItsOwnRealTable() {
        String sql = "select u.id from user_info u join audit_log a on a.user_id = u.id "
                + "where a.tenant_id = ${auditTenant}";

        ParseParameterResult result = parser.parseParameterSql(sql, false, conflictingTenantMetadata(false));
        assertParameterCounts(result, 1, 1);
        ParamInfo tenant = result.getInputParams().get(0);

        assertEquals("tenant_id", tenant.getFieldName());
        assertEquals("VARCHAR(64)", tenant.getDataType());
        assertEquals("审计租户", tenant.getDesc());
    }

    @Test
    public void shouldResolveAliasForSchemaQualifiedTable() {
        String sql = "select u.id from app.user_info u where u.tenant_id = ${tenantId}";
        Map<String, List<DataTypeColumn>> metadata = new LinkedHashMap<>();
        metadata.put("app.user_info", Arrays.asList(new DataTypeColumn("id", "BIGINT", "主键"),
                new DataTypeColumn("tenant_id", "BIGINT", "租户")));

        ParseParameterResult result = parser.parseParameterSql(sql, false, metadata);
        assertParameterCounts(result, 1, 1);
        ParamInfo tenant = result.getInputParams().get(0);

        assertEquals("tenant_id", tenant.getFieldName());
        assertEquals("BIGINT", tenant.getDataType());
        assertEquals("租户", tenant.getDesc());
    }

    @Test
    public void shouldResolveUniqueUnqualifiedColumnWithinJoinScope() {
        String sql = "select u.id from user_info u join audit_log a on a.user_id = u.id "
                + "where status = ${status}";
        Map<String, List<DataTypeColumn>> metadata = conflictingTenantMetadata(false);
        metadata.get("user_info").add(new DataTypeColumn("status", "VARCHAR(32)", "用户状态"));

        ParseParameterResult result = parser.parseParameterSql(sql, false, metadata);
        assertParameterCounts(result, 1, 1);
        ParamInfo status = result.getInputParams().get(0);

        assertEquals("status", status.getFieldName());
        assertEquals("VARCHAR(32)", status.getDataType());
        assertEquals("用户状态", status.getDesc());
    }

    @Test
    public void shouldUseNearestQueryBlockForRepeatedAlias() {
        String sql = "select u.id from user_info u where exists (select 1 from audit_log u "
                + "where u.tenant_id = ${auditTenant})";

        ParseParameterResult result = parser.parseParameterSql(sql, false, conflictingTenantMetadata(false));
        assertParameterCounts(result, 1, 1);
        ParamInfo tenant = result.getInputParams().get(0);

        assertEquals("VARCHAR(64)", tenant.getDataType());
        assertEquals("审计租户", tenant.getDesc());
    }

    @Test
    public void shouldUseResolvedFieldTypeWhenPreparingValue() {
        String sql = "select u.id from user_info u join audit_log a on a.user_id = u.id "
                + "where u.tenant_id = ${tenantId}";
        ParseParameterResult parsed = parser.parseParameterSql(sql, false, conflictingTenantMetadata(false));
        assertParameterCounts(parsed, 1, 1);
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", "9");

        PrepareCompileResult compiled = parser.buildSqlByPrepareCompile(sql, values, columnsFrom(parsed));

        assertEquals(Collections.<Object>singletonList(9L), compiled.getInputFields());
        assertTrue(compiled.getBeautifulSql().contains("u.tenant_id = ?"));
    }

    @Test
    public void shouldUseConfiguredValueWhenRequestValueIsMissing() {
        String sql = "select id from user_info where tenant_id = ${tenantId}";
        ParamInfo definition = param("tenantId", "BIGINT");
        definition.setValue("9");

        PrepareCompileResult compiled = parser.buildSqlByPrepareCompile(sql, Collections.emptyMap(),
                columns(definition));

        assertEquals(Collections.<Object>singletonList(9L), compiled.getInputFields());
        assertTrue(compiled.getBeautifulSql().contains("tenant_id = ?"));
    }

    @Test
    public void shouldUseDefaultValueWhenRequestAndConfiguredValuesAreMissing() {
        String sql = "select id from user_info where tenant_id = ${tenantId}";
        ParamInfo definition = param("tenantId", "BIGINT");
        definition.setDefaultValue("8");

        PrepareCompileResult compiled = parser.buildSqlByPrepareCompile(sql, Collections.emptyMap(),
                columns(definition));

        assertEquals(Collections.<Object>singletonList(8L), compiled.getInputFields());
        assertTrue(compiled.getBeautifulSql().contains("tenant_id = ?"));
    }

    @Test
    public void shouldResolveRealColumnsInComplexNestedQuery() {
        ParseParameterResult parsed = parser.parseParameterSql(readSqlByIndex(sqlTxtName, 18), false,
                complexNestedMetadata());

        assertParameterCounts(parsed, 8, 7);
        assertEquals(Arrays.asList("t1MinAge", "t2Name", "t3AgeA", "t3AgeB", "t4MinAge", "department",
                "olderAge", "backupDepartment"), names(parsed));

        assertParamBinding(parsed, "t1MinAge", "age", "INT", "年龄");
        assertParamBinding(parsed, "t2Name", "name", "VARCHAR(64)", "名称");
        assertParamBinding(parsed, "t3AgeA", "age", "INT", "年龄");
        assertParamBinding(parsed, "t3AgeB", "age", "INT", "年龄");
        assertParamBinding(parsed, "t4MinAge", "age", "INT", "年龄");
        assertParamBinding(parsed, "olderAge", "age", "INT", "年龄");
        assertParamBinding(parsed, "department", "department", "VARCHAR(64)", "部门");
        assertParamBinding(parsed, "backupDepartment", "department", "VARCHAR(64)", "部门");

        // API 输出契约只取最外层 UNION 第一分支的投影结构；CTE、派生表、标量子查询及后续
        // UNION 分支只参与字段来源/参数解析，不能追加成最终输出参数。
        assertOutputProjection(parsed, "t1.id", "t1.name", "t2_name", "t3_name", "t4_age", "dt.department",
                "older_count");

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("t1MinAge", "10");
        values.put("t2Name", "A%");
        values.put("t3AgeA", "18");
        values.put("t3AgeB", "65");
        values.put("t4MinAge", "20");
        values.put("olderAge", "30");
        values.put("department", "研发");
        values.put("backupDepartment", "平台");
        PrepareCompileResult compiled = parser.buildSqlByPrepareCompile(readSqlByIndex(sqlTxtName, 18), values,
                columnsFrom(parsed));

        assertEquals(Arrays.<Object>asList(10, "A%", 18, 65, 20, 30, "研发", "平台"), compiled.getInputFields());
        assertEquals(8, count(compiled.getBeautifulSql(), '?'));
    }

    // ---------- helpers ----------

    /**
     * 每个配置期解析用例都同时锁定输入参数和最终 API 输出字段数量，防止嵌套查询、CTE 或 UNION
     * 的内部投影被误追加到最外层输出契约中。
     */
    private static void assertParameterCounts(ParseParameterResult result, int expectedInputCount,
            int expectedOutputCount) {
        assertEquals("input parameter count", expectedInputCount, result.getInputParams().size());
        assertEquals("output parameter count", expectedOutputCount, result.getOutputParams().size());
    }

    private static Map<String, List<DataTypeColumn>> conflictingTenantMetadata(boolean auditFirst) {
        Map<String, List<DataTypeColumn>> metadata = new LinkedHashMap<>();
        List<DataTypeColumn> userColumns = new ArrayList<>(Arrays.asList(new DataTypeColumn("id", "BIGINT", "用户主键"),
                new DataTypeColumn("tenant_id", "BIGINT", "用户租户")));
        List<DataTypeColumn> auditColumns = Arrays.asList(new DataTypeColumn("user_id", "BIGINT", "用户主键"),
                new DataTypeColumn("tenant_id", "VARCHAR(64)", "审计租户"));
        if (auditFirst) {
            metadata.put("audit_log", auditColumns);
            metadata.put("user_info", userColumns);
        } else {
            metadata.put("user_info", userColumns);
            metadata.put("audit_log", auditColumns);
        }
        return metadata;
    }

    private static Map<String, List<DataTypeColumn>> complexNestedMetadata() {
        Map<String, List<DataTypeColumn>> metadata = new LinkedHashMap<>();
        metadata.put("digua_ceshi.demo_table_digua", Arrays.asList(new DataTypeColumn("id", "BIGINT", "主键"),
                new DataTypeColumn("name", "VARCHAR(64)", "名称"), new DataTypeColumn("age", "INT", "年龄")));
        metadata.put("digua_ceshi.new_table", Arrays.asList(new DataTypeColumn("id", "BIGINT", "主键"),
                new DataTypeColumn("name", "VARCHAR(64)", "名称"), new DataTypeColumn("age", "INT", "年龄")));
        metadata.put("digua_ceshi.new_table2", Arrays.asList(new DataTypeColumn("id", "BIGINT", "主键"),
                new DataTypeColumn("name", "VARCHAR(64)", "名称"), new DataTypeColumn("age", "INT", "年龄")));
        metadata.put("digua_ceshi.departments", Arrays.asList(new DataTypeColumn("employee_id", "BIGINT", "员工"),
                new DataTypeColumn("department", "VARCHAR(64)", "部门")));
        return metadata;
    }

    private static void assertParamBinding(ParseParameterResult result, String paramName, String fieldName,
            String dataType, String desc) {
        ParamInfo param = null;
        for (ParamInfo inputParam : result.getInputParams())
            if (paramName.equals(inputParam.getName())) {
                param = inputParam;
                break;
            }
        assertNotNull(param);
        assertEquals(fieldName, param.getFieldName());
        assertEquals(dataType, param.getDataType());
        assertEquals(desc, param.getDesc());
    }

    private static void assertOutputProjection(ParseParameterResult result, String... expected) {
        List<String> actual = result.getOutputParams().stream()
                .map(output -> output.getAlias() == null ? output.getSqlPart() : output.getAlias())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(expected), actual);
    }

    private static Map<String, List<DataTypeColumn>> userInfo() {
        Map<String, List<DataTypeColumn>> metadata = new HashMap<>();
        metadata.put("user_info", Arrays.asList(new DataTypeColumn("id", "BIGINT", "主键"), new DataTypeColumn("name",
                "VARCHAR(64)", "名称"), new DataTypeColumn("tenant_id", "BIGINT", "租户"), new DataTypeColumn("status",
                        "VARCHAR(32)", "状态"), new DataTypeColumn("score", "INT", "分数"), new DataTypeColumn("aged",
                                "INT", "年龄")));
        return metadata;
    }

    private static Map<String, List<DataTypeColumn>> nestedSqlMetadata() {
        Map<String, List<DataTypeColumn>> metadata = new HashMap<>();
        metadata.put("user_info", Arrays.asList(new DataTypeColumn("tenant_id", "BIGINT", null), new DataTypeColumn(
                "created_at", "VARCHAR", null), new DataTypeColumn("status", "VARCHAR", null)));
        metadata.put("orders", Arrays.asList(new DataTypeColumn("user_id", "BIGINT", null), new DataTypeColumn("amount",
                "DECIMAL(10,2)", null)));
        return metadata;
    }

    private static List<String> names(ParseParameterResult result) {
        return result.getInputParams().stream().map(ParamInfo::getName).collect(Collectors.toList());
    }

    private static ParamInfo param(String name, String type) {
        ParamInfo info = new ParamInfo(name);
        info.setDataType(type);
        return info;
    }

    private static Map<String, ParamInfo> columns(ParamInfo... params) {
        Map<String, ParamInfo> columns = new HashMap<>();
        for (ParamInfo param : params)
            columns.put(param.getName(), param);
        return columns;
    }

    private static Map<String, ParamInfo> columnsFrom(ParseParameterResult result) {
        Map<String, ParamInfo> columns = new HashMap<>();
        for (ParamInfo inputParam : result.getInputParams())
            columns.put(inputParam.getName(), inputParam);
        return columns;
    }

    private static int placeholderIndex(String sql, String marker) {
        return sql.indexOf('?', sql.indexOf(marker));
    }

    private static int count(String value, char target) {
        int count = 0;
        for (int i = 0; i < value.length(); i++)
            if (value.charAt(i) == target)
                count++;
        return count;
    }
}
