package com.changlu.sqlparserx.core.config;

public class Constant {

    public static final String ALL = "*";

    /**
     * 若是某个字段无来自某个表，此时该字段的table则为" "，最终血缘会完成过滤
     * 举例：create table new_table select id, '0' as num from table
     */
    public static final String NO_RELATION_TABLE = " ";

}
