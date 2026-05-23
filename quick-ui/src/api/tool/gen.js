import request from '@/utils/request'

/** 代码生成配置列表 */
export function listGenTable(query) {
  return request({
    url: '/tool/gen/list',
    method: 'get',
    params: query
  })
}

/** 数据库表候选 */
export function listDbTable(query) {
  return request({
    url: '/tool/gen/db/list',
    method: 'get',
    params: query
  })
}

/** 配置详情 */
export function getGenTable(tableId) {
  return request({
    url: '/tool/gen/' + tableId,
    method: 'get'
  })
}

/** 全局默认配置（参数设置 qc.gen.*） */
export function getGenDefaults() {
  return request({
    url: '/tool/gen/defaults',
    method: 'get'
  })
}

/** 保存配置 */
export function updateGenTable(data) {
  return request({
    url: '/tool/gen/update',
    method: 'post',
    data
  })
}

/** 导入表 */
export function importTable(tables) {
  return request({
    url: '/tool/gen/importTable',
    method: 'post',
    data: { tables }
  })
}

/** 建表 */
export function createTable(sql) {
  return request({
    url: '/tool/gen/createTable',
    method: 'post',
    data: { sql }
  })
}

/** 预览 */
export function previewTable(tableId) {
  return request({
    url: '/tool/gen/preview/' + tableId,
    method: 'get'
  })
}

/** 删除配置 */
export function delGenTable(tableId) {
  return request({
    url: '/tool/gen/remove/' + tableId,
    method: 'post'
  })
}

/** 同步库表 */
export function synchDb(tableName) {
  return request({
    url: '/tool/gen/synchDb/' + encodeURIComponent(tableName),
    method: 'post'
  })
}

/** 批量下载代码 Zip */
export function batchGenCode(tables) {
  return request({
    url: '/tool/gen/batchGenCode',
    method: 'post',
    data: { tables },
    responseType: 'blob'
  })
}

/** 自定义路径写盘 */
export function genCodeToPath(tableName) {
  return request({
    url: '/tool/gen/genCode/' + encodeURIComponent(tableName),
    method: 'post'
  })
}
