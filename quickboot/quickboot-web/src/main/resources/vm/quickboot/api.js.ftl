import request from '@/utils/request'

/** ${tableComment!} 分页列表 */
export function list${className}(query) {
  return request({
    url: '/${moduleName}/${businessName}/list',
    method: 'get',
    params: query
  })
}

/** ${tableComment!} 详情 */
export function get${className}(id) {
  return request({
    url: `/${moduleName}/${businessName}/${id}`,
    method: 'get'
  })
}

/** 新增 */
export function add${className}(data) {
  return request({
    url: '/${moduleName}/${businessName}/create',
    method: 'post',
    data
  })
}

/** 修改 */
export function update${className}(data) {
  return request({
    url: '/${moduleName}/${businessName}/update',
    method: 'post',
    data
  })
}

/** 删除 */
export function del${className}(ids) {
  return request({
    url: '/${moduleName}/${businessName}/remove',
    method: 'post',
    data: ids
  })
}
