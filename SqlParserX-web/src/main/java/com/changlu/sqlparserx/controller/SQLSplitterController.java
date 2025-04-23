package com.changlu.sqlparserx.controller;

import com.alibaba.druid.DbType;
import com.changlu.utils.SQLSplitterUtil;
import com.changlu.sqlparserx.enums.DataSourceType2DbType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sql")
public class SQLSplitterController {

    @PostMapping("/split")
    public List<String> splitSQL(@RequestParam Integer datasourceType, @RequestBody String sql) {
        // 获取对应的DbType
        DbType dbType = DataSourceType2DbType.getDbType(datasourceType);
        // 切分SQL
        return SQLSplitterUtil.splitSQL2(sql, dbType);
    }
}