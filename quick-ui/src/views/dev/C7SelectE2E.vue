<template>
  <div class="c7-select-e2e-wrap">
    <h1 data-testid="c7-select-title">C7Select Dev</h1>

    <section data-testid="tc-static">
      <h2>静态 dataList</h2>
      <c7-select
          v-model="staticVal"
          :data-list="staticOptions"
          placeholder="请选择"
          clearable
          data-testid="c7-sel-static"
      />
      <pre data-testid="c7-sel-static-model">{{ modelText(staticVal) }}</pre>
    </section>

    <section data-testid="tc-autoload">
      <h2>autoLoad（非 remote）</h2>
      <c7-select
          v-model="autoVal"
          :fetch-data="fetchAutoList"
          :auto-load="true"
          result-key="list"
          placeholder="挂载后自动加载"
          clearable
          data-testid="c7-sel-autoload"
      />
    </section>

    <section data-testid="tc-remote">
      <h2>remote 搜索</h2>
      <p class="hint">首次展开：无 <code>query</code>；输入关键字：带 <code>query</code>。</p>
      <c7-select
          v-model="remoteVal"
          remote
          :fetch-data="fetchRemoteList"
          result-key="list"
          placeholder="展开后全量，输入过滤"
          clearable
          filterable
          data-testid="c7-sel-remote"
      />
    </section>

    <section data-testid="tc-separator">
      <h2>多选 + separator（保留缺 option 的 value）</h2>
      <p class="hint">当前选项仅 a/b；v-model 预置含 <code>x</code> 的逗号串，不应被静默删除。</p>
      <c7-select
          v-model="sepVal"
          multiple
          separator
          :data-list="abOptions"
          placeholder="多选逗号输出"
          clearable
          data-testid="c7-sel-sep"
      />
      <pre data-testid="c7-sel-sep-model">{{ modelText(sepVal) }}</pre>
    </section>

    <section data-testid="tc-reload">
      <h2>reload 暴露</h2>
      <c7-select
          ref="reloadRef"
          v-model="reloadVal"
          remote
          :fetch-data="fetchRemoteList"
          result-key="list"
          placeholder="remote 后点按钮 reload"
          clearable
          data-testid="c7-sel-reload"
      />
      <el-button type="primary" data-testid="c7-sel-reload-btn" @click="onReloadClick">reload()</el-button>
    </section>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const staticVal = ref('')
const autoVal = ref('')
const remoteVal = ref('')
/** 预置含不存在于 options 的 `x`，用于验收「不静默删除」 */
const sepVal = ref('a,x,b')
const reloadVal = ref('')

const staticOptions = [
  {label: '苹果', value: 'apple'},
  {label: '香蕉', value: 'banana'}
]

const abOptions = [
  {label: 'A', value: 'a'},
  {label: 'B', value: 'b'}
]

const allRemote = [
  {label: '上海', value: 'sh'},
  {label: '北京', value: 'bj'},
  {label: '南京', value: 'nj'}
]

const reloadRef = ref(null)

/**
 * @param {*} v
 * @returns {string}
 */
function modelText(v) {
  return typeof v === 'string' ? JSON.stringify(v) : JSON.stringify(v)
}

/**
 * @param {Record<string, *>} merged
 * @returns {Promise<{ data: { list: Array } }>}
 */
async function fetchAutoList(merged) {
  void merged
  await delay(30)
  return {
    data: {
      list: [
        {label: '异步项1', value: '1'},
        {label: '异步项2', value: '2'}
      ]
    }
  }
}

/**
 * @param {Record<string, *>} merged
 * @returns {Promise<{ data: { list: Array } }>}
 */
async function fetchRemoteList(merged) {
  await delay(40)
  const hasQuery = Object.prototype.hasOwnProperty.call(merged, 'query')
  if (hasQuery) {
    const q = String(merged.query ?? '').trim().toLowerCase()
    const list = q
        ? allRemote.filter((row) => String(row.label).toLowerCase().includes(q))
        : allRemote
    return {data: {list}}
  }
  return {data: {list: [...allRemote]}}
}

/**
 * @param {number} ms
 * @returns {Promise<void>}
 */
function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function onReloadClick() {
  reloadRef.value?.reload?.()
}
</script>

<style scoped>
.c7-select-e2e-wrap {
  padding: 16px 24px 48px;
  max-width: 720px;
}

section {
  margin-bottom: 28px;
}

h2 {
  font-size: 16px;
  margin: 0 0 8px;
}

.hint {
  font-size: 13px;
  color: #606266;
  margin: 0 0 8px;
}

pre {
  margin-top: 8px;
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}
</style>
