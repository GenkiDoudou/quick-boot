/**
 * 分页列表 composable：关键词搜索 + 可选 filters + 下拉刷新 + 触底加载。
 * filters 变更时自动从第一页重载；空字符串筛选值不会传给 fetcher。
 */
import { ref, watch, type Ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import type { PageInfo } from '@/api/types'
import { toastErr, toastInfo } from '@/utils/toast'

/** 列表额外筛选（如 status）；空串表示「全部」 */
export type PagedListFilters = Record<string, string | undefined | null>

export type UsePagedListOptions<T> = {
  pageSize?: number
  /**
   * 分页数据源。
   * @param args.filters 已去掉空值的筛选对象
   */
  fetcher: (args: {
    current: number
    size: number
    keyword: string
    filters: Record<string, string>
  }) => Promise<PageInfo<T> | null | undefined>
  /** 额外筛选；deep watch 后重置分页 */
  filters?: Ref<PagedListFilters>
  /** 默认 true：注册 onShow / 下拉 / 触底 */
  bindLifecycle?: boolean
  /** 已加载完时触底提示文案 */
  emptyMoreToast?: string
}

/** 去掉 null/undefined/空白，得到可并入请求的筛选项 */
function compactFilters(raw?: PagedListFilters): Record<string, string> {
  const out: Record<string, string> = {}
  if (!raw) return out
  for (const [k, v] of Object.entries(raw)) {
    if (v == null) continue
    const s = String(v).trim()
    if (!s) continue
    out[k] = s
  }
  return out
}

/**
 * 关键词 + 可选 filters + 分页列表。
 */
export function usePagedList<T>(options: UsePagedListOptions<T>) {
  const pageSize = options.pageSize ?? 10
  const keyword = ref('')
  const rows = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const finished = ref(false)
  const current = ref(1)

  /** reset=true 时从第一页重载；finished 且非 reset 时不再请求 */
  async function load(reset = false) {
    if (loading.value) return
    if (reset) {
      current.value = 1
      finished.value = false
    }
    if (finished.value && !reset) return
    loading.value = true
    try {
      const data = await options.fetcher({
        current: current.value,
        size: pageSize,
        keyword: keyword.value.trim(),
        filters: compactFilters(options.filters?.value),
      })
      const list = data?.records || []
      rows.value = reset ? list : rows.value.concat(list)
      const total = Number(data?.total || 0)
      // 条数已满或本页不足 pageSize 则视为没有更多
      if (rows.value.length >= total || list.length < pageSize) {
        finished.value = true
      }
      else {
        current.value += 1
      }
    }
    catch (e) {
      toastErr(e)
    }
    finally {
      loading.value = false
      uni.stopPullDownRefresh()
    }
  }

  function onSearch() {
    load(true)
  }

  if (options.filters) {
    watch(
      options.filters,
      () => {
        load(true)
      },
      { deep: true },
    )
  }

  if (options.bindLifecycle !== false) {
    onShow(() => {
      load(true)
    })
    onPullDownRefresh(() => {
      load(true)
    })
    onReachBottom(() => {
      if (finished.value) {
        toastInfo(options.emptyMoreToast || '没有更多了')
        return
      }
      load(false)
    })
  }

  return {
    keyword,
    rows,
    loading,
    finished,
    load,
    onSearch,
  }
}
