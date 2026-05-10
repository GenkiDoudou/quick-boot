<template>
  <div class="c7-switch-e2e-wrap">
    <h1 data-testid="c7-switch-title">C7Switch Dev</h1>

    <section data-testid="tc-dict-priority">
      <h2>dictList 优先于 activeText / inactiveText</h2>
      <p>激活侧字典为「启用」，显式 activeText 为「开」——应显示字典。</p>
      <c7-switch
          v-model="dictVal"
          :active-value="1"
          :inactive-value="0"
          active-text="开"
          inactive-text="关"
          :dict-list="dictList"
          data-testid="c7-sw-dict"
      />
      <pre data-testid="c7-sw-dict-model">model: {{ dictVal }}</pre>
    </section>

    <section data-testid="tc-fallback-text">
      <h2>字典未命中时回退显式</h2>
      <c7-switch
          v-model="fbVal"
          :active-value="'on'"
          :inactive-value="'off'"
          active-text="开显式"
          inactive-text="关显式"
          :dict-list="[{ label: '仅开侧', value: 'on' }]"
          data-testid="c7-sw-fallback"
      />
      <pre data-testid="c7-sw-fb-model">model: {{ fbVal }}</pre>
    </section>

    <section data-testid="tc-before-silent">
      <h2>beforeChange 返回 false（完全静默，无 cancel）</h2>
      <c7-switch
          v-model="silentVal"
          :before-change="beforeAlwaysFalse"
          data-testid="c7-sw-silent"
          @cancel="onSilentCancel"
      />
      <pre data-testid="c7-sw-silent-log">cancel 次数: {{ silentCancelCount }}（应保持 0）</pre>
      <pre data-testid="c7-sw-silent-model">model: {{ silentVal }}</pre>
    </section>

    <section data-testid="tc-confirm-cancel">
      <h2>confirmMessage 取消 → cancel</h2>
      <c7-switch
          v-model="confirmVal"
          confirm-message="确定切换？"
          data-testid="c7-sw-confirm"
          @cancel="onConfirmCancel"
      />
      <pre data-testid="c7-sw-confirm-cancel">cancel 次数: {{ confirmCancelCount }}</pre>
      <pre data-testid="c7-sw-confirm-model">model: {{ confirmVal }}</pre>
    </section>

    <section data-testid="tc-async-fail">
      <h2>asyncChange reject（不切换、不 cancel）</h2>
      <c7-switch
          v-model="asyncVal"
          :async-change="asyncAlwaysFail"
          data-testid="c7-sw-async-fail"
          @cancel="onAsyncFailCancel"
      />
      <pre data-testid="c7-sw-async-cancel">cancel 次数: {{ asyncFailCancelCount }}（应保持 0）</pre>
      <pre data-testid="c7-sw-async-model">model: {{ asyncVal }}</pre>
    </section>

    <section data-testid="tc-sync-after">
      <h2>无 asyncChange + afterChange</h2>
      <c7-switch
          v-model="syncVal"
          :after-change="onAfterSync"
          data-testid="c7-sw-sync"
      />
      <pre data-testid="c7-sw-after-log">afterChange 最后一次 newVal: {{ afterLog }}</pre>
      <pre data-testid="c7-sw-sync-model">model: {{ syncVal }}</pre>
    </section>

    <section data-testid="tc-emit-order">
      <h2>emit 顺序（先 update:modelValue 再 change）</h2>
      <c7-switch
          v-model="orderVal"
          data-testid="c7-sw-order"
          @update:model-value="onOrderUpdate"
          @change="onOrderChange"
      />
      <pre data-testid="c7-sw-order-log">{{ orderLog.join('\n') }}</pre>
    </section>

    <section data-testid="tc-colors">
      <h2>activeColor / inactiveColor（CSS 变量）</h2>
      <c7-switch
          v-model="colorVal"
          active-color="#13ce66"
          inactive-color="#ff4949"
          data-testid="c7-sw-color"
      />
    </section>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const dictVal = ref(0)
const dictList = [
  {label: '启用', value: 1},
  {label: '停用', value: 0}
]

const fbVal = ref('off')

const silentVal = ref(false)
const silentCancelCount = ref(0)

const confirmVal = ref(false)
const confirmCancelCount = ref(0)

const asyncVal = ref(false)
const asyncFailCancelCount = ref(0)

const syncVal = ref(false)
const afterLog = ref('（无）')

const orderVal = ref(false)
const orderLog = ref([])

const colorVal = ref(true)

/**
 * @param {*} _newVal
 * @returns {false}
 */
function beforeAlwaysFalse(_newVal) {
  return false
}

function onSilentCancel() {
  silentCancelCount.value += 1
}

/**
 * @param {*} _newVal
 * @returns {Promise<never>}
 */
function asyncAlwaysFail(_newVal) {
  return Promise.reject(new Error('fail'))
}

/**
 * @param {*} v
 */
function onAfterSync(v) {
  afterLog.value = String(v)
}

function onConfirmCancel() {
  confirmCancelCount.value += 1
}

function onAsyncFailCancel() {
  asyncFailCancelCount.value += 1
}

let orderTick = 0

/**
 * @param {*} v
 */
function onOrderUpdate(v) {
  orderTick += 1
  const t = orderTick
  orderLog.value.push(`update:${v} (t=${t})`)
}

/**
 * @param {*} a
 * @param {*} b
 */
function onOrderChange(a, b) {
  const t = orderTick
  orderLog.value.push(`change:${a},${b} (t=${t})`)
}
</script>

<style scoped>
.c7-switch-e2e-wrap {
  padding: 16px;
  max-width: 720px;
}

section {
  margin-bottom: 28px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

pre {
  margin-top: 8px;
  font-size: 12px;
  white-space: pre-wrap;
}
</style>
