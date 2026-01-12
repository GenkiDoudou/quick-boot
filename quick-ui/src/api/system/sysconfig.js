import request from '@/utils/request'

/**
* 参数配置表 API
*/

/**
* 查询参数配置表列表
*/
export function listSysConfig(query) {
return request({
url: '/system/sysconfig/list',
method: 'get',
params: query
})
}

/**
* 查询参数配置表详情
*/
export function getSysConfig(configId) {
return request({
url: '/system/sysconfig/' + configId,
method: 'get'
})
}

/**
* 新增参数配置表
*/
export function addSysConfig(data) {
return request({
url: '/system/sysconfig',
method: 'post',
data: data
})
}

/**
* 修改参数配置表
*/
export function updateSysConfig(data) {
return request({
url: '/system/sysconfig',
method: 'put',
data: data
})
}

/**
* 删除参数配置表
*/
export function delSysConfig(ids) {
return request({
url: '/system/sysconfig' ,
method: 'delete',
data: ids
})
}
