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
        <div class="ai-app-chat-panel__bubble">{{ msg.content }}</div>
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
        <div class="ai-app-chat-panel__bubble">{{ streamBuffer }}<span class="ai-app-chat-panel__cursor">▌</span></div>
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

.ai-app-chat-panel__bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-app-chat-panel__msg--user .ai-app-chat-panel__bubble {
  background: var(--el-color-primary-light-9);
  color: var(--el-text-color-primary);
}

.ai-app-chat-panel__msg--assistant .ai-app-chat-panel__bubble,
.ai-app-chat-panel__msg--tool .ai-app-chat-panel__bubble {
  background: var(--el-fill-color-light);
}

.ai-app-chat-panel__cursor {
  animation: blink 1s step-end infinite;
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
