package com.changlu.sqlparserx.core.parser.starrocks.table;

import com.alibaba.druid.sql.ast.DistributedByType;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLUnique;
import com.alibaba.druid.sql.dialect.starrocks.ast.StarRocksAggregateKey;
import com.alibaba.druid.sql.dialect.starrocks.ast.StarRocksDuplicateKey;
import com.alibaba.druid.sql.dialect.starrocks.ast.statement.StarRocksCreateTableStatement;
import com.changlu.sqlparserx.core.domain.Pair;
import com.changlu.sqlparserx.core.parser.base.table.ParseCreateTableClient;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.DistributedByTypeEnum;
import com.changlu.sqlparserx.core.parser.base.table.createtable.enums.TableKeyEnum;
import com.changlu.sqlparserx.core.utils.CommonUtil;

import java.util.List;

/**
 * @description Starrocks解析create table特定处理
 * @author changlu
 * @date 2025/8/4 14:22
 */
public class StarrocksParseCreateTableClient extends ParseCreateTableClient {

    @Override
    protected Pair<DistributedByTypeEnum, List<String>> dealDistributeByColumns(SQLCreateTableStatement stmt) {
        if (stmt instanceof StarRocksCreateTableStatement) {
            StarRocksCreateTableStatement starRocksCreateTableStatement = (StarRocksCreateTableStatement) stmt;
            DistributedByType distributedByType = starRocksCreateTableStatement.getDistributedByType();
            List<SQLSelectOrderByItem> distributedBy = starRocksCreateTableStatement.getDistributedBy();

            // 设置结果值
            DistributedByTypeEnum resDistributedByType = null;
            // 设置类型
            if (distributedByType != null) {
                if (distributedByType == DistributedByType.Hash) {
                    resDistributedByType = DistributedByTypeEnum.Hash;
                } else if (distributedByType == DistributedByType.Random) {
                    resDistributedByType = DistributedByTypeEnum.Random;
                }
            }
            // 设置分布列
            List<String> resDistributedByColumns = CommonUtil.sqlSelectOrderByItems2Values(distributedBy);

            return new Pair<>(resDistributedByType, resDistributedByColumns);
        }
        // 其他场景继承基类逻辑
        return super.dealDistributeByColumns(stmt);
    }

    @Override
    protected TableKeyEnum dealTableKey(SQLUnique unique) {
        if (unique instanceof StarRocksDuplicateKey) {
            return TableKeyEnum.DUPLICATE;
        }else if (unique instanceof StarRocksAggregateKey) {
            return TableKeyEnum.AGGREGATE;
        }
        return super.dealTableKey(unique);
    }
}
