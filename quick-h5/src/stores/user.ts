/**
 * 当前登录用户状态：token、基本信息、角色与权限；登录/登出与 RUM、字典缓存联动。
 */
import { defineStore } from 'pinia'
import { fetchMe, login as loginApi } from '@/api/auth'
import { clearStoredToken, getStoredToken, setStoredToken } from '@/api/http'
import { clearSessionId, setRumUin } from '@/monitor/liteRum'
import { useDictStore } from './dict'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '' as string,
    userId: '' as string,
    username: '' as string,
    nickName: '' as string,
    /** 角色键列表（来自 /auth/me） */
    roles: [] as string[],
    /** 权限字符列表（来自 /auth/me，供 hasPermi） */
    permissions: [] as string[],
  }),
  getters: {
    /** 是否持有有效 token（不校验过期，由接口 401 处理） */
    isLoggedIn: (s) => !!s.token,
  },
  actions: {
    /** 将 /auth/me 结果写入 store */
    applyMe(me: {
      userId?: string
      username?: string
      nickName?: string
      roles?: string[]
      permissions?: string[]
    }, fallbackUsername?: string) {
      this.userId = me.userId || ''
      this.username = me.username || fallbackUsername || ''
      this.nickName = me.nickName || ''
      this.roles = Array.isArray(me.roles) ? me.roles.map(String) : []
      this.permissions = Array.isArray(me.permissions) ? me.permissions.map(String) : []
      if (this.username) {
        setRumUin(this.username)
      }
    },

    /** 有 token 时拉取 /auth/me 刷新身份与权限 */
    async refreshMe() {
      if (!this.token && !getStoredToken()) {
        return
      }
      if (!this.token) {
        this.token = getStoredToken()
      }
      const me = await fetchMe()
      this.applyMe(me)
    },

    /** 登录：写 token、拉 /auth/me、同步 RUM 用户标识 */
    async login(username: string, password: string) {
      const tokenVo = await loginApi(username, password)
      this.token = tokenVo.accessToken
      setStoredToken(tokenVo.accessToken)
      const me = await fetchMe()
      this.applyMe(me, username)
    },

    /** 登出：清本地状态、token、RUM 会话与字典缓存 */
    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.nickName = ''
      this.roles = []
      this.permissions = []
      clearStoredToken()
      clearSessionId()
      setRumUin('')
      useDictStore().cleanDict()
    },

    /** 应用启动时从 storage 恢复 token 到 store */
    hydrateFromStorage() {
      this.token = getStoredToken()
    },
  },
  persist: {
    paths: ['token', 'userId', 'username', 'nickName', 'roles', 'permissions'],
  },
})
