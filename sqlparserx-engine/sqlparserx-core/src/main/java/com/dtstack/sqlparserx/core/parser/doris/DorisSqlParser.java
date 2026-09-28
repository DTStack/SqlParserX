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
package com.dtstack.sqlparserx.core.parser.doris;

import com.dtstack.sqlparserx.core.enums.SQLParseStrategy;
import com.dtstack.sqlparserx.core.parser.IColumnLineageClient;
import com.dtstack.sqlparserx.core.parser.IParseTableClient;
import com.dtstack.sqlparserx.core.parser.ITableLineageClient;
import com.dtstack.sqlparserx.core.parser.base.BaseSqlParser;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.Column;
import com.dtstack.sqlparserx.core.parser.base.lineage.column.ColumnLineageClient;
import com.dtstack.sqlparserx.core.parser.base.lineage.table.TableLineageClient;
import com.dtstack.sqlparserx.core.parser.doris.table.DorisParseTableClient;

import com.alibaba.druid.DbType;

import java.util.List;
import java.util.Map;

public class DorisSqlParser extends BaseSqlParser {

    @Override
    public DbType getDbType() {
        return DbType.doris;
    }

    @Override
    public IColumnLineageClient getColumnLineageClient(Map<String, List<Column>> tableColumnsMap) {
        // doris支持大小写敏感
        return new ColumnLineageClient(tableColumnsMap,
                SQLParseStrategy.COLUMN_IGNORE_CASE,  // SQL中的字段 & 元数据表字段忽略大小写
                SQLParseStrategy.SUBQUERY_COLUMN_IGNORE_CASE // 子查询字段忽略大小写匹配
        );
    }

    @Override
    public IParseTableClient getParseTableClient() {
        return new DorisParseTableClient();
    }

    @Override
    public ITableLineageClient getTableLineageClient() {
        return new TableLineageClient(
                SQLParseStrategy.CTE_FORWARD_REFERENCE // 支持 CTE 前向引用 推断
        );
    }
}
