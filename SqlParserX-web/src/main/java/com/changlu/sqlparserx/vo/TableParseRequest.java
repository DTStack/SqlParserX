package com.changlu.sqlparserx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 添加请求体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableParseRequest {
    private Integer datasourceType;
    private String sql;
}