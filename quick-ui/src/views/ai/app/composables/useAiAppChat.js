import { onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addAiAppSession,
  addEmbedSession,
  listAiAppMessage,
  listEmbedMessage,
  subscribeAiAppChatStream,
  subscribeEmbedChatStream
} from '@/api/ai/app'

/**
 * 解析消息 metadata_json。
 * @param {string|null} metadataJson
 */
export function parseMessageMetadata(metadataJson) {
  if (!metadataJson) return {}
  try {
    return JSON.parse(metadataJson)
  } catch {
    return {}
  }
}

/**
 * AI 应用聊天逻辑（管理端 / 嵌入页共用）。
 * @param {{ mode?: 'app'|'embed', embedToken?: string, visitorId?: string }} options
 */
export function useAiAppChat(options = {}) {
  const { mode = 'app', embedToken = '', visitorId = '' } = options

  const sessionId = ref(null)
  const messages = ref([])
  const streaming = ref(false)
  const streamBuffer = ref('')
  const toolStatus = ref('')
  let cancelStream = null

  onUnmounted(() => {
    cancelStream?.()
  })

  async function loadMessages(sid) {
    if (!sid) {
      messages.value = []
      return
    }
    if (mode === 'embed') {
      const res = await listEmbedMessage(embedToken, sid, visitorId)
      messages.value = (res.data || []).map(normalizeMessage)
      return
    }
    const res = await listAiAppMessage(sid)
    messages.value = (res.data || []).map(normalizeMessage)
  }

  function normalizeMessage(msg) {
    return {
      ...msg,
      metadata: parseMessageMetadata(msg.metadataJson)
    }
  }

  async function ensureSession(appId, createFn) {
    if (sessionId.value) return sessionId.value
    const res = await createFn(appId)
    sessionId.value = res.data
    return sessionId.value
  }

  async function createSession(appId) {
    if (mode === 'embed') {
      return ensureSession(appId, () => addEmbedSession(embedToken, visitorId))
    }
    return ensureSession(appId, (id) => addAiAppSession({ appId: id, title: '新会话' }))
  }

  function sendChat(payload) {
    const { appId, message, preview = false, webSearch = false } = payload
    if (!sessionId.value || streaming.value) return

    const userMsg = {
      _localId: `u-${Date.now()}`,
      role: 'user',
      content: message,
      metadata: {}
    }
    messages.value.push(userMsg)
    streaming.value = true
    streamBuffer.value = ''
    toolStatus.value = ''

    const body = {
      appId,
      sessionId: sessionId.value,
      message,
      preview,
      webSearch
    }

    const handlers = {
      onEvent: (event, data) => {
        if (event === 'delta') {
          streamBuffer.value += data.content || ''
        } else if (event === 'tool_call') {
          toolStatus.value = `正在调用工具：${data.toolName || data.name || '...'}`
        } else if (event === 'done') {
          finishStream(data)
        } else if (event === 'error') {
          ElMessage.error(data.message || '对话失败')
          streaming.value = false
          streamBuffer.value = ''
        }
      },
      onError: (err) => {
        ElMessage.error(err.message || 'SSE 连接失败')
        streaming.value = false
        streamBuffer.value = ''
      }
    }

    cancelStream?.()
    if (mode === 'embed') {
      cancelStream = subscribeEmbedChatStream(embedToken, visitorId, body, handlers)
    } else {
      cancelStream = subscribeAiAppChatStream(body, handlers)
    }
  }

  function finishStream(data) {
    if (streamBuffer.value) {
      messages.value.push({
        id: data?.messageId,
        role: 'assistant',
        content: streamBuffer.value,
        metadata: {}
      })
    }
    streaming.value = false
    streamBuffer.value = ''
    toolStatus.value = ''
    loadMessages(sessionId.value)
  }

  function selectSession(sid) {
    sessionId.value = sid
    loadMessages(sid)
  }

  function resetChat() {
    cancelStream?.()
    sessionId.value = null
    messages.value = []
    streaming.value = false
    streamBuffer.value = ''
    toolStatus.value = ''
  }

  return {
    sessionId,
    messages,
    streaming,
    streamBuffer,
    toolStatus,
    loadMessages,
    createSession,
    sendChat,
    selectSession,
    resetChat
  }
}
