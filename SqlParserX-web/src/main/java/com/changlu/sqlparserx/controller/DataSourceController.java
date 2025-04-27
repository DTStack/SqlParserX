package com.changlu.sqlparserx.controller;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.changlu.sqlparserx.enums.DataSourceType2DbType;
import com.changlu.sqlparserx.enums.DatasourceTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    private static final Logger log = LoggerFactory.getLogger(DataSourceController.class);

    @GetMapping("/types")
    public List<DataSourceOption> getDataSourceTypes() {
        return Arrays.stream(DatasourceTypeEnum.values())
                .map(type -> new DataSourceOption(type.getCode(), type.name()))
                .collect(Collectors.toList());
    }

    @PostMapping("/grammarCheck")
    public Map<String, Object> grammarCheck(@RequestParam Integer datasourceType, @RequestBody String sql) {
        DbType dbType = DataSourceType2DbType.getDbType(datasourceType);
        Map<String, Object> res = new HashMap<>();
        String msg = "success!";
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
            SQLStatement stmt = parser.parseStatement();
            res.put("code", 200);
        } catch (Exception e) {
            msg = e.getMessage();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.info("grammarCheck error!", e);
            res.put("code", 500);
            res.put("msg", msg);
            res.put("stackTrace", stackTrace); // 添加堆栈信息
        }
        return res;
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