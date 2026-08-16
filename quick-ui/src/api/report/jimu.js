/**
 * 积木报表 API：报表与 BI 大屏目录列表，供菜单绑定（/report/jimu）。
 */
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
