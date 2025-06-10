import request from '@/utils/request'

// 查询团队成员
export function queryColumnLineage(params) {
    return request({
        url: '/api/lineage/query?' + params,
        method: 'get'
    })
}