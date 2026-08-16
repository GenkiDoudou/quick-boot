<script setup lang="ts">
/**
 * 登录日志只读详情：列表 stash 行数据后进入（无独立 get-by-id）。
 * 展示用户 / IP / 地点 / 浏览器 / 系统 / 状态 / 消息 / 时间。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import type { SysLogininfor } from '@/api/monitor/logininfor'
import { useDict } from '@/composables/useDict'
import { takeDetailRow } from '@/utils/detailStash'

const { sys_login_status } = useDict('sys_login_status')
const detail = ref<SysLogininfor | null>(null)

onLoad(() => {
  detail.value = takeDetailRow<SysLogininfor>('logininfor')
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
        <text class="qb-form-label">状态</text>
        <QbDictTag :value="detail.status" :options="sys_login_status" />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">消息</text>
        <text class="break">{{ detail.msg || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">登录时间</text>
        <text>{{ detail.loginTime || '—' }}</text>
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
