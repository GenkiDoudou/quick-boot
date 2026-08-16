<script setup lang="ts">
/**
 * 登录日志：用户名关键词 + sys_login_status 状态筛选；查看、删除、清空、解锁。
 */
import { computed, ref } from 'vue'
import {
  cleanLogininfor,
  pageLogininfor,
  removeLogininfor,
  unlockLogininfor,
  type SysLogininfor,
} from '@/api/monitor/logininfor'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { stashDetailRow } from '@/utils/detailStash'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'

const { sys_login_status } = useDict('sys_login_status')
const canView = computed(() => hasPermi(['monitor:logininfor:query', 'monitor:logininfor:list']))
const canRemove = computed(() => hasPermi('monitor:logininfor:remove'))
const canUnlock = computed(() => hasPermi('monitor:logininfor:unlock'))

const filters = ref({ status: '' })

/** 地点 / 浏览器 / IP / 登录时间 */
const cardColumns: QbCardColumn[] = [
  { prop: 'loginLocation', label: '地点', span: 12, kv: 'row' },
  { prop: 'browser', label: '浏览器', span: 12, kv: 'row' },
  { prop: 'ipaddr', label: 'IP', span: 12, kv: 'row' },
  { prop: 'loginTime', label: '登录时间', span: 12, kv: 'row' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysLogininfor>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageLogininfor({
      current,
      size,
      param: {
        userName: kw || undefined,
        status: f.status || undefined,
      },
    }),
})

/** 暂存行数据后进入只读详情（无 get-by-id） */
function goView(row: SysLogininfor) {
  stashDetailRow('logininfor', row)
  uni.navigateTo({ url: '/pages/monitor/logininfor/detail' })
}

function onRemove(row: SysLogininfor) {
  uni.showModal({
    title: '确认删除',
    content: '删除该登录日志？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeLogininfor([row.infoId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

function onClean() {
  uni.showModal({
    title: '清空',
    content: '清空全部登录日志？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await cleanLogininfor()
        toastOk('已清空')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

function onUnlock(row: SysLogininfor) {
  if (!row.userName) return
  uni.showModal({
    title: '解锁账户',
    content: `解锁用户「${row.userName}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await unlockLogininfor(row.userName!)
        toastOk('已解锁')
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
    <QbListFilters
      v-model="filters.status"
      label="状态"
      :options="sys_login_status"
    />
    <view v-if="canRemove" class="qb-toolbar">
      <text class="qb-link qb-link--danger" @click="onClean">清空</text>
    </view>

    <QbListCard
      v-for="row in rows"
      :key="String(row.infoId)"
      :title="row.userName || '-'"
      :subtitle="row.msg || ''"
    >
      <template #status>
        <QbDictTag :value="row.status" :options="sys_login_status" />
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text v-if="canUnlock && row.userName" class="qb-link" @click="onUnlock(row)">解锁</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无登录日志"
    />
  </view>
</template>
