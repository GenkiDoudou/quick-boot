/**
 * 登录日志 API：分页查询、删除、清空、解锁、导出。
 */
import request from '@/utils/request'

/** 登录日志分页（POST，param 为筛选条件） */
export function pageLogininfor(pageRequest) {
  return request({ url: '/monitor/logininfor/page', method: 'post', data: pageRequest })
}

/** 批量删除登录日志 */
export function removeLogininfor(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/monitor/logininfor/remove', method: 'post', data: list })
}

/** 清空全部登录日志 */
export function cleanLogininfor() {
  return request({ url: '/monitor/logininfor/clean', method: 'post' })
}

/** 解锁指定用户名的登录锁定 */
export function unlockLogininfor(userName) {
  return request({
    url: `/monitor/logininfor/unlock/${encodeURIComponent(userName)}`,
    method: 'get'
  })
}

/** 导出登录日志（返回 blob） */
export function exportLogininfor(snapshot) {
  return request({
    url: '/monitor/logininfor/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}
