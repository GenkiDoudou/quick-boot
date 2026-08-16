/**
 * 日志中心 API：多来源（页面/接口/SQL/操作/登录）近似合并检索。
 */
import request from '@/utils/request'

/** 按时间、来源、关键字等条件查询日志中心列表 */
export function listLogHub(query) {
  return request({
    url: '/monitor/logHub/list',
    method: 'get',
    params: query
  })
}
