import request from '@/utils/request'

/**
 * 积木报表 / BI 目录（菜单绑定用）
 */

/** 报表列表 */
export function listJimuReports() {
  return request({ url: '/report/jimu/catalog/reports', method: 'get' })
}

/** BI 大屏列表 */
export function listJimuBiPages() {
  return request({ url: '/report/jimu/catalog/bi-pages', method: 'get' })
}
