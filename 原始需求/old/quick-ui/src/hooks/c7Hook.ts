// hooks/useFetchOptions.ts
// 从 c7-plus 引用工具函数
import { jsonGet } from '../../packages/c7-plus/src/utils/utils'
import { ref, unref } from 'vue'

export function useFetchOptions({
                                    fetchData,
                                    fetchParams = {},
                                    resultKey = 'data',
                                    dataFormatter = null,
                                    dataList = [],
                                    lazy = false,
                                    fetchDataCallBack = null

                                }) {
    // 工具函数：安全解包响应式数据
    const safeUnwrap = (data) => {
        try {
            const unwrapped = unref(data)
            return Array.isArray(unwrapped) ? unwrapped : []
        } catch (error) {
            console.warn('useFetchOptions: 数据解包失败', error)
            return []
        }
    }
    const options = ref([])
    const loading = ref(false)
    let fetchAndUpdate = null;
    console.log('useFetchOptions', fetchData, fetchParams, resultKey, dataFormatter, dataList, lazy)
    if (!lazy) {
        fetchAndUpdate = async (query = '') => {
            if (!fetchData) {
                // 修复：安全解包响应式数据，确保 dataList 是数组
                options.value = safeUnwrap(dataList)
                return
            }

            loading.value = true
            try {
                const params = {...fetchParams, query}
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
