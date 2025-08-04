package com.changlu.sqlparserx.core.parser.base.table.createtable.domain;

import java.util.List;
import java.util.Objects;

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

    public TableElement() {
    }

    public TableElement(String name, String type, List<String> arguments, String polymerizeType, String comment, Integer columnStrategy, Integer scale, Integer precision) {
        this.name = name;
        this.type = type;
        this.arguments = arguments;
        this.polymerizeType = polymerizeType;
        this.comment = comment;
        this.columnStrategy = columnStrategy;
        this.scale = scale;
        this.precision = precision;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public String getPolymerizeType() {
        return polymerizeType;
    }

    public void setPolymerizeType(String polymerizeType) {
        this.polymerizeType = polymerizeType;
    }

    public Integer getColumnStrategy() {
        return columnStrategy;
    }

    public void setColumnStrategy(Integer columnStrategy) {
        this.columnStrategy = columnStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableElement that = (TableElement) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(arguments, that.arguments) && Objects.equals(polymerizeType, that.polymerizeType) && Objects.equals(comment, that.comment) && Objects.equals(columnStrategy, that.columnStrategy) && Objects.equals(scale, that.scale) && Objects.equals(precision, that.precision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, arguments, polymerizeType, comment, columnStrategy, scale, precision);
    }

    @Override
    public String toString() {
        return "TableElement{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", arguments=" + arguments +
                ", polymerizeType='" + polymerizeType + '\'' +
                ", comment='" + comment + '\'' +
                ", columnStrategy=" + columnStrategy +
                ", scale=" + scale +
                ", precision=" + precision +
                '}';
    }
}