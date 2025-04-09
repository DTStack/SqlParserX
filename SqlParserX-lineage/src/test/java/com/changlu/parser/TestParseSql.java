package com.changlu.parser;


import com.changlu.AbstractSqlParserTest;
import org.junit.Test;

public class TestParseSql extends AbstractSqlParserTest {

    @Test
    public void test() {
        String s = readSqlFile("1.txt");
        System.out.println(s);
    }

    @Override
    public String getSqlTestPath() {
        return "/parser/";
    }
}
