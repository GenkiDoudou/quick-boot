/**
 * 发布记录 API：分页、详情。
 */
import request from '@/utils/request'

/** 发布记录分页 */
export function pageDeployRecord(pageRequest) {
  return request({ url: '/monitor/deployRecord/page', method: 'post', data: pageRequest })
}

/** 发布记录详情 */
export function getDeployRecord(recordId) {
  return request({ url: `/monitor/deployRecord/${encodeURIComponent(recordId)}`, method: 'get' })
}
