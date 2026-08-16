<script setup lang="ts">
/**
 * 操作日志：模块标题关键词 + 状态筛选；详情 / 删除 / 清空。
 * 状态字典：sys_oper_status（与 quick-ui 一致）。
 */
import { computed, ref } from 'vue'
import {
  cleanOperlog,
  pageOperlog,
  removeOperlog,
  type SysOperlog,
} from '@/api/monitor/operlog'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'

const { sys_oper_status } = useDict('sys_oper_status')
const canRemove = computed(() => hasPermi('monitor:operlog:remove'))

const filters = ref({ status: '' })

/** 耗时 / 方法名 / 请求方式；长文本进详情页 */
const cardColumns: QbCardColumn[] = [
  { prop: 'costTime', label: '耗时(ms)', span: 8, kv: 'row' },
  { prop: 'requestMethod', label: '请求', span: 8, kv: 'row' },
  { prop: 'method', label: '方法', span: 24, kv: 'stack' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysOperlog>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageOperlog({
      current,
      size,
      param: {
        title: kw || undefined,
        status: f.status || undefined,
      },
    }),
})

function goDetail(row: SysOperlog) {
  uni.navigateTo({
    url: `/pages/monitor/operlog/detail?operId=${encodeURIComponent(String(row.operId))}`,
  })
}

function onRemove(row: SysOperlog) {
  uni.showModal({
    title: '确认删除',
    content: '删除该操作日志？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeOperlog([row.operId!])
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
    content: '清空全部操作日志？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await cleanOperlog()
        toastOk('已清空')
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
      placeholder="操作模块"
      :show-add="false"
      @search="onSearch"
    />
    <QbListFilters
      v-model="filters.status"
      label="状态"
      :options="sys_oper_status"
    />
    <view v-if="canRemove" class="qb-toolbar">
      <text class="qb-link qb-link--danger" @click="onClean">清空</text>
    </view>

    <QbListCard
      v-for="row in rows"
      :key="String(row.operId)"
      :title="row.title || '-'"
      :subtitle="`${row.operName || '-'} · ${row.operIp || ''} · ${row.operTime || ''}`"
    >
      <template #status>
        <QbDictTag :value="row.status" :options="sys_oper_status" />
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text class="qb-link" @click="goDetail(row)">详情</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无操作日志"
    />
  </view>
</template>
