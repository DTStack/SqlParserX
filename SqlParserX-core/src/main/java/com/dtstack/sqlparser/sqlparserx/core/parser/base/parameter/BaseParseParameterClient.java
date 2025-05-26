package com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.dtstack.sqlparser.sqlparserx.core.parser.IParseParameterClient;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain.DataTypeColumn;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain.ParamInfo;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.vo.ParseParameterResult;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.vo.PrepareCompileResult;

import java.util.List;
import java.util.Map;

public class BaseParseParameterClient implements IParseParameterClient {

    

    @Override
    public ParseParameterResult parseParameterSql(SQLStatement stmt, Boolean isIgnoreSyntaxCheck, Map<String, List<DataTypeColumn>> tableColumnsMap) {
        return null;
    }

    @Override
    public PrepareCompileResult buildSqlByPrepareCompile(SQLStatement stmt, Map<String, Object> inParams, Map<String, ParamInfo> columns) {
        return null;
    }
}
