<script setup lang="ts">
/**
 * 字典数据列表：按 dictType 筛选，标签关键词搜索；排序等字段进 cardColumns。
 */
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { pageDictData, removeDictData, type SysDictData } from '@/api/system/dictData'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_normal_disable } = useDict('sys_normal_disable')
const dictType = ref('')
const canAdd = computed(() => hasPermi('system:dict:add'))
const canEdit = computed(() => hasPermi('system:dict:edit'))
const canRemove = computed(() => hasPermi('system:dict:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:dict:query', 'system:dict:list']))

/** 排序从 subtitle/meta 迁入 columns，便于统一卡片布局 */
const cardColumns: QbCardColumn[] = [
  { prop: 'dictValue', label: '键值', span: 12, kv: 'row' },
  { prop: 'dictSort', label: '排序', span: 12, kv: 'row' },
  { prop: 'remark', label: '备注', span: 24, kv: 'stack', showIfProp: true },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysDictData>({
  fetcher: ({ current, size, keyword: kw }) => {
    if (!dictType.value) {
      return Promise.resolve({ current: 1, size, total: 0, pages: 0, records: [] })
    }
    return pageDictData({
      current,
      size,
      param: {
        dictType: dictType.value,
        dictLabel: kw || undefined,
      },
    })
  },
})

onLoad((query) => {
  dictType.value = query?.dictType ? String(query.dictType) : ''
  uni.setNavigationBarTitle({
    title: dictType.value ? `字典数据 · ${dictType.value}` : '字典数据',
  })
  if (!dictType.value) {
    toastInfo('缺少 dictType')
  }
})

function goAdd() {
  if (!dictType.value) return
  uni.navigateTo({
    url: `/pages/system/dict/data/form?dictType=${encodeURIComponent(dictType.value)}`,
  })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysDictData) {
  uni.navigateTo({
    url: `/pages/system/dict/data/form?dictCode=${encodeURIComponent(String(row.dictCode))}&dictType=${encodeURIComponent(dictType.value)}&mode=view`,
  })
}

function goEdit(row: SysDictData) {
  uni.navigateTo({
    url: `/pages/system/dict/data/form?dictCode=${encodeURIComponent(String(row.dictCode))}&dictType=${encodeURIComponent(dictType.value)}`,
  })
}

function onRemove(row: SysDictData) {
  uni.showModal({
    title: '确认删除',
    content: `删除「${row.dictLabel}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeDictData([row.dictCode!])
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
      placeholder="标签"
      :show-add="canAdd && !!dictType"
      @search="onSearch"
      @add="goAdd"
    />

    <QbListCard
      v-for="row in rows"
      :key="String(row.dictCode)"
      :title="row.dictLabel || '—'"
      :subtitle="`值 ${row.dictValue ?? '—'}`"
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
        <text v-if="canEdit" class="qb-link" @click="goEdit(row)">编辑</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无数据"
    />
  </view>
</template>
