/**
 * 签名配置
 * 从环境变量读取配置
 */

// 签名配置
export const signatureConfig = {
  // 是否启用签名
  enabled: import.meta.env.VITE_SIGNATURE_ENABLED === 'true',
  
  // 签名算法（固定为SM3）
  algorithm: import.meta.env.VITE_SIGNATURE_ALGORITHM || 'SM3',
  
  // 签名密钥
  secretKey: import.meta.env.VITE_SIGNATURE_SECRET_KEY || 'your-secret-key-2026',
  
  // 签名有效期（秒）
  expireTime: parseInt(import.meta.env.VITE_SIGNATURE_EXPIRE_TIME || '300'),
  
  // 接口白名单（不参与签名的接口）
  whitelistUrls: [

  ]
}

/**
 * 检查URL是否在白名单中
 * @param {string} url - 请求URL
 * @returns {boolean} 是否在白名单中
 */
export function isUrlInWhitelist(url) {
  if (!url) return false
  
  // 移除URL中的查询参数，只匹配路径部分
  const urlPath = url.split('?')[0]
  
  return signatureConfig.whitelistUrls.some(pattern => {
    // 简单的通配符匹配
    if (pattern.includes('**')) {
      // 匹配 /path/** 格式
      const prefix = pattern.replace('/**', '')
      return urlPath.startsWith(prefix) || urlPath.includes(prefix)
    } else if (pattern.includes('*')) {
      // 匹配 /path/* 格式
      const regex = new RegExp('^' + pattern.replace(/\*/g, '[^/]*') + '$')
      return regex.test(urlPath)
    } else {
      // 精确匹配或包含匹配
      return urlPath === pattern || urlPath.endsWith(pattern) || urlPath.includes(pattern)
    }
  })
}

/**
 * 打印签名配置（用于调试）
 */
export function printSignatureConfig() {
  console.log('========== 签名配置 ==========')
  console.log('启用状态:', signatureConfig.enabled)
  console.log('签名算法:', signatureConfig.algorithm)
  console.log('密钥:', signatureConfig.secretKey ? '已配置' : '未配置')
  console.log('有效期:', signatureConfig.expireTime, '秒')
  console.log('白名单URL数量:', signatureConfig.whitelistUrls.length)
  console.log('==============================')
}

