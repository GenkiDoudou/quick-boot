<script setup lang="ts">
/**
 * 文件元数据只读详情：优先 takeDetailRow('file')；无 stash 时仅有 fileId 也无法拉详情（后端无 get-by-id）。
 * 预览/下载仍在列表；本页展示 originalName / classify / size / ext / contentType / 上传人 / 时间。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import type { SysFile } from '@/api/system/file'
import { takeDetailRow } from '@/utils/detailStash'

const detail = ref<SysFile | null>(null)

onLoad(() => {
  detail.value = takeDetailRow<SysFile>('file')
})

/** 字节数可读化，与列表 fmtSize 一致 */
function fmtSize(bytes?: number) {
  const n = Number(bytes)
  if (!Number.isFinite(n) || n <= 0) return '—'
  if (n < 1024) return `${n}B`
  if (n < 1024 * 1024) return `${Math.round(n / 1024)}KB`
  return `${(n / (1024 * 1024)).toFixed(1)}MB`
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <view v-if="detail" class="qb-form-panel">
      <view class="qb-form-field">
        <text class="qb-form-label">文件名</text>
        <text class="break">{{ detail.originalName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">分类</text>
        <text>{{ detail.classify || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">大小</text>
        <text>{{ fmtSize(detail.sizeBytes) }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">扩展名</text>
        <text>{{ detail.ext || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">Content-Type</text>
        <text class="break">{{ detail.contentType || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">上传人</text>
        <text>{{ detail.uploaderUserName || '—' }}</text>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">上传时间</text>
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
