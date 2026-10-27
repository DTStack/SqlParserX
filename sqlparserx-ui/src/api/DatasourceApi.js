// DatasourceApi.js
import request from '@/utils/request'

/**
 * 获取数据源类型列表
 * @returns {Promise<Array<{value: number, label: string}>>} 数据源类型数组
 */
export function fetchDataSourceTypes() {
    return request({
        url: 'api/datasource/types',
        method: 'GET'
    })
}

/** Load the data source options returned by the backend Core parser enum. */
export async function loadDataSourceOptions(preferredName) {
    const response = await fetchDataSourceTypes()
    const options = Array.isArray(response && response.data)
        ? response.data
            .filter(option => option && Number.isFinite(Number(option.value)))
            .map(option => ({ ...option, value: Number(option.value) }))
        : []
    const preferred = options.find(option => option.label === preferredName)

    return {
        options,
        selectedValue: preferred ? preferred.value : (options.length ? options[0].value : null)
    }
}

export function parseTables(form) {
    return request({
        url: 'api/datasource/parseTables',
        method: 'post',
        data: form
    })
}
// 新增：解析建表SQL
export function parseCreateTable(data) {
    return request({
        url: 'api/datasource/parseCreateTable',
        method: 'post',
        data
    })
}

/** 解析 SQL 中的输入参数、输出字段及其元数据绑定 */
export function parseParameters(data) {
    return request({
        url: 'api/parameter/parse',
        method: 'post',
        data
    })
}

/** 将带变量 SQL 改写为预编译 SQL，并返回有序绑定值 */
export function prepareCompile(data) {
    return request({
        url: 'api/parameter/prepareCompile',
        method: 'post',
        data
    })
}

export default {
    fetchDataSourceTypes, loadDataSourceOptions, parseTables, parseCreateTable, parseParameters, prepareCompile
}
