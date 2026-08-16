<script setup lang="ts">
/**
 * 字典类型列表：名称关键词 + 状态筛选；卡片补备注；进入数据管理、增改删、刷新缓存。
 */
import { computed, ref } from 'vue'
import {
  pageDictType,
  removeDictType,
  refreshAllDictType,
  type SysDictType,
} from '@/api/system/dictType'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_normal_disable } = useDict('sys_normal_disable')
const canAdd = computed(() => hasPermi('system:dict:add'))
const canEdit = computed(() => hasPermi('system:dict:edit'))
const canRemove = computed(() => hasPermi('system:dict:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:dict:query', 'system:dict:list']))
const canRefresh = computed(() => hasPermi('system:dict:refresh'))

const filters = ref({ status: '' })

const cardColumns: QbCardColumn[] = [
  { prop: 'remark', label: '备注', span: 24, kv: 'stack', showIfProp: true },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysDictType>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageDictType({
      current,
      size,
      param: {
        dictName: kw || undefined,
        status: f.status || undefined,
      },
    }),
})

function goAdd() {
  uni.navigateTo({ url: '/pages/system/dict/type/form' })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysDictType) {
  uni.navigateTo({
    url: `/pages/system/dict/type/form?dictId=${encodeURIComponent(String(row.dictId))}&mode=view`,
  })
}

function goEdit(row: SysDictType) {
  uni.navigateTo({
    url: `/pages/system/dict/type/form?dictId=${encodeURIComponent(String(row.dictId))}`,
  })
}

function goData(row: SysDictType) {
  uni.navigateTo({
    url: `/pages/system/dict/data/index?dictType=${encodeURIComponent(String(row.dictType || ''))}`,
  })
}

async function onRefresh() {
  try {
    await refreshAllDictType()
    toastOk('缓存已刷新')
  }
  catch (e) {
    toastErr(e)
  }
}

function onRemove(row: SysDictType) {
  uni.showModal({
    title: '确认删除',
    content: `删除类型「${row.dictType}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeDictType([row.dictId!])
        toastOk('已删除')
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
      placeholder="字典名称或类型"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd"
    />
    <QbListFilters v-model="filters.status" label="状态" :options="sys_normal_disable" />
    <view v-if="canRefresh" class="qb-toolbar">
      <text class="qb-link" @click="onRefresh">刷新缓存</text>
    </view>

    <QbListCard
      v-for="row in rows"
      :key="String(row.dictId)"
      :title="row.dictName || '—'"
      :subtitle="row.dictType || '—'"
    >
      <template #status>
        <QbDictTag :value="row.status" :options="sys_normal_disable" />
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text class="qb-link" @click="goData(row)">数据</text>
        <text v-if="canEdit" class="qb-link" @click="goEdit(row)">编辑</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无字典类型"
    />
  </view>
</template>
