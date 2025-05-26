package com.dtstack.sqlparser.sqlparserx.core.parser;

import java.util.LinkedHashSet;
/**
 * SQL过滤相关的服务接口
 *  按照输出参数排序过滤：filterSortSelectField
 *  列级权限过滤：getColumnLevelPermissionSql
 *  行级权限管控：getRowLevelPermissionSql
 */
public interface ISqlFilterClient {

    /**
     * 列级权限过滤
     * 注意：过滤select的输出参数以及按照outputParams顺序进行排序。
     *  入参：select name1, name, id from table, outputparams = [id, name]
     *  输出：select id, name from table
     * @param sql sql
     * @param outputParams 应当输出的参数名
     * @return sql保留outputParams中指定的参数名返回，没有的参数名则从sql中剔除
     */
    String filterSortSelectField(String sql, LinkedHashSet<String> outputParams);

//    /**
//     * 行级权限过滤
//     * @param sql 原始SQL语句
//     * @param permissionParams 权限过滤参数列表
//     * @param dataType 数据类型
//     * @return 返回结果中的standardSql为生成的行级权限过滤的SQL语句
//     * @throws Exception 如果发生异常
//     */
//    SelectParseResult getRowLevelPermissionSql(String sql, List<ApiRowLevelPermissionFilterParam> permissionParams, Integer dataType) throws Exception;

}
