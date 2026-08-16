<script setup lang="ts">
/**
 * 慢 SQL 详情：展示完整 SQL 与指标。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSlowSql, type SysSlowSql } from '@/api/monitor/slowSql'
import { toastErr } from '@/utils/toast'

const loading = ref(true)
const detail = ref<SysSlowSql | null>(null)

onLoad(async (query) => {
  const id = query?.slowId ? String(query.slowId) : ''
  if (!id) {
    loading.value = false
    return
  }
  try {
    detail.value = await getSlowSql(id)
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
        <text class="qb-form-label">类型 / 来源</text>
        <text>{{ detail.sqlType || '—' }} · {{ detail.sqlSource || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">耗时</text>
        <text>{{ detail.costTime != null ? detail.costTime + ' ms' : '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">Mapper</text>
        <text class="break">{{ detail.mapperId || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">请求</text>
        <text class="break">{{ detail.requestUri || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">操作人</text>
        <text>{{ detail.operName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">SQL</text>
        <text class="qb-detail-mono">{{ detail.sqlText || '—' }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.break {
  word-break: break-all;
  font-size: 26rpx;
}
.sql {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 22rpx;
  line-height: 1.5;
  color: #111827;
}
</style>
