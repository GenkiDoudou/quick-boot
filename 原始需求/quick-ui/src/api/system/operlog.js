import request from '@/utils/request'

// 查询操作日志列表
export function listOperlog(query) {
  return request({
    url: '/system/sysoperlog/list',
    method: 'get',
    params: query
  })
}

// 获取操作日志详情
export function getOperlog(operId) {
  return request({
    url: '/system/sysoperlog/' + operId,
    method: 'get'
  })
}

// 删除操作日志
export function delOperlog(operId) {
  return request({
    url: '/system/sysoperlog/delete/' + operId,
    method: 'post'
  })
}

// 清空操作日志
export function cleanOperlog() {
  return request({
    url: '/system/sysoperlog/clean',
    method: 'post'
  })
}
