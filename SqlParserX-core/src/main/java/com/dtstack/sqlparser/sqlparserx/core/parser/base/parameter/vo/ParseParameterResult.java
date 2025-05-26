package com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.vo;

import com.dtstack.sqlparser.sqlparserx.core.parser.base.lineage.table.Table;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain.ApiParamSpan;
import com.dtstack.sqlparser.sqlparserx.core.parser.base.parameter.domain.ParamInfo;

import java.util.List;

public class ParseParameterResult {

    // 输出参数集合
    private List<ApiParamSpan> outputParams;

    // 输入参数集合
    private List<ParamInfo> inputParams;

    // 表集合
    private List<Table> tables;

}
