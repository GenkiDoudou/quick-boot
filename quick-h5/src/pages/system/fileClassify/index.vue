<script setup lang="ts">
/**
 * 文件分类列表：名称关键词搜索；后缀/大小上限走 QbJsonCardFields。
 * 大小字节经 cardRow 转成可读文案再展示。
 */
import { computed } from 'vue'
import {
  listFileClassify,
  removeFileClassify,
  type SysFileClassify,
} from '@/api/system/fileClassify'
import { toGetPageQuery } from '@/api/pageQuery'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_normal_disable } = useDict('sys_normal_disable')
const canAdd = computed(() => hasPermi('system:fileClassify:add'))
const canEdit = computed(() => hasPermi('system:fileClassify:edit'))
const canRemove = computed(() => hasPermi('system:fileClassify:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:fileClassify:query', 'system:fileClassify:list']))

const cardColumns: QbCardColumn[] = [
  { prop: 'limitExt', label: '允许后缀', span: 12, kv: 'row' },
  { prop: 'limitSizeText', label: '大小上限', span: 12, kv: 'row' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysFileClassify>({
  fetcher: ({ current, size, keyword: kw }) =>
    listFileClassify({
      ...toGetPageQuery(current, size),
      classifyName: kw || undefined,
    }),
})

function goAdd() {
  uni.navigateTo({ url: '/pages/system/fileClassify/form' })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysFileClassify) {
  uni.navigateTo({
    url: `/pages/system/fileClassify/form?classifyId=${encodeURIComponent(String(row.classifyId))}&mode=view`,
  })
}

function goEdit(row: SysFileClassify) {
  uni.navigateTo({
    url: `/pages/system/fileClassify/form?classifyId=${encodeURIComponent(String(row.classifyId))}`,
  })
}

function onRemove(row: SysFileClassify) {
  uni.showModal({
    title: '确认删除',
    content: `删除分类「${row.classify}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeFileClassify([row.classifyId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

/** 字节转可读；供卡片展示用 */
function fmtSize(bytes?: number | null) {
  const n = Number(bytes)
  if (!Number.isFinite(n) || n <= 0) return '—'
  if (n < 1024) return `${n}B`
  if (n < 1024 * 1024) return `${Math.round(n / 1024)}KB`
  return `${(n / (1024 * 1024)).toFixed(1)}MB`
}

/**
 * 行数据扩展：把 limitSizeBytes 格式化为 limitSizeText，供 columns 直接绑 prop。
 * @param row 原始分类行
 */
function cardRow(row: SysFileClassify) {
  return {
    ...row,
    limitExt: row.limitExt || '默认',
    limitSizeText: fmtSize(row.limitSizeBytes),
  }
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="分类名称"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd"
    />

    <QbListCard
      v-for="row in rows"
      :key="String(row.classifyId)"
      :title="row.classifyName || '—'"
      :subtitle="row.classify || '—'"
    >
      <template #status>
        <QbDictTag :value="row.status" :options="sys_normal_disable" />
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="cardRow(row)" :columns="cardColumns" />
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
      empty-text="暂无分类"
    />
  </view>
</template>
