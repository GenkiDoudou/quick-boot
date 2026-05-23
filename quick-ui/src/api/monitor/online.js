import request from '@/utils/request'

/** 在线用户分页列表 */
export function listOnline(query) {
  return request({
    url: '/monitor/online/list',
    method: 'get',
    params: query
  })
}

/** 强退指定会话 */
export function forceLogout(tokenId) {
  return request({
    url: '/monitor/online/forceLogout',
    method: 'post',
    data: { tokenId }
  })
}
