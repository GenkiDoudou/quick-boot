/**
 * 用户会话 Store：token、基本信息、角色与权限字符。
 *
 * - login：调用 /login，写入 Cookie token（见 utils/auth.js）
 * - getInfo：/getInfo 映射 userId/userName/avatar；roles 为空时兜底 ROLE_DEFAULT
 * - avatar：空则默认头像；否则拼接 VITE_APP_BASE_API + 相对路径
 * - permissions：供 v-hasPermi、$auth 校验（按角色菜单授权汇总）
 */
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { clearSessionId } from '@/monitor/sessionContext'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      avatar: '',
      roles: [],
      permissions: []
    }),
    actions: {
      /**
       * 登录并持久化 token。
       * @param {{ username: string, password: string, uuid?: string, captchaId?: string }} userInfo
       */
      login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        const uuid = userInfo.uuid || userInfo.captchaId
        return new Promise((resolve, reject) => {
          login(username, password, uuid).then(res => {
            const token = res.data.accessToken || res.data.access_token
            setToken(token)
            this.token = token
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      /**
       * 拉取当前用户信息与权限；路由守卫在 roles 为空时调用。
       * @returns {Promise} 原始 /getInfo 响应
       */
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then(res => {
            const user = res.data.user
            const avatar = (user.avatar == "" || user.avatar == null) ? defAva : import.meta.env.VITE_APP_BASE_API + user.avatar;

            if (res.data.roles && res.data.roles.length > 0) {
              this.roles = res.data.roles
            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            this.permissions = res.data.permissions || []
            this.id = user.userId
            this.name = user.userName
            this.avatar = avatar
            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },
      /**
       * 退出登录：始终请求注销接口，但无论成功失败都清空本地 token 与权限态，
       * 避免接口失败导致 Promise 一直 reject、上层无法完成跳转或重复触发。
       */
      logOut() {
        return logout()
          .catch(() => {
            /* 忽略服务端注销失败，仍执行本地清理 */
          })
          .finally(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            removeToken()
            clearSessionId()
          })
      }
    }
  })

export default useUserStore
