<template>
  <div
      class="c7-switch"
      :style="colorCssVars"
  >
    <el-switch
        v-bind="forwardedAttrs"
        :model-value="modelValue"
        :active-value="activeValue"
        :inactive-value="inactiveValue"
        :disabled="disabled"
        :loading="loading || internalLoading"
        :active-text="mergedActiveText"
        :inactive-text="mergedInactiveText"
        :before-change="onElementBeforeChange"
        @update:model-value="onInnerUpdateModelValue"
    />
  </div>
</template>

<script setup>
import {computed, ref, useAttrs} from 'vue'
import {ElMessageBox} from 'element-plus'

defineOptions({name: 'C7Switch', inheritAttrs: false})

/**
 * C7 业务开关：基于 **Element Plus 2.10+** 的 **`ElSwitch`**，用其 **`before-change`**（实现内对应 **`beforeChange` prop** 勿混淆）串联
 * **`beforeChange` → 确认（`confirmFn` 优先）→ 可选 `asyncChange` → 提交值`**；对外 **`v-model`** 与 **`activeValue` / `inactiveValue`** 值体系一致。
 *
 * **字典文案**：**`dictList`** 与 **`activeValue` / `inactiveValue`** 使用 **`===`** 匹配 **`item.value`**；命中则用 **`label`**，否则回退 **`activeText` / `inactiveText`**。
 *
 * **确认弹窗**：仅当 **未**配置 **`confirmFn`** 且 **`confirmMessage`** 为 **非空字符串（`trim()` 后）** 时，使用 **`ElMessageBox.confirm(confirmMessage, ...)`**。
 *
 * **颜色**：若传入 **`activeColor` / `inactiveColor`**，通过根节点上的 **`--el-switch-on-color` / `--el-switch-off-color`** 注入（与 EP 主题变量一致，可被外层覆盖）。
 *
 * **并发**：自 **`beforeChange` 流水线开始**至 **`update:modelValue` 提交完成**期间 **`busy`** 为真，新的切换请求 **直接拒绝**（返回 **`false`**，不 **`emit('cancel')`**）。
 *
 * @emits update:modelValue 成功提交新值时（先于 **change**）
 * @emits change 载荷 **`(newVal, oldVal)`**，仅在成功提交后
 * @emits cancel 用户中止确认（含 **`confirmFn` 假值 / reject** 与 **`confirmMessage` 弹窗取消**）；**`beforeChange` 返回 `false`** 时 **不**触发
 */

const RESERVED_ATTR_KEYS = new Set([
  'modelValue',
  'activeValue',
  'inactiveValue',
  'activeText',
  'inactiveText',
  'dictList',
  'confirmFn',
  'confirmMessage',
  'asyncChange',
  'afterChange',
  'beforeChange',
  'disabled',
  'activeColor',
  'inactiveColor',
  'loading'
])

const props = defineProps({
  modelValue: {type: [Boolean, String, Number], required: true},
  activeValue: {type: [Boolean, String, Number], default: true},
  inactiveValue: {type: [Boolean, String, Number], default: false},
  activeText: {type: String, default: ''},
  inactiveText: {type: String, default: ''},
  /** @type {{ label: string, value: * }[]} */
  dictList: {type: Array, default: null},
  /** @returns {boolean|Promise<boolean>} */
  confirmFn: {type: Function, default: null},
  /** 非空（trim 后）且未配置 `confirmFn` 时弹出 `ElMessageBox.confirm` */
  confirmMessage: {type: String, default: ''},
  /**
   * 异步提交；**reject/抛错** 时不更新 `modelValue`、**不** `emit('cancel')`。
   * @param {*} newVal
   * @returns {void|Promise<void>}
   */
  asyncChange: {type: Function, default: null},
  /**
   * 值已成功提交后调用（含无 `asyncChange` 的同步路径）。
   * @param {*} newVal
   */
  afterChange: {type: Function, default: null},
  /**
   * 切换前钩子；返回严格 **`false`** 时 **完全静默**中止（不 `emit('cancel')`）。
   * @param {*} newVal 目标值
   * @returns {boolean|Promise<boolean|void>|void}
   */
  beforeChange: {type: Function, default: null},
  disabled: {type: Boolean, default: false},
  /** 映射到 `--el-switch-on-color` */
  activeColor: {type: String, default: ''},
  /** 映射到 `--el-switch-off-color` */
  inactiveColor: {type: String, default: ''},
  /** 与内部 `asyncChange` loading 合并传入 `ElSwitch` */
  loading: {type: Boolean, default: false}
})

const emit = defineEmits(['update:modelValue', 'change', 'cancel'])

const attrs = useAttrs()

const forwardedAttrs = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (RESERVED_ATTR_KEYS.has(key)) {
      continue
    }
    out[key] = attrs[key]
  }
  return out
})

const internalLoading = ref(false)
const busy = ref(false)
/** 提交前缓存 `oldVal`（含 `null` / `false` 等假值），未进入提交流时为 `null` */
const pendingCommit = ref(null)

const colorCssVars = computed(() => {
  const s = {}
  if (props.activeColor) {
    s['--el-switch-on-color'] = props.activeColor
  }
  if (props.inactiveColor) {
    s['--el-switch-off-color'] = props.inactiveColor
  }
  return s
})

/**
 * @param {unknown} sideValue
 * @param {string} fallback
 * @returns {string}
 */
function labelForSide(sideValue, fallback) {
  const list = props.dictList
  if (Array.isArray(list)) {
    const hit = list.find((d) => d && Object.prototype.hasOwnProperty.call(d, 'value') && d.value === sideValue)
    if (hit && hit.label != null) {
      return String(hit.label)
    }
  }
  return fallback
}

const mergedActiveText = computed(() => labelForSide(props.activeValue, props.activeText || ''))
const mergedInactiveText = computed(() => labelForSide(props.inactiveValue, props.inactiveText || ''))

/**
 * @returns {*}
 */
function nextToggleValue() {
  const cur = props.modelValue
  return cur === props.activeValue ? props.inactiveValue : props.activeValue
}

function resetBusy() {
  busy.value = false
}

/**
 * 供 `ElSwitch` 的 **`before-change`**：返回 **`Promise<boolean>`** 以满足 EP 类型约束。
 * @returns {Promise<boolean>}
 */
function onElementBeforeChange() {
  return runPipeline()
}

/**
 * @returns {Promise<boolean>}
 */
async function runPipeline() {
  if (props.disabled) {
    return false
  }
  if (busy.value || internalLoading.value) {
    return false
  }

  const oldVal = props.modelValue
  const newVal = nextToggleValue()
  busy.value = true

  try {
    if (typeof props.beforeChange === 'function') {
      const ok = await Promise.resolve(props.beforeChange(newVal))
      if (ok === false) {
        resetBusy()
        return false
      }
    }

    if (typeof props.confirmFn === 'function') {
      try {
        const ok = await Promise.resolve(props.confirmFn())
        if (!ok) {
          emit('cancel')
          resetBusy()
          return false
        }
      } catch {
        emit('cancel')
        resetBusy()
        return false
      }
    } else {
      const msg = typeof props.confirmMessage === 'string' ? props.confirmMessage.trim() : ''
      if (msg) {
        try {
          await ElMessageBox.confirm(msg, '系统提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
        } catch {
          emit('cancel')
          resetBusy()
          return false
        }
      }
    }

    if (typeof props.asyncChange === 'function') {
      internalLoading.value = true
      try {
        await Promise.resolve(props.asyncChange(newVal))
      } catch {
        internalLoading.value = false
        resetBusy()
        return false
      }
      internalLoading.value = false
    }

    pendingCommit.value = {oldVal}
    return true
  } catch {
    internalLoading.value = false
    resetBusy()
    return false
  }
}

/**
 * `ElSwitch` 在 **`before-change`**  resolve **`true`** 后提交值时触发。
 * @param {*} newVal
 */
function onInnerUpdateModelValue(newVal) {
  resetBusy()
  const pair = pendingCommit.value
  const oldVal = pair ? pair.oldVal : props.modelValue
  pendingCommit.value = null

  emit('update:modelValue', newVal)
  if (typeof props.afterChange === 'function') {
    props.afterChange(newVal)
  }
  emit('change', newVal, oldVal)
}
</script>
