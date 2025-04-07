package com.changlu.sqlparserx.controller;

import com.changlu.sqlparserx.enums.DatasourceTypeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    @GetMapping("/types")
    public List<DataSourceOption> getDataSourceTypes() {
        return Arrays.stream(DatasourceTypeEnum.values())
                .map(type -> new DataSourceOption(type.getCode(), type.name()))
                .collect(Collectors.toList());
    }

    public static class DataSourceOption {
        private final int value;
        private final String label;

        public DataSourceOption(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}