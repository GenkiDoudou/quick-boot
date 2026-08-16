<script setup lang="ts">
/**
 * 系统角色列表：名称关键词 + 状态筛选；卡片展示权限字符/排序/备注。
 * 管理员角色 roleId=1 不可停用与删除；支持查看（mode=view）。
 */
import { computed, ref } from 'vue'
import {
  pageRole,
  changeRoleStatus,
  removeRole,
  type SysRole,
} from '@/api/system/role'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_normal_disable } = useDict('sys_normal_disable')
const canAdd = computed(() => hasPermi('system:role:add'))
const canEdit = computed(() => hasPermi('system:role:edit'))
const canRemove = computed(() => hasPermi('system:role:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:role:query', 'system:role:list']))

/** 状态筛选；空串=全部 */
const filters = ref({ status: '' })

/** 卡片字段：备注有值才显示 */
const cardColumns: QbCardColumn[] = [
  { prop: 'roleKey', label: '权限字符', span: 12, kv: 'row' },
  { prop: 'roleSort', label: '排序', span: 12, kv: 'row' },
  { prop: 'remark', label: '备注', span: 24, kv: 'stack', showIfProp: true },
]

const {
  keyword,
  rows,
  loading,
  finished,
  onSearch,
  load,
} = usePagedList<SysRole>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageRole({
      current,
      size,
      param: {
        roleName: kw || undefined,
        status: f.status || undefined,
      },
    }),
})

/** 内置管理员角色，禁止停用 */
function isAdminRole(row: SysRole) {
  return String(row.roleId) === '1'
}

function goAdd() {
  uni.navigateTo({ url: '/pages/system/role/form' })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysRole) {
  uni.navigateTo({
    url: `/pages/system/role/form?roleId=${encodeURIComponent(String(row.roleId))}&mode=view`,
  })
}

function goEdit(row: SysRole) {
  uni.navigateTo({ url: `/pages/system/role/form?roleId=${encodeURIComponent(String(row.roleId))}` })
}

/** 二次确认删除；roleId=1 禁止 */
function onRemove(row: SysRole) {
  if (isAdminRole(row)) {
    toastInfo('不能删除管理员角色')
    return
  }
  uni.showModal({
    title: '确认删除',
    content: `删除角色「${row.roleName || ''}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeRole([row.roleId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

/** 切换角色启停；管理员角色不可操作 */
async function onToggleStatus(row: SysRole) {
  if (isAdminRole(row)) {
    toastInfo('不能停用管理员角色')
    return
  }
  const next = row.status === '1' ? '0' : '1'
  try {
    await changeRoleStatus({ roleId: row.roleId!, status: next })
    row.status = next
    toastOk('状态已更新')
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
      placeholder="角色名称"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd"
    />
    <QbListFilters v-model="filters.status" label="状态" :options="sys_normal_disable" />

    <QbListCard
      v-for="row in rows"
      :key="String(row.roleId)"
      :title="row.roleName || '—'"
      :subtitle="row.roleKey || '—'"
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
        <text
          v-if="canEdit && !isAdminRole(row)"
          class="qb-link"
          @click="onToggleStatus(row)"
        >
          {{ row.status === '1' ? '启用' : '停用' }}
        </text>
        <text
          v-if="canRemove && !isAdminRole(row)"
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
      empty-text="暂无角色"
    />
  </view>
</template>
