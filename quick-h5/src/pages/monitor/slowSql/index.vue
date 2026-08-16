<script setup lang="ts">
/**
 * 慢 SQL 列表：URI 关键词；详情与删除。
 * 来源筛选暂缓：无固定字典（SysSlowSql.sqlSource 值域未知）。
 */
import { computed } from 'vue'
import { pageSlowSql, removeSlowSql, type SysSlowSql } from '@/api/monitor/slowSql'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'

const canRemove = computed(() => hasPermi('monitor:slowSql:remove'))

/** 耗时 / 类型 / 操作人；完整 SQL 进详情 */
const cardColumns: QbCardColumn[] = [
  { prop: 'costTime', label: '耗时(ms)', span: 8, kv: 'row' },
  { prop: 'sqlType', label: '类型', span: 8, kv: 'row' },
  { prop: 'operName', label: '操作人', span: 8, kv: 'row' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysSlowSql>({
  fetcher: ({ current, size, keyword: kw }) => pageSlowSql(current, size, kw || undefined),
})

function goDetail(row: SysSlowSql) {
  uni.navigateTo({
    url: `/pages/monitor/slowSql/detail?slowId=${encodeURIComponent(String(row.slowId))}`,
  })
}

function onRemove(row: SysSlowSql) {
  uni.showModal({
    title: '确认删除',
    content: '删除该慢 SQL 记录？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeSlowSql([row.slowId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

function briefSql(text?: string) {
  const s = String(text || '').replace(/\s+/g, ' ').trim()
  return s.length > 80 ? `${s.slice(0, 80)}…` : s || '-'
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="请求 URI"
      :show-add="false"
      @search="onSearch"
    />

    <QbListCard
      v-for="row in rows"
      :key="String(row.slowId)"
      :title="row.requestUri || '-'"
      :subtitle="briefSql(row.sqlText)"
    >
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
      empty-text="暂无慢 SQL"
    />
  </view>
</template>
