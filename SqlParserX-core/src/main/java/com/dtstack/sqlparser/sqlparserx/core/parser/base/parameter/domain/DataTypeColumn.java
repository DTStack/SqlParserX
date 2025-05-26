package com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain;

public class DataTypeColumn {

    // 表字段真实名称
    private String name;
    // 表字段类型
    private String type;
    // 表注释
    private String comment;

    public DataTypeColumn() {
    }

    public DataTypeColumn(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "DataTypeColumn{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
