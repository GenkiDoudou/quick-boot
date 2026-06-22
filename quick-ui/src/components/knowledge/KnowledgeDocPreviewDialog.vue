<template>
  <c7-dialog
    v-model="visible"
    :title="preview?.title || '文档预览'"
    width="880px"
    :show-confirm="false"
    @closed="onClosed"
  >
    <div v-loading="loading" class="kb-doc-preview">
      <el-alert
        v-if="preview?.docStatus === 'PARSING' || preview?.docStatus === 'PENDING'"
        type="info"
        :closable="false"
        show-icon
        title="文档正在构建索引，以下为已可用的预览内容。"
        class="kb-doc-preview__alert"
      />

      <div v-if="preview?.previewMode === 'WEB' && preview?.sourceUrl" class="kb-doc-preview__web">
        <span class="kb-doc-preview__web-label">来源网页：</span>
        <el-link :href="preview.sourceUrl" target="_blank" type="primary">{{ preview.sourceUrl }}</el-link>
      </div>

      <el-tabs v-if="hasTabs" v-model="activeTab" class="kb-doc-preview__tabs">
        <el-tab-pane v-if="preview?.streamable && blobUrl" label="原文" name="file">
          <iframe v-if="preview?.previewMode === 'PDF'" :src="blobUrl" class="kb-doc-preview__iframe" title="PDF 预览" />
          <iframe
            v-else-if="preview?.previewMode === 'HTML'"
            :src="blobUrl"
            class="kb-doc-preview__iframe"
            sandbox="allow-same-origin"
            title="HTML 预览"
          />
        </el-tab-pane>
        <el-tab-pane v-if="hasText" :label="textTabLabel" name="text">
          <div class="kb-doc-preview__text-wrap">
            <el-tag v-if="preview?.textTruncated" size="small" type="warning" class="kb-doc-preview__trunc-tag">
              内容已截断，仅展示前部
            </el-tag>
            <pre class="kb-doc-preview__text">{{ preview.textContent }}</pre>
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-empty
        v-else-if="!loading && !hasText && !(preview?.streamable && blobUrl)"
        description="暂无可预览内容（请等待入库完成或查看分段）"
      />
    </div>
  </c7-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocumentPreviewInfo, previewDocumentStream } from '@/api/knowledge/doc'

/**
 * 知识库文档预览弹窗：PDF/HTML 原文 + 文本/分段内容。
 */
defineOptions({ name: 'KnowledgeDocPreviewDialog' })

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  docId: { type: [String, Number], default: null }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const preview = ref(null)
const blobUrl = ref('')
const activeTab = ref('file')

const hasText = computed(() => !!String(preview.value?.textContent || '').trim())

const hasTabs = computed(() => {
  const stream = !!(preview.value?.streamable && blobUrl.value)
  return stream || hasText.value
})

const textTabLabel = computed(() => {
  const mode = preview.value?.previewMode
  if (mode === 'CHUNKS' || mode === 'OFFICE') return '解析文本'
  if (mode === 'MARKDOWN') return 'Markdown'
  return '文本'
})

watch(
  () => [props.modelValue, props.docId],
  ([open, docId]) => {
    if (open && docId) {
      loadPreview(docId)
    }
  }
)

async function loadPreview(docId) {
  loading.value = true
  revokeBlob()
  preview.value = null
  activeTab.value = 'file'
  try {
    const res = await getDocumentPreviewInfo(docId)
    preview.value = res?.data || null
    if (preview.value?.streamable) {
      const blobRes = await previewDocumentStream(docId)
      const raw = blobRes?.data
      if (raw instanceof Blob) {
        blobUrl.value = URL.createObjectURL(raw)
      }
    }
    if (!(preview.value?.streamable && blobUrl.value) && String(preview.value?.textContent || '').trim()) {
      activeTab.value = 'text'
    }
    if (!preview.value?.streamable && !String(preview.value?.textContent || '').trim()) {
      ElMessage.warning('暂无可预览内容')
    }
  } catch {
    visible.value = false
  } finally {
    loading.value = false
  }
}

function revokeBlob() {
  if (blobUrl.value) {
    URL.revokeObjectURL(blobUrl.value)
    blobUrl.value = ''
  }
}

function onClosed() {
  revokeBlob()
  preview.value = null
}
</script>

<style scoped>
.kb-doc-preview {
  min-height: 420px;
}

.kb-doc-preview__alert {
  margin-bottom: 12px;
}

.kb-doc-preview__web {
  margin-bottom: 12px;
  font-size: 13px;
  word-break: break-all;
}

.kb-doc-preview__web-label {
  color: var(--el-text-color-secondary);
  margin-right: 6px;
}

.kb-doc-preview__tabs {
  min-height: 400px;
}

.kb-doc-preview__iframe {
  width: 100%;
  height: 520px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: #fff;
}

.kb-doc-preview__text-wrap {
  max-height: 520px;
  overflow: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: #fafafa;
  padding: 12px;
}

.kb-doc-preview__trunc-tag {
  margin-bottom: 8px;
}

.kb-doc-preview__text {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}
</style>
