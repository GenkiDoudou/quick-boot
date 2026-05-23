import request from '@/utils/request'

/** 登录页可用外部 IdP */
export function listLoginProviders() {
  return request({ url: '/oauth/login/providers', method: 'get' })
}
