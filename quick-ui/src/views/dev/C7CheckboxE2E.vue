<template>
  <div class="c7-checkbox-e2e-wrap">
    <h1 data-testid="c7-checkbox-title">C7Checkbox Dev</h1>

    <section data-testid="tc-join">
      <h2>joinValue：逗号串回显 / 输出</h2>
      <c7-checkbox
          v-model="joinVal"
          join-value
          :data-list="fruitList"
          data-testid="c7-cb-join"
          @change="onJoinChange"
      />
      <pre data-testid="c7-cb-join-model">{{ modelText(joinVal) }}</pre>
      <p class="hint">上次 change（应为 string[]）：<code>{{ modelText(lastChangeJoin) }}</code></p>
    </section>

    <section data-testid="tc-autoload">
      <h2>autoLoad + 解析链（无静态 dataList）</h2>
      <c7-checkbox
          v-model="autoVal"
          join-value
          :fetch-data="fetchCityList"
          :auto-load="true"
          result-key="list"
          data-testid="c7-cb-autoload"
      />
      <pre data-testid="c7-cb-autoload-model">{{ modelText(autoVal) }}</pre>
    </section>

    <section data-testid="tc-static-priority">
      <h2>静态 dataList 优先（不发起 fetch）</h2>
      <c7-checkbox
          v-model="prioVal"
          :data-list="fruitList"
          :fetch-data="fetchNeverCalled"
          :auto-load="true"
          data-testid="c7-cb-prio"
      />
      <pre data-testid="c7-cb-prio-model">{{ modelText(prioVal) }}</pre>
    </section>

    <section data-testid="tc-select-all">
      <h2>全选 / 半选 + 禁用项不参与全选</h2>
      <c7-checkbox
          v-model="saVal"
          show-select-all
          :data-list="mixedList"
          data-testid="c7-cb-sa"
      />
      <pre data-testid="c7-cb-sa-model">{{ modelText(saVal) }}</pre>
    </section>

    <section data-testid="tc-max-select-all">
      <h2>max：可选中项数 &gt; max 时「全选」禁用</h2>
      <c7-checkbox
          v-model="maxVal"
          show-select-all
          :max="3"
          :data-list="tenList"
          data-testid="c7-cb-max"
      />
      <pre data-testid="c7-cb-max-model">{{ modelText(maxVal) }}</pre>
    </section>

    <section data-testid="tc-orphan">
      <h2>保留缺 option 的 value（joinValue）</h2>
      <p class="hint">选项仅 a/b；v-model 预置含 <code>x</code> 的逗号串。</p>
      <c7-checkbox
          v-model="orphanVal"
          join-value
          :data-list="abList"
          data-testid="c7-cb-orphan"
      />
      <pre data-testid="c7-cb-orphan-model">{{ modelText(orphanVal) }}</pre>
    </section>

    <section data-testid="tc-style-border">
      <h2>checkboxStyle=border</h2>
      <c7-checkbox
          v-model="borderVal"
          checkbox-style="border"
          :data-list="abList"
          data-testid="c7-cb-border"
      />
    </section>

    <section data-testid="tc-reload">
      <h2>reload()</h2>
      <c7-checkbox
          ref="reloadRef"
          v-model="reloadVal"
          join-value
          :fetch-data="fetchCityList"
          :auto-load="true"
          result-key="list"
          data-testid="c7-cb-reload"
      />
      <el-button type="primary" data-testid="c7-cb-reload-btn" @click="onReloadClick">reload()</el-button>
    </section>
  </div>
</template>

<script setup>
/** C7Checkbox 组件 E2E 联调页：覆盖 joinValue、全选与静态/异步选项。 */
import {ref} from 'vue'

const joinVal = ref('apple,banana')
const lastChangeJoin = ref([])
const autoVal = ref('')
const prioVal = ref([])
const saVal = ref(['a'])
const maxVal = ref([])
const orphanVal = ref('a,x,b')
const borderVal = ref([])
const reloadVal = ref('')

const reloadRef = ref(null)

function onJoinChange(v) {
  lastChangeJoin.value = v
}

const fruitList = [
  {label: '苹果', value: 'apple'},
  {label: '香蕉', value: 'banana'},
  {label: '橙子', value: 'orange'}
]

const mixedList = [
  {label: 'A', value: 'a'},
  {label: 'B', value: 'b', disabled: true},
  {label: 'C', value: 'c'}
]

const tenList = Array.from({length: 10}).map((_, i) => ({
  label: `项${i + 1}`,
  value: `v${i + 1}`
}))

const abList = [
  {label: 'A', value: 'a'},
  {label: 'B', value: 'b'}
]

function modelText(v) {
  return JSON.stringify(v)
}

async function fetchCityList() {
  return {
    data: {
      list: [
        {label: '上海', value: 'sh'},
        {label: '北京', value: 'bj'}
      ]
    }
  }
}

async function fetchNeverCalled() {
  // 若被调用说明「静态优先」失败
  // eslint-disable-next-line no-console
  console.error('fetchNeverCalled should not run when dataList is set')
  return {data: {list: []}}
}

function onReloadClick() {
  reloadRef.value?.reload?.()
}
</script>

<style scoped>
.c7-checkbox-e2e-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hint {
  color: #909399;
  font-size: 13px;
}
</style>
