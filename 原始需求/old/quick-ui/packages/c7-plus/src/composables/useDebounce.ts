import { onUnmounted } from 'vue'

/**
 * 防抖 Hook
 * @param func 要防抖的函数
 * @param wait 等待时间（毫秒）
 * @returns 防抖后的函数
 */
export function useDebounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): T {
  let timeout: NodeJS.Timeout | null = null
  
  const debounced = ((...args: Parameters<T>) => {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => {
      func(...args)
    }, wait)
  }) as T
  
  // 组件卸载时清理定时器
  onUnmounted(() => {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
  })
  
  return debounced
}

