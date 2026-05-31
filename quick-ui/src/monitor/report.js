/**
 * 监控批次上报：独立于 axios，避免响应拦截弹 Toast 与递归采集。
 */
import { getToken } from '@/utils/auth'
import { buildSignedFetchHeaders } from '@/utils/clientSign'
import { scheduleIdleTask } from './scheduleIdle'

/** @type {{ url: string, payload: Record<string, unknown> }[]} */
const deferredQueue = []
let deferredScheduled = false

function drainDeferredQueue() {
  deferredScheduled = false
  const jobs = deferredQueue.splice(0, deferredQueue.length)
  for (const job of jobs) {
    postTrackBatch(job.url, job.payload)
  }
}

/**
 * 空闲时上报（合并同 tick 内多次 flush，避免连续 HMAC + fetch 卡 UI）。
 *
 * @param {string} reportUrl
 * @param {Record<string, unknown>} payload
 */
export function postTrackBatchDeferred(reportUrl, payload) {
  deferredQueue.push({ url: reportUrl, payload })
  if (deferredScheduled) {
    return
  }
  deferredScheduled = true
  scheduleIdleTask(drainDeferredQueue)
}

/**
 * 将一批前端事件 POST 到后端；失败时静默丢弃（监控不应影响主流程）。
 *
 * @param {string} reportUrl 完整或相对 API 路径
 * @param {{ reason: string, browserVisitId?: string, sessionId?: string, pageVisitId?: string, operationId?: string, triggerAction?: string, triggerLabel?: string, events: Record<string, unknown>[] }} payload
 * @returns {Promise<void>}
 */
export async function postTrackBatch(reportUrl, payload) {
  const body = JSON.stringify(payload)
  const path = '/monitor/clientTrack/report'
  const headers = {
    'Content-Type': 'application/json;charset=utf-8',
    ...(await buildSignedFetchHeaders('POST', path, body))
  }
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  try {
    await fetch(reportUrl, {
      method: 'POST',
      headers,
      body,
      keepalive: true,
      credentials: 'same-origin'
    })
  } catch {
    // 监控上报失败不影响业务
  }
}

export default postTrackBatch
