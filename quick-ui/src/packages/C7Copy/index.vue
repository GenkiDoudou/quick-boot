<template>
  <el-button
      v-if="resolvedMode === 'button'"
      v-bind="buttonAttrs"
      :disabled="disabled"
      @click="handleTrigger"
  >
    <slot>{{ buttonText }}</slot>
  </el-button>
  <el-button
      v-else-if="resolvedMode === 'icon'"
      v-bind="iconButtonAttrs"
      :disabled="disabled"
      @click="handleTrigger"
  >
    <el-icon class="c7-copy__icon">
      <DocumentCopy/>
    </el-icon>
    <span v-if="iconLabel" class="c7-copy__icon-label">{{ iconLabel }}</span>
  </el-button>
  <span
      v-else-if="resolvedMode === 'text'"
      :class="['c7-copy__text', { 'is-disabled': disabled }]"
      role="button"
      :tabindex="disabled ? -1 : 0"
      :aria-disabled="disabled ? 'true' : 'false'"
      @click="handleTrigger"
      @keydown.enter.prevent="handleTrigger"
      @keydown.space.prevent="handleTrigger"
  >{{ displayText }}</span>
  <div
      v-else
      :class="['c7-copy__slot-wrap', { 'is-disabled': disabled }]"
      role="button"
      :tabindex="disabled ? -1 : 0"
      :aria-disabled="disabled ? 'true' : 'false'"
      @click="handleTrigger"
      @keydown.enter.prevent="handleTrigger"
      @keydown.space.prevent="handleTrigger"
  >
    <slot/>
  </div>
</template>

<script setup>
import {computed, ref, useAttrs} from 'vue'
import {ElMessage} from 'element-plus'
import {DocumentCopy} from '@element-plus/icons-vue'

defineOptions({name: 'C7Copy', inheritAttrs: false})

/**
 * C7 复制：统一纯文本写入剪贴板（Clipboard API 优先，`execCommand('copy')` 降级）、提示与事件。
 *
 * **安全上下文**：仅在 `window.isSecureContext` 且存在 `navigator.clipboard.writeText` 时走 Clipboard；
 * 否则直接降级。`writeText` reject 时继续尝试降级。
 *
 * **重入**：一次复制流程（含 `getCopyText` 的 Promise）未完成前再次触发会被忽略。
 *
 * **disabled**：不调用 `beforeCopy`、不写剪贴板、不 emit、不提示（与 design 一致）。
 *
 * @emits copy(resolvedText) 已解析待写入串、尚未写入剪贴板
 * @emits success(resolvedText) 写入成功
 * @emits error(err: Error) 写入失败
 */
const props = defineProps({
  /** 待复制内容；`null`/`undefined` 视为 `''`；`Number` 会转为字符串 */
  text: {type: [String, Number, null], default: undefined},
  /**
   * 基于规范化后的 `text` 字符串生成最终写入内容；可返回 Promise。
   * 返回值非字符串时会 `String(...)`；`null`/`undefined` 结果视为 `''`。
   */
  getCopyText: {type: Function, default: undefined},
  /**
   * 展示形态：`button` | `icon` | `text` | `clickable`；`none` 为 `clickable` 的兼容别名。
   */
  mode: {type: String, default: 'button'},
  /** 为 true 时不触发复制与任何提示 */
  disabled: {type: Boolean, default: false},
  /** 未传入 `notify` 时是否使用默认 `ElMessage` */
  showMessage: {type: Boolean, default: true},
  /**
   * 自定义通知；传入后成功/失败 **仅**走此回调，不再调用 `ElMessage`。
   * @param {'success'|'error'|'info'|'warning'} type
   * @param {string} message
   */
  notify: {type: Function, default: undefined},
  /** 返回严格 `false` 时阻止复制（不调用 `getCopyText`、不写剪贴板、不 emit） */
  beforeCopy: {type: Function, default: undefined},
  /** 剪贴板写入成功后、`emit('success')` 之前调用 */
  afterCopy: {type: Function, default: undefined},
  /** `mode=button` 时默认插槽为空则显示的按钮文案 */
  buttonText: {type: String, default: '复制'},
  /** `mode=button` 时传给 `ElButton` 的 `type` */
  buttonType: {type: String, default: 'default'},
  /** `mode=icon` 时传给 `ElButton` 的 `type`（仅影响颜色语义，按钮为 link） */
  iconButtonType: {type: String, default: 'primary'},
  /** `mode=icon` 时图标右侧可选文案 */
  iconLabel: {type: String, default: ''},
  /** 默认复制成功提示（`notify` 未传入且 `showMessage` 为 true 时使用） */
  successMessage: {type: String, default: '复制成功'},
  /** 默认复制失败提示文案前缀（会拼接 `err.message`） */
  errorMessagePrefix: {type: String, default: '复制失败：'}
})

const emit = defineEmits(['copy', 'success', 'error'])

const attrs = useAttrs()

/** 复制流程进行中，用于忽略重入 */
const copyInFlight = ref(false)

const resolvedMode = computed(() => {
  const m = props.mode === 'none' ? 'clickable' : props.mode
  if (['button', 'icon', 'text', 'clickable'].includes(m)) return m
  return 'button'
})

/** 供 `getCopyText` 与展示的规范化 `text` */
const baseText = computed(() => normalizeText(props.text))

const displayText = computed(() => baseText.value)

const buttonAttrs = computed(() => ({
  type: props.buttonType,
  size: attrs.size,
  ...filterAttrsForElButton(attrs)
}))

const iconButtonAttrs = computed(() => ({
  link: true,
  type: props.iconButtonType,
  size: attrs.size,
  ...filterAttrsForElButton(attrs)
}))

/**
 * 从 `useAttrs()` 中挑出可安全透传到 `ElButton` 的常用属性（避免把自定义 prop 名透传进 EP）。
 * @param {Record<string, unknown>} raw
 * @returns {Record<string, unknown>}
 */
function filterAttrsForElButton(raw) {
  const allow = new Set(['size', 'plain', 'round', 'circle', 'loading', 'class', 'style'])
  const out = {}
  for (const k of Object.keys(raw)) {
    if (allow.has(k)) out[k] = raw[k]
  }
  return out
}

/**
 * @param {unknown} v
 * @returns {string}
 */
function normalizeText(v) {
  if (v === null || v === undefined) return ''
  if (typeof v === 'number' && Number.isFinite(v)) return String(v)
  return String(v)
}

/**
 * @param {unknown} v
 * @returns {string}
 */
function normalizeResolved(v) {
  if (v === null || v === undefined) return ''
  return String(v)
}

function canUseClipboardApi() {
  if (typeof window === 'undefined' || typeof navigator === 'undefined') return false
  if (!window.isSecureContext) return false
  const w = navigator.clipboard && typeof navigator.clipboard.writeText === 'function'
  return Boolean(w)
}

/**
 * 使用临时 textarea + `execCommand('copy')` 写入剪贴板。
 * @param {string} str
 * @returns {boolean} 是否认为成功（`execCommand` 返回 true）
 */
function copyViaExecCommand(str) {
  const ta = document.createElement('textarea')
  ta.value = str
  ta.setAttribute('readonly', '')
  ta.style.position = 'fixed'
  ta.style.left = '-9999px'
  ta.style.top = '0'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.focus()
  ta.select()
  let ok = false
  try {
    ok = document.execCommand('copy')
  } finally {
    document.body.removeChild(ta)
  }
  return ok
}

/**
 * @param {string} str
 * @returns {Promise<void>}
 */
async function writeToClipboard(str) {
  if (canUseClipboardApi()) {
    try {
      await navigator.clipboard.writeText(str)
      return
    } catch {
      /* 继续降级 */
    }
  }
  if (copyViaExecCommand(str)) return
  throw new Error('当前环境无法完成复制（Clipboard 与 execCommand 均失败）')
}

function notifySuccess(msg) {
  if (typeof props.notify === 'function') {
    props.notify('success', msg)
    return
  }
  if (props.showMessage) ElMessage.success(msg)
}

function notifyError(msg) {
  if (typeof props.notify === 'function') {
    props.notify('error', msg)
    return
  }
  if (props.showMessage) ElMessage.error(msg)
}

async function runCopy() {
  if (props.disabled || copyInFlight.value) return
  copyInFlight.value = true
  try {
    if (typeof props.beforeCopy === 'function') {
      const gate = props.beforeCopy()
      if (gate === false) return
    }

    let resolved = baseText.value
    if (typeof props.getCopyText === 'function') {
      const raw = props.getCopyText(baseText.value)
      const awaited = raw && typeof raw.then === 'function' ? await raw : raw
      resolved = normalizeResolved(awaited)
    }

    emit('copy', resolved)
    await writeToClipboard(resolved)

    if (typeof props.afterCopy === 'function') {
      props.afterCopy(resolved)
    }
    emit('success', resolved)
    notifySuccess(props.successMessage)
  } catch (e) {
    const err = e instanceof Error ? e : new Error(String(e))
    emit('error', err)
    notifyError(props.errorMessagePrefix + err.message)
  } finally {
    copyInFlight.value = false
  }
}

function handleTrigger() {
  if (props.disabled) return
  void runCopy()
}
</script>

<style scoped lang="scss">
.c7-copy__icon {
  vertical-align: middle;
}

.c7-copy__icon-label {
  margin-left: 4px;
  vertical-align: middle;
}

.c7-copy__text {
  cursor: pointer;
  color: var(--el-color-primary);
  text-decoration: underline;
  user-select: none;

  &:focus {
    outline: 2px solid var(--el-color-primary-light-5);
    outline-offset: 2px;
  }

  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.5;
    pointer-events: none;
  }
}

.c7-copy__slot-wrap {
  display: inline-block;
  cursor: pointer;
  user-select: none;

  &:focus {
    outline: 2px solid var(--el-color-primary-light-5);
    outline-offset: 2px;
  }

  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.5;
    pointer-events: none;
  }
}
</style>
