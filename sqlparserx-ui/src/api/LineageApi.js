import request from '@/utils/request'

function queryLineage(url, sql, dataSourceType) {
    return request({
        url,
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        data: sql,
        params: { datasourceType: dataSourceType }
    })
}

/** 调用字段血缘解析接口。 */
export function analyzeColumnLineage(sql, dataSourceType) {
    return queryLineage('/api/lineage/column/query', sql, dataSourceType)
}

/** 调用表血缘解析接口。 */
export function analyzeTableLineage(sql, dataSourceType) {
    return queryLineage('/api/lineage/table/query', sql, dataSourceType)
}

// 保留原有调用名称，默认解析字段血缘。
export const analyzeSQL = analyzeColumnLineage

export default { analyzeSQL, analyzeColumnLineage, analyzeTableLineage }
