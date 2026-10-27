// sqlparser.js
import request from '@/utils/request'


/**
 * 切分SQL语句
 * @param {string} sql - 要切分的SQL语句
 * @param {number} datasourceType - 数据源类型
 * @returns {Promise<Array<string>>} 切分后的SQL语句数组
 */
export function splitSQL(sql, datasourceType) {
    return request({
        url: 'api/sql/split',
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        params: {
            datasourceType: datasourceType
        },
        data: sql
    })
}

/**
 * 验证SQL语法
 * @param {string} sql - 要验证的SQL语句
 * @param {number} datasourceType - 数据源类型
 * @returns {Promise<Object>} 验证结果对象
 */
export function validateSQL(sql, datasourceType) {
    return request({
        url: 'api/datasource/grammarCheck',
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        params: {
            datasourceType: datasourceType
        },
        data: sql
    })
}

export default {
    splitSQL,
    validateSQL
}
