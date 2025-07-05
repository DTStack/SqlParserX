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

export default {
    fetchDataSourceTypes
}