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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * 功能：重新加载自定义的logback配置文件路径
 */
@Component
public class LogbackComponent implements InitializingBean {

    private static Logger LOGGER = LoggerFactory.getLogger(LogbackComponent.class);

    // logback文件路径
    private static String logback = System.getProperty("user.dir") + "/conf/logback.xml";

    /**
     * 初始化logger
     * @throws IOException
     * @throws JoranException
     */
    private void setupLogger() throws IOException, JoranException {
        // 获取Logback的LoggerContext实例，管理Logback配置的上下文环境
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        File externalConfigFile = new File(logback);
        // 检查配置文件是否存在，如果不存在则抛出IOException。
        if (!externalConfigFile.exists()) {
            throw new IOException("Logback External Config File Parameter does not reference a file that exists");
        } else {
            // 如果文件存在，检查它是否是一个普通文件（而不是目录）
            if (!externalConfigFile.isFile()) {
                throw new IOException("Logback External Config File Parameter exists, but does not reference a file");
            } else {
                // 如果是一个文件，检查它是否可读
                if (!externalConfigFile.canRead()) {
                    throw new IOException("Logback External Config File exists and is a file, but cannot be read.");
                } else {
                    // 如果文件存在、是一个文件且可读，创建一个JoranConfigurator实例，用于解析和应用Logback配置文件
                    JoranConfigurator configurator = new JoranConfigurator();
                    // 将LoggerContext实例设置到JoranConfigurator中
                    configurator.setContext(lc);
                    // 重置LoggerContext，以便重新加载配置
                    lc.reset();
                    // 使用JoranConfigurator解析指定路径的Logback配置文件，并应用配置
                    configurator.doConfigure(logback);
                    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        try {
            setupLogger();
        } catch (Throwable e) {
            LOGGER.error("contextInitialized error!", e);
        }
    }
}