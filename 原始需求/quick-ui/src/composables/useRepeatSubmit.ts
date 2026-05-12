/**
 * 防重复提交 Composable
 * 用于请求拦截器中防止重复提交
 */
import cache from '@/plugins/cache'

/**
 * 请求对象接口
 */
interface RequestObj {
  url: string
  data: string
  time: number
}

/**
 * 检查是否为重复提交
 * @param config axios 请求配置
 * @param interval 间隔时间（毫秒），默认 1000ms
 * @returns 如果是重复提交返回 true，否则返回 false
 */
export function checkRepeatSubmit(config: any, interval: number = 1000): boolean {
  // 检查是否需要防止重复提交
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  if (isRepeatSubmit) {
    return false
  }

  // 只对 POST 和 PUT 请求进行防重复提交检查
  if (config.method !== 'post' && config.method !== 'put') {
    return false
  }

  const requestObj: RequestObj = {
    url: config.url || '',
    data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data || '',
    time: new Date().getTime()
  }

  // 检查请求数据大小（限制 5M）
  const requestSize = JSON.stringify(requestObj).length
  const limitSize = 5 * 1024 * 1024 // 5M
  if (requestSize >= limitSize) {
    console.warn(`[${config.url}]: 请求数据大小超出允许的5M限制，无法进行防重复提交验证。`)
    return false
  }

  const sessionObj = cache.session.getJSON('sessionObj') as RequestObj | null

  if (!sessionObj) {
    // 首次请求，保存请求信息
    cache.session.setJSON('sessionObj', requestObj)
    return false
  }

  // 检查是否为重复提交
  const { url: s_url, data: s_data, time: s_time } = sessionObj
  const isRepeat = 
    s_data === requestObj.data &&
    requestObj.time - s_time < interval &&
    s_url === requestObj.url

  if (isRepeat) {
    console.warn(`[${s_url}]: 数据正在处理，请勿重复提交`)
    return true
  }

  // 更新请求信息
  cache.session.setJSON('sessionObj', requestObj)
  return false
}

