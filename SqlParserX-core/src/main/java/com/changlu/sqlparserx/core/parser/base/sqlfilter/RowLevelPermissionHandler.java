package com.changlu.sqlparserx.core.parser.base.sqlfilter;

import com.changlu.sqlparserx.core.parser.base.sqlfilter.row.builder.BaseExprBuilder;
import com.changlu.sqlparserx.core.parser.base.sqlfilter.row.vo.RowLevelPermissionFilterParam;

import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;

public interface RowLevelPermissionHandler {

    /**
     * 行级权限过滤
     * @param sqlStatement SQL语法树
     * @param permissionParams 权限过滤参数列表
     * @return 返回结果中的standardSql为生成的行级权限过滤的SQL语句
     * @throws Exception 如果发生异常
     */
    String getRowLevelPermissionSql(SQLStatement sqlStatement, List<RowLevelPermissionFilterParam> permissionParams);

    /**
     * 获取Expr构建器
     * @return BaseExprBuilder
     */
    default BaseExprBuilder getExprBuilder() {
        return new BaseExprBuilder();
    }

}
