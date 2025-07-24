package com.changlu.sqlparserx.core.utils;

/**
 * @description  字段相关utils
 * @author changlu
 * @date 2025/7/24 16:11
 */
public class ColumnUtil {

    /**
     * 去除字符串中的反引号(`)和单引号(')
     * @param input 输入字符串
     * @return 处理后的字符串，如果输入为null则返回null
     */
    public static String removeQuotes(String input) {
        if (input == null) {
            return null;
        }
        // 去除字符串开头和结尾的反引号或单引号
        return input.replaceAll("`", "")
                .replaceAll("'", "")
                .replaceAll("\"", "");
    }

}
