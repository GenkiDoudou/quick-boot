import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import { buildSignedFetchHeaders } from '@/utils/clientSign'

const EMBED_VISITOR_KEY = 'ai_app_embed_visitor_id'

/**
 * 获取或创建嵌入访客 UUID（localStorage 持久化）。
 * @returns {string}
 */
export function getEmbedVisitorId() {
  let id = localStorage.getItem(EMBED_VISITOR_KEY)
  if (!id) {
    id = crypto.randomUUID?.() || `v-${Date.now()}-${Math.random().toString(36).slice(2)}`
    localStorage.setItem(EMBED_VISITOR_KEY, id)
  }
  return id
}

/**
 * AI 应用分页列表。
 * @param {Record<string, any>} params pageNum、pageSize、name、appType、status
 */
export function listAiApp(params) {
  return request({ url: '/ai/app/list', method: 'get', params })
}

/**
 * AI 应用详情（含 configJson）。
 * @param {number|string} appId
 */
export function getAiAppInfo(appId) {
  return request({ url: '/ai/app/getInfo', method: 'get', params: { appId } })
}

/**
 * 新增 AI 应用。
 * @param {Record<string, any>} data name、appType、description、icon、configJson
 * @returns {Promise<{ data: number }>} 新应用 ID
 */
export function addAiApp(data) {
  return request({ url: '/ai/app/add', method: 'post', data })
}

/**
 * 修改 AI 应用。
 * @param {Record<string, any>} data 含 id
 */
export function updateAiApp(data) {
  return request({ url: '/ai/app/update', method: 'post', data })
}

/**
 * 发布 AI 应用。
 * @param {{ appId: number|string }} data
 */
export function publishAiApp(data) {
  return request({ url: '/ai/app/publish', method: 'post', data })
}

/**
 * 删除 AI 应用。
 * @param {Array<number|string>} appIds
 */
export function removeAiApp(appIds) {
  return request({ url: '/ai/app/remove', method: 'post', data: appIds })
}

/**
 * 会话列表。
 * @param {number|string} appId
 */
export function listAiAppSession(appId) {
  return request({ url: '/ai/app/session/list', method: 'get', params: { appId } })
}

/**
 * 新建会话。
 * @param {{ appId: number|string, title?: string }} data
 */
export function addAiAppSession(data) {
  return request({ url: '/ai/app/session/add', method: 'post', data })
}

/**
 * 删除会话。
 * @param {number|string} sessionId
 */
export function removeAiAppSession(sessionId) {
  return request({ url: '/ai/app/session/remove', method: 'post', params: { sessionId } })
}

/**
 * 消息列表。
 * @param {number|string} sessionId
 */
export function listAiAppMessage(sessionId) {
  return request({ url: '/ai/app/message/list', method: 'get', params: { sessionId } })
}

/**
 * 获取嵌入发布配置。
 * @param {number|string} appId
 */
export function getAiAppEmbedInfo(appId) {
  return request({ url: '/ai/app/publish/getEmbedInfo', method: 'get', params: { appId } })
}

/**
 * 保存嵌入/菜单配置。
 * @param {Record<string, any>} data
 */
export function saveAiAppEmbed(data) {
  return request({ url: '/ai/app/publish/saveEmbed', method: 'post', data })
}

/**
 * 嵌入：应用公开信息。
 * @param {string} token
 */
export function getEmbedAppInfo(token) {
  return request({ url: `/ai/embed/${token}/app`, method: 'get' })
}

/**
 * 嵌入：新建会话。
 * @param {string} token 嵌入令牌
 * @param {string} visitorId 访客标识
 */
export function addEmbedSession(token, visitorId) {
  return request({
    url: `/ai/embed/${token}/session/add`,
    method: 'post',
    headers: { 'X-Embed-Visitor-Id': visitorId }
  })
}

/**
 * 嵌入：会话列表。
 * @param {string} token
 * @param {string} visitorId
 */
export function listEmbedSession(token, visitorId) {
  return request({
    url: `/ai/embed/${token}/session/list`,
    method: 'get',
    headers: { 'X-Embed-Visitor-Id': visitorId }
  })
}

/**
 * 嵌入：消息列表。
 * @param {string} token
 * @param {number|string} sessionId
 * @param {string} visitorId
 */
export function listEmbedMessage(token, sessionId, visitorId) {
  return request({
    url: `/ai/embed/${token}/message/list`,
    method: 'get',
    params: { sessionId },
    headers: { 'X-Embed-Visitor-Id': visitorId }
  })
}

/**
 * 解析 SSE 流（fetch ReadableStream）。
 * @param {ReadableStreamDefaultReader} reader
 * @param {(event: string, data: any) => void} onEvent
 */
async function consumeSseReader(reader, onEvent) {
  const decoder = new TextDecoder()
  let buffer = ''
  let eventName = 'message'
  let dataLines = []

  const flushEvent = () => {
    if (!dataLines.length) {
      eventName = 'message'
      return
    }
    const raw = dataLines.join('\n')
    dataLines = []
    let payload = raw
    try {
      payload = JSON.parse(raw)
    } catch {
      // 保留原文
    }
    onEvent(eventName, typeof payload === 'object' ? payload : { raw: payload })
    eventName = 'message'
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (line.startsWith('event:')) {
        flushEvent()
        eventName = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).trim())
      } else if (line === '') {
        flushEvent()
      }
    }
  }
  flushEvent()
}

/**
 * 管理端 SSE 对话订阅（POST /ai/app/chat/stream）。
 * @param {Record<string, any>} body appId、sessionId、message、preview、webSearch
 * @param {{ onEvent?: Function, onError?: Function, onOpen?: Function }} handlers
 * @returns {() => void} 取消函数
 */
export function subscribeAiAppChatStream(body, handlers = {}) {
  const controller = new AbortController()
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const prefix = base.endsWith('/') ? base.slice(0, -1) : base
  const url = `${prefix}/ai/app/chat/stream`
  const path = '/ai/app/chat/stream'
  const bodyStr = JSON.stringify(body)

  ;(async () => {
    try {
      const signHeaders = await buildSignedFetchHeaders('POST', path, bodyStr)
      const headers = {
        Accept: 'text/event-stream',
        'Content-Type': 'application/json',
        ...signHeaders
      }
      const token = getToken()
      if (token) {
        headers.Authorization = 'Bearer ' + token
      }
      const response = await fetch(url, {
        method: 'POST',
        headers,
        body: bodyStr,
        signal: controller.signal
      })
      if (!response.ok) {
        throw new Error(`SSE 连接失败: HTTP ${response.status}`)
      }
      handlers.onOpen?.()
      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('SSE 响应体不可读')
      }
      await consumeSseReader(reader, (event, data) => handlers.onEvent?.(event, data))
    } catch (err) {
      if (err?.name !== 'AbortError') {
        handlers.onError?.(err instanceof Error ? err : new Error(String(err)))
      }
    }
  })()

  return () => controller.abort()
}

/**
 * 嵌入页 SSE 对话订阅（POST /ai/embed/{token}/chat/stream）。
 * @param {string} embedToken
 * @param {string} visitorId
 * @param {Record<string, any>} body sessionId、message、webSearch
 * @param {{ onEvent?: Function, onError?: Function, onOpen?: Function }} handlers
 * @returns {() => void}
 */
export function subscribeEmbedChatStream(embedToken, visitorId, body, handlers = {}) {
  const controller = new AbortController()
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const prefix = base.endsWith('/') ? base.slice(0, -1) : base
  const url = `${prefix}/ai/embed/${embedToken}/chat/stream`
  const path = `/ai/embed/${embedToken}/chat/stream`
  const bodyStr = JSON.stringify(body)

  ;(async () => {
    try {
      const signHeaders = await buildSignedFetchHeaders('POST', path, bodyStr)
      const headers = {
        Accept: 'text/event-stream',
        'Content-Type': 'application/json',
        'X-Embed-Visitor-Id': visitorId,
        ...signHeaders
      }
      const response = await fetch(url, {
        method: 'POST',
        headers,
        body: bodyStr,
        signal: controller.signal
      })
      if (!response.ok) {
        throw new Error(`SSE 连接失败: HTTP ${response.status}`)
      }
      handlers.onOpen?.()
      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('SSE 响应体不可读')
      }
      await consumeSseReader(reader, (event, data) => handlers.onEvent?.(event, data))
    } catch (err) {
      if (err?.name !== 'AbortError') {
        handlers.onError?.(err instanceof Error ? err : new Error(String(err)))
      }
    }
  })()

  return () => controller.abort()
}
