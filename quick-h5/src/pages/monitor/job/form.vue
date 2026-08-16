<script setup lang="ts">
/**
 * 定时任务只读详情：按 jobId 拉取 getJob，展示名称/分组/Cron/调用目标/状态/备注。
 * 本期不做 Cron 编辑与保存；可接受 mode=view（仅查看语义，无编辑分支）。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJob, type SysJob } from '@/api/monitor/job'
import { toastErr } from '@/utils/toast'

const loading = ref(true)
const detail = ref<SysJob | null>(null)

onLoad(async (query) => {
  const id = query?.jobId != null && query.jobId !== '' ? String(query.jobId) : ''
  if (!id) {
    loading.value = false
    return
  }
  try {
    detail.value = await getJob(id)
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    loading.value = false
  }
})

/** 任务状态文案：0 正常 / 1 暂停 */
function statusText(s?: string) {
  return s === '0' ? '正常' : s === '1' ? '暂停' : (s || '—')
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <view v-if="loading" class="qb-form-loading qb-muted">加载中…</view>
    <view v-else-if="detail" class="qb-form-panel">
      <view class="qb-form-field">
        <text class="qb-form-label">任务名称</text>
        <text>{{ detail.jobName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">任务分组</text>
        <text>{{ detail.jobGroup || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">Cron</text>
        <text class="break">{{ detail.cronExpression || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">调用目标</text>
        <text class="break">{{ detail.invokeTarget || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <text>{{ statusText(detail.status) }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">备注</text>
        <text class="break">{{ detail.remark || '—' }}</text>
      </view>
    </view>
    <view v-else class="qb-muted">无数据</view>
  </view>
</template>

<style scoped lang="scss">
.break {
  word-break: break-all;
  font-size: 26rpx;
}
</style>
