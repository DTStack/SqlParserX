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
package com.dtstack.sqlparserx.core.parser.base.sqltype;

import com.dtstack.sqlparserx.core.enums.SqlType;
import com.dtstack.sqlparserx.core.parser.IParseSqlTypeClient;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerExecStatement;

/**
 * @description SQL 类型解析默认实现：按 Druid 语句类型分派判定业务 SQL 类型；无法识别返回 {@link SqlType#OTHER}
 * @author changlu
 * @date 2026/10/12 17:40
 */
public class BaseParseSqlTypeClient implements IParseSqlTypeClient {

    @Override
    public SqlType parseSqlType(SQLStatement stmt) {
        if (stmt == null) {
            return SqlType.OTHER;
        }
        // 查询
        if (stmt instanceof SQLSelectStatement) {
            return selectType((SQLSelectStatement) stmt);
        }
        // 数据操作
        if (stmt instanceof SQLInsertStatement) {
            return SqlType.INSERT;
        }
        if (stmt instanceof SQLUpdateStatement) {
            return SqlType.UPDATE;
        }
        if (stmt instanceof SQLDeleteStatement) {
            return SqlType.DELETE;
        }
        if (stmt instanceof SQLTruncateStatement) {
            return SqlType.TRUNCATE;
        }
        if (stmt instanceof SQLMergeStatement) {
            return SqlType.MERGE_INTO;
        }
        if (stmt instanceof SQLBlockStatement || stmt instanceof SQLBeginStatement) {
            return SqlType.BEGIN_END;
        }
        // 结构操作
        if (stmt instanceof SQLCreateTableStatement) {
            return createTableType((SQLCreateTableStatement) stmt);
        }
        if (stmt instanceof SQLCreateViewStatement) {
            return createViewType((SQLCreateViewStatement) stmt);
        }
        if (stmt instanceof SQLCreateMaterializedViewStatement) {
            return SqlType.CREATE_MATERIALIZED_VIEW;
        }
        if (stmt instanceof SQLDropTableStatement || stmt instanceof SQLDropViewStatement) {
            return SqlType.DROP;
        }
        if (stmt instanceof SQLDropMaterializedViewStatement) {
            return SqlType.DROP_MATERIALIZED_VIEW;
        }
        if (stmt instanceof SQLAlterTableStatement) {
            return alterTableType((SQLAlterTableStatement) stmt);
        }
        if (stmt instanceof SQLAlterMaterializedViewStatement) {
            return SqlType.ALTER_MATERIALIZED_VIEW;
        }
        if (stmt instanceof SQLRefreshMaterializedViewStatement) {
            return SqlType.REFRESH_MATERIALIZED_VIEW;
        }
        if (stmt instanceof SQLCommentStatement) {
            return SqlType.COMMENT_ON;
        }
        // 描述
        if (stmt instanceof SQLDescribeStatement) {
            return describeType((SQLDescribeStatement) stmt);
        }
        // SHOW：具体子类优先于笼统 SHOW
        if (stmt instanceof SQLShowDatabasesStatement) {
            return SqlType.DATABASE_SHOW;
        }
        if (stmt instanceof SQLShowStatement) {
            return showType((SQLShowStatement) stmt);
        }
        // 执行计划
        if (stmt instanceof SQLExplainStatement || stmt instanceof SQLExplainAnalyzeStatement) {
            return SqlType.EXPLAIN;
        }
        // 函数 / 过程
        if (stmt instanceof SQLCreateFunctionStatement) {
            return SqlType.CREATE_FUNCTION;
        }
        if (stmt instanceof SQLDropFunctionStatement) {
            return SqlType.DROP_FUNCTION;
        }
        if (stmt instanceof SQLServerExecStatement || stmt instanceof SQLCallStatement) {
            return SqlType.PROCEDURE_EXEC;
        }
        // 数据库
        if (stmt instanceof SQLCreateDatabaseStatement) {
            return SqlType.DATABASE_CREATE;
        }
        if (stmt instanceof SQLDropDatabaseStatement) {
            return SqlType.DATABASE_DROP;
        }
        return SqlType.OTHER;
    }

    /**
     * 查询：WITH_QUERY（含 CTE）/ QUERY_NO_FROM（无 from）/ QUERY
     */
    protected SqlType selectType(SQLSelectStatement stmt) {
        SQLSelect select = stmt.getSelect();
        if (select != null && select.getWithSubQuery() != null) {
            return SqlType.WITH_QUERY;
        }
        if (isQueryNoFrom(select)) {
            return SqlType.QUERY_NO_FROM;
        }
        return SqlType.QUERY;
    }

    private boolean isQueryNoFrom(SQLSelect select) {
        if (select == null) {
            return false;
        }
        SQLSelectQuery query = select.getQuery();
        return query instanceof SQLSelectQueryBlock && ((SQLSelectQueryBlock) query).getFrom() == null;
    }

    /**
     * 建表：CREATE_AS（as select）/ CREATE_LIKE（like）/ CREATE_TEMP（临时表）/ CREATE
     */
    protected SqlType createTableType(SQLCreateTableStatement stmt) {
        if (stmt.getSelect() != null) {
            return SqlType.CREATE_AS;
        }
        if (stmt.getLike() != null) {
            return SqlType.CREATE_LIKE;
        }
        if (stmt.isTemporary()) {
            return SqlType.CREATE_TEMP;
        }
        return SqlType.CREATE;
    }

    /**
     * 建视图：CREATE_AS（as select）/ CREATE_VIEW
     */
    protected SqlType createViewType(SQLCreateViewStatement stmt) {
        return stmt.getSubQuery() != null ? SqlType.CREATE_AS : SqlType.CREATE_VIEW;
    }

    /**
     * 改表：按 alter item 细分，未命中归 ALTER
     */
    protected SqlType alterTableType(SQLAlterTableStatement stmt) {
        for (SQLAlterTableItem item : stmt.getItems()) {
            if (item instanceof SQLAlterTableRenameColumn) {
                return SqlType.ALTERTABLE_RENAMECOL;
            }
            if (item instanceof SQLAlterTableAddColumn) {
                return SqlType.ALTERTABLE_ADDCOLS;
            }
            if (item instanceof SQLAlterTableReplaceColumn) {
                return SqlType.ALTERTABLE_REPLACECOLS;
            }
            if (item instanceof SQLAlterTableAddPartition) {
                return SqlType.ALTERTABLE_ADDPARTS;
            }
            if (item instanceof SQLAlterTableDropPartition) {
                return SqlType.ALTERTABLE_DROPPARTS;
            }
            if (item instanceof SQLAlterTableRenamePartition) {
                return SqlType.ALTERTABLE_RENAMEPART;
            }
            if (item instanceof SQLAlterTableRename) {
                return SqlType.ALTERTABLE_RENAME;
            }
        }
        return SqlType.ALTER;
    }

    /**
     * 描述：DATABASE_DESC / DESC_TABLE
     */
    protected SqlType describeType(SQLDescribeStatement stmt) {
        return stmt.getObjectType() == SQLObjectType.DATABASE ? SqlType.DATABASE_DESC : SqlType.DESC_TABLE;
    }

    /**
     * SHOW 细分：具体语句优先，未命中归 SHOW
     */
    protected SqlType showType(SQLShowStatement stmt) {
        if (stmt instanceof SQLShowTablesStatement) {
            return SqlType.SHOW_TABLES;
        }
        if (stmt instanceof SQLShowCreateTableStatement) {
            return SqlType.SHOW_CREATETABLE;
        }
        if (stmt instanceof SQLShowColumnsStatement) {
            return SqlType.SHOW_COLUMNS;
        }
        if (stmt instanceof SQLShowFunctionsStatement) {
            return SqlType.SHOW_FUNCTION;
        }
        if (stmt instanceof SQLShowCreateMaterializedViewStatement) {
            return SqlType.SHOW_CREATE_MATERIALIZED_VIEW;
        }
        if (stmt instanceof SQLShowMaterializedViewStatement) {
            return SqlType.SHOW_MATERIALIZED_VIEW;
        }
        return SqlType.SHOW;
    }
}
