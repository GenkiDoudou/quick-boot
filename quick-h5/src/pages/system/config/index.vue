<script setup lang="ts">
/**
 * 系统参数列表：关键词搜名称 + 系统内置筛选；卡片展示键名/键值/备注。
 * API 降级说明：关键词仅映射 configName（后端无合并 configKey OR 时，键名搜索靠 placeholder 提示）。
 */
import { computed, ref } from 'vue'
import { pageConfig, removeConfig, refreshConfigCache, type SysConfig } from '@/api/system/config'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_yes_no } = useDict('sys_yes_no')
const canAdd = computed(() => hasPermi('system:config:add'))
const canEdit = computed(() => hasPermi('system:config:edit'))
const canRemove = computed(() => hasPermi('system:config:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:config:query', 'system:config:list']))
const canRefresh = computed(() => hasPermi(['system:config:query', 'system:config:list']))

/** 系统内置筛选（sys_yes_no）；空串=全部 */
const filters = ref({ configType: '' })

const cardColumns: QbCardColumn[] = [
  { prop: 'configKey', label: '键名', span: 24, kv: 'stack' },
  { prop: 'configValue', label: '键值', span: 24, kv: 'stack' },
  { prop: 'remark', label: '备注', span: 24, kv: 'stack', showIfProp: true },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysConfig>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageConfig({
      current,
      size,
      param: {
        // 降级：仅 configName；不传 configKey（避免双字段互斥过滤）
        configName: kw || undefined,
        configType: f.configType || undefined,
      },
    }),
})

function goAdd() {
  uni.navigateTo({ url: '/pages/system/config/form' })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysConfig) {
  uni.navigateTo({
    url: `/pages/system/config/form?configId=${encodeURIComponent(String(row.configId))}&mode=view`,
  })
}

function goEdit(row: SysConfig) {
  uni.navigateTo({
    url: `/pages/system/config/form?configId=${encodeURIComponent(String(row.configId))}`,
  })
}

/** 内置参数禁止删除 */
function isBuiltin(row: SysConfig) {
  return String(row.configType) === '1'
}

async function onRefreshCache() {
  try {
    await refreshConfigCache()
    toastOk('缓存已刷新')
  }
  catch (e) {
    toastErr(e)
  }
}

function onRemove(row: SysConfig) {
  if (isBuiltin(row)) {
    toastInfo('内置参数不可删除')
    return
  }
  uni.showModal({
    title: '确认删除',
    content: `删除参数「${row.configKey}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeConfig([row.configId!])
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
      placeholder="参数名称或键名"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd"
    />
    <QbListFilters v-model="filters.configType" label="内置" :options="sys_yes_no" />
    <view v-if="canRefresh" class="qb-toolbar">
      <text class="qb-link" @click="onRefreshCache">刷新缓存</text>
    </view>

    <QbListCard
      v-for="row in rows"
      :key="String(row.configId)"
      :title="row.configName || '—'"
      :subtitle="row.configKey || '—'"
    >
      <template #status>
        <QbDictTag :value="row.configType" :options="sys_yes_no" />
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text v-if="canEdit" class="qb-link" @click="goEdit(row)">编辑</text>
        <text
          v-if="canRemove && !isBuiltin(row)"
          class="qb-link qb-link--danger"
          @click="onRemove(row)"
        >
          删除
        </text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无参数"
    />
  </view>
</template>
