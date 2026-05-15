<template>
  <el-button
    v-bind="mergedAttrs"
    :loading="loading"
    @click="handleClick"
  >
    <template v-if="resolvedIcon" #icon>
      <el-icon><component :is="resolvedIcon" /></el-icon>
    </template>
    <slot>{{ resolvedLabel }}</slot>
  </el-button>
</template>

<script setup>
import { ref, computed, useAttrs } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { debounce } from 'lodash'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

defineOptions({ name: 'C7Button', inheritAttrs: false })

const props = defineProps({
  // ── 预设类型 ──
  btnType: {
    type: String,
    default: ''
  },
  // ── 外观 ──
  type: {
    type: String,
    default: ''
  },
  plain: {
    type: Boolean,
    default: undefined
  },
  label: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: ''
  },
  // ── 行为 ──
  clickFunction: {
    type: Function,
    default: null
  },
  debounceDelay: {
    type: Number,
    default: 300
  },
  // ── 确认 ──
  confirm: {
    type: Boolean,
    default: false
  },
  confirmMessage: {
    type: String,
    default: '确认执行此操作吗？'
  },
  confirmFn: {
    type: Function,
    default: null
  },
  // ── 校验 ──
  validate: {
    type: Boolean,
    default: false
  },
  validateRef: {
    type: Object,
    default: null
  },
  // ── 回调 ──
  isSuccessCallback: {
    type: Boolean,
    default: false
  },
  successMessage: {
    type: String,
    default: '操作成功'
  },
  successNotify: {
    type: Function,
    default: null
  },
  isErrorCallback: {
    type: Boolean,
    default: false
  },
  errorMessage: {
    type: String,
    default: '操作失败'
  },
  errorNotify: {
    type: Function,
    default: null
  },
  showErrorToast: {
    type: Boolean,
    default: true
  },
  // ── 结果判断 ──
  checkSuccess: {
    type: Function,
    default: () => true
  }
})

const emit = defineEmits([
  'successCallback',
  'errorCallback',
  'before-click',
  'after-click'
])

// ── 预设配置表 ──
const buttonConfigs = {
  add:      { icon: 'Plus',     label: '新增', type: 'primary', plain: true  },
  edit:     { icon: 'Edit',     label: '修改', type: 'success', plain: true  },
  delete:   { icon: 'Delete',   label: '删除', type: 'danger',  plain: true  },
  query:    { icon: 'Search',   label: '查询', type: 'primary', plain: false },
  refresh:  { icon: 'Refresh',  label: '重置', type: 'default', plain: false },
  upload:   { icon: 'Upload',   label: '上传', type: 'info',    plain: true  },
  download: { icon: 'Download', label: '下载', type: 'warning', plain: true  },
  submit:   { icon: '',         label: '确定', type: 'primary', plain: true  },
  cancel:   { icon: '',         label: '取消', type: 'info',    plain: true  },
}

const attrs = useAttrs()
const loading = ref(false)

const preset = computed(() => buttonConfigs[props.btnType] || null)

const resolvedLabel = computed(() => {
  return props.label || preset.value?.label || ''
})

const resolvedIcon = computed(() => {
  const iconName = preset.value?.icon
  if (!iconName) return null
  return ElementPlusIconsVue[iconName] || null
})

const mergedAttrs = computed(() => {
  const base = {
    type: props.type || preset.value?.type || 'default',
    plain: props.plain !== undefined ? props.plain : (preset.value?.plain ?? false),
  }
  if (props.size) base.size = props.size
  return { ...base, ...attrs }
})

// ── 核心点击逻辑 ──
async function _execute() {
  emit('before-click')

  // 1. 表单校验
  if (props.validate && props.validateRef) {
    try {
      await props.validateRef.validate()
    } catch {
      return
    }
  }

  // 2. 确认框
  if (props.confirm) {
    if (props.confirmFn) {
      const confirmed = await props.confirmFn()
      if (confirmed === false) return
    } else {
      try {
        await ElMessageBox.confirm(props.confirmMessage, '系统提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return
      }
    }
  }

  if (!props.clickFunction) {
    emit('after-click', true)
    return
  }

  // 3. 执行业务函数
  loading.value = true
  let success = false
  try {
    const result = await props.clickFunction()

    if (!props.checkSuccess(result)) {
      throw new Error(props.errorMessage)
    }

    success = true

    // 成功通知
    if (props.isSuccessCallback && props.successMessage) {
      const notifyFn = props.successNotify || ((msg) => ElMessage.success(msg))
      notifyFn(props.successMessage)
    }

    emit('successCallback', result)
  } catch (error) {
    // 错误通知
    if (props.showErrorToast) {
      const notifyFn = props.errorNotify || ((msg) => ElMessage.error(msg))
      notifyFn(error?.message || props.errorMessage)
    }
    emit('errorCallback', error)
  } finally {
    loading.value = false
    emit('after-click', success)
  }
}

const handleClick = debounce(_execute, props.debounceDelay)
</script>
