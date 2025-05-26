package com.changlu.sqlparserx.controller;

import com.changlu.sqlparserx.service.LineageService;
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
    public Map<String, Object> query(@RequestParam Integer datasourceType, @RequestBody String sql) {
        // 调用服务层方法并返回结果
        return lineageService.getLineage(sql, datasourceType);
    }

}
