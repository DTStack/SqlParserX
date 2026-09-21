package com.changlu.sqlparserx.core.parser.base.parameter;

import com.changlu.sqlparserx.core.parser.IParseParameterClient;
import com.changlu.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.changlu.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.changlu.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.changlu.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;

import com.alibaba.druid.sql.ast.SQLStatement;

import java.util.List;
import java.util.Map;

public class BaseParseParameterClient implements IParseParameterClient {

    @Override
    public ParseParameterResult parseParameterSql(SQLStatement stmt, Boolean isIgnoreSyntaxCheck,
            Map<String, List<DataTypeColumn>> tableColumnsMap) {
        return null;
    }

    @Override
    public PrepareCompileResult buildSqlByPrepareCompile(SQLStatement stmt, Map<String, Object> inParams,
            Map<String, ParamInfo> columns) {
        return null;
    }
}
