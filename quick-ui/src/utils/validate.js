/**
 * 表单/URL 校验工具。
 * 当前业务主要使用 isExternal（外链判断）、isHttp（是否 http(s) URL）。
 * 其余 validator 通过 main.js 挂载为 $validate，供模板或 Options API 按需调用。
 */

/** 是否为外链（http(s)/mailto/tel） */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/** 是否包含 http:// 或 https:// 协议头 */
export function isHttp(url) {
  return url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1
}

/** 简单邮箱格式 */
export function isEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

/** 中国大陆手机号（11 位） */
export function isPhone(phone) {
  const re = /^1[3|4|5|6|7|8|9][0-9]\d{8}$/
  return re.test(phone)
}

/** http(s)/ftp URL */
export function isUrl(url) {
  const re = /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i
  return re.test(url)
}

/** 纯数字字符串 */
export function isNumber(num) {
  return /^[0-9]*$/.test(num)
}

/** 整数（可带正负号） */
export function isInteger(num) {
  return /^[-+]?\d+$/.test(num)
}

/** 小数（可带正负号） */
export function isDecimal(num) {
  return /^[-+]?\d+(\.\d+)?$/.test(num)
}

/** 纯英文字母 */
export function isAlphabets(str) {
  return /^[a-zA-Z]*$/.test(str)
}

/** 纯大写字母 */
export function isAlphabetsUpper(str) {
  return /^[A-Z]*$/.test(str)
}

/** 纯小写字母 */
export function isAlphabetsLower(str) {
  return /^[a-z]*$/.test(str)
}

/** 字母与数字 */
export function isAlphanumeric(str) {
  return /^[a-zA-Z0-9]*$/.test(str)
}

/** 纯中文 */
export function isChinese(str) {
  return /^[\u4e00-\u9fa5]*$/.test(str)
}

/** 15/18 位身份证号 */
export function isIdCard(idCard) {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)
}
