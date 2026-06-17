<template>
  <div class="ai-embed-page">
    <div v-if="loading" class="ai-embed-page__loading">加载中...</div>
    <div v-else-if="error" class="ai-embed-page__error">{{ error }}</div>
    <div v-else class="ai-embed-page__chat">
      <AiAppChatPanel
        :session-id="sessionId"
        :messages="messages"
        :streaming="streaming"
        :stream-buffer="streamBuffer"
        :tool-status="toolStatus"
        :opening-message="chatConfig.openingMessage"
        :suggested-questions="chatConfig.suggestedQuestions"
        :quick-commands="chatConfig.quickCommands"
        :show-web-search="isQwenModel"
        :web-search-model="isQwenModel"
        @send="onSend"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listModelOptions } from '@/api/ai/model'
import { getEmbedAppInfo, getEmbedVisitorId, addEmbedSession, listEmbedSession } from '@/api/ai/app'
import AiAppChatPanel from '../components/AiAppChatPanel.vue'
import { useAiAppChat } from '../composables/useAiAppChat'

defineOptions({ name: 'AiAppEmbed' })

const route = useRoute()
const token = computed(() => route.params.token)
const visitorId = getEmbedVisitorId()

const loading = ref(true)
const error = ref('')
const appId = ref(null)
const chatConfig = reactive({
  openingMessage: '',
  suggestedQuestions: [],
  quickCommands: [],
  chatModelId: null
})
const chatModels = ref([])

const {
  sessionId,
  messages,
  streaming,
  streamBuffer,
  toolStatus,
  selectSession,
  sendChat
} = useAiAppChat({
  mode: 'embed',
  embedToken: token.value,
  visitorId
})

const isQwenModel = computed(() => {
  const m = chatModels.value.find((item) => item.modelId === chatConfig.chatModelId)
  if (!m) return false
  const code = (m.code || '').toLowerCase()
  const provider = (m.provider || '').toLowerCase()
  return code.includes('qwen') || provider.includes('dashscope') || provider.includes('aliyun')
})

onMounted(async () => {
  try {
    const [models, appRes] = await Promise.all([
      listModelOptions('CHAT'),
      getEmbedAppInfo(token.value)
    ])
    chatModels.value = models.data || []
    const info = appRes.data || {}
    appId.value = info.appId
    chatConfig.openingMessage = info.openingMessage || ''
    chatConfig.suggestedQuestions = info.suggestedQuestions || []
    chatConfig.quickCommands = info.quickCommands || []
    chatConfig.chatModelId = info.chatModelId || null

    const sessions = await listEmbedSession(token.value, visitorId)
    if (sessions.data?.length) {
      selectSession(sessions.data[0].id)
    } else {
      const res = await addEmbedSession(token.value, visitorId)
      selectSession(res.data)
    }
  } catch (e) {
    error.value = e?.message || '嵌入页加载失败，请检查令牌与白名单'
  } finally {
    loading.value = false
  }
})

function onSend({ message, webSearch }) {
  sendChat({
    appId: appId.value || 0,
    message,
    preview: false,
    webSearch
  })
}
</script>

<style scoped>
.ai-embed-page {
  height: 100vh;
  background: var(--el-bg-color);
}

.ai-embed-page__loading,
.ai-embed-page__error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-text-color-secondary);
}

.ai-embed-page__error {
  color: var(--el-color-danger);
  padding: 24px;
  text-align: center;
}

.ai-embed-page__chat {
  height: 100%;
}
</style>
