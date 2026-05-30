import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  registerObservationEmitter,
  beginRequestObservation,
  finalizeRequestObservationSuccess
} from '@/monitor/requestObservation'
import { beginOperation } from '@/monitor/operationContext'

describe('requestObservation', () => {
  beforeEach(() => {
    registerObservationEmitter(() => {}, { slowApiMs: 3000 })
  })

  it('clientTraceId 与 Header 一致；有 R.traceId 时 serverTraceId 取响应值', () => {
    const track = vi.fn()
    registerObservationEmitter(track, { slowApiMs: 3000 })

    beginOperation('user-edit:1')
    const config = { url: '/system/user/1', method: 'get', headers: {}, metadata: {} }
    beginRequestObservation(config)

    expect(config.headers['x-trace-id']).toBeTruthy()
    expect(config.metadata.observation.clientTraceId).toBe(config.headers['x-trace-id'])

    finalizeRequestObservationSuccess({
      status: 200,
      config,
      data: { code: 200, traceId: 'server-trace-from-r', data: {} }
    })

    expect(track).toHaveBeenCalledTimes(1)
    expect(track.mock.calls[0][0]).toMatchObject({
      type: 'api_call',
      clientTraceId: config.headers['x-trace-id'],
      responseTraceId: 'server-trace-from-r',
      serverTraceId: 'server-trace-from-r',
      operationId: expect.any(String),
      url: '/system/user/1',
      trigger: 'user-edit:1'
    })
    expect(track.mock.calls[0][0].clientTraceId).not.toBe('server-trace-from-r')
  })

  it('无 R.traceId 时 serverTraceId 回退为请求头 clientTraceId', () => {
    const track = vi.fn()
    registerObservationEmitter(track, { slowApiMs: 3000 })

    const config = { url: '/system/user/list', method: 'get', headers: {}, metadata: {} }
    beginRequestObservation(config)
    const clientId = config.headers['x-trace-id']

    finalizeRequestObservationSuccess({
      status: 200,
      config,
      data: { code: 200, data: { records: [] } }
    })

    expect(track).toHaveBeenCalledTimes(1)
    expect(track.mock.calls[0][0]).toMatchObject({
      type: 'api_call',
      clientTraceId: clientId,
      serverTraceId: clientId
    })
    expect(track.mock.calls[0][0].responseTraceId).toBeUndefined()
  })

  it('跳过监控上报自身 URL', () => {
    const track = vi.fn()
    registerObservationEmitter(track)

    beginRequestObservation({
      url: '/monitor/clientTrack/report',
      method: 'post',
      headers: {},
      metadata: {}
    })

    expect(track).not.toHaveBeenCalled()
  })
})
