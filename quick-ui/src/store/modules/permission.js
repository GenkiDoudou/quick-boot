import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index.vue'
import ParentView from '@/components/ParentView/index.vue'
import InnerLink from '@/layout/components/InnerLink/index.vue'
import useSettingsStore from '@/store/modules/settings'
import useAppStore from '@/store/modules/app'
import { applyNavLayout, normalizeNavType } from '@/utils/navLayout'
import { defineStore } from 'pinia'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      routes: [],
      addRoutes: [],
      defaultRoutes: [],
      topbarRouters: [],
      sidebarRouters: []
    }),
    actions: {
      setRoutes(routes) {
        this.addRoutes = routes
        this.routes = constantRoutes.concat(routes)
      },
      setDefaultRoutes(routes) {
        this.defaultRoutes = constantRoutes.concat(routes)
      },
      setTopbarRoutes(routes) {
        this.topbarRouters = routes
      },
      setSidebarRouters(routes) {
        this.sidebarRouters = routes
      },
      generateRoutes(roles) {
        return new Promise(resolve => {
          // 向后端请求路由数据
          getRouters().then(res => {
            const normalized = (res.data || []).map(wrapRootInnerLinkRaw)
            const sdata = JSON.parse(JSON.stringify(normalized))
            const rdata = JSON.parse(JSON.stringify(normalized))
            const defaultData = JSON.parse(JSON.stringify(normalized))
            const sidebarRoutes = filterAsyncRouter(sdata)
            const rewriteRoutes = filterAsyncRouter(rdata, false, true)
            const defaultRoutes = filterAsyncRouter(defaultData)
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
            asyncRoutes.forEach(route => {
              router.addRoute(route)
            })
            this.setRoutes(rewriteRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(defaultRoutes)

            const settingsStore = useSettingsStore()
            const appStore = useAppStore()
            if (normalizeNavType(settingsStore.navType) === 2) {
              applyNavLayout({
                navType: 2,
                permissionStore: this,
                appStore,
                route: router.currentRoute.value
              })
            }

            resolve(rewriteRoutes)
          })
        })
      }
    }
  })

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    // 非顶级目录若误配 Layout，降级为 ParentView，避免嵌套 Layout 双侧栏
    if (route.component === 'Layout' && lastRouter && isLayoutRoute(lastRouter)) {
      route.component = 'ParentView'
    }
    if (route.component) {
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

/** 是否 Layout 路由（字符串阶段或已解析为布局组件） */
function isLayoutRoute(route) {
  if (!route) {
    return false
  }
  if (route.component === 'Layout') {
    return true
  }
  return route.component === Layout
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach((el) => {
    if (el.children && el.children.length) {
      if (el.component === 'ParentView' && !lastRouter) {
        el.children.forEach(c => {
          c.path = el.path + '/' + c.path
          if (c.children && c.children.length) {
            children = children.concat(filterChildren(c.children, c))
            return
          }
          children.push(c)
        })
        return
      }
    }
    if (lastRouter) {
      el.path = lastRouter.path + '/' + el.path
      if (el.children && el.children.length) {
        children = children.concat(filterChildren(el.children, el))
        return
      }
    }
    children.push(el)
  })
  return children
}

/** 顶级 InnerLink 包 Layout（JSON 字符串组件阶段，侧栏与路由注册共用） */
function wrapRootInnerLinkRaw(route) {
  if (route.children?.length) {
    return route
  }
  if (route.component !== 'InnerLink' || !route.meta?.link) {
    return route
  }
  return {
    path: route.path,
    component: 'Layout',
    meta: { title: route.meta.title, icon: route.meta.icon },
    children: [{
      path: '',
      component: 'InnerLink',
      name: route.name,
      meta: { ...route.meta }
    }]
  }
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  if (!view) {
    return undefined
  }
  const normalizedView = String(view).replace(/\\/g, '/')
  let res
  for (const path in modules) {
    const chunk = path.split(/views[/\\]/)[1]
    if (!chunk) {
      continue
    }
    const dir = chunk.split('.vue')[0].replace(/\\/g, '/')
    if (dir === normalizedView) {
      res = () => modules[path]()
    }
  }
  if (!res && import.meta.env.DEV) {
    console.warn(`[loadView] 未找到视图组件: ${normalizedView}`)
  }
  return res
}

export default usePermissionStore
