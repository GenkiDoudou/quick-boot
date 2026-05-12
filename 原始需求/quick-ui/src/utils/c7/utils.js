// C7 工具函数库

// 简易深度取值函数：支持 'a.b.c' 格式
export function jsonGet(obj, path, defaultValue = undefined) {
  if (!obj || typeof obj !== 'object') return defaultValue
  return path.split('.').reduce((res, seg) => (res == null ? undefined : res[seg]), obj) ?? defaultValue
}

// 防抖函数
export function debounce(func, wait, immediate = false) {
  let timeout = null
  
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func(...args)
    }
    
    const callNow = immediate && !timeout
    
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    
    if (callNow) func(...args)
  }
}

// 节流函数
export function throttle(func, limit) {
  let inThrottle = false
  
  return function executedFunction(...args) {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 生成唯一ID
export function generateId(prefix = 'id') {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 格式化日期
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 类型判断
export function isType(value, type) {
  return Object.prototype.toString.call(value) === `[object ${type}]`
}

// 数组去重
export function uniqueArray(arr, key) {
  if (!Array.isArray(arr)) return []
  
  if (key) {
    const seen = new Set()
    return arr.filter(item => {
      const val = item[key]
      if (seen.has(val)) {
        return false
      }
      seen.add(val)
      return true
    })
  }
  
  return [...new Set(arr)]
}

// 数组分组
export function groupBy(arr, key) {
  if (!Array.isArray(arr)) return {}
  
  return arr.reduce((groups, item) => {
    const group = item[key]
    if (!groups[group]) {
      groups[group] = []
    }
    groups[group].push(item)
    return groups
  }, {})
}

// 树形数据扁平化
export function flattenTreeData(tree, childrenKey = 'children') {
  if (!Array.isArray(tree)) return []
  
  const result = []
  
  function flatten(nodes) {
    nodes.forEach(node => {
      result.push(node)
      if (node[childrenKey] && Array.isArray(node[childrenKey])) {
        flatten(node[childrenKey])
      }
    })
  }
  
  flatten(tree)
  return result
}

// 深拷贝
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

// 获取URL参数
export function getUrlParams(url = window.location.href) {
  const params = {}
  const urlObj = new URL(url)
  urlObj.searchParams.forEach((value, key) => {
    params[key] = value
  })
  return params
}

// 本地存储工具
export const storage = {
  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error('存储失败:', error)
    }
  },
  
  get(key, defaultValue = null) {
    try {
      const value = localStorage.getItem(key)
      return value ? JSON.parse(value) : defaultValue
    } catch (error) {
      console.error('读取失败:', error)
      return defaultValue
    }
  },
  
  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error('删除失败:', error)
    }
  },
  
  clear() {
    try {
      localStorage.clear()
    } catch (error) {
      console.error('清空失败:', error)
    }
  }
}
