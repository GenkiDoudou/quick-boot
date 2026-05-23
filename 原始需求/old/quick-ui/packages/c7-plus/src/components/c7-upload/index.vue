<template>
  <el-upload
      v-bind="$attrs"
      :action="uploadUrl"
      :headers="headers"
      :file-list="fileList"
      :limit="limit"
      :accept="acceptTypes"
      :before-upload="beforeUpload"
      :on-success="onSuccess"
      :on-error="onError"
      :on-remove="onRemove"
      :on-exceed="onExceed"
      :list-type="listType"
      :show-file-list="showFileList"
  >
    <slot>
      <el-button type="primary">点击上传</el-button>
    </slot>
    
    <template #tip>
      <div class="el-upload__tip" v-if="tip">
        {{ tip }}
      </div>
    </template>
  </el-upload>
</template>

<script setup lang="ts">
import { ref, computed, watch, defineOptions } from 'vue'
import { ElMessage } from 'element-plus'
import { logger } from '../../utils/logger'
import { handleError } from '../../utils/errorHandler'

defineOptions({ name: 'C7Upload' })

/**
 * 文件列表项接口
 */
interface FileItem {
  name: string
  url: string
  status: string
}

/**
 * 上传响应接口
 */
interface UploadResponse {
  code?: number
  data?: {
    url?: string
  }
  [key: string]: any
}

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: string                   // 绑定值（逗号分隔的URL字符串）
  uploadUrl?: string                    // 上传地址
  deleteUrl?: string                    // 删除地址（预留）
  fileSize?: number                     // 文件大小限制（MB）
  fileType?: string                     // 文件类型限制（逗号分隔）
  limit?: number                        // 最大上传数量
  headers?: Record<string, any>         // 请求头
  listType?: 'text' | 'picture' | 'picture-card'  // 列表类型
  showFileList?: boolean                // 是否显示文件列表
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  uploadUrl: '',
  deleteUrl: '',
  fileSize: 5,
  fileType: 'jpg,png',
  limit: 1,
  headers: () => ({}),
  listType: 'picture-card',
  showFileList: true
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'change': [value: string]
}>()

/**
 * 文件列表
 */
const fileList = ref<FileItem[]>([])

/**
 * 计算属性：接受的文件类型
 */
const acceptTypes = computed(() => {
  const types = props.fileType.split(',').map(type => `.${type.trim()}`)
  return types.join(',')
})

/**
 * 计算属性：提示信息
 */
const tip = computed(() => {
  const sizeText = `文件大小不超过 ${props.fileSize}MB`
  const typeText = `支持 ${props.fileType} 格式`
  return `${sizeText}，${typeText}`
})

/**
 * 监听 modelValue 变化，更新文件列表
 */
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    const urls = newVal.split(',').filter(url => url.trim())
    fileList.value = urls.map((url, index) => ({
      name: `file-${index + 1}`,
      url: url.trim(),
      status: 'success'
    }))
  } else {
    fileList.value = []
  }
}, { immediate: true })

/**
 * 上传前验证
 * @param file 文件对象
 * @returns 是否通过验证
 */
const beforeUpload = (file: File): boolean => {
  // 文件大小验证
  const isValidSize = file.size / 1024 / 1024 < props.fileSize
  if (!isValidSize) {
    ElMessage.error(`文件大小不能超过 ${props.fileSize}MB!`)
    return false
  }

  // 文件类型验证
  const fileExtension = file.name.split('.').pop()?.toLowerCase() || ''
  const allowedTypes = props.fileType.split(',').map(type => type.trim().toLowerCase())
  const isValidType = allowedTypes.includes(fileExtension)
  if (!isValidType) {
    ElMessage.error(`只支持 ${props.fileType} 格式的文件!`)
    return false
  }

  return true
}

/**
 * 上传成功回调
 * @param response 响应数据
 * @param file 文件对象
 */
const onSuccess = (response: UploadResponse, file: any) => {
  // 假设后端返回格式为 { code: 200, data: { url: 'xxx' } }
  let fileUrl = ''
  if (response && response.data && response.data.url) {
    fileUrl = response.data.url
  } else if (typeof response === 'string') {
    fileUrl = response
  }

  if (fileUrl) {
    updateModelValue(fileUrl)
    ElMessage.success('上传成功!')
  } else {
    ElMessage.error('上传失败，未获取到文件地址!')
  }
}

/**
 * 上传失败回调
 * @param error 错误信息
 */
const onError = (error: Error) => {
  handleError(error, {
    showToast: true,
    defaultMessage: '上传失败',
    logError: true
  })
  logger.error('上传失败:', error)
}

/**
 * 删除文件回调
 * @param file 文件对象
 */
const onRemove = (file: any) => {
  const currentUrls = props.modelValue ? props.modelValue.split(',') : []
  const newUrls = currentUrls.filter(url => url.trim() !== file.url)
  updateModelValue(newUrls.join(','))
}

/**
 * 超出限制回调
 */
const onExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件!`)
}

/**
 * 更新 modelValue
 * @param value 新值
 */
const updateModelValue = (value: string) => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped>
.el-upload__tip {
  color: #606266;
  font-size: 12px;
  margin-top: 7px;
}
</style>

