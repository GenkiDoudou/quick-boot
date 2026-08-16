/**
 * 工作流 API：流程 CRUD、图 DSL 保存、运行实例与 SSE 流式输出（/workflow）。
 */
import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import { buildObfuscatedBasicAuthorization } from '@/utils/oauthClientBasic'

/**
 * 工作流分页列表。
 * @param {Record<string, any>} params 查询参数（pageNum、pageSize、name、status）
 * @returns {Promise<{ data: { records: Array, total: number } }>}
 */
export function listWorkflow(params) {
  return request({ url: '/workflow/list', method: 'get', params })
}

/**
 * 查询工作流详情（含草稿 graph DSL）。
 * @param {number|string} workflowId 工作流 ID
 * @returns {Promise<any>}
 */
export function getWorkflow(workflowId) {
  return request({ url: '/workflow/getInfo', method: 'get', params: { workflowId } })
}

/**
 * 新增工作流元数据。
 * @param {Record<string, any>} data 工作流数据（name、description）
 * @returns {Promise<any>}
 */
export function addWorkflow(data) {
  return request({ url: '/workflow/add', method: 'post', data })
}

/**
 * 修改工作流元数据。
 * @param {Record<string, any>} data 工作流数据（含 workflowId）
 * @returns {Promise<any>}
 */
export function updateWorkflow(data) {
  return request({ url: '/workflow/update', method: 'post', data })
}

/**
 * 批量删除工作流。
 * @param {Array<number|string>} workflowIds 工作流 ID 集合
 * @returns {Promise<any>}
 */
export function removeWorkflow(workflowIds) {
  return request({ url: '/workflow/remove', method: 'post', data: workflowIds })
}

/**
 * 保存工作流图 DSL（草稿）。
 * @param {Record<string, any>} data 含 workflowId 与 graph（nodes/edges/version）
 * @returns {Promise<any>}
 */
export function saveGraph(data) {
  return request({ url: '/workflow/saveGraph', method: 'post', data })
}

/**
 * 校验工作流图（不落库）。
 * @param {Record<string, any>} data 含 graph DSL
 * @returns {Promise<any>}
 */
export function validateGraph(data) {
  return request({ url: '/workflow/validateGraph', method: 'post', data })
}

/**
 * 发布工作流。
 * @param {Record<string, any>} data 含 workflowId
 * @returns {Promise<any>}
 */
export function publishWorkflow(data) {
  return request({ url: '/workflow/publish', method: 'post', data })
}

/**
 * 内置模板列表。
 * @returns {Promise<{ data: Array }>}
 */
export function listTemplates() {
  return request({ url: '/workflow/template/list', method: 'get' })
}

/**
 * 同步 Debug 运行。
 * @param {Record<string, any>} data 含 workflowId、inputs、useDraft、kbId、stream
 * @returns {Promise<{ data: object }>}
 */
export function runDebug(data) {
  return request({ url: '/workflow/run/debug', method: 'post', data, timeout: 120000 })
}

/**
 * 异步运行工作流。
 * @param {Record<string, any>} data 含 workflowId、inputs、usePublished、kbId、stream
 * @returns {Promise<{ data: object }>}
 */
export function runAsync(data) {
  return request({ url: '/workflow/run/async', method: 'post', data })
}

/**
 * 查询运行详情（含步骤 Trace）。
 * @param {number|string} runId 运行 ID
 * @returns {Promise<{ data: object }>}
 */
export function getRunInfo(runId) {
  return request({ url: '/workflow/run/getInfo', method: 'get', params: { runId } })
}

/**
 * 运行历史分页列表。
 * @param {Record<string, any>} params 查询参数（pageNum、pageSize、workflowId、status）
 * @returns {Promise<{ data: { records: Array, total: number } }>}
 */
export function listRuns(params) {
  return request({ url: '/workflow/run/list', method: 'get', params })
}

/**
 * 构建 SSE 流式订阅 URL（GET /workflow/run/stream?runId=）。
 * EventSource 无法携带 Authorization，请配合 {@link subscribeRunStream} 使用 fetch 订阅。
 * @param {number|string} runId 运行 ID
 * @returns {string} 完整 SSE 请求 URL
 */
export function createRunStreamUrl(runId) {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const prefix = base.endsWith('/') ? base.slice(0, -1) : base
  return `${prefix}/workflow/run/stream?runId=${runId}`
}

/**
 * 使用 fetch 订阅工作流 SSE 事件流（支持 Bearer Token 与 Client 签名）。
 * @param {number|string} runId 运行 ID
 * @param {{
 *   onEvent?: (event: string, data: Record<string, any>) => void,
 *   onError?: (err: Error) => void,
 *   onOpen?: () => void
 * }} handlers 事件回调
 * @returns {() => void} 取消订阅函数
 */
export function subscribeRunStream(runId, handlers = {}) {
  const controller = new AbortController()
  const url = createRunStreamUrl(runId)

  ;(async () => {
    try {
      const headers = {
        Accept: 'text/event-stream'
      }
      const token = getToken()
      if (token) {
        headers.Authorization = 'Bearer ' + token
      } else {
        const basic = buildObfuscatedBasicAuthorization()
        if (basic) {
          headers.Authorization = basic
        }
      }

      const response = await fetch(url, {
        method: 'GET',
        headers,
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
          // 非 JSON 时保留原文
        }
        handlers.onEvent?.(eventName, typeof payload === 'object' ? payload : { raw: payload })
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
    } catch (err) {
      if (err?.name !== 'AbortError') {
        handlers.onError?.(err instanceof Error ? err : new Error(String(err)))
      }
    }
  })()

  return () => controller.abort()
}
