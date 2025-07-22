package com.changlu.sqlparserx.core.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供了读取文件中的sql实现
 */
public class AbstractSqlParserTest {

    public String readSqlFile(String testFile){
        try {
            String path = getSqlTestPath() + testFile;
            String relativePath = this.getClass().getResource(path).getPath();
            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(relativePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }
            }
            return sql.toString();
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("读取文件异常！");
        }
    }

    /**
     * 读取指定文件中的第n条SQL语句
     * @param testFile 文件名
     * @param index SQL索引(从1开始)
     * @return 指定的SQL语句
     */
    public String readSqlByIndex(String testFile, int index) {
        try {
            String path = getSqlTestPath() + testFile;
            String relativePath = this.getClass().getResource(path).getPath();

            Map<Integer, String> sqlMap = new HashMap<>();
            StringBuilder currentSql = new StringBuilder();
            Integer currentIndex = null;

            try (BufferedReader reader = new BufferedReader(new FileReader(relativePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().matches("-{5,}\\d+-{5,}")) {
                        // 遇到索引分隔线
                        if (currentIndex != null && currentSql.length() > 0) {
                            // 保存上一个SQL
                            sqlMap.put(currentIndex, currentSql.toString().trim());
                            currentSql = new StringBuilder();
                        }
                        // 提取当前索引
                        currentIndex = extractIndexFromDelimiter(line.trim());
                    } else {
                        // 普通SQL行，添加到当前SQL
                        if (currentIndex != null) {
                            currentSql.append(line).append("\n");
                        }
                    }
                }
                // 添加最后一个SQL
                if (currentIndex != null && currentSql.length() > 0) {
                    sqlMap.put(currentIndex, currentSql.toString().trim());
                }
            }

            if (!sqlMap.containsKey(index)) {
                throw new IllegalArgumentException("SQL索引"+index+"不存在，有效范围:" + sqlMap.keySet());
            }

            return sqlMap.get(index);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("读取文件异常！");
        }
    }

    /**
     * 从分隔符中提取索引数字
     * @param delimiter 分隔符字符串，如"-----1-----"
     * @return 提取到的数字
     */
    private Integer extractIndexFromDelimiter(String delimiter) {
        // 移除所有横线，只保留数字
        String numStr = delimiter.replaceAll("-", "").trim();
        return Integer.parseInt(numStr);
    }

    public String getSqlTestPath() {
        return "/";
    }

}
