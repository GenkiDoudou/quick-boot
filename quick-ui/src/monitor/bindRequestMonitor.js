/**
 * 注册 requestObservation 事件写入（API 采集已在 request.js 内完成）。
 */
import { registerObservationEmitter } from './requestObservation'

/** @typedef {(row: Record<string, unknown>) => void} TrackFn */

/**
 * @param {import('axios').AxiosInstance} _service 保留参数以兼容旧调用
 * @param {TrackFn} track
 * @param {{ slowApiMs?: number, skipUrlPattern?: RegExp }} [options]
 */
export function bindRequestMonitor(_service, track, options = {}) {
  registerObservationEmitter(track, {
    slowApiMs: options.slowApiMs,
    skipUrlPattern: options.skipUrlPattern
  })
}

export default bindRequestMonitor
