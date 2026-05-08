<template>
  <div class="c7-json-table-e2e">
    <h1 data-testid="e2e-title">C7JsonTable Dev</h1>
    <p class="hint">Mock 列表 / 导出 Blob / 批量删除；列设置持久化 key：<code>c7-json-table-e2e</code></p>
    <C7JsonTable
        data-testid="c7-json-table-main"
        :list-function="listFn"
        :table-columns="columns"
        :search-columns="searchCols"
        :default-search-param="defaultSearch"
        column-setting-key="c7-json-table-e2e"
        :delete-function="delFn"
        :export-function="expFn"
        :check-delete-success="(res) => res && res.code === 200"
        row-key="id"
        export-default-file-name="mock-export.txt"
        :export-loading-options="false"
        @delete-success="onDel"
        @export-success="onExp"
        @fetch-error="onErr"
    />
    <p data-testid="e2e-events">删除次数 {{ delCount }}，导出次数 {{ expCount }}，fetch-error {{ errCount }}</p>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const delCount = ref(0)
const expCount = ref(0)
const errCount = ref(0)

const defaultSearch = {kw: '', status: ''}

const searchCols = [
  {prop: 'kw', label: '关键字', type: 'input', order: 1, span: 8},
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    order: 2,
    span: 8,
    options: [
      {label: '全部', value: ''},
      {label: '启用', value: '1'},
      {label: '停用', value: '0'},
    ],
  },
]

const columns = [
  {prop: 'name', label: '名称', columnType: 'text', minWidth: 120},
  {prop: 'status', label: '状态', columnType: 'tag', width: 100, options: [{label: '启用', value: '1'}, {label: '停用', value: '0'}]},
]

/**
 * @param {Record<string, unknown>} params
 */
async function listFn(params) {
  await new Promise((r) => setTimeout(r, 120))
  const pageNum = Number(params.pageNum) || 1
  const pageSize = Number(params.pageSize) || 10
  const kw = String(params.kw || '')
  const all = Array.from({length: 37}, (_, i) => ({
    id: i + 1,
    name: `行-${i + 1}${kw ? `(${kw})` : ''}`,
    status: i % 2 === 0 ? '1' : '0',
  }))
  const start = (pageNum - 1) * pageSize
  const records = all.slice(start, start + pageSize)
  return {data: {records, total: all.length}}
}

/**
 * @param {unknown[]} ids
 */
async function delFn(ids) {
  await new Promise((r) => setTimeout(r, 50))
  void ids
  return {code: 200, msg: 'ok'}
}

/**
 * @param {Record<string, unknown>} snapshot
 */
async function expFn(snapshot) {
  await new Promise((r) => setTimeout(r, 80))
  const text = `kw=${snapshot.kw}&status=${snapshot.status}\n`
  return new Blob([text], {type: 'text/plain;charset=utf-8'})
}

function onDel() {
  delCount.value++
}

function onExp() {
  expCount.value++
}

function onErr() {
  errCount.value++
}
</script>

<style scoped>
.c7-json-table-e2e {
  padding: 16px;
}

.hint {
  color: #666;
  font-size: 13px;
}
</style>
