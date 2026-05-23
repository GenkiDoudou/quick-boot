/**
 * 表单验证工具函数
 * 支持 Element Plus 表单验证规则
 * 兼容旧的回调风格和新的 Promise 风格
 */

/**
 * 验证规则类型
 */
export interface ValidationRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  validator?: (rule: any, value: any, callback: (error?: Error) => void) => void
}

/**
 * 校验密码 - 等保三级密码强度要求
 * 要求：8-20位，必须包含大小写字母、数字、特殊字符中的至少三种
 * 
 * @param rule 验证规则
 * @param value 密码值
 * @param callback 回调函数（兼容旧风格）
 * @returns Promise（新风格）或 void（旧风格）
 */
export function validatePassword(
  rule: any,
  value: string,
  callback?: (error?: Error) => void
): Promise<void> | void {
  // 如果没有值
  if (!value) {
    const error = new Error('密码不能为空')
    if (callback) {
      callback(error)
      return
    }
    return Promise.reject(error)
  }

  // 使用单个正则表达式校验长度和复杂度
  // 必须满足：8-20位长度，且包含至少三种类型（小写字母、大写字母、数字、特殊字符）
  const regex = /^(?=.{8,20}$)(?:(?=.*[a-z])(?=.*[A-Z])(?=.*\d)|(?=.*[a-z])(?=.*[A-Z])(?=.*[^\w\s])|(?=.*[a-z])(?=.*\d)(?=.*[^\w\s])|(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]))/

  if (!regex.test(value)) {
    const errorMessage = value.length < 8 || value.length > 20
      ? '密码长度必须为8-20位'
      : '密码必须包含小写字母、大写字母、数字、特殊字符中的至少三种'
    const error = new Error(errorMessage)
    
    if (callback) {
      callback(error)
      return
    }
    return Promise.reject(error)
  }

  // 验证通过
  if (callback) {
    callback()
    return
  }
  return Promise.resolve()
}

/**
 * 校验手机号
 * 
 * @param rule 验证规则
 * @param value 手机号值
 * @param callback 回调函数（兼容旧风格）
 * @returns Promise（新风格）或 void（旧风格）
 */
export function validatePhone(
  rule: any,
  value: string,
  callback?: (error?: Error) => void
): Promise<void> | void {
  // 如果为空，允许通过（因为可能不是必填项）
  if (!value) {
    if (callback) {
      callback()
      return
    }
    return Promise.resolve()
  }

  // 中国大陆手机号正则表达式
  const phoneRegex = /^1[3-9]\d{9}$/

  if (!phoneRegex.test(value)) {
    const error = new Error('请输入正确的手机号')
    if (callback) {
      callback(error)
      return
    }
    return Promise.reject(error)
  }

  // 验证通过
  if (callback) {
    callback()
    return
  }
  return Promise.resolve()
}

/**
 * 校验邮箱
 * 
 * @param rule 验证规则
 * @param value 邮箱值
 * @param callback 回调函数（兼容旧风格）
 * @returns Promise（新风格）或 void（旧风格）
 */
export function validateEmail(
  rule: any,
  value: string,
  callback?: (error?: Error) => void
): Promise<void> | void {
  // 如果为空，允许通过（因为可能不是必填项）
  if (!value) {
    if (callback) {
      callback()
      return
    }
    return Promise.resolve()
  }

  // 邮箱正则表达式
  const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/

  if (!emailRegex.test(value)) {
    const error = new Error('请输入正确的邮箱地址')
    if (callback) {
      callback(error)
      return
    }
    return Promise.reject(error)
  }

  // 验证通过
  if (callback) {
    callback()
    return
  }
  return Promise.resolve()
}

/**
 * 判断url是否是http或https
 */
export function isHttp(url: string): boolean {
  return url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1
}

/**
 * 判断path是否为外链
 */
export function isExternal(path: string): boolean {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 验证用户名（示例）
 */
export function validUsername(str: string): boolean {
  const valid_map = ['admin', 'editor']
  return valid_map.indexOf(str.trim()) >= 0
}

/**
 * 验证URL
 */
export function validURL(url: string): boolean {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

/**
 * 验证小写字母
 */
export function validLowerCase(str: string): boolean {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/**
 * 验证大写字母
 */
export function validUpperCase(str: string): boolean {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/**
 * 验证字母
 */
export function validAlphabets(str: string): boolean {
  const reg = /^[A-Za-z]+$/
  return reg.test(str)
}

/**
 * 验证邮箱（简单验证）
 */
export function validEmail(email: string): boolean {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

/**
 * 判断是否为字符串
 */
export function isString(str: any): boolean {
  return typeof str === 'string' || str instanceof String
}

/**
 * 判断是否为数组
 */
export function isArray(arg: any): boolean {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(arg) === '[object Array]'
  }
  return Array.isArray(arg)
}

