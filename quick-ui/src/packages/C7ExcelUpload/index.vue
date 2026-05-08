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
    <div class="c7-excel-upload__row">
      <el-button :disabled="uploading" @click="openFilePicker">
        选择文件
      </el-button>
      <span v-if="selectedFileName" class="c7-excel-upload__name">{{ selectedFileName }}</span>
    </div>
    <el-radio-group
        v-model="duplicateStrategy"
        class="c7-excel-upload__strategy"
        :disabled="uploading"
    >
      <el-radio value="overwrite">{{ overwriteLabelText }}</el-radio>
      <el-radio value="ignore">{{ ignoreLabelText }}</el-radio>
    </el-radio-group>
    <el-button
        v-bind="importButtonBindAttrs"
        :loading="uploading"
        @click="handleImportClick"
    >
      导入
    </el-button>
    <div v-if="lastResult" class="c7-excel-upload__result">
      <div>总计：{{ lastResult.total }}，成功：{{ lastResult.successCount }}，失败：{{ lastResult.failCount }}</div>
      <a
          v-if="showErrorFileLink"
          class="c7-excel-upload__error-link"
          :href="trimmedErrorFileUrl"
          target="_blank"
          rel="noopener noreferrer"
      >下载错误明细</a>
    </div>
  </div>
</template>

<script setup>
import {computed, ref, useAttrs} from 'vue'
import {ElMessage} from 'element-plus'

defineOptions({name: 'C7ExcelUpload', inheritAttrs: false})

/**
 * C7 Excel 导入：隐藏 **`input[type=file]`**、扩展名与大小校验、重复策略 **`overwrite|ignore`**、
 * 调用业务 **`uploadFn(file, strategy)`**、展示统计与 **`errorFileUrl`** 直链入口；
 * **`v-model:uploading`**、**`v-model:duplicateStrategy`**、**`reset()`** 与 **`C7ExcelDownload`** 的进行中语义对齐。
 *
 * **校验失败**（扩展名/大小）：仅 **`notify` / `ElMessage`**，**不** **`emit('error')`**。
 * **`uploadFn` reject**：**`notify` + `emit('error')`**。
 *
 * **`uploadFn` 契约**：由业务自行 **`FormData` / `request`**；成功 **resolve** **`{ total, successCount, failCount, errorFileUrl? }`**（组件不解析 **`R`**）。
 *
 * @emits success(result) **`uploadFn` resolve** 且结果已写入展示后
 * @emits error(err) 仅 **`uploadFn` reject**（或等价异步失败）
 */
const props = defineProps({
  /** 传给原生 **`input`** 的 **`accept`**，默认 **`.xls,.xlsx`** */
  accept: {type: String, default: '.xls,.xlsx'},
  /** 允许的最大体积（**MB**），按 **1 MB = 1024×1024 字节** 换算 */
  maxSizeMb: {type: Number, required: true},
  /** 覆盖策略展示文案；缺省为 **「覆盖」** */
  overwriteLabel: {type: String, default: ''},
  /** 忽略策略展示文案；缺省为 **「忽略」** */
  ignoreLabel: {type: String, default: ''},
  /**
   * **`(file, strategy) => Promise<C7ExcelUploadResult>`**；未传入时点击导入在开发环境 **`console.warn`** 并 **no-op**。
   * @typedef {{ total: number, successCount: number, failCount: number, errorFileUrl?: string }} C7ExcelUploadResult
   */
  uploadFn: {type: Function, default: undefined},
  /**
   * 自定义通知；未传入时走 **`ElMessage`**。
   * @param {'success'|'error'|'warning'|'info'} type
   * @param {string} message
   */
  notify: {type: Function, default: undefined},
})

const emit = defineEmits(['success', 'error'])

/** 与父同步：**`v-model:duplicateStrategy`**，默认 **`ignore`**（**`reset()`** 亦复位到此值） */
const duplicateStrategy = defineModel('duplicateStrategy', {
  type: String,
  default: 'ignore',
})

/** 与父同步：**`v-model:uploading`** */
const uploading = defineModel('uploading', {type: Boolean, default: false})

const attrs = useAttrs()
const fileInputRef = ref(/** @type {HTMLInputElement | null} */ (null))
const selectedFile = ref(/** @type {File | null} */ (null))
/** @type {import('vue').Ref<import('vue').UnwrapRef<{ total: number, successCount: number, failCount: number, errorFileUrl?: string } | null>>} */
const lastResult = ref(null)

const selectedFileName = computed(() => selectedFile.value?.name ?? '')

const overwriteLabelText = computed(() => (props.overwriteLabel && props.overwriteLabel.trim()) ? props.overwriteLabel : '覆盖')
const ignoreLabelText = computed(() => (props.ignoreLabel && props.ignoreLabel.trim()) ? props.ignoreLabel : '忽略')

const trimmedErrorFileUrl = computed(() => (lastResult.value?.errorFileUrl ?? '').trim())

const showErrorFileLink = computed(() => {
  const r = lastResult.value
  if (!r) return false
  return r.failCount > 0 && trimmedErrorFileUrl.value.length > 0
})

const rootBindAttrs = computed(() => ({
  class: attrs.class,
  style: attrs.style,
}))

const importButtonBindAttrs = computed(() => ({
  ...filterAttrsForElButton(attrs),
}))

/**
 * 判断文件名是否为允许的 Excel 扩展名（**不区分大小写**，仅 **`.xls` / `.xlsx`**）。
 * @param {string} fileName
 * @returns {boolean}
 */
function isAllowedExcelFileName(fileName) {
  const n = (fileName || '').toLowerCase()
  return n.endsWith('.xls') || n.endsWith('.xlsx')
}

/**
 * **`maxSizeMb` → 字节上限**（**1 MB = 1024×1024**）。
 * @param {number} maxSizeMb
 * @returns {number}
 */
function maxSizeMbToBytes(maxSizeMb) {
  return maxSizeMb * 1024 * 1024
}

/**
 * @param {File} file
 * @param {number} maxSizeMb
 * @returns {boolean}
 */
function isFileWithinSizeLimit(file, maxSizeMb) {
  return file.size <= maxSizeMbToBytes(maxSizeMb)
}

/**
 * 从 **`useAttrs()`** 挑出可安全透传到 **`ElButton`（导入）** 的属性。
 * @param {Record<string, unknown>} raw
 * @returns {Record<string, unknown>}
 */
function filterAttrsForElButton(raw) {
  const allow = new Set([
    'size', 'plain', 'round', 'circle', 'type', 'link', 'text', 'bg', 'nativeType',
    'class', 'style', 'icon', 'disabled', 'autofocus',
  ])
  const out = {}
  for (const k of Object.keys(raw)) {
    if (allow.has(k)) out[k] = raw[k]
  }
  return out
}

/**
 * @param {'success'|'error'|'warning'|'info'} type
 * @param {string} message
 */
function pushNotify(type, message) {
  if (typeof props.notify === 'function') {
    props.notify(type, message)
    return
  }
  if (type === 'error') ElMessage.error(message)
  else if (type === 'success') ElMessage.success(message)
  else if (type === 'warning') ElMessage.warning(message)
  else ElMessage.info(message)
}

/**
 * @param {unknown} err
 * @returns {string}
 */
function formatUploadError(err) {
  if (err == null) return '导入失败'
  if (typeof err === 'string') return err
  if (err instanceof Error && err.message) return err.message
  try {
    return String(err)
  } catch {
    return '导入失败'
  }
}

function openFilePicker() {
  fileInputRef.value?.click()
}

/**
 * 选择文件后：扩展名 + 大小校验；通过后暂存 **`File`** 并清空 **`input.value`** 以支持重复选择同一文件。
 * @param {Event} e
 */
function onFileInputChange(e) {
  const input = /** @type {HTMLInputElement} */ (e.target)
  const file = input.files && input.files[0] ? input.files[0] : null
  input.value = ''

  if (!file) return

  if (!isAllowedExcelFileName(file.name)) {
    selectedFile.value = null
    pushNotify('error', '仅支持 .xls 或 .xlsx 文件')
    return
  }

  if (!isFileWithinSizeLimit(file, props.maxSizeMb)) {
    selectedFile.value = null
    pushNotify('error', `文件大小不能超过 ${props.maxSizeMb} MB`)
    return
  }

  selectedFile.value = file
}

async function handleImportClick() {
  if (uploading.value) return
  if (typeof props.uploadFn !== 'function') {
    console.warn('[C7ExcelUpload] 缺少有效的 uploadFn')
    return
  }
  if (!selectedFile.value) {
    pushNotify('error', '请先选择文件')
    return
  }

  uploading.value = true
  try {
    const raw = await props.uploadFn(selectedFile.value, /** @type {'overwrite'|'ignore'} */ (duplicateStrategy.value))
    const result = normalizeUploadResult(raw)
    lastResult.value = result
    emit('success', result)
  } catch (err) {
    const msg = formatUploadError(err)
    pushNotify('error', msg)
    emit('error', err)
  } finally {
    uploading.value = false
  }
}

/**
 * 将 **`uploadFn`** 返回值规范为结果对象；非法形态抛错并走 **`catch`**。
 * @param {unknown} raw
 * @returns {{ total: number, successCount: number, failCount: number, errorFileUrl?: string }}
 */
function normalizeUploadResult(raw) {
  if (!raw || typeof raw !== 'object') throw new Error('uploadFn 返回值须为对象')
  const o = /** @type {Record<string, unknown>} */ (raw)
  const total = Number(o.total)
  const successCount = Number(o.successCount)
  const failCount = Number(o.failCount)
  if (!Number.isFinite(total) || !Number.isFinite(successCount) || !Number.isFinite(failCount)) {
    throw new Error('uploadFn 返回值须包含有效的 total/successCount/failCount')
  }
  const errorFileUrl = typeof o.errorFileUrl === 'string' ? o.errorFileUrl : undefined
  return {total, successCount, failCount, errorFileUrl}
}

/**
 * **`reset()`**：清空已选文件、**`input.value`**、上次结果；**`duplicateStrategy`** 复位为 **`ignore`**。
 */
function reset() {
  selectedFile.value = null
  lastResult.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  duplicateStrategy.value = 'ignore'
}

defineExpose({
  /** 与 **`v-model:uploading`** 同源 */
  uploading,
  reset,
})
</script>

<style scoped>
.c7-excel-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.c7-excel-upload__input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  overflow: hidden;
  pointer-events: none;
}

.c7-excel-upload__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.c7-excel-upload__name {
  font-size: 14px;
  color: var(--el-text-color-regular);
  word-break: break-all;
}

.c7-excel-upload__strategy {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.c7-excel-upload__result {
  font-size: 14px;
  color: var(--el-text-color-primary);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.c7-excel-upload__error-link {
  color: var(--el-color-primary);
  text-decoration: none;
}

.c7-excel-upload__error-link:hover {
  text-decoration: underline;
}
</style>
