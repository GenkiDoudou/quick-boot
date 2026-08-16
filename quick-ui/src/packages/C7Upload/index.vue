<template>
  <div class="c7-upload" v-bind="rootBindAttrs">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :class="{ 'is-disabled': disabled || !ruleReady }"
      :disabled="disabled || !ruleReady"
      :limit="limitCount"
      :accept="acceptAttr"
      :multiple="limitCount > 1"
      :auto-upload="autoUpload"
      :http-request="handleHttpRequest"
      :before-upload="beforeUpload"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      :on-success="handleSuccess"
      :on-error="handleError"
      drag
    >
      <el-icon class="c7-upload__icon"><UploadFilled /></el-icon>
      <div class="c7-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div v-if="loadingRule" class="c7-upload__tip">正在加载分类配置…</div>
        <div v-else-if="!classifyKey" class="c7-upload__tip c7-upload__tip--warn">请先选择上传分类</div>
        <div v-else-if="rule" class="c7-upload__tip">
          分类 <strong>{{ rule.classify }}</strong>：最多 {{ rule.limitCount }} 个文件，
          单文件不超过 {{ sizeHint }}，
          类型 {{ extHint }}
          <template v-if="isCompressOn">
            ；超过 {{ compressMinKb }}KB 的图片将在上传前压缩
          </template>
        </div>
        <div v-else class="c7-upload__tip c7-upload__tip--warn">未找到分类「{{ classifyKey }}」的配置</div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { computed, ref, useAttrs, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { formatFileSize, getFileClassify, limitExtToAccept, uploadCommonFile } from '@/api/common/file'
import { maybeCompressImageFile } from '@/utils/compressImage'

defineOptions({ name: 'C7Upload', inheritAttrs: false })

/**
 * C7 文件上传：基于 `ElUpload` 拖拽上传，按 `classify` 拉取分类规则（数量/大小/扩展名/压缩），
 * 可选图片压缩后调用 `/file/upload`；`v-model:results` 绑定已成功上传的结果列表。
 *
 * @prop {string} classify 上传分类（必填，对应 sys_file_classify.classify）
 * @prop {boolean} [autoUpload=true] 选文件后是否立即上传
 * @emits update:results 上传成功或删除后更新结果列表（v-model:results）
 * @emits success 单文件上传成功
 * @emits error 上传或加载分类规则失败
 * @emits change 文件列表或结果变更
 */
const props = defineProps({
  /** 上传分类（必填，对应 sys_file_classify.classify） */
  classify: { type: String, required: true },
  /** 选中文件后是否立即上传 */
  autoUpload: { type: Boolean, default: true },
  /** 禁用上传 */
  disabled: { type: Boolean, default: false },
  /** 自定义上传函数 (file, classify) => Promise，默认走 /file/upload */
  uploadFn: { type: Function, default: undefined }
})

const emit = defineEmits(['success', 'error', 'change'])

/** 已成功上传的结果列表 */
const results = defineModel('results', { type: Array, default: () => [] })

const attrs = useAttrs()
const rootBindAttrs = computed(() => {
  const { class: _c, style: _s, ...rest } = attrs
  return rest
})

const uploadRef = ref(null)
const fileList = ref([])
const rule = ref(null)
const loadingRule = ref(false)

const classifyKey = computed(() => (props.classify == null ? '' : String(props.classify).trim()))
const ruleReady = computed(() => !!rule.value && !loadingRule.value)
const limitCount = computed(() => (rule.value?.limitCount > 0 ? rule.value.limitCount : 1))
const acceptAttr = computed(() => limitExtToAccept(rule.value?.limitExt))
const sizeHint = computed(() => {
  if (rule.value?.limitSizeBytes > 0) return formatFileSize(rule.value.limitSizeBytes)
  if (rule.value?.limitSize != null && typeof rule.value.limitSize === 'string') return rule.value.limitSize
  return '10MB'
})
const extHint = computed(() => {
  const ext = rule.value?.limitExt
  return ext && String(ext).trim() !== '' ? ext : '内置默认白名单'
})
const isCompressOn = computed(() => {
  const v = rule.value?.compressEnabled
  return v === '1' || v === true || v === 1
})
const compressMinKb = computed(() => {
  const n = Number(rule.value?.compressMinSizeKb)
  return Number.isFinite(n) && n > 0 ? n : 200
})

async function loadRule(classify) {
  if (!classify) {
    rule.value = null
    return
  }
  loadingRule.value = true
  try {
    const res = await getFileClassify(classify)
    rule.value = res?.data ?? null
  } catch (e) {
    rule.value = null
    emit('error', e)
  } finally {
    loadingRule.value = false
  }
}

watch(classifyKey, (val) => {
  fileList.value = []
  results.value = []
  loadRule(val)
}, { immediate: true })

function fileExtLower(name) {
  const n = String(name || '')
  const i = n.lastIndexOf('.')
  if (i < 0 || i === n.length - 1) return ''
  return n.substring(i + 1).toLowerCase()
}

function beforeUpload(file) {
  if (!rule.value) {
    ElMessage.warning('分类配置未就绪')
    return false
  }
  const maxBytes = rule.value.limitSizeBytes > 0 ? rule.value.limitSizeBytes : 10 * 1024 * 1024
  if (file.size > maxBytes) {
    ElMessage.error(`文件「${file.name}」超过大小限制（最大 ${sizeHint.value}）`)
    return false
  }
  const allowed = rule.value.limitExt
  if (allowed && String(allowed).trim() !== '') {
    const ext = fileExtLower(file.name)
    const set = String(allowed).split(',').map((s) => s.trim().toLowerCase().replace(/^\./, '')).filter(Boolean)
    if (!set.includes(ext)) {
      ElMessage.error(`不允许的文件类型：.${ext || '（无扩展名）'}`)
      return false
    }
  }
  return true
}

function handleExceed() {
  ElMessage.warning(`最多只能上传 ${limitCount.value} 个文件`)
}

function handleRemove() {
  emit('change', { fileList: fileList.value, results: results.value })
}

function handleSuccess(response, uploadFile) {
  const data = response?.data ?? response
  if (data) {
    results.value = [...results.value, data]
  }
  emit('success', { response, file: uploadFile, results: results.value })
  emit('change', { fileList: fileList.value, results: results.value })
}

function handleError(err) {
  emit('error', err)
}

async function handleHttpRequest(options) {
  const fn = props.uploadFn || uploadCommonFile
  try {
    const file = await maybeCompressImageFile(options.file, rule.value || {})
    const res = await fn(file, classifyKey.value)
    options.onSuccess(res)
  } catch (e) {
    options.onError(e)
  }
}

/** 手动上传队列（autoUpload=false 时由父组件调用） */
function submit() {
  if (!ruleReady.value) {
    ElMessage.warning('请先选择有效分类')
    return Promise.reject(new Error('rule not ready'))
  }
  const pending = fileList.value.filter((f) => f.status === 'ready')
  if (!pending.length) {
    ElMessage.warning('请先选择文件')
    return Promise.reject(new Error('no files'))
  }
  return Promise.all(
    pending.map(
      (item) =>
        new Promise((resolve, reject) => {
          handleHttpRequest({
            file: item.raw,
            onSuccess: (res) => {
              item.status = 'success'
              handleSuccess(res, item)
              resolve(res)
            },
            onError: (err) => {
              item.status = 'fail'
              handleError(err)
              reject(err)
            }
          })
        })
    )
  ).then((res) => {
    clearFiles()
    return res
  })
}

function clearFiles() {
  uploadRef.value?.clearFiles()
  fileList.value = []
  results.value = []
}

/** 返回已选本地文件项（含 raw），供 autoUpload=false 时父组件校验与提交 */
function getFiles() {
  return fileList.value.filter((f) => f.raw)
}

defineExpose({ submit, clearFiles, getFiles, reloadRule: () => loadRule(classifyKey.value) })
</script>

<style scoped>
.c7-upload__icon {
  font-size: 48px;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.c7-upload__text em {
  color: var(--el-color-primary);
  font-style: normal;
}

.c7-upload__tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.c7-upload__tip--warn {
  color: var(--el-color-warning);
}

:deep(.el-upload-dragger) {
  padding: 24px 16px;
}
</style>
