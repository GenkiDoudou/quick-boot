<template>
  <div class="ai-app-chat-page">
    <div class="ai-app-chat-page__header">
      <div class="ai-app-chat-page__title">
        <el-button link @click="goBack"><el-icon><ArrowLeft /></el-icon></el-button>
        <span>{{ app.name || '演示聊天' }}</span>
      </div>
      <el-button type="success" @click="publishVisible = true" v-hasPermi="['aiapp:publish']">发布设置</el-button>
    </div>

    <div class="ai-app-chat-page__body">
      <aside v-if="multiSession" class="ai-app-chat-page__sessions">
        <div class="ai-app-chat-page__sessions-header">
          <span>会话</span>
          <el-button link type="primary" @click="newSession">新建</el-button>
        </div>
        <div
          v-for="s in sessions"
          :key="s.id"
          class="ai-app-chat-page__session-item"
          :class="{ 'is-active': s.id === sessionId }"
          @click="selectSession(s.id)"
        >
          <span class="ai-app-chat-page__session-title">{{ s.title || '新会话' }}</span>
          <el-button link type="danger" size="small" @click.stop="deleteSession(s.id)">删</el-button>
        </div>
      </aside>

      <main class="ai-app-chat-page__main">
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
      </main>
    </div>

    <PublishDialog
      v-model="publishVisible"
      :app-id="appId"
      :app-name="app.name"
      :status="app.status"
      @published="loadApp"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listModelOptions } from '@/api/ai/model'
import {
  addAiAppSession,
  getAiAppInfo,
  listAiAppSession,
  removeAiAppSession
} from '@/api/ai/app'
import AiAppChatPanel from '../components/AiAppChatPanel.vue'
import PublishDialog from '../components/PublishDialog.vue'
import { useAiAppChat } from '../composables/useAiAppChat'

defineOptions({ name: 'AiAppChat' })

const route = useRoute()
const router = useRouter()
const appId = computed(() => String(route.params.appId || ''))

const app = reactive({ name: '', status: 'draft', appType: 'agent' })
const chatConfig = reactive({
  openingMessage: '',
  suggestedQuestions: [],
  quickCommands: [],
  chatModelId: null,
  multiSession: true
})

const sessions = ref([])
const publishVisible = ref(false)
const chatModels = ref([])
const {
  sessionId,
  messages,
  streaming,
  streamBuffer,
  toolStatus,
  selectSession: activateChatSession,
  resetChat,
  sendChat
} = useAiAppChat()

const multiSession = computed(() => chatConfig.multiSession !== false)

const isQwenModel = computed(() => {
  const m = chatModels.value.find((item) => item.modelId === chatConfig.chatModelId)
  if (!m) return false
  const code = (m.code || '').toLowerCase()
  const provider = (m.provider || '').toLowerCase()
  return code.includes('qwen') || provider.includes('dashscope') || provider.includes('aliyun')
})

onMounted(async () => {
  const models = await listModelOptions('CHAT')
  chatModels.value = models.data || []
  await loadApp()
  await loadSessions()
})

async function loadApp() {
  const res = await getAiAppInfo(appId.value)
  Object.assign(app, res.data || {})
  const json = app.status === 'published' ? res.data?.publishedConfigJson : res.data?.configJson
  if (json) {
    try {
      const parsed = JSON.parse(json)
      Object.assign(chatConfig, {
        openingMessage: parsed.openingMessage || '',
        suggestedQuestions: parsed.suggestedQuestions || [],
        quickCommands: parsed.quickCommands || [],
        chatModelId: parsed.chatModelId || null,
        multiSession: parsed.multiSession !== false
      })
    } catch {
      // ignore
    }
  }
}

async function loadSessions() {
  const res = await listAiAppSession(appId.value)
  sessions.value = res.data || []
  if (sessions.value.length) {
    activateChatSession(sessions.value[0].id)
  } else {
    await newSession()
  }
}

async function newSession() {
  const res = await addAiAppSession({ appId: appId.value, title: '新会话' })
  await loadSessions()
  activateChatSession(res.data)
}

function selectSession(id) {
  activateChatSession(id)
}

async function deleteSession(id) {
  await ElMessageBox.confirm('确认删除该会话？', '提示', { type: 'warning' })
  await removeAiAppSession(id)
  if (sessionId.value === id) {
    resetChat()
  }
  await loadSessions()
  ElMessage.success('已删除')
}

function onSend({ message, webSearch }) {
  if (app.status !== 'published') {
    ElMessage.warning('应用未发布，请先在编排页预览或发布后再演示')
    return
  }
  sendChat({ appId: appId.value, message, preview: false, webSearch })
}

function goBack() {
  router.push('/ai/app/list')
}
</script>

<style scoped>
.ai-app-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
}

.ai-app-chat-page__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.ai-app-chat-page__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.ai-app-chat-page__body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.ai-app-chat-page__sessions {
  width: 240px;
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
}

.ai-app-chat-page__sessions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  font-weight: 600;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.ai-app-chat-page__session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
}

.ai-app-chat-page__session-item:hover,
.ai-app-chat-page__session-item.is-active {
  background: var(--el-color-primary-light-9);
}

.ai-app-chat-page__session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.ai-app-chat-page__main {
  flex: 1;
  min-width: 0;
}
</style>
