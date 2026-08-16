/**
 * OAuth 授权 API：登录页外部 IdP 列表、授权跳转等（/oauth）。
 */
import request from '@/utils/request'

/** 登录页可用外部 IdP */
export function listLoginProviders() {
  return request({ url: '/oauth/login/providers', method: 'get' })
}
