/**
 * 通知公告管理 API。
 * 封装 `/system/notice` 列表、详情及 CRUD（读 GET / 写 POST）。
 */
import request from '@/utils/request'

/**
 * 分页查询通知公告。
 * @param {Record<string, any>} params 查询参数（含 pageNum、pageSize）
 * @returns {Promise<any>}
 */
export function listNotice(params) {
  return request({ url: '/system/notice/list', method: 'get', params })
}

/**
 * 查询通知公告详情。
 * @param {number|string} noticeId 公告主键
 * @returns {Promise<any>}
 */
export function getNotice(noticeId) {
  return request({ url: '/system/notice/' + noticeId, method: 'get' })
}

/**
 * 新增通知公告。
 * @param {Record<string, any>} data 表单数据
 * @returns {Promise<any>}
 */
export function addNotice(data) {
  return request({ url: '/system/notice/create', method: 'post', data })
}

/**
 * 修改通知公告。
 * @param {Record<string, any>} data 表单数据（含 noticeId）
 * @returns {Promise<any>}
 */
export function updateNotice(data) {
  return request({ url: '/system/notice/update', method: 'post', data })
}

/**
 * 批量删除通知公告。
 * @param {Array<number|string>} noticeIds 主键集合
 * @returns {Promise<any>}
 */
export function removeNotice(noticeIds) {
  return request({ url: '/system/notice/remove', method: 'post', data: noticeIds })
}
