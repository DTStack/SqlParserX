/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.sqlparserx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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

    /**
     * Serve the static site entry points at directory URLs.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/front").setViewName("redirect:/front/");
        registry.addViewController("/front/").setViewName("forward:/front/index.html");
        registry.addViewController("/website").setViewName("redirect:/website/");
        registry.addViewController("/website/").setViewName("forward:/website/index.html");
    }
}
