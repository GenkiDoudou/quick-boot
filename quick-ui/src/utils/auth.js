/**
 * Sa-Token 访问令牌 Cookie 读写。
 * Cookie 名 Admin-Token，与后端 Authorization: Bearer 约定一致（request.js 拦截器注入）。
 */
import Cookies from 'js-cookie'

/** 与后端 Sa-Token tokenName 对应的 Cookie 键 */
const TokenKey = 'Admin-Token'

/** @returns {string|undefined} 当前 token */
export function getToken() {
  return Cookies.get(TokenKey)
}

/** @param {string} token 登录成功后写入 */
export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

/** 退出登录时清除 */
export function removeToken() {
  return Cookies.remove(TokenKey)
}
