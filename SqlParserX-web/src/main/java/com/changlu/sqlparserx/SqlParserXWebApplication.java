package com.changlu.sqlparserx;

import com.changlu.sqlparserx.env.SystemPropertyUtil;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SqlParserXWebApplication {

    public static void main(String[] args) {
        SystemPropertyUtil.setSystemUserDir();
        System.out.println("Static resources path: " + System.getProperty("user.dir.conf") + "/static/");
        new SpringApplicationBuilder(SqlParserXWebApplication.class)
                .properties(
                        "spring.config.location=file:${user.dir.conf}/application.properties",
                        "spring.resources.static-locations=file:${user.dir.conf}/static/").run(args);
    }

}
