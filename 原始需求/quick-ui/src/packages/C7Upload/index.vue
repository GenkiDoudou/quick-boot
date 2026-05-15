<template>
  <div class="c7-upload">
    <el-upload
      v-bind="$attrs"
      :file-list="fileList"
      :action="uploadUrl"
      :headers="headers"
      :http-request="httpRequest || undefined"
      :accept="acceptTypes"
      :limit="limit"
      :list-type="listType"
      :show-file-list="showFileList"
      :before-upload="handleBeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :on-progress="handleProgress"
    >
      <!-- 默认触发区域 -->
      <slot>
        <el-button type="primary">
          <el-icon><UploadFilled /></el-icon>
          点击上传
        </el-button>
      </slot>

      <!-- 提示文字 -->
      <template v-if="resolvedTip" #tip>
        <slot name="tip">
          <div class="c7-upload__tip">{{ resolvedTip }}</div>
        </slot>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

defineOptions({ name: 'C7Upload', inheritAttrs: false })

const props = defineProps({
  /** v-model 绑定值，逗号分隔的 URL 字符串 */
  modelValue: {
    type: String,
    default: ''
  },
  /** 上传地址（httpRequest 优先） */
  uploadUrl: {
    type: String,
    default: ''
  },
  /** 【新增】自定义上传函数，不内置请求，优先于 uploadUrl */
  httpRequest: {
    type: Function,
    default: null
  },
  /** 请求头 */
  headers: {
    type: Object,
    default: () => ({})
  },
  /**
   * 【新增】响应 URL 提取函数
   * 默认: (res) => res?.data?.url ?? res?.url ?? ''
   */
  responseParser: {
    type: Function,
    default: null
  },
  /** 文件大小限制（MB），默认 5 */
  fileSize: {
    type: Number,
    default: 5
  },
  /** 允许的文件类型（逗号分隔扩展名），默认 'jpg,png' */
  fileType: {
    type: String,
    default: 'jpg,png'
  },
  /** 最大上传数量，默认 1 */
  limit: {
    type: Number,
    default: 1
  },
  /** 文件列表展示方式 */
  listType: {
    type: String,
    default: 'picture-card',
    validator: (v) => ['text', 'picture', 'picture-card'].includes(v)
  },
  /** 是否显示文件列表，默认 true */
  showFileList: {
    type: Boolean,
    default: true
  },
  /**
   * 【新增】自定义通知函数，替代内置 ElMessage
   * (type: 'success' | 'error' | 'warning', message: string) => void
   */
  notify: {
    type: Function,
    default: null
  },
  /** 自定义提示文字（不传则自动生成） */
  tip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'success',
  'error',
  'remove',
  'exceed'
])

// ── 上传中计数，用于暴露 uploading 状态 ──
const uploadingCount = ref(0)
const uploading = computed(() => uploadingCount.value > 0)

// ── 触发通知 ──
function triggerNotify(type, message) {
  if (props.notify) {
    props.notify(type, message)
    return
  }
  if (type === 'error') ElMessage.error(message)
  else if (type === 'warning') ElMessage.warning(message)
  else ElMessage.success(message)
}

// ── accept 属性：由 fileType 生成 ──
const acceptTypes = computed(() => {
  if (!props.fileType) return ''
  return props.fileType
    .split(',')
    .map(ext => `.${ext.trim()}`)
    .join(',')
})

// ── 自动生成提示文字 ──
const resolvedTip = computed(() => {
  if (props.tip) return props.tip
  const parts = []
  if (props.fileType) parts.push(`支持 ${props.fileType} 格式`)
  if (props.fileSize) parts.push(`大小不超过 ${props.fileSize}MB`)
  if (props.limit > 1) parts.push(`最多上传 ${props.limit} 个文件`)
  return parts.join('，')
})

// ── 当前 URL 列表（从 modelValue 解析） ──
const urlList = computed(() => {
  if (!props.modelValue) return []
  return props.modelValue.split(',').filter(Boolean).map(u => u.trim())
})

// ── el-upload 需要的 fileList 格式 ──
const fileList = computed(() =>
  urlList.value.map((url, i) => ({
    name: url.split('/').pop() || `file-${i + 1}`,
    url,
    status: 'success'
  }))
)

// ── 上传前校验 ──
function handleBeforeUpload(file) {
  // 类型校验
  if (props.fileType) {
    const ext = file.name.split('.').pop()?.toLowerCase() || ''
    const allowed = props.fileType.split(',').map(t => t.trim().toLowerCase())
    if (!allowed.includes(ext)) {
      triggerNotify('error', `仅支持上传 ${props.fileType} 格式的文件`)
      return false
    }
  }
  // 大小校验
  if (props.fileSize && file.size > props.fileSize * 1024 * 1024) {
    triggerNotify('error', `文件大小不能超过 ${props.fileSize}MB`)
    return false
  }
  return true
}

// ── 上传中 ──
function handleProgress() {
  uploadingCount.value++
}

// ── 上传成功 ──
function handleSuccess(response, uploadFile) {
  uploadingCount.value = Math.max(0, uploadingCount.value - 1)

  // 解析 URL
  let url = ''
  if (props.responseParser) {
    const parsed = props.responseParser(response)
    url = Array.isArray(parsed) ? parsed[0] : parsed
  } else {
    url = response?.data?.url ?? response?.url ?? ''
  }

  if (!url) {
    triggerNotify('error', '上传成功但未获取到文件地址')
    return
  }

  const newList = [...urlList.value, url]
  const newValue = newList.join(',')
  emit('update:modelValue', newValue)
  emit('change', newValue)
  emit('success', url, response)
}

// ── 上传失败 ──
function handleError(error) {
  uploadingCount.value = Math.max(0, uploadingCount.value - 1)
  triggerNotify('error', error?.message || '上传失败')
  emit('error', error)
}

// ── 删除文件 ──
function handleRemove(uploadFile) {
  const removedUrl = uploadFile.url
  const newList = urlList.value.filter(url => url !== removedUrl)
  const newValue = newList.join(',')
  emit('update:modelValue', newValue)
  emit('change', newValue)
  emit('remove', removedUrl)
}

// ── 超出数量限制 ──
function handleExceed() {
  triggerNotify('warning', `最多只能上传 ${props.limit} 个文件`)
  emit('exceed')
}

// ── 暴露给父组件 ──
defineExpose({ uploading })
</script>

<style scoped>
.c7-upload {
  width: 100%;
}

.c7-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.5;
}
</style>
