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

import com.dtstack.sqlparserx.domain.ResponseResult;
import com.dtstack.sqlparserx.enums.DataSourceType2DbType;
import com.dtstack.sqlparserx.service.LineageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    @Autowired
    private LineageService lineageService;

    /**
     * 查询SQL字段血缘关系接口
     * @param sql 要分析的SQL语句（通过请求参数传入）
     * @return 字段血缘分析结果
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestParam Integer datasourceType, @RequestBody String sql) {
        if (datasourceType == null || !DataSourceType2DbType.isCoreSupported(datasourceType)) {
            return ResponseResult.error("暂不支持该数据源方言");
        }
        // 调用服务层方法并返回结果
        Map<String, Object> lineage = lineageService.getLineage(sql, datasourceType);
        return ResponseResult.success(lineage);
    }

}
