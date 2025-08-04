package com.changlu.sqlparserx.core.utils;

import java.util.Collection;

/**
 * @description  集合工具库
 * @author changlu
 * @date 2025/8/4 11:09
 */
public class CollectionUtil {

    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(Collection coll) {
        return !isEmpty(coll);
    }

}
