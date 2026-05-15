<template>
  <el-button
    v-bind="$attrs"
    :loading="downloading"
    @click="handleDownload"
  >
    <el-icon v-if="!downloading"><Download /></el-icon>
    {{ label }}
  </el-button>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

defineOptions({ name: 'C7ExcelDownload', inheritAttrs: false })

const props = defineProps({
  /**
   * 下载函数（必填）
   * 返回值：Blob | { data: Blob, headers?: Record<string, string> }
   */
  downloadFn: {
    type: Function,
    required: true
  },
  /** 文件名（优先级最高，不传则从 Content-Disposition 解析） */
  fileName: {
    type: String,
    default: ''
  },
  /** 兜底文件名，fileName 未传且响应头无法解析时使用 */
  defaultFileName: {
    type: String,
    default: 'download.xlsx'
  },
  /** 按钮文案 */
  label: {
    type: String,
    default: '下载'
  },
  /**
   * 自定义通知函数，替代内置 ElMessage
   * (type: 'success' | 'error', message: string) => void
   */
  notify: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['success', 'error'])

/** 是否正在下载中（暴露给父组件） */
const downloading = ref(false)

defineExpose({ downloading })

/**
 * 从响应解析最终文件名
 * 优先级：fileName prop > Content-Disposition UTF-8 > Content-Disposition ASCII > defaultFileName
 */
function resolveFileName(response) {
  // 1. prop 优先
  if (props.fileName) return props.fileName

  // 2. 从响应头解析
  if (response && !(response instanceof Blob) && response.headers) {
    const disposition = response.headers['content-disposition'] ||
                        response.headers['Content-Disposition'] || ''
    // filename*=UTF-8'' 编码优先
    const utf8Match = disposition.match(/filename\*=UTF-8''([^;\s]+)/i)
    if (utf8Match) return decodeURIComponent(utf8Match[1])
    // 普通 filename="..."
    const asciiMatch = disposition.match(/filename="?([^"\n;]+)"?/i)
    if (asciiMatch) return asciiMatch[1].trim()
  }

  // 3. 兜底
  return props.defaultFileName
}

/**
 * 触发浏览器文件下载
 */
function triggerDownload(blob, name) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = url
  a.download = name
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/**
 * 触发通知
 */
function triggerNotify(type, message) {
  if (props.notify) {
    props.notify(type, message)
    return
  }
  if (type === 'error') ElMessage.error(message)
}

/**
 * 核心下载流程
 */
async function handleDownload() {
  if (downloading.value) return
  downloading.value = true
  try {
    const response = await props.downloadFn()
    const name = resolveFileName(response)
    const blob = response instanceof Blob ? response : response.data
    triggerDownload(blob, name)
    emit('success', name)
  } catch (err) {
    const error = err instanceof Error ? err : new Error(String(err))
    triggerNotify('error', error.message || '下载失败')
    emit('error', error)
  } finally {
    downloading.value = false
  }
}
</script>
