// DatasourceApi.js
import request from '@/utils/request'

/**
 * 获取数据源类型列表
 * @returns {Promise<Array<{value: number, label: string}>>} 数据源类型数组
 */
export function fetchDataSourceTypes() {
    return request({
        url: '/api/datasource/types',
        method: 'GET'
    })
}

export function parseTables(form) {
    return request({
        url: '/api/datasource/parseTables',
        method: 'post',
        data: form
    })
}
// 新增：解析建表SQL
export function parseCreateTable(data) {
    return request({
        url: '/api/datasource/parseCreateTable',
        method: 'post',
        data
    })
}

export default {
    fetchDataSourceTypes, parseTables, parseCreateTable
}