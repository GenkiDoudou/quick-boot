import request from '@/utils/request'

/**
* ${tableComment!} API
*/

/**
* 查询${tableComment!}列表
*/
export function list${className}(query) {
return request({
url: '/${moduleName!}/${className ?lower_case}/list',
method: 'get',
params: query
})
}

/**
* 查询${tableComment!}详情
*/
export function get${className}(${keyField}) {
return request({
url: '/${moduleName!}/${className ?lower_case}/' + ${keyField},
method: 'get'
})
}

/**
* 新增${tableComment!}
*/
export function add${className}(data) {
return request({
url: '/${moduleName!}/${className ?lower_case}',
method: 'post',
data: data
})
}

/**
* 修改${tableComment!}
*/
export function update${className}(data) {
return request({
url: '/${moduleName!}/${className ?lower_case}',
method: 'put',
data: data
})
}

/**
* 删除${tableComment!}
*/
export function del${className}(ids) {
return request({
url: '/${moduleName!}/${className ?lower_case}' ,
method: 'delete',
data: ids
})
}
