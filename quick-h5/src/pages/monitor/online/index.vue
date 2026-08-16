<script setup lang="ts">
/**
 * 在线用户：仅用户名关键词（无状态筛选）；查看详情；强退须二次确认。
 */
import { computed } from 'vue'
import { forceLogout, pageOnline, type SysUserOnline } from '@/api/monitor/online'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'
import { usePagedList } from '@/composables/usePagedList'
import { stashDetailRow } from '@/utils/detailStash'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'

const canView = computed(() => hasPermi(['monitor:online:query', 'monitor:online:list']))
const canForce = computed(() => hasPermi('monitor:online:forceLogout'))

/** 部门 / IP / 登录时间 / 浏览器 */
const cardColumns: QbCardColumn[] = [
  { prop: 'deptName', label: '部门', span: 12, kv: 'row' },
  { prop: 'ipaddr', label: 'IP', span: 12, kv: 'row' },
  { prop: 'loginTime', label: '登录时间', span: 12, kv: 'row' },
  { prop: 'browser', label: '浏览器', span: 12, kv: 'row' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysUserOnline>({
  fetcher: ({ current, size, keyword: kw }) => pageOnline(current, size, kw || undefined),
})

/** 暂存行数据后进入只读详情；强退仍在本列表 */
function goView(row: SysUserOnline) {
  stashDetailRow('online', row)
  uni.navigateTo({ url: '/pages/monitor/online/detail' })
}

function onForce(row: SysUserOnline) {
  if (!row.tokenId) return
  uni.showModal({
    title: '强退确认',
    content: `强制下线用户「${row.userName}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await forceLogout(row.tokenId!)
        toastOk('已强退')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="用户名"
      :show-add="false"
      @search="onSearch"
    />

    <QbListCard
      v-for="row in rows"
      :key="String(row.tokenId)"
      :title="row.userName || '-'"
    >
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text v-if="canForce" class="qb-link qb-link--danger" @click="onForce(row)">强退</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无在线用户"
    />
  </view>
</template>
