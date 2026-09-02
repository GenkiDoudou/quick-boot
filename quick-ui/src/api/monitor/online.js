/**
 * 在线用户监控 API（前缀 {@code /monitor/online}）。
 */
import request from '@/utils/request'
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/monitor/online')

/** 在线用户分页（POST page）。 */
export const pageOnline = crud.page

/** 在线用户分页列表（兼容 C7JsonTable）。 */
export function listOnline(query) {
  return crud.page(toPageRequest(query))
}

/** 强退指定会话 */
export function forceLogout(tokenId) {
  return request({
    url: '/monitor/online/forceLogout',
    method: 'post',
    data: { tokenId }
  })
}
