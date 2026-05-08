<template>
  <el-button
      v-bind="buttonBindAttrs"
      :loading="downloading"
      @click="handleClick"
  >
    <slot>{{ defaultLabel }}</slot>
  </el-button>
</template>

<script setup>
import {computed, useAttrs} from 'vue'
import {ElMessage} from 'element-plus'
import errorCode from '@/utils/errorCode'
import {blobValidate} from '@/utils/ruoyi'

defineOptions({name: 'C7ExcelDownload', inheritAttrs: false})

/**
 * C7 导出下载按钮：执行 **`downloadFn`** 取 **Blob**（或 **`{ data, headers }`**），
 * 按优先级解析文件名后通过 **`objectURL` + `<a download>`** 触发浏览器下载；
 * 管理 **`v-model:downloading`**；**JSON 错误 Blob** 与 **`download()`** 对齐提示。
 *
 * **与 `request` 配合**：需要 **`Content-Disposition`** 时，请使用
 * **`downloadRequest(url, params, { returnBlobWithHeaders: true })`**，
 * 在 **`downloadFn`** 中 **`return`** 该 Promise 结果（或自行组装 **`{ data, headers }`**）。
 *
 * @emits success(fileName) 下载链成功触发后
 * @emits error(err) 失败（含 **`downloadFn` reject**、JSON Blob、无可用文件名等）
 */
const props = defineProps({
  /**
   * 返回 **`Blob`** 或 **`{ data: Blob, headers }`**；须与项目 **`axios`/`downloadRequest`** 习惯一致（**reject** 表示失败）。
   */
  /** 未传入时点击无效并在开发环境 `console.warn`（见 **`handleClick`**） */
  downloadFn: {type: Function, default: undefined},
  /** 非空时优先作为保存文件名，**不再**解析 **`Content-Disposition`** */
  fileName: {type: String, default: ''},
  /** 当 **`fileName`** 与响应头均未得到有效名时的兜底文件名 */
  defaultFileName: {type: String, default: ''},
  /**
   * 自定义通知；传入后错误路径 **仅**走此回调（成功路径仍 **`emit('success')`**，不强制 toast）。
   * @param {'success'|'error'|'warning'|'info'} type
   * @param {string} message
   */
  notify: {type: Function, default: undefined},
  /** 默认插槽为空时的按钮文案 */
  defaultLabel: {type: String, default: '导出'},
})

const emit = defineEmits(['success', 'error'])

/** 与父组件同步进行中状态（**`v-model:downloading`**） */
const downloading = defineModel('downloading', {type: Boolean, default: false})

const attrs = useAttrs()

const buttonBindAttrs = computed(() => ({
  ...filterAttrsForElButton(attrs),
}))

/**
 * 从 **`useAttrs()`** 挑出可安全透传到 **`ElButton`** 的属性（避免未知键误入 EP）。
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
 * @param {unknown} err
 * @returns {string}
 */
function formatRequestError(err) {
  if (err == null) return '下载失败'
  if (typeof err === 'string') return err
  if (err instanceof Error && err.message) return err.message
  try {
    return String(err)
  } catch {
    return '下载失败'
  }
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
 * @param {unknown} headers
 * @param {string} name
 * @returns {string|undefined}
 */
function getHeader(headers, name) {
  if (headers == null) return undefined
  const lower = name.toLowerCase()
  if (typeof headers.get === 'function') {
    return headers.get(lower) ?? headers.get(name)
  }
  if (typeof headers === 'object') {
    const h = /** @type {Record<string, string>} */ (headers)
    return h[lower] ?? h[name] ?? h['Content-Disposition']
  }
  return undefined
}

/**
 * 从 **`Content-Disposition`** 取值中解析 **`filename*`** / **`filename`**（**`filename*`** 优先）。
 * @param {string} cd
 * @returns {string|null}
 */
function parseFilenameFromContentDisposition(cd) {
  if (!cd || typeof cd !== 'string') return null

  const starRegex = /filename\*\s*=\s*([^;]+)/gi
  let m
  while ((m = starRegex.exec(cd)) !== null) {
    const raw = m[1].trim().replace(/^"(.*)"$/, '$1')
    const decoded = tryDecodeFilenameStar(raw)
    if (decoded) return sanitizeFilename(decoded)
  }

  const quoted = /filename\s*=\s*"((?:\\.|[^"\\])*)"/gi
  while ((m = quoted.exec(cd)) !== null) {
    const inner = m[1].replace(/\\(.)/g, '$1')
    if (inner) return sanitizeFilename(inner)
  }

  const plain = /filename\s*=\s*([^";,\s]+)/gi
  while ((m = plain.exec(cd)) !== null) {
    const v = m[1].trim().replace(/^"(.*)"$/, '$1')
    if (v && !/^filename\*/i.test(m[0])) return sanitizeFilename(v)
  }

  return null
}

/**
 * @param {string} rhs `filename*` 右侧（可能含 **`UTF-8''`** 或 **`charset'lang'`** 前缀）
 * @returns {string|null}
 */
function tryDecodeFilenameStar(rhs) {
  const utf8TwoQuote = /^UTF-8''/i
  if (utf8TwoQuote.test(rhs)) {
    try {
      return decodeURIComponent(rhs.replace(/^UTF-8''/i, ''))
    } catch {
      return null
    }
  }
  const parts = rhs.split("'")
  if (parts.length >= 3) {
    try {
      return decodeURIComponent(parts.slice(2).join("'"))
    } catch {
      return null
    }
  }
  try {
    return decodeURIComponent(rhs)
  } catch {
    return rhs || null
  }
}

/**
 * 去掉路径分隔符等，降低异常头导致的路径穿越风险。
 * @param {string} name
 * @returns {string}
 */
function sanitizeFilename(name) {
  return name.replace(/[/\\?%*:|"<>]/g, '_').trim() || ''
}

/**
 * @param {unknown} raw
 * @returns {{ blob: Blob, headers?: object }}
 */
function normalizeDownloadResult(raw) {
  if (raw instanceof Blob) return {blob: raw}
  if (raw && typeof raw === 'object' && 'data' in raw) {
    const data = /** @type {{ data: unknown, headers?: object }} */ (raw).data
    const headers = /** @type {{ data: unknown, headers?: object }} */ (raw).headers
    if (data instanceof Blob) return {blob: data, headers}
  }
  throw new Error('downloadFn 返回值须为 Blob 或 { data: Blob, headers }')
}

/**
 * @param {string} propFile
 * @param {object|undefined} headers
 * @param {string} defaultFile
 * @returns {string|null}
 */
function resolveFileName(propFile, headers, defaultFile) {
  const trimmedProp = (propFile || '').trim()
  if (trimmedProp) return sanitizeFilename(trimmedProp) || trimmedProp

  const cd = getHeader(headers, 'content-disposition')
  if (cd) {
    const fromHeader = parseFilenameFromContentDisposition(cd)
    if (fromHeader) return fromHeader
  }

  const d = (defaultFile || '').trim()
  if (d) return sanitizeFilename(d) || d

  return null
}

/**
 * @param {Blob} blob
 * @returns {Promise<string>}
 */
async function messageFromJsonBlob(blob) {
  try {
    const text = await blob.text()
    const rspObj = JSON.parse(text)
    return errorCode[rspObj.code] || rspObj.msg || errorCode['default'] || '导出失败'
  } catch {
    return '导出失败'
  }
}

async function handleClick() {
  if (downloading.value) return
  if (typeof props.downloadFn !== 'function') {
    console.warn('[C7ExcelDownload] 缺少有效的 downloadFn')
    return
  }

  downloading.value = true
  try {
    const raw = await props.downloadFn()
    const {blob, headers} = normalizeDownloadResult(raw)

    if (!blobValidate(blob)) {
      const msg = await messageFromJsonBlob(blob)
      pushNotify('error', msg)
      emit('error', new Error(msg))
      return
    }

    const fileName = resolveFileName(props.fileName, headers, props.defaultFileName)
    if (!fileName) {
      const msg = '无法确定下载文件名，请配置 fileName、defaultFileName 或响应头 Content-Disposition'
      pushNotify('error', msg)
      emit('error', new Error(msg))
      return
    }

    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.style.display = 'none'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)

    emit('success', fileName)

    const revoke = () => URL.revokeObjectURL(url)
    if (typeof requestAnimationFrame === 'function') requestAnimationFrame(revoke)
    else setTimeout(revoke, 0)
  } catch (err) {
    const msg = formatRequestError(err)
    pushNotify('error', msg)
    emit('error', err)
  } finally {
    downloading.value = false
  }
}
</script>
