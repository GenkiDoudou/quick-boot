import request from '@/utils/request'

/**
 * 客户端管理 API
 */

/**
 * 查询客户端管理列表
 */
export function listSysOauthClient(query) {
    return request({
        url: '/system/sysoauthclient/list',
        method: 'get',
        params: query
    })
}

/**
 * 查询客户端管理详情
 */
export function getSysOauthClient(id) {
    return request({
        url: '/system/sysoauthclient/' + id,
        method: 'get'
    })
}

/**
 * 新增客户端管理
 */
export function addSysOauthClient(data) {
    return request({
        url: '/system/sysoauthclient',
        method: 'post',
        data: data
    })
}

/**
 * 修改客户端管理
 */
export function updateSysOauthClient(data) {
    return request({
        url: '/system/sysoauthclient/update',
        method: 'post',
        data: data
    })
}

/**
 * 删除客户端管理
 */
export function delSysOauthClient(ids) {
    return request({
        url: '/system/sysoauthclient/delete',
        method: 'post',
        data: ids
    })
}

/**
 * 修改状态
 */
export function updateSysOauthClientStatus(id, status) {
    return request({
        url: '/system/sysoauthclient/updateStatus',
        method: 'post',
        params: {
            status: status,
            id: id
        }
    })
}

/**
 * 重新生成加解密密钥
 */
export function generateEncryptionKey(id) {
    return request({
        url: '/system/sysoauthclient/generateEncryptionKey/' + id,
        method: 'post'
    })
}