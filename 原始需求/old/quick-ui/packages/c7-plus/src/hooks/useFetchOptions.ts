import { ref, unref, Ref } from 'vue'
import { jsonGet } from '../utils/utils'

export interface UseFetchOptionsParams {
  fetchData?: Function | null
  fetchParams?: Record<string, any>
  resultKey?: string
  dataFormatter?: Function | null
  dataList?: any[]
  lazy?: boolean
}

export interface UseFetchOptionsReturn {
  options: Ref<any[]>
  loading: Ref<boolean>
  fetchAndUpdate: ((query?: string) => Promise<void>) | null
}

/**
 * 通用的数据获取 Hook
 */
export function useFetchOptions({
  fetchData,
  fetchParams = {},
  resultKey = 'data',
  dataFormatter = null,
  dataList = [],
  lazy = false
}: UseFetchOptionsParams): UseFetchOptionsReturn {
  
  // 工具函数：安全解包响应式数据
  const safeUnwrap = (data: any): any[] => {
    try {
      const unwrapped = unref(data)
      return Array.isArray(unwrapped) ? unwrapped : []
    } catch (error) {
      console.warn('useFetchOptions: 数据解包失败', error)
      return []
    }
  }

  const options = ref<any[]>([])
  const loading = ref(false)
  let fetchAndUpdate: ((query?: string) => Promise<void>) | null = null

  if (!lazy) {
    fetchAndUpdate = async (query: string = '') => {
      if (!fetchData) {
        // 修复：安全解包响应式数据，确保 dataList 是数组
        options.value = safeUnwrap(dataList)
        return
      }

      loading.value = true
      try {
        const params = { ...fetchParams, query }
        const result = await fetchData(params)
        let list = jsonGet(result, resultKey, [])
        
        if (typeof dataFormatter === 'function') {
          list = dataFormatter(list)
        }
        
        options.value = Array.isArray(list) ? list : []
      } catch (err) {
        console.error('useFetchOptions error:', err)
        options.value = []
      } finally {
        loading.value = false
      }
    }
  }

  return {
    options,
    loading,
    fetchAndUpdate
  }
}

