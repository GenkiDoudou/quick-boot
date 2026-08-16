<script setup lang="ts">
/**
 * OAuth 客户端列表：名称关键词 + 状态筛选；卡片展示路径/超时/验证码。
 * API 降级：关键词仅映射 clientName（placeholder 含 ID 仅为提示，不传 clientId）。
 * 列表快捷启停仍走 updateOauthClient（通过表单改 status 同源字段）。
 */
import { computed, ref } from 'vue'
import { pageOauthClient, updateOauthClient, removeOauthClient, type SysOauthClient } from '@/api/system/oauthClient'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_normal_disable, sys_yes_no } = useDict('sys_normal_disable', 'sys_yes_no')
const canAdd = computed(() => hasPermi('system:oauthClient:add'))
const canEdit = computed(() => hasPermi('system:oauthClient:edit'))
const canRemove = computed(() => hasPermi('system:oauthClient:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:oauthClient:query', 'system:oauthClient:list']))

const filters = ref({ status: '' })

/** 验证码列依赖字典 options，用 computed */
const cardColumns = computed<QbCardColumn[]>(() => [
  { prop: 'apiPathPatterns', label: '接口路径', span: 24, kv: 'stack' },
  { prop: 'tokenTimeout', label: 'Token秒数', span: 12, kv: 'row' },
  {
    prop: 'checkCaptcha',
    label: '验证码',
    span: 12,
    kv: 'row',
    type: 'dict',
    options: sys_yes_no.value,
  },
])

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysOauthClient>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageOauthClient({
      current,
      size,
      param: {
        clientName: kw || undefined,
        status: f.status || undefined,
      },
    }),
})

function goAdd() {
  uni.navigateTo({ url: '/pages/system/oauthClient/form' })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysOauthClient) {
  uni.navigateTo({
    url: `/pages/system/oauthClient/form?id=${encodeURIComponent(String(row.id))}&mode=view`,
  })
}

function goEdit(row: SysOauthClient) {
  uni.navigateTo({
    url: `/pages/system/oauthClient/form?id=${encodeURIComponent(String(row.id))}`,
  })
}

/** 二次确认后删除客户端并刷新列表 */
function onRemove(row: SysOauthClient) {
  uni.showModal({
    title: '确认删除',
    content: `删除客户端「${row.clientName || row.clientId || ''}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeOauthClient([row.id!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

/** 列表快捷启停 */
async function onToggleStatus(row: SysOauthClient) {
  if (!canEdit.value) return
  const next = row.status === '1' ? '0' : '1'
  try {
    await updateOauthClient({
      id: row.id,
      clientId: row.clientId,
      clientName: row.clientName,
      apiPathPatterns: row.apiPathPatterns,
      tokenTimeout: row.tokenTimeout,
      checkCaptcha: row.checkCaptcha,
      status: next,
      remark: row.remark,
    })
    row.status = next
    toastOk('状态已更新')
    load(true)
  }
  catch (e) {
    toastErr(e)
  }
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="客户端名称或 ID"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd"
    />
    <QbListFilters v-model="filters.status" label="状态" :options="sys_normal_disable" />

    <QbListCard
      v-for="row in rows"
      :key="String(row.id)"
      :title="row.clientName || '—'"
      :subtitle="row.clientId || '—'"
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
        <text v-if="canEdit" class="qb-link" @click="onToggleStatus(row)">
          {{ row.status === '1' ? '启用' : '停用' }}
        </text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无客户端"
    />
  </view>
</template>
