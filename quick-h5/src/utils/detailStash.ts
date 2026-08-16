/**
 * 列表 → 只读详情页传行数据（无独立 get 接口时用）。
 * 写入后 navigateTo；详情页 take 后清除，避免脏读。
 */
const PREFIX = 'qb_h5_detail_'

/** 暂存一行详情数据 */
export function stashDetailRow(key: string, row: unknown) {
  try {
    uni.setStorageSync(`${PREFIX}${key}`, JSON.stringify(row ?? null))
  }
  catch {
    // ignore storage failure; detail page will show empty
  }
}

/** 取出并清除暂存行 */
export function takeDetailRow<T>(key: string): T | null {
  const full = `${PREFIX}${key}`
  try {
    const raw = uni.getStorageSync(full)
    uni.removeStorageSync(full)
    if (!raw) return null
    return JSON.parse(String(raw)) as T
  }
  catch {
    return null
  }
}
