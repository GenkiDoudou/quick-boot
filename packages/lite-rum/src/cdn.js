/**
 * CDN / UMD 构建入口：将 createLiteRum、session、actionCapture 等挂到 LiteRum 静态属性，
 * 供 script 标签引入后通过全局 LiteRum 一次性访问完整 SDK 能力。
 */
import LiteRum, {
  createLiteRum,
  normalizeConfig,
  SDK_VERSION
} from './create.js'
import {
  clearSessionId,
  getOrCreateSessionId,
  resetSessionId,
  onSessionContextChange
} from './session.js'
import { bindActionCapture, unbindActionCapture } from './actionCapture.js'

LiteRum.createLiteRum = createLiteRum
LiteRum.normalizeConfig = normalizeConfig
LiteRum.SDK_VERSION = SDK_VERSION
LiteRum.clearSessionId = clearSessionId
LiteRum.getOrCreateSessionId = getOrCreateSessionId
LiteRum.resetSessionId = resetSessionId
LiteRum.onSessionContextChange = onSessionContextChange
LiteRum.bindActionCapture = bindActionCapture
LiteRum.unbindActionCapture = unbindActionCapture

export default LiteRum
