/**
 * OAuth 客户端凭证混淆 + Basic 头（与后端 ClientCredentialObfuscator 一致）。
 * XOR 盐 QuickBootOAuth1 + URL-safe Base64（无 padding）。混淆≠加密。
 */

const XOR_KEY = new TextEncoder().encode('QuickBootOAuth1')

function xorBytes(bytes) {
  const out = new Uint8Array(bytes.length)
  for (let i = 0; i < bytes.length; i++) {
    out[i] = bytes[i] ^ XOR_KEY[i % XOR_KEY.length]
  }
  return out
}

function bytesToBase64Url(bytes) {
  let binary = ''
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '')
}

/** @param {string} plain */
export function obfuscateCredential(plain) {
  const bytes = new TextEncoder().encode(plain)
  return bytesToBase64Url(xorBytes(bytes))
}

/**
 * 组装 Authorization: Basic + 混淆(clientId:clientSecret)
 * （与后端 ClientBasicPasswordCodes / OauthClientUtils.getHeader 一致；不再二次标准 Base64）
 * @param {string} [clientId]
 * @param {string} [clientSecret]
 * @returns {string|null}
 */
export function buildObfuscatedBasicAuthorization(clientId, clientSecret) {
  const id = clientId ?? import.meta.env.VITE_OAUTH_CLIENT_ID ?? ''
  const secret = clientSecret ?? import.meta.env.VITE_OAUTH_CLIENT_SECRET ?? ''
  if (!id || !secret) {
    return null
  }
  return 'Basic ' + obfuscateCredential(`${id}:${secret}`)
}

/** 凭证文本块，便于一次复制 */
export function formatClientCredentialsBlock({ clientId, clientSecret, authorization }) {
  const auth = authorization || buildObfuscatedBasicAuthorization(clientId, clientSecret) || ''
  return [
    `Client ID: ${clientId || ''}`,
    `Client Secret: ${clientSecret || ''}`,
    `Authorization: ${auth}`
  ].join('\n')
}
