/**
 * 前端观测入口：仅 Lite RUM（请求链路）+ axios 请求头注入。
 * clientTrack 用户行为批次上报已移除。
 */
export {
  setupLiteRum,
  getLiteRum,
  isLiteRumEnabled,
  loadLiteRumConfig,
  clearSessionId,
  getOrCreateSessionId
} from './liteRum'
export { nextRequestTraceHeaders, shouldAttachRequestTrace } from './requestTrace'
export {
  beginRequestObservation,
  finalizeRequestObservationSuccess,
  finalizeRequestObservationError
} from './requestObservation'
