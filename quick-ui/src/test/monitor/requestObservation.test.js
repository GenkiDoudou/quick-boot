import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  registerObservationEmitter,
  beginRequestObservation,
  finalizeRequestObservationSuccess
} from '@/monitor/requestObservation'
import { openBatch, openPageVisit, resetBatchSessionForTest } from '@/monitor/batchSession'

async function flushIdleTasks() {
  await new Promise((resolve) => setTimeout(resolve, 0))
}

describe('requestObservation', () => {
  beforeEach(() => {
    resetBatchSessionForTest()
    registerObservationEmitter(() => {}, { slowApiMs: 3000 })
  })
  it('clientTraceId 与 Header 一致；有 R.traceId 时 serverTraceId 取响应值', async () => {
    const track = vi.fn()
    registerObservationEmitter(track, { slowApiMs: 3000 })

    openBatch('user-edit:1')
    const config = { url: '/system/user/1', method: 'get', headers: {}, metadata: {} }
    beginRequestObservation(config)

    expect(config.headers['x-trace-id']).toBeTruthy()
    expect(config.metadata.observation.clientTraceId).toBe(config.headers['x-trace-id'])

    finalizeRequestObservationSuccess({
      status: 200,
      config,
      data: { code: 200, traceId: 'server-trace-from-r', data: {} }
    })

    await flushIdleTasks()

    expect(track).toHaveBeenCalledTimes(1)
    expect(track.mock.calls[0][0]).toMatchObject({
      type: 'api_call',
      clientTraceId: config.headers['x-trace-id'],
      responseTraceId: 'server-trace-from-r',
      serverTraceId: 'server-trace-from-r',
      operationId: expect.any(String),
      url: '/system/user/1',
      trigger: '修改'
    })
    expect(track.mock.calls[0][0].clientTraceId).not.toBe('server-trace-from-r')  })

  it('无 R.traceId 时 serverTraceId 回退为请求头 clientTraceId', async () => {
    const track = vi.fn()
    registerObservationEmitter(track, { slowApiMs: 3000 })

    openPageVisit('用户管理', '/system/user')
    const config = { url: '/system/user/list', method: 'get', headers: {}, metadata: {} }
    beginRequestObservation(config)
    const clientId = config.headers['x-trace-id']

    finalizeRequestObservationSuccess({
      status: 200,
      config,
      data: { code: 200, data: { records: [] } }
    })

    await flushIdleTasks()

    expect(track).toHaveBeenCalledTimes(1)
    expect(track.mock.calls[0][0]).toMatchObject({
      type: 'api_call',
      clientTraceId: clientId,
      serverTraceId: clientId
    })
    expect(track.mock.calls[0][0].responseTraceId).toBeUndefined()  })

  it('跳过前端监控相关 API（report/list/remove）', () => {
    const track = vi.fn()
    registerObservationEmitter(track)

    for (const url of [
      '/monitor/clientTrack/report',
      '/monitor/clientTrack/list',
      '/monitor/clientTrack/remove'
    ]) {
      beginRequestObservation({
        url,
        method: 'post',
        headers: {},
        metadata: {}
      })
    }

    expect(track).not.toHaveBeenCalled()
  })

  it('无活跃批次时仅注入 trace，不创建 observation', () => {
    const track = vi.fn()
    registerObservationEmitter(track)

    const config = { url: '/system/user/list', method: 'get', headers: {}, metadata: {} }
    beginRequestObservation(config)

    expect(config.headers['x-trace-id']).toBeTruthy()
    expect(config.metadata?.observation).toBeUndefined()
  })
})