<template>
  <div class="c7-excel-upload" v-bind="rootBindAttrs">
    <input
      ref="fileInputRef"
      class="c7-excel-upload__input"
      type="file"
      :accept="accept"
      :disabled="uploading"
      @change="onFileInputChange"
    />

    <div
      class="c7-excel-upload__dropzone"
      :class="{ 'is-dragover': dragOver }"
      @click="openFilePicker"
      @dragover.prevent="onDragOver"
      @dragleave.prevent="onDragLeave"
      @drop.prevent="onDrop"
    >
      <el-icon class="c7-excel-upload__drop-icon"><UploadFilled /></el-icon>
      <div class="c7-excel-upload__drop-text">将文件拖到此处，或<span class="link">点击上传</span></div>
      <div v-if="selectedFileName" class="c7-excel-upload__file-name">{{ selectedFileName }}</div>
    </div>

    <div class="c7-excel-upload__tips">
      <el-checkbox v-model="updateSupport" :disabled="uploading">是否更新已经存在的数据</el-checkbox>
      <div class="tip-row">
        仅允许导入 xls、xlsx 格式文件。
        <C7ExcelDownload
          v-if="hasTemplateFn"
          link
          type="primary"
          :download-fn="templateDownloadFn"
          :default-file-name="templateFileName"
        >下载模板</C7ExcelDownload>
      </div>
    </div>

    <div v-if="lastResult" class="c7-excel-upload__result">
      <div>导入结果：总条数 {{ lastResult.total }}，成功 {{ lastResult.successCount }}，失败 {{ lastResult.failCount }}</div>
      <el-button
        v-if="lastResult.failCount > 0 && lastResult.errorFileBase64"
        type="danger"
        link
        @click="downloadErrorFile(lastResult)"
      >
        下载失败导入.txt
      </el-button>
    </div>

    <div class="c7-excel-upload__actions">
      <el-button type="primary" :loading="uploading" @click="handleImportClick">确定</el-button>
      <el-button :disabled="uploading" @click="handleCancelClick">取消</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, useAttrs } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import C7ExcelDownload from '../C7ExcelDownload/index.vue'

defineOptions({ name: 'C7ExcelUpload', inheritAttrs: false })

const props = defineProps({
  accept: { type: String, default: '.xls,.xlsx' },
  maxSizeMb: { type: Number, required: true },
  uploadFn: { type: Function, default: undefined },
  notify: { type: Function, default: undefined },
  templateDownloadFn: { type: Function, default: undefined },
  templateFileName: { type: String, default: 'import-template.xlsx' },
})

const emit = defineEmits(['success', 'error', 'cancel'])

const duplicateStrategy = defineModel('duplicateStrategy', { type: String, default: 'ignore' })
const uploading = defineModel('uploading', { type: Boolean, default: false })

const attrs = useAttrs()
const fileInputRef = ref(null)
const selectedFile = ref(null)
const lastResult = ref(null)
const dragOver = ref(false)
const updateSupport = ref(false)

const selectedFileName = computed(() => selectedFile.value?.name ?? '')
const hasTemplateFn = computed(() => typeof props.templateDownloadFn === 'function')
const rootBindAttrs = computed(() => ({ class: attrs.class, style: attrs.style }))

function pushNotify(type, message) {
  if (typeof props.notify === 'function') return props.notify(type, message)
  if (type === 'error') ElMessage.error(message)
  else if (type === 'success') ElMessage.success(message)
}

function isAllowedExcelFileName(fileName) {
  const n = (fileName || '').toLowerCase()
  return n.endsWith('.xls') || n.endsWith('.xlsx')
}

function maxSizeMbToBytes(maxSizeMb) {
  return maxSizeMb * 1024 * 1024
}

function applyFile(file) {
  if (!file) return
  if (!isAllowedExcelFileName(file.name)) {
    selectedFile.value = null
    pushNotify('error', '仅支持 .xls 或 .xlsx 文件')
    return
  }
  if (file.size > maxSizeMbToBytes(props.maxSizeMb)) {
    selectedFile.value = null
    pushNotify('error', `文件大小不能超过 ${props.maxSizeMb} MB`)
    return
  }
  selectedFile.value = file
}

function openFilePicker() {
  fileInputRef.value?.click()
}

function onFileInputChange(e) {
  const input = e.target
  const file = input.files && input.files[0] ? input.files[0] : null
  input.value = ''
  applyFile(file)
}

function onDragOver() {
  dragOver.value = true
}

function onDragLeave() {
  dragOver.value = false
}

function onDrop(e) {
  dragOver.value = false
  const file = e.dataTransfer?.files?.[0]
  applyFile(file || null)
}

async function handleImportClick() {
  if (uploading.value) return
  if (typeof props.uploadFn !== 'function') return
  if (!selectedFile.value) {
    pushNotify('error', '请先选择文件')
    return
  }
  uploading.value = true
  duplicateStrategy.value = updateSupport.value ? 'overwrite' : 'ignore'
  try {
    const raw = await props.uploadFn(selectedFile.value, duplicateStrategy.value)
    const result = normalizeUploadResult(raw)
    lastResult.value = result
    emit('success', result)
  } catch (err) {
    pushNotify('error', err?.message || '导入失败')
    emit('error', err)
  } finally {
    uploading.value = false
  }
}

function normalizeUploadResult(raw) {
  if (!raw || typeof raw !== 'object') throw new Error('uploadFn 返回值必须为对象')
  return {
    total: Number(raw.total || 0),
    successCount: Number(raw.successCount || 0),
    failCount: Number(raw.failCount || 0),
    failRows: typeof raw.failRows === 'string' ? raw.failRows : '',
    errorFileName: typeof raw.errorFileName === 'string' ? raw.errorFileName : '',
    errorFileBase64: typeof raw.errorFileBase64 === 'string' ? raw.errorFileBase64 : '',
  }
}

function downloadErrorFile(result) {
  if (!result?.errorFileBase64) return
  try {
    const bytes = atob(result.errorFileBase64)
    const arr = new Uint8Array(bytes.length)
    for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i)
    const blob = new Blob([arr], { type: 'text/plain;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = result.errorFileName || '失败导入.txt'
    link.click()
    URL.revokeObjectURL(link.href)
  } catch {
    pushNotify('error', '失败明细文件下载失败')
  }
}

function reset() {
  selectedFile.value = null
  lastResult.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  updateSupport.value = false
}

function handleCancelClick() {
  reset()
  emit('cancel')
}

defineExpose({ uploading, reset })
</script>

<style scoped>
.c7-excel-upload { display: flex; flex-direction: column; gap: 12px; }
.c7-excel-upload__input { position: absolute; width: 0; height: 0; opacity: 0; }
.c7-excel-upload__dropzone {
  border: 1px dashed #409eff;
  border-radius: 6px;
  min-height: 170px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #606266;
  background: #fafcff;
}
.c7-excel-upload__dropzone.is-dragover { background: #eef5ff; }
.c7-excel-upload__drop-icon { font-size: 56px; color: #c0c4cc; margin-bottom: 10px; }
.c7-excel-upload__drop-text { font-size: 14px; }
.c7-excel-upload__drop-text .link { color: #409eff; margin-left: 2px; }
.c7-excel-upload__file-name { margin-top: 8px; color: #606266; font-size: 12px; }
.c7-excel-upload__tips { color: #606266; font-size: 13px; }
.tip-row { margin-top: 4px; }
.c7-excel-upload__actions { display: flex; justify-content: center; gap: 10px; margin-top: 6px; }
.c7-excel-upload__result { font-size: 13px; color: #303133; line-height: 1.8; }
</style>
