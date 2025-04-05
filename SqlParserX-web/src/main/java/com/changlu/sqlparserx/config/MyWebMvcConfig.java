package com.changlu.sqlparserx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {

    /**
     * 解决跨域问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  //指定的映射地址
                .allowedHeaders("*") //允许携带的请求头
                .allowedMethods("*") //允许的请求方法
                .allowedOrigins("*");  //添加跨域请求头 Access-Control-Allow-Origin，值如："https://domain1.com"或"*"
    }
}
