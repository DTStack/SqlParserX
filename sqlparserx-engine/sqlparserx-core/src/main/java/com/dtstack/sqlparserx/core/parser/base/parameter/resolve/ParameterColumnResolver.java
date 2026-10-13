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
package com.dtstack.sqlparserx.core.parser.base.parameter.resolve;

import com.dtstack.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparserx.core.utils.TableSourceUtil;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLLateralViewTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 在参数所在查询块的 FROM 作用域内，将字段限定符解析为真实表名。
 *
 * <p>该组件只服务参数绑定，不修改或依赖字段血缘的内部状态。</p>
 */
public class ParameterColumnResolver {

    /**
     * 将参数条件中的字段表达式定位为当前查询块内的真实表字段。
     *
     * <p>核心流程：</p>
     * <ol>
     *     <li>拆解字段 AST：{@code u.tenant_id -> owner=u, column=tenant_id}；</li>
     *     <li>沿 AST parent 找到最近的 {@link SQLSelectQueryBlock}，保证内外层查询别名互不串用；</li>
     *     <li>递归扫描当前查询块的 FROM/JOIN，建立“别名/表名 -> 真实表名”映射；</li>
     *     <li>限定字段按 owner 精确定位真实表；裸字段按当前作用域元数据唯一性消歧；</li>
     *     <li>只返回真实表和字段身份，字段类型与注释由 parameter 自己的元数据模型查询。</li>
     * </ol>
     */
    public ResolvedParameterColumn resolve(SQLExpr field, Map<String, List<DataTypeColumn>> metadata) {
        // 1. 当前阶段只解析简单字段。复杂表达式由后续阶段递归提取其中的字段集合。
        FieldReference reference = fieldReference(field);
        if (reference == null)
            return null;

        // 2. 字段 AST 最近的 QueryBlock 就是它的查询作用域；例如内层也声明别名 u 时，
        // 只读取内层 FROM，不会错误使用外层 u 对应的真实表。
        Map<String, String> tablesByQualifier = new LinkedHashMap<>();
        collectTables(findQueryBlock(field), tablesByQualifier);

        // 3. u.tenant_id：先用 u 查询当前作用域别名映射；若 SQL 直接写真实表名，
        // 再与 metadata key（table/schema.table/catalog.schema.table）进行兼容匹配。
        if (reference.owner != null) {
            String realTable = tablesByQualifier.get(normalize(reference.owner));
            if (realTable == null)
                realTable = matchTableName(reference.owner, metadata);
            return new ResolvedParameterColumn(realTable, reference.column);
        }

        // 裸字段只允许在当前查询块中唯一命中；多表同名字段保持未解析，禁止依赖 Map 顺序任选一个。
        List<String> matches = new ArrayList<>();
        for (String table : uniqueTables(tablesByQualifier))
            if (findColumn(table, reference.column, metadata) != null)
                matches.add(table);
        if (matches.size() == 1)
            return new ResolvedParameterColumn(matches.get(0), reference.column);

        // 没有可识别的 FROM 实表时，仅允许全局元数据唯一命中，用于兼容缺少查询上下文的简单表达式。
        if (tablesByQualifier.isEmpty()) {
            String uniqueTable = uniqueMetadataTable(reference.column, metadata);
            if (uniqueTable != null)
                return new ResolvedParameterColumn(uniqueTable, reference.column);
        }
        return new ResolvedParameterColumn(null, reference.column);
    }

    public DataTypeColumn findColumn(ResolvedParameterColumn resolved,
            Map<String, List<DataTypeColumn>> metadata) {
        if (resolved == null || resolved.getTableName() == null)
            return null;
        // 4. 真实表已经确定后，只允许在该表内查字段，不再扫描所有表使用最后一个同名字段。
        return findColumn(resolved.getTableName(), resolved.getColumnName(), metadata);
    }

    private SQLSelectQueryBlock findQueryBlock(SQLObject object) {
        SQLObject current = object;
        while (current != null && !(current instanceof SQLSelectQueryBlock))
            current = current.getParent();
        return (SQLSelectQueryBlock) current;
    }

    private void collectTables(SQLSelectQueryBlock block, Map<String, String> result) {
        if (block != null)
            collectTables(block.getFrom(), result);
    }

    private void collectTables(SQLTableSource source, Map<String, String> result) {
        if (source instanceof SQLExprTableSource) {
            // FROM schema.user_info u 同时登记：
            //   u -> schema.user_info
            //   schema.user_info -> schema.user_info
            //   user_info -> schema.user_info
            SQLExprTableSource table = (SQLExprTableSource) source;
            String realTable = TableSourceUtil.extractTableName(table);
            String alias = TableSourceUtil.removeQuotes(table.getAlias());
            put(result, realTable, realTable);
            if (alias != null)
                put(result, alias, realTable);
        } else if (source instanceof SQLJoinTableSource) {
            // JOIN 的左右两侧属于同一 QueryBlock 作用域，分别递归收集后才能对裸字段做唯一性判断。
            SQLJoinTableSource join = (SQLJoinTableSource) source;
            collectTables(join.getLeft(), result);
            collectTables(join.getRight(), result);
        } else if (source instanceof SQLLateralViewTableSource) {
            collectTables(((SQLLateralViewTableSource) source).getTableSource(), result);
        }
        // 派生表、CTE 和 UNION 的输出列穿透由后续阶段补充；这里不做全局同名字段猜测。
    }

    private void put(Map<String, String> result, String qualifier, String realTable) {
        if (qualifier == null || realTable == null)
            return;
        result.put(normalize(qualifier), realTable);
        int dot = realTable.lastIndexOf('.');
        if (dot >= 0)
            result.putIfAbsent(normalize(realTable.substring(dot + 1)), realTable);
    }

    private List<String> uniqueTables(Map<String, String> tablesByQualifier) {
        List<String> result = new ArrayList<>();
        for (String table : tablesByQualifier.values())
            if (!containsIgnoreCase(result, table))
                result.add(table);
        return result;
    }

    private boolean containsIgnoreCase(List<String> values, String expected) {
        for (String value : values)
            if (value.equalsIgnoreCase(expected))
                return true;
        return false;
    }

    private String matchTableName(String tableName, Map<String, List<DataTypeColumn>> metadata) {
        if (metadata == null)
            return null;
        for (String key : metadata.keySet())
            if (sameTable(key, tableName))
                return key;
        return null;
    }

    private DataTypeColumn findColumn(String tableName, String columnName,
            Map<String, List<DataTypeColumn>> metadata) {
        String metadataTable = matchTableName(tableName, metadata);
        if (metadataTable == null)
            return null;
        List<DataTypeColumn> columns = metadata.get(metadataTable);
        if (columns != null)
            for (DataTypeColumn column : columns)
                if (column.getName().equalsIgnoreCase(columnName))
                    return column;
        return null;
    }

    private String uniqueMetadataTable(String columnName, Map<String, List<DataTypeColumn>> metadata) {
        String result = null;
        if (metadata != null)
            for (Map.Entry<String, List<DataTypeColumn>> entry : metadata.entrySet())
                for (DataTypeColumn column : entry.getValue())
                    if (column.getName().equalsIgnoreCase(columnName)) {
                        if (result != null && !result.equalsIgnoreCase(entry.getKey()))
                            return null;
                        result = entry.getKey();
                    }
        return result;
    }

    private boolean sameTable(String left, String right) {
        String normalizedLeft = normalize(left);
        String normalizedRight = normalize(right);
        return normalizedLeft.equals(normalizedRight) || normalizedLeft.endsWith("." + normalizedRight)
                || normalizedRight.endsWith("." + normalizedLeft);
    }

    private String normalize(String value) {
        return TableSourceUtil.removeQuotes(value).toLowerCase(Locale.ROOT);
    }

    private FieldReference fieldReference(SQLExpr field) {
        if (field instanceof SQLPropertyExpr) {
            SQLPropertyExpr property = (SQLPropertyExpr) field;
            return new FieldReference(TableSourceUtil.removeQuotes(property.getOwnerName()),
                    TableSourceUtil.removeQuotes(property.getName()));
        }
        if (field instanceof SQLIdentifierExpr)
            return new FieldReference(null,
                    TableSourceUtil.removeQuotes(((SQLIdentifierExpr) field).getName()));
        return null;
    }

    private static class FieldReference {
        private final String owner;
        private final String column;

        private FieldReference(String owner, String column) {
            this.owner = owner;
            this.column = column;
        }
    }
}
