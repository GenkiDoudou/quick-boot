/**
 * lite-rum 包入口：聚合 re-export create / session / actionCapture 等公开 API，
 * 供 ESM 构建（npm import）使用。
 */
export {
  LiteRum,
  createLiteRum,
  normalizeConfig,
  SDK_VERSION
} from './create.js'
export { default } from './create.js'

export {
  getOrCreateSessionId,
  resetSessionId,
  clearSessionId,
  onSessionContextChange,
  resetSessionContextForTest,
  configureSessionStorage
} from './session.js'

export { bindActionCapture, unbindActionCapture } from './actionCapture.js'
