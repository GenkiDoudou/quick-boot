<template>
  <div class="c7-radio-e2e-wrap">
    <h1 data-testid="c7-radio-title">C7Radio Dev</h1>

    <section data-testid="tc-static">
      <h2>静态 dataList</h2>
      <c7-radio
          v-model="staticVal"
          :data-list="fruitList"
          data-testid="c7-rad-static"
      />
      <pre data-testid="c7-rad-static-model">{{ modelText(staticVal) }}</pre>
    </section>

    <section data-testid="tc-autoload">
      <h2>autoLoad + resultKey</h2>
      <c7-radio
          v-model="autoVal"
          :fetch-data="fetchDict"
          result-key="list"
          data-testid="c7-rad-autoload"
      />
      <pre data-testid="c7-rad-autoload-model">{{ modelText(autoVal) }}</pre>
    </section>

    <section data-testid="tc-priority">
      <h2>静态 dataList 优先（不发起 fetch）</h2>
      <c7-radio
          v-model="prioVal"
          :data-list="fruitList"
          :fetch-data="fetchNever"
          data-testid="c7-rad-prio"
      />
    </section>

    <section data-testid="tc-style-default">
      <h2>radioStyle=default</h2>
      <c7-radio v-model="s1" radio-style="default" :data-list="abList" data-testid="c7-rad-def"/>
    </section>

    <section data-testid="tc-style-button">
      <h2>radioStyle=button</h2>
      <c7-radio v-model="s2" radio-style="button" :data-list="abList" data-testid="c7-rad-btn"/>
    </section>

    <section data-testid="tc-style-border">
      <h2>radioStyle=border</h2>
      <c7-radio v-model="s3" radio-style="border" :data-list="abList" data-testid="c7-rad-border"/>
    </section>

    <section data-testid="tc-empty-text">
      <h2>空列表 + emptyDisplay=text</h2>
      <c7-radio
          v-model="emptyVal"
          :fetch-data="fetchEmpty"
          empty-display="text"
          empty-text="暂无可选项"
          data-testid="c7-rad-empty"
      />
    </section>

    <section data-testid="tc-fetch-fail">
      <h2>fetch 失败 keep-last（第二次点 reload 成功）</h2>
      <c7-radio
          ref="failRef"
          v-model="failVal"
          :fetch-data="fetchFailThenOk"
          :auto-load="true"
          fetch-error-behavior="keep-last"
          data-testid="c7-rad-fail"
      />
      <el-button type="primary" data-testid="c7-rad-fail-reload" @click="onFailReload">reload()</el-button>
    </section>

    <section data-testid="tc-form">
      <h2>el-form-item + rules（必选）</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="单选" prop="r">
          <c7-radio v-model="form.r" :data-list="abList" data-testid="c7-rad-form"/>
        </el-form-item>
        <el-button type="primary" data-testid="c7-rad-form-submit" @click="onFormSubmit">校验</el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup>
/** C7Radio 组件 E2E 联调页：覆盖静态/异步、空态与 radioStyle 形态。 */
import {ref} from 'vue'

const staticVal = ref('')
const autoVal = ref('')
const prioVal = ref('')
const s1 = ref('a')
const s2 = ref('a')
const s3 = ref('a')
const emptyVal = ref('')
const failVal = ref('')

const fruitList = [
  {label: '苹果', value: 'apple'},
  {label: '香蕉', value: 'banana'}
]

const abList = [
  {label: 'A', value: 'a'},
  {label: 'B', value: 'b'}
]

const failRef = ref(null)
const formRef = ref(null)
const form = ref({r: ''})
const rules = {r: [{required: true, message: '请选择', trigger: 'change'}]}

let failCalls = 0

/**
 * @param {*} v
 * @returns {string}
 */
function modelText(v) {
  return JSON.stringify(v)
}

/**
 * @param {Record<string, *>} merged
 * @returns {Promise<{ data: { list: Array } }>}
 */
async function fetchDict(merged) {
  void merged
  await delay(20)
  return {
    data: {
      list: [
        {label: '字典甲', value: 'ja'},
        {label: '字典乙', value: 'jb'}
      ]
    }
  }
}

/**
 * @returns {Promise<never>}
 */
async function fetchNever() {
  throw new Error('fetchNever 不应被调用')
}

/**
 * @param {Record<string, *>} merged
 * @returns {Promise<{ data: { list: Array } }>}
 */
async function fetchEmpty(merged) {
  void merged
  await delay(10)
  return {data: {list: []}}
}

/**
 * @param {Record<string, *>} merged
 * @returns {Promise<{ data: { list: Array } }>}
 */
async function fetchFailThenOk(merged) {
  void merged
  failCalls++
  await delay(15)
  if (failCalls === 1) {
    throw new Error('首次失败')
  }
  return {data: {list: [{label: '恢复项', value: 'ok'}]}}
}

/**
 * @param {number} ms
 * @returns {Promise<void>}
 */
function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function onFailReload() {
  failRef.value?.reload?.()
}

async function onFormSubmit() {
  try {
    await formRef.value?.validate()
    ElMessage.success('通过')
  } catch {
    ElMessage.warning('未通过')
  }
}
</script>

<style scoped>
.c7-radio-e2e-wrap {
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

pre {
  margin-top: 8px;
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}
</style>
