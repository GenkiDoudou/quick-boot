<template>
  <div class="ai-app-chat-panel">
    <div v-if="openingMessage" class="ai-app-chat-panel__opening">
      <el-alert :title="openingMessage" type="info" :closable="false" show-icon />
    </div>

    <div v-if="suggestedQuestions?.length" class="ai-app-chat-panel__suggestions">
      <el-button
        v-for="(q, idx) in suggestedQuestions"
        :key="idx"
        size="small"
        round
        @click="sendQuick(q)"
      >
        {{ q }}
      </el-button>
    </div>

    <div v-if="quickCommands?.length" class="ai-app-chat-panel__quick-cmds">
      <el-button
        v-for="(cmd, idx) in quickCommands"
        :key="idx"
        size="small"
        type="primary"
        plain
        @click="sendQuick(cmd.prompt)"
      >
        {{ cmd.label }}
      </el-button>
    </div>

    <div ref="scrollRef" class="ai-app-chat-panel__messages">
      <div
        v-for="msg in messages"
        :key="msg.id || msg._localId"
        class="ai-app-chat-panel__msg"
        :class="`ai-app-chat-panel__msg--${msg.role}`"
      >
        <div class="ai-app-chat-panel__bubble-row">
          <div
            v-if="msg.role === 'assistant' || msg.role === 'tool'"
            class="ai-app-chat-panel__bubble ai-app-chat-panel__bubble--md"
            v-html="renderAssistantContent(msg.content)"
          ></div>
          <div v-else class="ai-app-chat-panel__bubble">{{ msg.content }}</div>
          <el-dropdown
            v-if="msg.content && (msg.role === 'assistant' || msg.role === 'tool')"
            trigger="click"
            @command="(fmt) => copyMessage(msg.content, fmt)"
          >
            <el-button link type="primary" size="small" class="ai-app-chat-panel__copy">复制</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="text">复制为文本</el-dropdown-item>
                <el-dropdown-item command="markdown">复制为 Markdown</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            v-else-if="msg.content"
            link
            type="primary"
            size="small"
            class="ai-app-chat-panel__copy"
            @click="copyMessage(msg.content, 'text')"
          >
            复制
          </el-button>
        </div>
        <div v-if="msg.metadata?.webSearch" class="ai-app-chat-panel__meta">
          <el-tag size="small" type="success">联网搜索</el-tag>
        </div>
        <div v-if="msg.metadata?.citations?.length" class="ai-app-chat-panel__citations">
          <div class="ai-app-chat-panel__citations-title">引用来源</div>
          <div v-for="(c, i) in msg.metadata.citations" :key="i" class="ai-app-chat-panel__citation">
            {{ c.title || c.source || c }}
          </div>
        </div>
      </div>
      <div v-if="streaming" class="ai-app-chat-panel__msg ai-app-chat-panel__msg--assistant">
        <div class="ai-app-chat-panel__bubble-row">
          <div
            class="ai-app-chat-panel__bubble ai-app-chat-panel__bubble--md"
            v-html="renderAssistantContent(streamBuffer)"
          ></div>
          <span v-if="streaming" class="ai-app-chat-panel__cursor">▌</span>
          <el-dropdown
            v-if="streamBuffer"
            trigger="click"
            @command="(fmt) => copyMessage(streamBuffer, fmt)"
          >
            <el-button link type="primary" size="small" class="ai-app-chat-panel__copy">复制</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="text">复制为文本</el-dropdown-item>
                <el-dropdown-item command="markdown">复制为 Markdown</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div v-if="toolStatus" class="ai-app-chat-panel__tool-status">{{ toolStatus }}</div>
    </div>

    <div class="ai-app-chat-panel__input">
      <div v-if="showWebSearch" class="ai-app-chat-panel__toolbar">
        <el-switch v-model="webSearch" active-text="联网搜索" />
      </div>
      <el-input
        v-model="input"
        type="textarea"
        :rows="3"
        placeholder="输入消息，Enter 发送，Shift+Enter 换行"
        :disabled="streaming || !sessionId"
        @keydown.enter="onEnter"
      />
      <div class="ai-app-chat-panel__actions">
        <el-button type="primary" :loading="streaming" :disabled="!sessionId || !input.trim()" @click="send">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { markdownToPlainText, renderMarkdownToHtml } from '@/utils/markdown'

const props = defineProps({
  sessionId: { type: [Number, String], default: null },
  messages: { type: Array, default: () => [] },
  streaming: { type: Boolean, default: false },
  streamBuffer: { type: String, default: '' },
  toolStatus: { type: String, default: '' },
  openingMessage: { type: String, default: '' },
  suggestedQuestions: { type: Array, default: () => [] },
  quickCommands: { type: Array, default: () => [] },
  showWebSearch: { type: Boolean, default: false },
  webSearchModel: { type: Boolean, default: false }
})

const emit = defineEmits(['send'])

const input = ref('')
const webSearch = ref(false)
const scrollRef = ref(null)

watch(
  () => [props.messages.length, props.streamBuffer, props.streaming],
  () => {
    nextTick(() => {
      if (scrollRef.value) {
        scrollRef.value.scrollTop = scrollRef.value.scrollHeight
      }
    })
  }
)

function sendQuick(text) {
  if (!text?.trim() || props.streaming || !props.sessionId) return
  input.value = text
  send()
}

function onEnter(e) {
  if (e.shiftKey) return
  e.preventDefault()
  send()
}

function send() {
  const text = input.value.trim()
  if (!text || !props.sessionId || props.streaming) return
  if (props.showWebSearch && webSearch.value && !props.webSearchModel) {
    ElMessage.warning('当前模型不支持联网搜索，已忽略开关')
  }
  emit('send', {
    message: text,
    webSearch: props.showWebSearch && webSearch.value && props.webSearchModel
  })
  input.value = ''
}

function renderAssistantContent(content) {
  return renderMarkdownToHtml(content)
}

async function copyMessage(content, format) {
  if (!content) return
  const text = format === 'markdown' ? content : markdownToPlainText(content)
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(format === 'markdown' ? '已复制为 Markdown' : '已复制为文本')
  } catch {
    ElMessage.error('复制失败，请手动选择文本复制')
  }
}

defineExpose({ webSearch })
</script>

<style scoped>
.ai-app-chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--el-bg-color);
}

.ai-app-chat-panel__opening,
.ai-app-chat-panel__suggestions,
.ai-app-chat-panel__quick-cmds {
  padding: 8px 12px 0;
  flex-shrink: 0;
}

.ai-app-chat-panel__suggestions,
.ai-app-chat-panel__quick-cmds {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ai-app-chat-panel__messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  min-height: 0;
}

.ai-app-chat-panel__msg {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
}

.ai-app-chat-panel__msg--user {
  align-items: flex-end;
}

.ai-app-chat-panel__msg--assistant,
.ai-app-chat-panel__msg--tool {
  align-items: flex-start;
}

.ai-app-chat-panel__bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  max-width: 90%;
}

.ai-app-chat-panel__msg--user .ai-app-chat-panel__bubble-row {
  flex-direction: row-reverse;
}

.ai-app-chat-panel__copy {
  flex-shrink: 0;
  padding: 4px 0;
  user-select: none;
}

.ai-app-chat-panel__bubble {
  max-width: 100%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  user-select: text;
}

.ai-app-chat-panel__bubble--md {
  white-space: normal;
}

.ai-app-chat-panel__bubble--md :deep(p) {
  margin: 0 0 0.6em;
}

.ai-app-chat-panel__bubble--md :deep(p:last-child) {
  margin-bottom: 0;
}

.ai-app-chat-panel__bubble--md :deep(h1),
.ai-app-chat-panel__bubble--md :deep(h2),
.ai-app-chat-panel__bubble--md :deep(h3),
.ai-app-chat-panel__bubble--md :deep(h4) {
  margin: 0.4em 0 0.5em;
  font-size: 1em;
  font-weight: 600;
  line-height: 1.4;
}

.ai-app-chat-panel__bubble--md :deep(ul),
.ai-app-chat-panel__bubble--md :deep(ol) {
  margin: 0.4em 0;
  padding-left: 1.4em;
}

.ai-app-chat-panel__bubble--md :deep(li) {
  margin: 0.2em 0;
}

.ai-app-chat-panel__bubble--md :deep(code) {
  padding: 0.1em 0.35em;
  border-radius: 4px;
  background: var(--el-fill-color);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.9em;
}

.ai-app-chat-panel__bubble--md :deep(pre) {
  margin: 0.5em 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-darker, var(--el-fill-color));
  overflow-x: auto;
}

.ai-app-chat-panel__bubble--md :deep(pre code) {
  padding: 0;
  background: transparent;
}

.ai-app-chat-panel__bubble--md :deep(blockquote) {
  margin: 0.5em 0;
  padding-left: 10px;
  border-left: 3px solid var(--el-border-color);
  color: var(--el-text-color-secondary);
}

.ai-app-chat-panel__bubble--md :deep(table) {
  border-collapse: collapse;
  margin: 0.5em 0;
  font-size: 0.92em;
}

.ai-app-chat-panel__bubble--md :deep(th),
.ai-app-chat-panel__bubble--md :deep(td) {
  border: 1px solid var(--el-border-color-lighter);
  padding: 4px 8px;
}

.ai-app-chat-panel__cursor {
  flex-shrink: 0;
  align-self: flex-end;
  padding-bottom: 10px;
  animation: blink 1s step-end infinite;
}

.ai-app-chat-panel__msg--user .ai-app-chat-panel__bubble {
  background: var(--el-color-primary-light-9);
  color: var(--el-text-color-primary);
}

.ai-app-chat-panel__msg--assistant .ai-app-chat-panel__bubble,
.ai-app-chat-panel__msg--tool .ai-app-chat-panel__bubble {
  background: var(--el-fill-color-light);
}

@keyframes blink {
  50% { opacity: 0; }
}

.ai-app-chat-panel__meta,
.ai-app-chat-panel__citations {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.ai-app-chat-panel__tool-status {
  font-size: 12px;
  color: var(--el-color-warning);
  padding: 4px 0;
}

.ai-app-chat-panel__input {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 12px;
  flex-shrink: 0;
}

.ai-app-chat-panel__toolbar {
  margin-bottom: 8px;
}

.ai-app-chat-panel__actions {
  margin-top: 8px;
  text-align: right;
}
</style>
