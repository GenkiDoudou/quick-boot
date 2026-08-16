/**
 * 操作日志 API：分页查询、详情、删除、清空、导出。
 */
import request from '@/utils/request'

/** 操作日志分页（POST，param 为筛选条件） */
export function pageOperlog(pageRequest) {
  return request({ url: '/monitor/operlog/page', method: 'post', data: pageRequest })
}

/** 操作日志详情 */
export function getOperlog(operId) {
  return request({ url: `/monitor/operlog/${encodeURIComponent(operId)}`, method: 'get' })
}

/** 批量删除操作日志 */
export function removeOperlog(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/monitor/operlog/remove', method: 'post', data: list })
}

/** 清空全部操作日志 */
export function cleanOperlog() {
  return request({ url: '/monitor/operlog/clean', method: 'post' })
}

/** 导出操作日志（返回 blob） */
export function exportOperlog(snapshot) {
  return request({
    url: '/monitor/operlog/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}
