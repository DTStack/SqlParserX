package com.changlu.sqlparserx.core.parser.base.table.createtable.domain;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TableElement {
    private String name;
    private String type;
    private List<String> arguments;
    private String polymerizeType;
    private String comment;
    /**
     * 字段策略：0 可以 null, 1 不可以 null
     */
    private Integer columnStrategy;

    /**
     * 字段精度
     */
    private Integer scale;

    /**
     * 字段精度
     */
    private Integer precision;

    /**
     * 聚合类型
     */
    private String aggType;

    public TableElement() {
    }

    public TableElement(String name, String type, List<String> arguments, String polymerizeType, String comment,
            Integer columnStrategy, Integer scale, Integer precision) {
        this.name = name;
        this.type = type;
        this.arguments = arguments;
        this.polymerizeType = polymerizeType;
        this.comment = comment;
        this.columnStrategy = columnStrategy;
        this.scale = scale;
        this.precision = precision;
    }
}