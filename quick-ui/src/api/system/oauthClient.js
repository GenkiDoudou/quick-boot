import request from '@/utils/request'

/** OAuth 客户端列表 */
export function listOauthClient(params) {
  return request({ url: '/system/oauthClient/list', method: 'get', params })
}

export function getOauthClient(clientId) {
  return request({ url: `/system/oauthClient/${clientId}`, method: 'get' })
}

/** 校验当前用户密码后返回明文 client_secret */
export function revealOauthClientSecret(clientId, password) {
  return request({
    url: `/system/oauthClient/${clientId}/revealSecret`,
    method: 'post',
    data: { password }
  })
}

export function addOauthClient(data) {
  return request({ url: '/system/oauthClient/create', method: 'post', data })
}

export function updateOauthClient(data) {
  return request({ url: '/system/oauthClient/update', method: 'post', data })
}

export function removeOauthClient(ids) {
  return request({ url: '/system/oauthClient/remove', method: 'post', data: { ids } })
}
