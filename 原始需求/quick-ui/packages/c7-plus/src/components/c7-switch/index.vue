<template>
  <el-switch
    v-model="switchValue"
    :active-value="activeValue"
    :inactive-value="inactiveValue"
    :active-text="computedActiveText"
    :inactive-text="computedInactiveText"
    :active-color="activeColor"
    :inactive-color="inactiveColor"
    :size="size"
    :disabled="disabled || loading"
    :loading="loading"
    :inline-prompt="inlinePrompt"
    :width="width"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, defineOptions } from 'vue'
import { ElMessageBox } from 'element-plus'
import { logger } from '../../utils/logger'

defineOptions({
  name: 'C7Switch'
})

/**
 * 字典选项接口
 */
interface DictOption {
  label: string
  value: string | number | boolean
}

/**
 * 组件属性接口
 */
interface Props {
  // v-model 绑定值
  modelValue?: string | number | boolean
  // 激活时的值
  activeValue?: string | number | boolean
  // 非激活时的值
  inactiveValue?: string | number | boolean
  // 激活时的文本
  activeText?: string
  // 非激活时的文本
  inactiveText?: string
  // 激活时的颜色
  activeColor?: string
  // 非激活时的颜色
  inactiveColor?: string
  // 尺寸
  size?: 'large' | 'default' | 'small'
  // 是否禁用
  disabled?: boolean
  // 是否显示加载状态
  loading?: boolean
  // 是否显示内联提示
  inlinePrompt?: boolean
  // 宽度
  width?: string | number
  // 字典数据列表（用于自动设置 activeText/inactiveText）
  dictList?: DictOption[]
  // 切换前确认提示（为空则不显示确认）
  confirmMessage?: string
  // 确认框标题
  confirmTitle?: string
  // 异步切换函数（返回 Promise，支持 loading 状态）
  asyncChange?: (value: string | number | boolean) => Promise<any>
  // 切换前的回调（返回 false 可阻止切换）
  beforeChange?: (value: string | number | boolean) => boolean | Promise<boolean>
  // 切换后的回调
  afterChange?: (value: string | number | boolean) => void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  activeValue: true,
  inactiveValue: false,
  activeText: '',
  inactiveText: '',
  activeColor: '#409EFF',
  inactiveColor: '#C0CCDA',
  size: 'default',
  disabled: false,
  loading: false,
  inlinePrompt: false,
  width: 40,
  dictList: () => [],
  confirmMessage: '',
  confirmTitle: '提示',
  asyncChange: undefined,
  beforeChange: undefined,
  afterChange: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | boolean]
  'change': [value: string | number | boolean, oldValue: string | number | boolean]
}>()

/**
 * 计算属性：激活时的文本（优先使用 dictList）
 */
const computedActiveText = computed(() => {
  if (props.activeText) {
    return props.activeText
  }
  
  if (props.dictList && props.dictList.length > 0) {
    const activeOption = props.dictList.find(item => item.value === props.activeValue)
    if (activeOption) {
      return activeOption.label
    }
  }
  
  return ''
})

/**
 * 计算属性：非激活时的文本（优先使用 dictList）
 */
const computedInactiveText = computed(() => {
  if (props.inactiveText) {
    return props.inactiveText
  }
  
  if (props.dictList && props.dictList.length > 0) {
    const inactiveOption = props.dictList.find(item => item.value === props.inactiveValue)
    if (inactiveOption) {
      return inactiveOption.label
    }
  }
  
  return ''
})

/**
 * 内部开关值
 */
const switchValue = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
  }
})

/**
 * 内部加载状态
 */
const internalLoading = ref(false)

/**
 * 计算属性：是否显示加载状态
 */
const loading = computed(() => props.loading || internalLoading.value)

/**
 * 处理切换
 */
const handleChange = async (value: string | number | boolean) => {
  const oldValue = props.modelValue
  
  try {
    // 切换前回调
    if (props.beforeChange) {
      const canChange = await props.beforeChange(value)
      if (canChange === false) {
        // 恢复原值
        emit('update:modelValue', oldValue)
        return
      }
    }
    
    // 确认提示
    if (props.confirmMessage) {
      try {
        await ElMessageBox.confirm(
          props.confirmMessage,
          props.confirmTitle,
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
      } catch {
        // 取消切换，恢复原值
        emit('update:modelValue', oldValue)
        return
      }
    }
    
    // 异步切换函数
    if (props.asyncChange) {
      internalLoading.value = true
      try {
        await props.asyncChange(value)
        emit('change', value, oldValue)
        
        // 切换后回调
        if (props.afterChange) {
          props.afterChange(value)
        }
      } catch (error: any) {
        logger.error('切换失败:', error)
        // 恢复原值
        emit('update:modelValue', oldValue)
        throw error
      } finally {
        internalLoading.value = false
      }
    } else {
      emit('change', value, oldValue)
      
      // 切换后回调
      if (props.afterChange) {
        props.afterChange(value)
      }
    }
  } catch (error: any) {
    logger.error('切换失败:', error)
    // 恢复原值
    emit('update:modelValue', oldValue)
  }
}
</script>

<style scoped>
/* 可在此处添加组件样式 */
</style>

