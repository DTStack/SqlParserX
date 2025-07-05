import request from '@/utils/request'

/**
 * 解析SQL血缘关系
 * @param {string} sql SQL语句
 * @param {number} dataSourceType 数据源类型ID
 * @returns {Promise<{tableData: Array, relationData: Array}>} 血缘关系数据
 */
export function analyzeSQL(sql, dataSourceType) {
    return request({
        url: '/api/lineage/query',
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        data: sql,
        params: {
            datasourceType: dataSourceType
        }
    })
}

export default {
    analyzeSQL
}