<template>
  <el-button 
    v-bind="$attrs" 
    :icon="computedIcon" 
    :type="computedType" 
    :plain="computedPlain"
    :loading="loading"
    :size="computedSize"
    @click="handleClick"
  >
    <slot>{{ computedLabel }}</slot>
  </el-button>
</template>

<script setup lang="ts">
import { ref, computed, defineOptions } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDebounce } from '../../composables/useDebounce'
import { handleError } from '../../utils/errorHandler'
import { logger } from '../../utils/logger'
import { DEFAULT_DEBOUNCE_TIME } from '../../constants'
import { getConfig } from '../../config'

defineOptions({
  name: 'C7Button'
})

/**
 * 按钮类型
 */
type ButtonType = 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default'

/**
 * 按钮预设类型
 */
type BtnType = 'add' | 'edit' | 'delete' | 'query' | 'refresh' | 'upload' | 'download' | 'submit' | 'cancel'

/**
 * 按钮大小
 */
type Size = 'large' | 'default' | 'small'

/**
 * 按钮配置接口
 */
interface ButtonConfig {
  icon: string
  label: string
  type: ButtonType
  plain: boolean
}

/**
 * 组件属性接口
 */
interface Props {
  type?: ButtonType                     // 按钮类型
  btnType?: BtnType | string            // 按钮预设类型
  plain?: boolean                       // 是否朴素按钮
  label?: string                        // 按钮文本
  clickFunction?: () => Promise<any> | any  // 点击回调函数
  confirm?: boolean                     // 是否需要确认
  confirmMessage?: string               // 确认提示信息
  isSuccessCallback?: boolean           // 是否触发成功回调
  successMessage?: string               // 成功提示信息
  isErrorCallback?: boolean             // 是否触发错误回调
  checkSuccess?: (res: any) => boolean  // 检查是否成功的函数
  errorMessageType?: string             // 错误信息类型
  errorMessage?: string                 // 错误信息字段名
  validate?: boolean                    // 是否需要表单验证
  validateRef?: { validate: () => Promise<void> } | null  // 表单引用
  size?: Size                           // 按钮大小
  showErrorToast?: boolean              // 是否显示错误提示
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  btnType: '',
  plain: true,
  label: '',
  clickFunction: undefined,
  confirm: false,
  confirmMessage: '',
  isSuccessCallback: false,
  successMessage: '',
  isErrorCallback: false,
  checkSuccess: (res: any) => res.code === 200,
  errorMessageType: 'res',
  errorMessage: 'msg',
  validate: false,
  validateRef: null,
  size: 'small',
  showErrorToast: false
})

const emit = defineEmits<{
  successCallback: [result: any]
  errorCallback: [error: any]
}>()

/**
 * 按钮类型配置映射
 */
const buttonConfigs: Record<string, ButtonConfig> = {
  add: { icon: 'Plus', label: '新增', type: 'primary', plain: true },
  edit: { icon: 'Edit', label: '修改', type: 'success', plain: true },
  delete: { icon: 'Delete', label: '删除', type: 'danger', plain: true },
  query: { icon: 'Search', label: '查询', type: 'primary', plain: false },
  refresh: { icon: 'Refresh', label: '重置', type: 'default', plain: false },
  upload: { icon: 'Upload', label: '上传', type: 'info', plain: true },
  download: { icon: 'Download', label: '下载', type: 'warning', plain: true },
  submit: { icon: '', label: '确定', type: 'primary', plain: true },
  cancel: { icon: '', label: '取消', type: 'info', plain: true }
}

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 计算属性：按钮图标
 */
const computedIcon = computed(() => {
  if (props.btnType && buttonConfigs[props.btnType]) {
    return buttonConfigs[props.btnType].icon
  }
  return ''
})

/**
 * 计算属性：按钮类型
 */
const computedType = computed(() => {
  if (props.btnType && buttonConfigs[props.btnType]) {
    return buttonConfigs[props.btnType].type
  }
  return props.type
})

/**
 * 计算属性：按钮样式
 */
const computedPlain = computed(() => {
  if (props.btnType && buttonConfigs[props.btnType]) {
    return buttonConfigs[props.btnType].plain
  }
  return props.plain
})

/**
 * 计算属性：按钮标签
 */
const computedLabel = computed(() => {
  if (props.label) {
    return props.label
  }
  if (props.btnType && buttonConfigs[props.btnType]) {
    return buttonConfigs[props.btnType].label
  }
  return ''
})

/**
 * 计算属性：按钮大小
 */
const computedSize = computed(() => {
  return props.size
})

/**
 * 获取防抖时间
 */
const getDebounceTime = (): number => {
  const config = getConfig()
  return config.button?.debounce ?? DEFAULT_DEBOUNCE_TIME
}

/**
 * 点击处理（使用优化的防抖函数）
 */
const handleClick = useDebounce(async () => {
  if (!props.clickFunction) return

  // 表单验证
  if (props.validate && props.validateRef) {
    try {
      await props.validateRef.validate()
    } catch (error) {
      return
    }
  }

  // 确认框
  if (props.confirm) {
    try {
      await ElMessageBox.confirm(
        props.confirmMessage || '确定要执行此操作吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
    } catch {
      return
    }
  }

  loading.value = true
  try {
    const result = await props.clickFunction()
    
    // 检查是否成功
    if (props.checkSuccess(result)) {
      if (props.isSuccessCallback) {
        emit('successCallback', result)
      }
      if (props.successMessage) {
        ElMessage.success(props.successMessage)
      }
    } else {
      throw new Error('操作失败')
    }
  } catch (error: any) {
    // 使用统一的错误处理
    if (props.isErrorCallback) {
      emit('errorCallback', error)
    }
    
    // 使用统一的错误处理函数
    handleError(error, {
      showToast: props.showErrorToast,
      defaultMessage: props.errorMessage || '操作失败',
      logError: true
    })
  } finally {
    loading.value = false
  }
}, getDebounceTime())
</script>

<style scoped>
/* 可在此处添加组件样式 */
</style>

