package com.changlu.sqlparserx.core.parser.mysql.table;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;
import com.changlu.sqlparserx.core.parser.base.table.ParseTableClient;
import com.changlu.sqlparserx.core.parser.base.lineage.table.Table;
import com.changlu.sqlparserx.core.parser.base.table.domain.ParseTableResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description  针对mysql数据源提供的解析表client
 * @author changlu
 * @date 2025/4/27 12:01
 */
public class MysqlParseTableClient extends ParseTableClient {

    @Override
    public ParseTableResult parseTables(SQLStatement stmt) {
        if (stmt instanceof MySqlRenameTableStatement) {
            this.parseMySqlRenameTable((MySqlRenameTableStatement) stmt);
        }
        return super.parseTables(stmt);
    }

    public void parseMySqlRenameTable(MySqlRenameTableStatement mySqlRenameTableStatement) {
        List<MySqlRenameTableStatement.Item> items = mySqlRenameTableStatement.getItems();
        List<String> tableNames = items.stream().map(item -> item.getName().getSimpleName()).collect(Collectors.toList());
        if (!tableNames.isEmpty()) {
            this.mainTable = Table.TableTransfer.parse(tableNames.get(0));
            this.allTables = tableNames.stream().map(Table.TableTransfer::parse).collect(Collectors.toSet());
        }
    }

}
