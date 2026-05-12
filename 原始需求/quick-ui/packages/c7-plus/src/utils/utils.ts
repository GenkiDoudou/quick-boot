/**
 * 工具函数：从嵌套对象中获取值
 * @param obj 对象
 * @param path 路径，如 'data.list'
 * @param defaultValue 默认值
 */
export function jsonGet(obj: any, path: string, defaultValue: any = undefined): any {
  if (!obj || !path) return defaultValue
  
  const keys = path.split('.')
  let result = obj
  
  for (const key of keys) {
    if (result && typeof result === 'object' && key in result) {
      result = result[key]
    } else {
      return defaultValue
    }
  }
  
  return result !== undefined ? result : defaultValue
}

/**
 * 工具函数：滚动到指定位置
 * @param x 横坐标
 * @param y 纵坐标
 */
export function scrollTo(x: number = 0, y: number = 0): void {
  window.scrollTo({
    top: y,
    left: x,
    behavior: 'smooth'
  })
}

