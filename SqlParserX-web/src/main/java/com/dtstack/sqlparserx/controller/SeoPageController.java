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
package com.dtstack.sqlparserx.controller;

import com.dtstack.sqlparserx.env.EnvironmentContext;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SeoPageController {

    private static final String SITE_URL_PLACEHOLDER = "__SEO_SITE_URL__";
    private static final MediaType HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);
    private static final Map<String, String> PAGE_TEMPLATES = createPageTemplates();

    private final EnvironmentContext environmentContext;

    public SeoPageController(EnvironmentContext environmentContext) {
        this.environmentContext = environmentContext;
    }

    @GetMapping(value = {
            "/sql-lineage.html",
            "/sql-syntax-validator.html",
            "/sql-table-parser.html",
            "/create-table-sql-parser.html",
            "/sql-parameter-parser.html",
            "/sql-splitter.html"
    }, produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> page(HttpServletRequest request) throws IOException {
        String route = request.getRequestURI().substring(request.getContextPath().length());
        if (route.length() > 1 && route.endsWith("/")) {
            route = route.substring(0, route.length() - 1);
        }

        String templateName = PAGE_TEMPLATES.get(route);
        if (templateName == null) {
            return ResponseEntity.notFound().build();
        }

        ClassPathResource template = new ClassPathResource("templates/seo/" + templateName + ".html");
        String html = StreamUtils.copyToString(template.getInputStream(), StandardCharsets.UTF_8)
                .replace(SITE_URL_PLACEHOLDER, environmentContext.getSeoSiteUrl());
        return ResponseEntity.ok().contentType(HTML_UTF8).body(html);
    }

    private static Map<String, String> createPageTemplates() {
        Map<String, String> templates = new HashMap<>();
        templates.put("/sql-lineage.html", "sql-lineage");
        templates.put("/sql-syntax-validator.html", "sql-syntax-validator");
        templates.put("/sql-table-parser.html", "sql-table-parser");
        templates.put("/create-table-sql-parser.html", "create-table-sql-parser");
        templates.put("/sql-parameter-parser.html", "sql-parameter-parser");
        templates.put("/sql-splitter.html", "sql-splitter");
        return Collections.unmodifiableMap(templates);
    }
}
