import request from '@/utils/request'

export function listOauthProvider(params) {
  return request({ url: '/system/oauthProvider/list', method: 'get', params })
}

export function getOauthProvider(providerCode) {
  return request({ url: `/system/oauthProvider/${providerCode}`, method: 'get' })
}

export function addOauthProvider(data) {
  return request({ url: '/system/oauthProvider/create', method: 'post', data })
}

export function updateOauthProvider(data) {
  return request({ url: '/system/oauthProvider/update', method: 'post', data })
}

export function removeOauthProvider(ids) {
  return request({ url: '/system/oauthProvider/remove', method: 'post', data: { ids } })
}
