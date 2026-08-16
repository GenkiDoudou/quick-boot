/**
 * OAuth 客户端凭证混淆与 Basic Authorization 构造。
 * 与 quick-ui 对齐：XOR + Base64Url，避免明文 clientId:secret 出现在请求头。
 */

/** 与后端/管理端一致的 XOR 密钥 */
const XOR_KEY = new TextEncoder().encode('QuickBootOAuth1')

/** 按字节循环 XOR，用于凭证混淆 */
function xorBytes(bytes: Uint8Array): Uint8Array {
  const out = new Uint8Array(bytes.length)
  for (let i = 0; i < bytes.length; i++) {
    out[i] = bytes[i]! ^ XOR_KEY[i % XOR_KEY.length]!
  }
  return out
}

/** 二进制转 Base64Url（无 padding），H5 用 btoa，Node/测试环境回退 Buffer */
function bytesToBase64Url(bytes: Uint8Array): string {
  let binary = ''
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]!)
  }
  const b64 =
    typeof btoa === 'function'
      ? btoa(binary)
      : Buffer.from(binary, 'binary').toString('base64')
  return b64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '')
}

/** 将明文凭证（如 clientId:secret）混淆为 Base64Url 字符串 */
export function obfuscateCredential(plain: string): string {
  return bytesToBase64Url(xorBytes(new TextEncoder().encode(plain)))
}

/**
 * 构造 `Authorization: Basic …` 请求头值。
 * 未传参时从 VITE_OAUTH_CLIENT_ID / VITE_OAUTH_CLIENT_SECRET 读取；缺任一则返回 null。
 */
export function buildObfuscatedBasicAuthorization(
  clientId?: string,
  clientSecret?: string,
): string | null {
  const id = clientId ?? import.meta.env.VITE_OAUTH_CLIENT_ID ?? ''
  const secret = clientSecret ?? import.meta.env.VITE_OAUTH_CLIENT_SECRET ?? ''
  if (!id || !secret) {
    return null
  }
  return 'Basic ' + obfuscateCredential(`${id}:${secret}`)
}
