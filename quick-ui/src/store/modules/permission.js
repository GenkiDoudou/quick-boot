/**
 * 权限路由 Store
 *
 * 职责：
 * 1. 调用 /getRouters 获取后端菜单树
 * 2. 将 component 字符串转为 Vue 懒加载组件（import.meta.glob）
 * 3. 区分 sidebarRoutes / rewriteRoutes / topbarRouters 供不同导航模式使用
 * 4. 混合导航模式（navType=2）下联动 appStore 切换侧栏
 *
 * 注意：业务 views 多数由 Flyway sys_menu 动态挂载，勿在 router/index.js 重复注册
 */
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

/** Vite 构建时收集 views 下全部 .vue，供 loadView 按路径字符串懒加载 */
const modules = import.meta.glob('./../../views/**/*.vue')

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      /** 完整路由表（constantRoutes + 动态路由） */
      routes: [],
      /** 本次登录新增的动态路由（供 router.addRoute） */
      addRoutes: [],
      /** 侧栏默认树（navType=1 时使用） */
      defaultRoutes: [],
      /** 顶栏一级菜单（navType=2/3 时使用） */
      topbarRouters: [],
      /** 当前侧栏展示的路由子集 */
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
      /**
       * 拉取并解析后端菜单，写入 store 并返回可注册路由。
       * @param {string[]} roles 预留参数（当前由后端菜单权限控制，前端未过滤）
       * @returns {Promise<Array>} rewriteRoutes，供 permission.js 中 router.addRoute
       */
      generateRoutes(roles) {
        return new Promise(resolve => {
          getRouters().then(res => {
            const normalized = (res.data || []).map(wrapRootInnerLinkRaw)
            // 深拷贝三份：侧栏树、扁平 rewrite、顶栏默认树各自独立变换
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

/**
 * 将后端路由 JSON 转为 Vue Router 可注册的路由对象。
 * @param {Array} asyncRouterMap 后端返回的路由数组
 * @param {Object|false} lastRouter 父路由（嵌套递归时传入）
 * @param {boolean} type true 时对 children 做扁平化（rewriteRoutes 注册用）
 */
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

/**
 * 扁平化 ParentView 下的多级 children，拼接完整 path 供 router 注册。
 * 若依菜单中「目录 + 多级菜单」经此处理后变为一级 children 列表。
 */
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

/**
 * 顶级 InnerLink 外链菜单包一层 Layout，使侧栏与路由注册结构一致。
 * 后端若直接返回 component=InnerLink 的顶级项，需在此包装为 Layout > InnerLink。
 */
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

/**
 * 过滤 router/index.js 中 dynamicRoutes：按 permissions 或 roles 校验本地权限。
 * 当前 dynamicRoutes 为空数组，逻辑保留供未来本地静态路由扩展。
 */
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

/**
 * 将后端 component 路径（如 system/user/index）匹配为 Vite 懒加载函数。
 * 匹配规则：views/ 之后的路径去掉 .vue 后缀，与 normalizedView 全等。
 * @param {string} view 后端菜单 component 字段
 * @returns {(() => Promise) | undefined} 懒加载组件工厂，未找到时 DEV 下 console.warn
 */
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
