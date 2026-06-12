/**
 * 导航布局工具：navType 1/2/3 与侧栏、顶栏联动。
 *
 * | navType | 模式       | 侧栏来源              |
 * |---------|------------|-----------------------|
 * | 1       | 左侧菜单   | defaultRoutes         |
 * | 2       | 混合菜单   | 顶栏选中项的子菜单    |
 * | 3       | 顶部菜单   | defaultRoutes，侧栏隐藏 |
 */
import { constantRoutes } from '@/router'
import { isHttp } from '@/utils/validate'

const HIDE_LIST = ['/index', '/user/profile']

/**
 * @param {unknown} value
 * @returns {1|2|3}
 */
export function normalizeNavType(value) {
  const n = Number(value)
  if (n === 2 || n === 3) {
    return n
  }
  return 1
}

/**
 * @param {import('vue-router').RouteLocationNormalizedLoaded|string} routeOrPath
 * @returns {string}
 */
export function resolveMixTopPath(routeOrPath) {
  const path = typeof routeOrPath === 'string' ? routeOrPath : routeOrPath.path
  if (!path || HIDE_LIST.includes(path)) {
    return path || '/index'
  }
  if (path.lastIndexOf('/') > 0) {
    const tmpPath = path.substring(1)
    return '/' + tmpPath.substring(0, tmpPath.indexOf('/'))
  }
  return path
}

/**
 * @param {Array<Record<string, unknown>>} routers
 */
export function buildTopMenus(routers = []) {
  const menus = []
  routers.forEach((menu) => {
    if (menu?.hidden === true) {
      return
    }
    if (menu.path === '/' && menu.children?.length) {
      menus.push(menu.children[0])
    } else {
      menus.push(menu)
    }
  })
  return menus
}

/**
 * @param {Array<Record<string, unknown>>} routers
 */
export function buildChildrenMenus(routers = []) {
  const list = []
  routers.forEach((parent) => {
    if (!parent?.children?.length) {
      return
    }
    parent.children.forEach((child) => {
      const item = { ...child }
      if (item.parentPath === undefined) {
        if (parent.path === '/') {
          item.path = '/' + String(item.path || '').replace(/^\//, '')
        } else if (!isHttp(String(item.path || ''))) {
          const parentPath = String(parent.path || '').replace(/\/$/, '')
          const childPath = String(item.path || '').replace(/^\//, '')
          item.path = `${parentPath}/${childPath}`
        }
        item.parentPath = parent.path
      }
      list.push(item)
    })
  })
  return constantRoutes.concat(list)
}

/**
 * @param {string} key
 * @param {Array<Record<string, unknown>>} childrenMenus
 */
export function pickMixSidebarRoutes(key, childrenMenus = []) {
  return childrenMenus.filter((item) => key === item.parentPath || (key === 'index' && item.path === ''))
}

/**
 * 混合菜单：按当前顶栏路径刷新侧栏子菜单。
 */
export function applyMixSidebar({ activeTopPath, permissionStore, appStore, routers }) {
  const source = routers?.length ? routers : permissionStore.topbarRouters
  const childrenMenus = buildChildrenMenus(source)
  const routes = pickMixSidebarRoutes(activeTopPath, childrenMenus)
  if (routes.length > 0) {
    permissionStore.setSidebarRouters(routes)
    appStore.toggleSideBarHide(false)
  } else {
    appStore.toggleSideBarHide(true)
  }
  return routes
}

/**
 * 按导航模式同步侧栏与顶栏布局。
 * @param {{ navType: number, permissionStore: object, appStore: object, route?: object }} opts
 */
export function applyNavLayout({ navType, permissionStore, appStore, route }) {
  const type = normalizeNavType(navType)
  if (type === 1) {
    // 经典左侧树
    appStore.sidebar.opened = true
    appStore.toggleSideBarHide(false)
    if (permissionStore.defaultRoutes?.length) {
      permissionStore.setSidebarRouters(permissionStore.defaultRoutes)
    }
    return
  }
  if (type === 2) {
    // 混合：顶栏一级 + 侧栏二级
    appStore.sidebar.opened = true
    appStore.toggleSideBarHide(false)
    if (route && permissionStore.topbarRouters?.length) {
      applyMixSidebar({
        activeTopPath: resolveMixTopPath(route),
        permissionStore,
        appStore,
        routers: permissionStore.topbarRouters
      })
    }
    return
  }
  // navType=3：纯顶栏，隐藏侧栏
  appStore.sidebar.opened = false
  appStore.toggleSideBarHide(true)
  if (permissionStore.defaultRoutes?.length) {
    permissionStore.setSidebarRouters(permissionStore.defaultRoutes)
  }
}
