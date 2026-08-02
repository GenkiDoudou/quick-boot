import request from '@/utils/request'

/**
 * 登录页：是否启用天爱行为验证码（对应后端 qc.captcha.enabled）。
 * @returns {Promise<{ captchaEnabled: boolean, type?: string }>}
 */
export function getCaptchaConfig() {
  return request({
    url: '/api/captcha/config',
    headers: { isToken: false },
    method: 'get'
  }).then((body) => {
    // 后端直接返回 Map，也可能被包成 R
    const data = body?.data !== undefined && (body.code === 200 || body.code === undefined)
      ? (body.data ?? body)
      : body
    return {
      captchaEnabled: data?.captchaEnabled === true,
      type: data?.type
    }
  })
}

/**
 * TAC SDK 使用的绝对接口地址（走 Vite /dev-api 代理）。
 * @returns {{ generateUrl: string, validateUrl: string }}
 */
export function getCaptchaTacUrls() {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const normalized = base.endsWith('/') ? base.slice(0, -1) : base
  let originBase
  if (!normalized) {
    originBase = window.location.origin
  } else if (normalized.startsWith('http')) {
    originBase = normalized
  } else {
    const prefix = normalized.startsWith('/') ? normalized : `/${normalized}`
    originBase = `${window.location.origin}${prefix}`
  }
  return {
    generateUrl: `${originBase}/api/captcha/generate`,
    validateUrl: `${originBase}/api/captcha/validate`
  }
}
