<script setup lang="ts">
/**
 * 调度日志详情。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJobLog, type SysJobLog } from '@/api/monitor/jobLog'
import { toastErr } from '@/utils/toast'

const loading = ref(true)
const detail = ref<SysJobLog | null>(null)

onLoad(async (query) => {
  const id = query?.jobLogId ? String(query.jobLogId) : ''
  if (!id) {
    loading.value = false
    return
  }
  try {
    detail.value = await getJobLog(id)
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
        <text class="qb-form-label">任务</text>
        <text>{{ detail.jobName }} / {{ detail.jobGroup }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">调用目标</text>
        <text class="break">{{ detail.invokeTarget || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <text>{{ detail.status === '0' ? '成功' : '失败' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">日志信息</text>
        <text class="break">{{ detail.jobMessage || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">异常</text>
        <text class="break">{{ detail.exceptionInfo || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">时间</text>
        <text>{{ detail.createTime || '—' }}</text>
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
