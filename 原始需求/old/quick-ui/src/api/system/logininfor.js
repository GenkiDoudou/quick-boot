import request from '@/utils/request'

// 查询登录日志列表
export function listLogininfor(query) {
  return request({
    url: '/system/syslogininfor/list',
    method: 'get',
    params: query
  })
}

// 获取登录日志详情
export function getLogininfor(infoId) {
  return request({
    url: '/system/syslogininfor/' + infoId,
    method: 'get'
  })
}

// 删除登录日志
export function delLogininfor(infoId) {
  return request({
    url: '/system/syslogininfor/delete/' + infoId,
    method: 'post'
  })
}

// 清空登录日志
export function cleanLogininfor() {
  return request({
    url: '/system/syslogininfor/clean',
    method: 'post'
  })
}
