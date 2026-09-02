/**
 * 代码生成 API：表元数据查询、配置编辑、预览与生成下载（/tool/gen）。
 */
import request from '@/utils/request'
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/tool/gen')

/** 代码生成配置分页（POST page）。 */
export const pageGenTable = crud.page

/** 代码生成配置列表（兼容 C7JsonTable 扁平 query）。 */
export function listGenTable(query) {
  return crud.page(toPageRequest(query))
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
export const getGenTable = crud.get

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
