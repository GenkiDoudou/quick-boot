<template>
  <c7-dialog v-model="visible" title="添加文档" width="720px" @closed="resetWizard">
    <el-steps :active="wizardStep" finish-status="success" align-center class="kb-doc-wizard__steps">
      <el-step title="选择来源" />
      <el-step title="分段与清洗" />
      <el-step title="分段预览" />
    </el-steps>

    <div v-show="wizardStep === 0" class="kb-doc-wizard__body">
      <el-tabs v-model="sourceTab">
        <el-tab-pane label="文件上传" name="file">
          <C7Upload
            v-if="visible && kbId"
            ref="c7UploadRef"
            :key="'file-' + uploadSessionKey"
            classify="knowledge"
            :auto-upload="false"
            :limit="1"
            v-model:results="uploadResults"
          />
        </el-tab-pane>
        <el-tab-pane label="手动录入" name="manual">
          <el-input v-model="manualTitle" placeholder="文档标题" maxlength="255" class="kb-doc-wizard__gap" />
          <el-input v-model="manualContent" type="textarea" :rows="10" placeholder="正文（纯文本或 Markdown）" />
        </el-tab-pane>
        <el-tab-pane label="网页录入" name="web">
          <el-input v-model="webUrl" placeholder="https://..." class="kb-doc-wizard__gap" />
          <el-input v-model="webTitle" placeholder="可选标题" maxlength="255" />
        </el-tab-pane>
        <el-tab-pane label="文档库" name="library">
          <el-button type="primary" link @click="libraryPickerVisible = true">选择文档库文件</el-button>
          <div v-if="libFile" class="kb-doc-wizard__picked">
            已选：{{ libFile.title }}（ID: {{ libFile.libFileId }}）
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-show="wizardStep === 1" class="kb-doc-wizard__body">
      <el-form label-width="120px">
        <el-form-item label="继承知识库默认">
          <el-switch v-model="customizeSegment" active-text="自定义本次设置" inactive-text="使用库默认" />
        </el-form-item>
        <template v-if="!customizeSegment">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="分段模式">{{ segmentPreview.segmentMode }}</el-descriptions-item>
            <el-descriptions-item label="分块大小">{{ segmentPreview.chunkSize }} tokens</el-descriptions-item>
            <el-descriptions-item label="分块重叠">{{ segmentPreview.chunkOverlap }} tokens</el-descriptions-item>
          </el-descriptions>
        </template>
        <template v-else>
          <el-form-item label="分段模式">
            <el-radio-group v-model="segment.segmentMode">
              <el-radio label="AUTO">自动</el-radio>
              <el-radio label="CUSTOM">自定义</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="segment.segmentMode === 'CUSTOM'" label="分隔符">
            <el-select v-model="segment.chunkDelimiter" style="width: 100%">
              <el-option label="单换行" value="SINGLE_NEWLINE" />
              <el-option label="双换行" value="DOUBLE_NEWLINE" />
            </el-select>
          </el-form-item>
          <el-form-item label="分块大小">
            <el-input-number v-model="segment.chunkSize" :min="128" :max="4096" style="width: 100%" />
          </el-form-item>
          <el-form-item label="分块重叠">
            <el-input-number v-model="segment.chunkOverlap" :min="0" :max="512" style="width: 100%" />
          </el-form-item>
          <el-form-item label="预处理">
            <el-checkbox v-model="segment.preprocessNormalizeWs">归一化连续空白</el-checkbox>
            <el-checkbox v-model="segment.preprocessRemoveUrl">删除 URL</el-checkbox>
            <el-checkbox v-model="segment.preprocessRemoveEmail">删除邮箱</el-checkbox>
          </el-form-item>
        </template>
      </el-form>
    </div>

    <div v-show="wizardStep === 2" v-loading="previewLoading" class="kb-doc-wizard__body">
      <div class="kb-doc-wizard__preview-head">
        <span>共 {{ previewData.total ?? 0 }} 段</span>
        <el-tag v-if="previewData.truncated" size="small" type="warning">仅展示前 200 段</el-tag>
      </div>
      <el-empty v-if="!previewLoading && !previewSegmentList.length" description="暂无预览分段" />
      <div v-else class="kb-doc-wizard__preview-list">
        <div v-for="item in previewSegmentList" :key="item.chunkIndex" class="kb-doc-wizard__preview-item">
          <div class="kb-doc-wizard__preview-item-head">
            <span>#{{ (item.chunkIndex ?? 0) + 1 }}</span>
            <span v-if="item.tokenCount != null">{{ item.tokenCount }} tokens</span>
          </div>
          <p class="kb-doc-wizard__preview-text">{{ item.content }}</p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="kb-doc-wizard__footer">
        <el-button @click="visible = false">取消</el-button>
        <div class="kb-doc-wizard__footer-actions">
          <el-button v-if="wizardStep > 0" @click="wizardStep -= 1">上一步</el-button>
          <el-button type="primary" :loading="submitting || previewLoading" @click="onPrimaryClick">
            {{ primaryButtonLabel }}
          </el-button>
        </div>
      </div>
    </template>
  </c7-dialog>

  <c7-dialog v-model="libraryPickerVisible" title="选择文档库文件" width="800px" :on-confirm="confirmLibraryPick" @open="loadLibraryTree">
    <el-row :gutter="12">
      <el-col :span="8">
        <el-tree
          node-key="folderId"
          :data="libraryTree"
          :props="{ label: 'name', children: 'children' }"
          highlight-current
          @node-click="onLibraryFolderClick"
        />
      </el-col>
      <el-col :span="16">
        <el-table :data="libraryFiles" height="320" highlight-current-row @current-change="onLibraryFilePick">
          <el-table-column prop="title" label="文件名" min-width="160" />
          <el-table-column prop="fileExt" label="类型" width="70" />
        </el-table>
      </el-col>
    </el-row>
  </c7-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getKnowledgeBase } from '@/api/knowledge/base'
import {
  addFromLibraryDocument,
  addFromWebDocument,
  addManualDocument,
  previewSegments as previewSegmentsApi,
  previewSegmentsFile,
  uploadDocument
} from '@/api/knowledge/doc'
import { listLibraryFile, listLibraryFolderTree } from '@/api/knowledge/library'
import { buildSegmentConfig, defaultSegmentForm, segmentFormFromKb } from '@/api/knowledge/segment'

/**
 * 知识库文档入库向导：四来源 + 分段配置，绑定固定 kbId。
 */
defineOptions({ name: 'KnowledgeDocWizard' })

const props = defineProps({
  /** 显隐 */
  modelValue: { type: Boolean, default: false },
  /** 目标知识库 ID（字符串，Snowflake 不可转 Number） */
  kbId: { type: String, required: true },
  /** 初始来源 Tab */
  initialSourceTab: { type: String, default: 'file' }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const wizardStep = ref(0)
const submitting = ref(false)
const previewLoading = ref(false)
const previewData = ref({ total: 0, truncated: false, segments: [] })
const previewSegmentList = computed(() => previewData.value.segments || [])
const sourceTab = ref('file')
const manualTitle = ref('')
const manualContent = ref('')
const webUrl = ref('')
const webTitle = ref('')
const libFile = ref(null)
const customizeSegment = ref(false)
const segment = ref(defaultSegmentForm())
const kbDetail = ref(null)
const segmentPreview = computed(() => segmentFormFromKb(kbDetail.value))

const primaryButtonLabel = computed(() => {
  if (wizardStep.value === 0 || wizardStep.value === 1) return '下一步'
  return '确认入库'
})

const c7UploadRef = ref(null)
const uploadSessionKey = ref(0)
const uploadResults = ref([])

const libraryPickerVisible = ref(false)
const libraryTree = ref([])
const libraryFiles = ref([])
const libraryPickTemp = ref(null)

watch(
  () => [props.modelValue, props.kbId, props.initialSourceTab],
  ([open, kbId, tab]) => {
    if (open && kbId) {
      uploadSessionKey.value += 1
      wizardStep.value = 0
      sourceTab.value = tab || 'file'
      manualTitle.value = ''
      manualContent.value = ''
      webUrl.value = ''
      webTitle.value = ''
      libFile.value = null
      customizeSegment.value = false
      uploadResults.value = []
      previewData.value = { total: 0, truncated: false, segments: [] }
      getKnowledgeBase(kbId).then((res) => {
        kbDetail.value = res?.data || null
        segment.value = segmentFormFromKb(kbDetail.value)
      })
    }
  }
)

function resetWizard() {
  wizardStep.value = 0
  previewData.value = { total: 0, truncated: false, segments: [] }
  c7UploadRef.value?.clearFiles?.()
}

function validateSourceStep() {
  const tab = sourceTab.value
  if (tab === 'file') {
    const files = c7UploadRef.value?.getFiles?.() || []
    if (!files.length) {
      ElMessage.warning('请选择文件')
      return false
    }
  } else if (tab === 'manual') {
    if (!String(manualTitle.value || '').trim() || !String(manualContent.value || '').trim()) {
      ElMessage.warning('请填写标题与正文')
      return false
    }
  } else if (tab === 'web') {
    if (!String(webUrl.value || '').trim()) {
      ElMessage.warning('请输入 URL')
      return false
    }
  } else if (tab === 'library') {
    if (!libFile.value?.libFileId) {
      ElMessage.warning('请选择文档库文件')
      return false
    }
  }
  return true
}

const SOURCE_TYPE_MAP = { file: 'FILE', manual: 'MANUAL', web: 'WEB', library: 'LIBRARY' }

function loadSegmentPreview() {
  const seg = buildSegmentConfig(customizeSegment.value, segment.value)
  const kbId = String(props.kbId)
  const tab = sourceTab.value
  previewLoading.value = true
  let req
  if (tab === 'file') {
    const files = c7UploadRef.value?.getFiles?.() || []
    const raw = files[0]?.raw
    if (!raw) {
      previewLoading.value = false
      return Promise.reject(new Error('no file'))
    }
    req = previewSegmentsFile(kbId, raw, seg)
  } else {
    const payload = { kbId, sourceType: SOURCE_TYPE_MAP[tab], segmentConfig: seg }
    if (tab === 'manual') {
      payload.title = manualTitle.value.trim()
      payload.content = manualContent.value
    } else if (tab === 'web') {
      payload.url = webUrl.value.trim()
      payload.title = webTitle.value.trim() || undefined
    } else {
      payload.libFileId = libFile.value.libFileId
    }
    req = previewSegmentsApi(payload)
  }
  return req
    .then((res) => {
      const data = res?.data || {}
      previewData.value = {
        total: data.total ?? 0,
        truncated: !!data.truncated,
        segments: Array.isArray(data.segments) ? data.segments : []
      }
      if (!previewSegmentList.value.length) {
        ElMessage.warning('分块结果为空，请调整分段设置')
        return Promise.reject(new Error('empty preview'))
      }
    })
    .finally(() => {
      previewLoading.value = false
    })
}

async function onPrimaryClick() {
  if (wizardStep.value === 0) {
    if (!validateSourceStep()) return
    wizardStep.value = 1
    return
  }
  if (wizardStep.value === 1) {
    if (!validateSourceStep()) return
    try {
      await loadSegmentPreview()
      wizardStep.value = 2
    } catch {
      // 保持当前步
    }
    return
  }
  submitting.value = true
  try {
    await submitWizard()
    visible.value = false
    emit('success')
  } catch {
    // 保持弹窗
  } finally {
    submitting.value = false
  }
}

function submitWizard() {
  if (!validateSourceStep()) return Promise.reject(new Error('validate'))
  const seg = buildSegmentConfig(customizeSegment.value, segment.value)
  const kbId = String(props.kbId)
  const tab = sourceTab.value
  let req
  if (tab === 'file') {
    const files = c7UploadRef.value?.getFiles?.() || []
    const raw = files[0]?.raw
    if (!raw) {
      ElMessage.warning('请选择文件')
      return Promise.reject(new Error('no file'))
    }
    req = uploadDocument(kbId, raw, seg)
  } else if (tab === 'manual') {
    req = addManualDocument({
      kbId,
      title: manualTitle.value.trim(),
      content: manualContent.value,
      segmentConfig: seg
    })
  } else if (tab === 'web') {
    req = addFromWebDocument({
      kbId,
      url: webUrl.value.trim(),
      title: webTitle.value.trim() || undefined,
      segmentConfig: seg
    })
  } else {
    req = addFromLibraryDocument({
      kbId,
      libFileId: libFile.value.libFileId,
      segmentConfig: seg
    })
  }
  return req.then(() => ElMessage.success('已提交入库任务'))
}

function loadLibraryTree() {
  return listLibraryFolderTree().then((res) => {
    libraryTree.value = res?.data || []
  })
}

function onLibraryFolderClick(node) {
  listLibraryFile({ folderId: node.folderId, pageNum: 1, pageSize: 200 }).then((res) => {
    libraryFiles.value = res?.data?.records || []
  })
}

function onLibraryFilePick(row) {
  libraryPickTemp.value = row
}

function confirmLibraryPick() {
  if (!libraryPickTemp.value) {
    ElMessage.warning('请选择文件')
    return Promise.reject(new Error('no pick'))
  }
  libFile.value = libraryPickTemp.value
  libraryPickerVisible.value = false
  return Promise.resolve()
}
</script>

<style scoped>
.kb-doc-wizard__steps {
  margin-bottom: 20px;
}

.kb-doc-wizard__body {
  min-height: 280px;
}

.kb-doc-wizard__gap {
  margin-bottom: 12px;
}

.kb-doc-wizard__picked {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.kb-doc-wizard__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.kb-doc-wizard__footer-actions {
  display: flex;
  gap: 8px;
}

.kb-doc-wizard__preview-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
}

.kb-doc-wizard__preview-list {
  max-height: 360px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kb-doc-wizard__preview-item {
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: #fafafa;
}

.kb-doc-wizard__preview-item-head {
  display: flex;
  gap: 12px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.kb-doc-wizard__preview-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
