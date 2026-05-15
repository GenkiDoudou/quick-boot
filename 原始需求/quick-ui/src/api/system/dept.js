/**
 * 系统管理 — 部门 API（与后端 {@code /system/dept} 一致）。
 * 成功时 axios 拦截器 resolve 为 {@code { code, msg, data }} 整体对象。
 */
import request from '@/utils/request'

/**
 * 部门平铺列表。
 * @param {object} [query] 查询参数（如名称筛选，视后端实现）
 * @returns {Promise<{ code: number, msg: string, data: object[] }>}
 */
export function listDept(query) {
  return request({ url: '/system/dept/list', method: 'get', params: query })
}

/**
 * 部门树（维护视图），{@code data} 为树根数组，节点含 {@code children}。
 * @param {object} [query] 预留查询参数
 * @returns {Promise<{ code: number, msg: string, data: object[] }>}
 */
export function listTreeDept(query) {
  return request({ url: '/system/dept/treeselect', method: 'get', params: query })
}

/**
 * 部门详情。
 * @param {number|string} deptId 部门主键
 * @returns {Promise<{ code: number, msg: string, data: object }>}
 */
export function getDept(deptId) {
  return request({ url: '/system/dept/' + deptId, method: 'get' })
}

/**
 * 新增部门。
 * @param {object} data 请求体（deptName、parentId、orderNum、leader 等）
 * @returns {Promise<{ code: number, msg: string, data?: object }>}
 */
export function addDept(data) {
  return request({ url: '/system/dept', method: 'post', data })
}

/**
 * 修改部门（须含 deptId）。
 * @param {object} data 请求体
 * @returns {Promise<{ code: number, msg: string, data?: object }>}
 */
export function updateDept(data) {
  return request({ url: '/system/dept', method: 'put', data })
}

/**
 * 逻辑删除部门。
 * @param {number|string} deptId 部门主键
 * @returns {Promise<{ code: number, msg: string, data?: object }>}
 */
export function delDept(deptId) {
  return request({ url: '/system/dept/' + deptId, method: 'delete' })
}
