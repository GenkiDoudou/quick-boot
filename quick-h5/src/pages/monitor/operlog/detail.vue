<script setup lang="ts">
/**
 * 操作日志详情：关键字段 + 可折叠长文本。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOperlog, type SysOperlog } from '@/api/monitor/operlog'
import { toastErr } from '@/utils/toast'

const loading = ref(true)
const detail = ref<SysOperlog | null>(null)
const showParam = ref(false)
const showResult = ref(false)

onLoad(async (query) => {
  const id = query?.operId ? String(query.operId) : ''
  if (!id) {
    loading.value = false
    return
  }
  try {
    detail.value = await getOperlog(id)
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    loading.value = false
  }
})
</script>

<template>
  <view class="qb-page qb-crud-page">
    <view v-if="loading" class="qb-form-loading qb-muted">加载中…</view>
    <view v-else-if="detail" class="qb-form-panel">
      <view class="qb-form-field">
        <text class="qb-form-label">模块</text>
        <text>{{ detail.title || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">操作人</text>
        <text>{{ detail.operName || '—' }} / {{ detail.deptName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">请求</text>
        <text class="break">{{ detail.requestMethod }} {{ detail.operUrl }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">IP / 地点</text>
        <text>{{ detail.operIp || '—' }} · {{ detail.operLocation || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">耗时</text>
        <text>{{ detail.costTime != null ? detail.costTime + ' ms' : '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">时间</text>
        <text>{{ detail.operTime || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label qb-link" @click="showParam = !showParam">
          请求参数 {{ showParam ? '收起' : '展开' }}
        </text>
        <text v-if="showParam" class="qb-detail-mono">{{ detail.operParam || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label qb-link" @click="showResult = !showResult">
          返回结果 {{ showResult ? '收起' : '展开' }}
        </text>
        <text v-if="showResult" class="qb-detail-mono">{{ detail.jsonResult || detail.errorMsg || '—' }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.break {
  word-break: break-all;
  font-size: 24rpx;
  color: #374151;
}
</style>
