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
      login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        const captchaId = userInfo.captchaId
        return new Promise((resolve, reject) => {
          login(username, password, captchaId).then(res => {
            setToken(res.data.access_token)
            this.token = res.data.access_token
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
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
