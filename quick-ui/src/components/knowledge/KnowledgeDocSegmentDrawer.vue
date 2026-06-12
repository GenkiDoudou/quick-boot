<template>
  <el-drawer
    :model-value="modelValue"
    :title="docDetail?.title || '文档分段'"
    size="560px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @open="loadData"
  >
    <div v-loading="loading" class="kb-segment-drawer">
      <el-descriptions v-if="docDetail" :column="1" border size="small" class="kb-segment-drawer__meta">
        <el-descriptions-item label="来源">{{ sourceLabel(docDetail.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(docDetail.docStatus) }}</el-descriptions-item>
        <el-descriptions-item label="分块数">{{ docDetail.chunkCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item v-if="docDetail.sourceUrl" label="来源 URL">{{ docDetail.sourceUrl }}</el-descriptions-item>
        <el-descriptions-item label="分段模式">{{ docDetail.segmentMode || 'AUTO' }}</el-descriptions-item>
      </el-descriptions>

      <div class="kb-segment-drawer__list-head">
        <span>分段列表</span>
        <el-tag size="small">{{ chunks.length }} 段</el-tag>
      </div>

      <el-empty v-if="!loading && !chunks.length" description="暂无分段（文档可能仍在构建中）" />
      <div v-else class="kb-segment-drawer__list">
        <div
          v-for="chunk in chunks"
          :key="chunk.chunkId"
          class="kb-segment-drawer__item"
          :class="{ 'is-disabled': chunk.enabled === 0 }"
        >
          <div class="kb-segment-drawer__item-head">
            <span class="kb-segment-drawer__index">#{{ (chunk.chunkIndex ?? 0) + 1 }}</span>
            <span v-if="chunk.tokenCount != null" class="kb-segment-drawer__tokens">{{ chunk.tokenCount }} tokens</span>
            <span v-if="chunk.pageNumber" class="kb-segment-drawer__page">第 {{ chunk.pageNumber }} 页</span>
            <el-switch
              v-model="chunk.enabled"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              :loading="chunk._toggling"
              v-hasPermi="['knowledge:doc:reindex']"
              @change="(val) => onToggleEnabled(chunk, val)"
            />
            <el-button link type="primary" size="small" v-hasPermi="['knowledge:doc:reindex']" @click="openEdit(chunk)">编辑</el-button>
            <el-button link type="primary" size="small" @click="copyText(displayContent(chunk))">复制</el-button>
          </div>
          <p class="kb-segment-drawer__preview">{{ displayContent(chunk) }}</p>
        </div>
      </div>
    </div>
  </el-drawer>

  <el-dialog v-model="editVisible" title="编辑分段" width="560px" destroy-on-close @closed="editChunk = null">
    <el-input v-model="editContent" type="textarea" :rows="12" maxlength="32000" show-word-limit />
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editSaving" v-hasPermi="['knowledge:doc:reindex']" @click="saveEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocument, listDocumentChunks, updateDocumentChunk } from '@/api/knowledge/doc'

defineOptions({ name: 'KnowledgeDocSegmentDrawer' })

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  docId: { type: [String, Number], default: null }
})

const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const docDetail = ref(null)
const chunks = ref([])

const editVisible = ref(false)
const editChunk = ref(null)
const editContent = ref('')
const editSaving = ref(false)

const SOURCE_MAP = { FILE: '文件', MANUAL: '手动', WEB: '网页', LIBRARY: '文档库' }
const STATUS_MAP = {
  PENDING: '待入库',
  PARSING: '构建中',
  INDEXED: '已完成',
  FAILED: '失败'
}

function sourceLabel(type) {
  return SOURCE_MAP[type] || type || '—'
}

function statusLabel(status) {
  return STATUS_MAP[status] || status || '—'
}

function displayContent(chunk) {
  return chunk.content || chunk.contentPreview || ''
}

function loadData() {
  if (!props.docId) return
  loading.value = true
  Promise.all([getDocument(props.docId), listDocumentChunks(props.docId)])
    .then(([docRes, chunkRes]) => {
      docDetail.value = docRes?.data || null
      chunks.value = (chunkRes?.data || []).map((item) => ({
        ...item,
        enabled: item.enabled == null ? 1 : item.enabled,
        _toggling: false
      }))
    })
    .finally(() => {
      loading.value = false
    })
}

function onToggleEnabled(chunk, enabled) {
  chunk._toggling = true
  updateDocumentChunk({ chunkId: chunk.chunkId, enabled })
    .then(() => {
      ElMessage.success(enabled === 1 ? '已启用' : '已禁用')
    })
    .catch(() => {
      chunk.enabled = enabled === 1 ? 0 : 1
    })
    .finally(() => {
      chunk._toggling = false
    })
}

function openEdit(chunk) {
  editChunk.value = chunk
  editContent.value = displayContent(chunk)
  editVisible.value = true
}

function saveEdit() {
  const text = String(editContent.value || '').trim()
  if (!text) {
    ElMessage.warning('正文不能为空')
    return
  }
  if (!editChunk.value) return
  editSaving.value = true
  updateDocumentChunk({ chunkId: editChunk.value.chunkId, content: text })
    .then(() => {
      ElMessage.success('已保存并重嵌入')
      editVisible.value = false
      return loadData()
    })
    .finally(() => {
      editSaving.value = false
    })
}

async function copyText(text) {
  const content = String(text || '').trim()
  if (!content) return
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.kb-segment-drawer__meta {
  margin-bottom: 16px;
}

.kb-segment-drawer__list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
  font-size: 14px;
}

.kb-segment-drawer__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.kb-segment-drawer__item {
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: #fafafa;

  &.is-disabled {
    opacity: 0.65;
    background: #f5f5f5;
  }
}

.kb-segment-drawer__item-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.kb-segment-drawer__index {
  font-weight: 600;
  color: var(--el-color-primary);
}

.kb-segment-drawer__preview {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
