<script setup lang="ts">
/**
 * 在线用户只读详情：列表 stash 行数据后进入（无独立 get-by-id）。
 * 强退仍在列表页；本页仅展示会话元数据。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import type { SysUserOnline } from '@/api/monitor/online'
import { takeDetailRow } from '@/utils/detailStash'

const detail = ref<SysUserOnline | null>(null)

onLoad(() => {
  detail.value = takeDetailRow<SysUserOnline>('online')
})
</script>

<template>
  <view class="qb-page qb-crud-page">
    <view v-if="detail" class="qb-form-panel">
      <view class="qb-form-field">
        <text class="qb-form-label">用户名</text>
        <text>{{ detail.userName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">部门</text>
        <text>{{ detail.deptName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">IP</text>
        <text>{{ detail.ipaddr || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">登录地点</text>
        <text>{{ detail.loginLocation || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">浏览器</text>
        <text>{{ detail.browser || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">操作系统</text>
        <text>{{ detail.os || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">登录时间</text>
        <text>{{ detail.loginTime || '—' }}</text>
      </view>
      <view v-if="detail.tokenId" class="qb-form-field">
        <text class="qb-form-label">会话 Token</text>
        <text class="break">{{ detail.tokenId }}</text>
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
