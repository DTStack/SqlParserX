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
package com.dtstack.sqlparserx.env;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentContext {

    private static final String DEFAULT_SITE_URL = "http://localhost:16758";

    private final Environment environment;

    public EnvironmentContext(Environment environment) {
        this.environment = environment;
    }

    public String getServerPort() {
        return environment.getProperty("server.port", "16758");
    }

    /**
     * Returns the externally visible site root without a trailing slash.
     * The value may include a reverse-proxy path prefix.
     */
    public String getSeoSiteUrl() {
        String siteUrl = environment.getProperty("seo.site-url", DEFAULT_SITE_URL).trim();
        while (siteUrl.endsWith("/")) {
            siteUrl = siteUrl.substring(0, siteUrl.length() - 1);
        }
        if (!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://")) {
            throw new IllegalStateException("seo.site-url must start with http:// or https://");
        }
        return siteUrl;
    }
}
