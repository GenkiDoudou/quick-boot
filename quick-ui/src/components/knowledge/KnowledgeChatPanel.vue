<template>
  <div class="kb-chat-panel">
    <div class="kb-chat-panel__intro">
      <h3 class="kb-chat-panel__title">对话测试</h3>
      <p class="kb-chat-panel__desc">基于当前知识库向量检索 + 本地 LLM，与工作流知识库检索节点共用同一检索链路。</p>
    </div>

    <div class="kb-chat-panel__form">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        placeholder="请输入问题，Ctrl + Enter 提交"
        maxlength="4000"
        show-word-limit
        @keyup.ctrl.enter="handleAsk"
      />
      <div class="kb-chat-panel__actions">
        <el-checkbox v-model="useMcpTools">启用 MCP 工具</el-checkbox>
        <el-button type="primary" :loading="loading" v-hasPermi="['knowledge:chat']" @click="handleAsk">提问</el-button>
        <el-button @click="handleClear">清空</el-button>
      </div>
    </div>

    <div v-if="mcpToolsUsed.length" class="kb-chat-panel__mcp-tools">
      <span class="kb-chat-panel__block-title">MCP 工具：</span>
      <el-tag v-for="name in mcpToolsUsed" :key="name" size="small" type="warning" class="kb-chat-panel__mcp-tag">{{ name }}</el-tag>
    </div>

    <div v-if="loading || answer" v-loading="loading" class="kb-chat-panel__answer-block">
      <div class="kb-chat-panel__block-title">回答</div>
      <div v-if="!loading" class="kb-chat-panel__answer">{{ answer || '—' }}</div>
    </div>

    <div v-if="citations.length" class="kb-chat-panel__citations">
      <div class="kb-chat-panel__block-title">引用来源</div>
      <div v-for="(item, index) in citations" :key="item.chunkId || index" class="kb-chat-panel__citation">
        <div class="kb-chat-panel__citation-head">
          <span class="kb-chat-panel__citation-index">[{{ index + 1 }}]</span>
          <span>{{ item.fileName || '未知文件' }}</span>
          <el-tag size="small" type="primary">Score {{ formatScore(item.score) }}</el-tag>
        </div>
        <p class="kb-chat-panel__citation-text">{{ item.contentPreview }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chatKnowledge } from '@/api/knowledge/chat'

defineOptions({ name: 'KnowledgeChatPanel' })

const props = defineProps({
  /** 当前知识库 ID（字符串） */
  kbId: { type: String, required: true }
})

const question = ref('')
const useMcpTools = ref(true)
const loading = ref(false)
const answer = ref('')
const citations = ref([])
const mcpToolsUsed = ref([])

function formatScore(score) {
  if (score == null || Number.isNaN(Number(score))) return '—'
  return Number(score).toFixed(4)
}

function handleAsk() {
  if (!props.kbId) {
    ElMessage.warning('知识库无效')
    return
  }
  const q = String(question.value || '').trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  answer.value = ''
  citations.value = []
  mcpToolsUsed.value = []
  chatKnowledge({ kbId: props.kbId, question: q, useMcpTools: useMcpTools.value })
    .then((res) => {
      const data = res?.data || {}
      answer.value = data.answer || ''
      citations.value = Array.isArray(data.citations) ? data.citations : []
      mcpToolsUsed.value = Array.isArray(data.mcpToolsUsed) ? data.mcpToolsUsed : []
    })
    .catch(() => {
      answer.value = ''
      citations.value = []
      mcpToolsUsed.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function handleClear() {
  question.value = ''
  answer.value = ''
  citations.value = []
  mcpToolsUsed.value = []
}
</script>

<style scoped>
.kb-chat-panel__intro {
  margin-bottom: 16px;
}

.kb-chat-panel__title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
}

.kb-chat-panel__desc {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.kb-chat-panel__form {
  margin-bottom: 20px;
}

.kb-chat-panel__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.kb-chat-panel__mcp-tools {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
}

.kb-chat-panel__mcp-tag {
  margin-right: 0;
}

.kb-chat-panel__block-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.kb-chat-panel__answer {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
}

.kb-chat-panel__answer-block {
  margin-bottom: 20px;
}

.kb-chat-panel__citations {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kb-chat-panel__citation {
  padding: 12px 14px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border-left: 3px solid var(--el-color-primary);
}

.kb-chat-panel__citation-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.kb-chat-panel__citation-index {
  font-weight: 600;
  color: var(--el-color-primary);
}

.kb-chat-panel__citation-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
