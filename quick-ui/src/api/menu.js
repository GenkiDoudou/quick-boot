/**
 * 动态路由菜单 API。
 */
import request from '@/utils/request'

/** 获取当前用户可访问的路由树（正式路径） */
export function getMenuRoutes() {
  return request({
    url: '/api/menu/routes',
    method: 'get'
  })
}

/**
 * @deprecated 请使用 getMenuRoutes
 */
export const getRouters = getMenuRoutes
