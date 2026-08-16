/**
 * 动态路由菜单 API。
 */
import request from '@/utils/request'

/** 获取当前用户可访问的路由树（后端根据权限过滤） */
export const getRouters = () => {
  return request({
    url: '/getRouters',
    method: 'get'
  })
}
