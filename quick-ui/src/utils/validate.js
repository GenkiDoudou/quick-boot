/**
 * 判断url是否是http或https 
 * @param {string} path
 * @returns {Boolean}
 */
 export function isHttp(url) {
  return url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1
}

/**
 * 判断path是否为外链
 * @param {string} path
 * @returns {Boolean}
 */
 export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUsername(str) {
  const valid_map = ['admin', 'editor']
  return valid_map.indexOf(str.trim()) >= 0
}

/**
 * @param {string} url
 * @returns {Boolean}
 */
export function validURL(url) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validLowerCase(str) {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUpperCase(str) {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validAlphabets(str) {
  const reg = /^[A-Za-z]+$/
  return reg.test(str)
}

/**
 * @param {string} email
 * @returns {Boolean}
 */
export function validEmail(email) {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function isString(str) {
  if (typeof str === 'string' || str instanceof String) {
    return true
  }
  return false
}

/**
 * @param {Array} arg
 * @returns {Boolean}
 */
export function isArray(arg) {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(arg) === '[object Array]'
  }
  return Array.isArray(arg)
}


/**
 * 校验密码 - 等保三级密码强度要求
 * 要求：8-20位，必须包含大小写字母、数字、特殊字符中的至少三种
 * 支持的特殊字符包括：!@#$%^&*()_+-=[]{}|;:,.<>? 等除字母数字外的所有字符
 */
export function validatePassword(rule, value, callback) {
  if (!value) {
    callback(new Error('密码不能为空'))
    return
  }

  // 使用单个正则表达式校验长度和复杂度
  // 必须满足：8-20位长度，且包含至少三种类型（小写字母、大写字母、数字、特殊字符）
  const regex = /^(?=.{8,20}$)(?:(?=.*[a-z])(?=.*[A-Z])(?=.*\d)|(?=.*[a-z])(?=.*[A-Z])(?=.*[^\w\s])|(?=.*[a-z])(?=.*\d)(?=.*[^\w\s])|(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]))/

  if (!regex.test(value)) {
    if (value.length < 8 || value.length > 20) {
      callback(new Error('密码长度必须为8-20位'))
    } else {
      callback(new Error('密码必须包含小写字母、大写字母、数字、特殊字符中的至少三种'))
    }
    return
  }

  callback()
}
// 校验手机号
export function validatePhone(rule, value, callback) {
  if (!value) {
    callback()
    return
  }

  // 中国大陆手机号正则表达式
  const phoneRegex = /^1[3-9]\d{9}$/

  if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号'))
    return
  }

  callback()
}

// 校验邮箱
/**
 * 校验邮箱
 * @param {Object} rule - 验证规则
 * @param {string} value - 邮箱值
 * @param {Function} callback - 回调函数
 */
export function validateEmail(rule, value, callback) {
  if (!value) {
    callback()
    return
  }

  // 邮箱正则表达式
  const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/

  if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
    return
  }
  callback()
}
