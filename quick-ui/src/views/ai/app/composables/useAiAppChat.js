import { onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addAiAppSession,
  addEmbedSession,
  listAiAppMessage,
  listAiAppSession,
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

  async function createSession(appId, title = '新会话') {
    if (mode === 'embed') {
      return ensureSession(appId, () => addEmbedSession(embedToken, visitorId))
    }
    return ensureSession(appId, (id) => addAiAppSession({ appId: id, title }))
  }

  /**
   * 复用已有会话或创建一条（编排预览等场景，避免每次进入都新建）。
   * @param {string|number} appId
   * @param {string} [preferredTitle='预览调试']
   */
  async function reuseOrCreateSession(appId, preferredTitle = '预览调试') {
    if (sessionId.value) {
      await loadMessages(sessionId.value)
      return sessionId.value
    }
    if (mode === 'embed') {
      const sessions = await listEmbedSession(embedToken, visitorId)
      const list = sessions.data || []
      if (list.length) {
        selectSession(list[0].id)
        return list[0].id
      }
      return createSession(appId)
    }
    const res = await listAiAppSession(appId)
    const list = res.data || []
    const preferred = list.find((s) => s.title === preferredTitle)
    if (preferred) {
      selectSession(preferred.id)
      return preferred.id
    }
    return createSession(appId, preferredTitle)
  }

  /**
   * 发送前确保已有会话（演示页懒创建；优先复用无消息的空会话）。
   * @param {string|number} appId
   * @param {string} [title='新会话']
   */
  async function ensureSessionForSend(appId, title = '新会话') {
    if (sessionId.value) return sessionId.value
    if (mode === 'embed') {
      return createSession(appId)
    }
    const res = await listAiAppSession(appId)
    const demoList = (res.data || []).filter((s) => s.title !== '预览调试')
    for (const s of demoList) {
      const sessionTitle = (s.title || '新会话').trim()
      if (sessionTitle !== title) continue
      const msgRes = await listAiAppMessage(s.id)
      if (!(msgRes.data || []).length) {
        sessionId.value = s.id
        messages.value = []
        return s.id
      }
    }
    const created = await addAiAppSession({ appId, title })
    sessionId.value = created.data
    messages.value = []
    return created.data
  }

  function sendChat(payload) {
    const { appId, message, preview = false, webSearch = false, previewConfigJson } = payload
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
      webSearch,
      ...(previewConfigJson ? { previewConfigJson } : {})
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
    reuseOrCreateSession,
    ensureSessionForSend,
    sendChat,
    selectSession,
    resetChat
  }
}
