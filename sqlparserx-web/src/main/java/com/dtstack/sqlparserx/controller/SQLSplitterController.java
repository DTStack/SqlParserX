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

import com.dtstack.sqlparserx.core.utils.SQLSplitterUtil;
import com.dtstack.sqlparserx.domain.ResponseResult;
import com.dtstack.sqlparserx.enums.DataSourceType2DbType;

import com.alibaba.druid.DbType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sql")
public class SQLSplitterController {

    @PostMapping("/split")
    public ResponseResult splitSQL(@RequestParam Integer datasourceType, @RequestBody String sql) {
        if (datasourceType == null || !DataSourceType2DbType.isCoreSupported(datasourceType)) {
            return ResponseResult.error("暂不支持该数据源方言");
        }
        // 获取对应的DbType
        DbType dbType = DataSourceType2DbType.getDbType(datasourceType);
        List<String> sqls = SQLSplitterUtil.splitSQL(sql, dbType);
        // 切分SQL
        return ResponseResult.success(sqls);
    }
}
