export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

export function isHttp(url) {
  return url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1
}

export function isEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

export function isPhone(phone) {
  const re = /^1[3|4|5|6|7|8|9][0-9]\d{8}$/
  return re.test(phone)
}

export function isUrl(url) {
  const re = /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i
  return re.test(url)
}

export function isNumber(num) {
  return /^[0-9]*$/.test(num)
}

export function isInteger(num) {
  return /^[-+]?\d+$/.test(num)
}

export function isDecimal(num) {
  return /^[-+]?\d+(\.\d+)?$/.test(num)
}

export function isAlphabets(str) {
  return /^[a-zA-Z]*$/.test(str)
}

export function isAlphabetsUpper(str) {
  return /^[A-Z]*$/.test(str)
}

export function isAlphabetsLower(str) {
  return /^[a-z]*$/.test(str)
}

export function isAlphanumeric(str) {
  return /^[a-zA-Z0-9]*$/.test(str)
}

export function isChinese(str) {
  return /^[\u4e00-\u9fa5]*$/.test(str)
}

export function isIdCard(idCard) {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)
}
