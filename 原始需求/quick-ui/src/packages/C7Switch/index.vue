<template>
  <el-switch
    :model-value="innerValue"
    :active-value="activeValue"
    :inactive-value="inactiveValue"
    :active-text="resolvedActiveText"
    :inactive-text="resolvedInactiveText"
    :size="size"
    :disabled="disabled || internalLoading"
    :loading="internalLoading"
    :inline-prompt="inlinePrompt"
    :width="width"
    :style="switchStyle"
    @click.prevent="handleClick"
  />
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessageBox } from 'element-plus'

defineOptions({ name: 'C7Switch' })

const props = defineProps({
  // ── 值绑定 ──
  modelValue: {
    type: [String, Number, Boolean],
    default: false
  },
  activeValue: {
    type: [String, Number, Boolean],
    default: true
  },
  inactiveValue: {
    type: [String, Number, Boolean],
    default: false
  },
  // ── 文字 ──
  activeText: {
    type: String,
    default: undefined
  },
  inactiveText: {
    type: String,
    default: undefined
  },
  // ── 颜色（保留兼容，建议用 CSS 变量）──
  activeColor: {
    type: String,
    default: undefined
  },
  inactiveColor: {
    type: String,
    default: undefined
  },
  // ── 样式与状态 ──
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['large', 'default', 'small'].includes(v)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  inlinePrompt: {
    type: Boolean,
    default: false
  },
  width: {
    type: [String, Number],
    default: undefined
  },
  // ── 字典映射 ──
  /**
   * 自动从字典映射 activeText / inactiveText
   * [{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]
   */
  dictList: {
    type: Array,
    default: null
  },
  // ── 确认 ──
  /** 确认框消息文字（使用内置 ElMessageBox） */
  confirmMessage: {
    type: String,
    default: ''
  },
  /** 确认框标题 */
  confirmTitle: {
    type: String,
    default: '提示'
  },
  /**
   * 自定义确认函数，优先级高于 confirmMessage
   * (newVal, oldVal) => Promise<boolean> | boolean
   * 返回 false 则取消切换
   */
  confirmFn: {
    type: Function,
    default: null
  },
  // ── 钩子 ──
  /**
   * 切换前钩子，返回 false 则取消
   * (value: any) => boolean | Promise<boolean>
   */
  beforeChange: {
    type: Function,
    default: null
  },
  /**
   * 异步切换函数，切换失败自动回滚
   * (value: any) => Promise<any>
   */
  asyncChange: {
    type: Function,
    default: null
  },
  /**
   * 切换成功后钩子
   * (value: any) => void
   */
  afterChange: {
    type: Function,
    default: null
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'cancel'
])

// ── 内部 loading 状态 ──
const internalLoading = ref(false)

// ── 内部值（跟随外部 modelValue）──
const innerValue = computed(() => props.modelValue)

// ── 计算新值 / 旧值 ──
function getNewValue() {
  return innerValue.value === props.activeValue
    ? props.inactiveValue
    : props.activeValue
}

// ── dictList 自动映射 activeText / inactiveText ──
const resolvedActiveText = computed(() => {
  if (props.activeText !== undefined) return props.activeText
  if (props.dictList?.length) {
    const item = props.dictList.find((d) => d.value === props.activeValue)
    return item?.label ?? undefined
  }
  return undefined
})

const resolvedInactiveText = computed(() => {
  if (props.inactiveText !== undefined) return props.inactiveText
  if (props.dictList?.length) {
    const item = props.dictList.find((d) => d.value === props.inactiveValue)
    return item?.label ?? undefined
  }
  return undefined
})

// ── 颜色 CSS 变量（兼容 activeColor / inactiveColor）──
const switchStyle = computed(() => {
  const style = {}
  if (props.activeColor) style['--el-switch-on-color'] = props.activeColor
  if (props.inactiveColor) style['--el-switch-off-color'] = props.inactiveColor
  return style
})

// ── 点击处理（执行顺序：beforeChange → confirm → asyncChange → afterChange）──
async function handleClick() {
  if (props.disabled || internalLoading.value) return

  const oldValue = innerValue.value
  const newValue = getNewValue()

  // 1. beforeChange 钩子
  if (props.beforeChange) {
    const result = await props.beforeChange(newValue)
    if (result === false) {
      emit('cancel')
      return
    }
  }

  // 2. 确认框（confirmFn 优先级高于 confirmMessage）
  if (props.confirmFn) {
    const confirmed = await props.confirmFn(newValue, oldValue)
    if (confirmed === false) {
      emit('cancel')
      return
    }
  } else if (props.confirmMessage) {
    try {
      await ElMessageBox.confirm(props.confirmMessage, props.confirmTitle, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      emit('cancel')
      return
    }
  }

  // 3. asyncChange —— 有则异步执行，失败回滚
  if (props.asyncChange) {
    internalLoading.value = true
    try {
      await props.asyncChange(newValue)
      // 成功：更新值
      emit('update:modelValue', newValue)
      emit('change', newValue, oldValue)
      // 4. afterChange 钩子
      props.afterChange?.(newValue)
    } catch {
      // 失败：回滚（保持原值，不 emit）
    } finally {
      internalLoading.value = false
    }
    return
  }

  // 无 asyncChange：直接更新
  emit('update:modelValue', newValue)
  emit('change', newValue, oldValue)
  props.afterChange?.(newValue)
}
</script>
