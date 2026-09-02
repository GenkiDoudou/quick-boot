/**
 * 全局路由守卫
 *
 * - 集成 NProgress 进度条（beforeEach 启动，afterEach 结束）
 * - 白名单（settings.permissionWhiteList）无需 token 即可访问
 * - 有 token 但 roles 为空时：先 getInfo → generateRoutes → addRoute → replace 重进当前页
 * - 无 token 且非白名单：跳转 /login?redirect=
 *
 * 注意：动态路由仅在首次 getInfo 成功后注入，避免重复 addRoute
 */
import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp } from '@/utils/validate'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { getMenuRoutes } from '@/api/menu'
import defaultSetting from '@/settings'

NProgress.configure({ showSpinner: false })

const { permissionWhiteList } = defaultSetting
/** 无需登录即可访问的路径前缀列表 */
const whiteList = permissionWhiteList

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    // 已登录：同步页面标题
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    if (to.path === '/login') {
      // 已登录访问登录页 → 重定向首页
      next({ path: '/' })
      NProgress.done()
    } else if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        // 首次进入：并行拉用户信息与菜单，减少登录后白屏等待
        Promise.all([
          useUserStore().getInfo(),
          getMenuRoutes()
        ]).then(([, routersRes]) => {
          usePermissionStore().buildRoutesFromMenuData(routersRes.data || []).then(accessRoutes => {
            accessRoutes.forEach(route => {
              // 外链菜单（http）不注册到 vue-router
              if (!isHttp(route.path)) {
                router.addRoute(route)
              }
            })
            next({ ...to, replace: true })
          })
        }).catch(err => {
          useUserStore().logOut().finally(() => {
            ElMessage.error(err)
            next('/login')
          })
        })
      } else {
        next()
      }
    }
  } else {
    // 未登录
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
