import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperlog, pageOperlog } from '@/api/monitor/operlog'

/**
 * 按 traceId / operId 打开操作日志详情弹窗。
 * @returns {{
 *   operLogDetailVisible: import('vue').Ref<boolean>,
 *   operLogDetailRow: import('vue').Ref<Record<string, unknown>|null>,
 *   operLogDetailLoading: import('vue').Ref<boolean>,
 *   openOperLogByTraceId: (traceId: string) => Promise<void>,
 *   openOperLogByOperId: (operId: number|string) => Promise<void>,
 * }}
 */
export function useOperLogDetail() {
  const operLogDetailVisible = ref(false)
  const operLogDetailRow = ref(null)
  const operLogDetailLoading = ref(false)

  /**
   * @param {number|string} operId
   */
  async function openOperLogByOperId(operId) {
    if (operId == null || operId === '') {
      return
    }
    try {
      const res = await getOperlog(operId)
      operLogDetailRow.value = res?.data ?? res
      operLogDetailVisible.value = true
    } catch {
      ElMessage.error('加载操作日志失败')
    }
  }

  /**
   * @param {string} traceId
   */
  async function openOperLogByTraceId(traceId) {
    const normalized = String(traceId || '').trim()
    if (!normalized) {
      return
    }
    operLogDetailLoading.value = true
    try {
      const res = await pageOperlog({
        current: 1,
        size: 1,
        param: { traceId: normalized },
      })
      const records = res?.data?.records ?? res?.records ?? []
      if (!records.length) {
        ElMessage.warning('未找到对应操作日志')
        return
      }
      await openOperLogByOperId(records[0].operId)
    } catch {
      ElMessage.error('加载操作日志失败')
    } finally {
      operLogDetailLoading.value = false
    }
  }

  return {
    operLogDetailVisible,
    operLogDetailRow,
    operLogDetailLoading,
    openOperLogByTraceId,
    openOperLogByOperId,
  }
}
