package com.changlu;

import java.io.BufferedReader;
import java.io.FileReader;

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

    public String getSqlTestPath() {
        return "/";
    }

}
