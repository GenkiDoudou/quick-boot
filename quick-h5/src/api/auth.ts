/**
 * 认证相关 API：登录与当前用户信息。
 */
import { request } from './http'

/** 登录成功返回的 token 信息 */
export type LoginToken = { accessToken: string; tokenName?: string }

/** /auth/me 当前登录用户概要 */
export type AuthMe = {
  userId?: string
  username?: string
  nickName?: string
  roles?: string[]
  permissions?: string[]
}

/** 用户名密码登录，不带 Bearer，使用 Client Basic */
export function login(username: string, password: string) {
  return request<LoginToken>({
    url: '/login',
    method: 'POST',
    data: { username, password },
    auth: false,
  })
}

/** 获取当前登录用户（需有效 token） */
export function fetchMe() {
  return request<AuthMe>({ url: '/auth/me', method: 'GET' })
}
