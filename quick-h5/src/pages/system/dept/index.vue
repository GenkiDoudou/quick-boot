<script setup lang="ts">
/**
 * 部门树形列表（拍平展示）：名称关键词 + 状态筛选、下拉刷新、新增下级/编辑/删除。
 * listDept 已支持 status；筛选变更后重新拉树并拍平。
 */
import { computed, ref, watch } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { delDept, listDept, type SysDept } from '@/api/system/dept'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

type FlatDept = SysDept & { depth: number }

const { sys_normal_disable } = useDict('sys_normal_disable')
const canAdd = computed(() => hasPermi('system:dept:add'))
const canEdit = computed(() => hasPermi('system:dept:edit'))
const canRemove = computed(() => hasPermi('system:dept:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:dept:query', 'system:dept:list']))

const keyword = ref('')
/** 状态筛选；空串=全部，不传 status */
const filters = ref({ status: '' })
const flatRows = ref<FlatDept[]>([])
const loading = ref(false)

/** 卡片：负责人/电话有值才展示 */
const cardColumns: QbCardColumn[] = [
  { prop: 'leader', label: '负责人', span: 12, kv: 'row', showIfProp: true },
  { prop: 'phone', label: '电话', span: 12, kv: 'row', showIfProp: true },
]

/** 深度优先将部门树拍平，附带 depth 供卡片缩进 */
function flattenTree(nodes: SysDept[], depth = 0): FlatDept[] {
  const out: FlatDept[] = []
  for (const n of nodes || []) {
    out.push({ ...n, depth, children: undefined })
    if (n.children?.length) {
      out.push(...flattenTree(n.children, depth + 1))
    }
  }
  return out
}

/** 按关键词 + 状态拉取部门树并拍平；结束时停止下拉刷新动画 */
async function load() {
  if (loading.value) return
  loading.value = true
  try {
    const name = keyword.value.trim()
    const status = filters.value.status.trim()
    const tree = await listDept({
      deptName: name || undefined,
      status: status || undefined,
    })
    flatRows.value = flattenTree(tree || [])
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

function onSearch() {
  load()
}

/** 状态芯片变更即重载（与 usePagedList.filters 行为对齐） */
watch(
  filters,
  () => {
    load()
  },
  { deep: true },
)

function goAdd(parentId?: number | string) {
  const q = parentId != null && parentId !== ''
    ? `?parentId=${encodeURIComponent(String(parentId))}`
    : ''
  uni.navigateTo({ url: `/pages/system/dept/form${q}` })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysDept) {
  uni.navigateTo({
    url: `/pages/system/dept/form?deptId=${encodeURIComponent(String(row.deptId))}&mode=view`,
  })
}

function goEdit(row: SysDept) {
  uni.navigateTo({
    url: `/pages/system/dept/form?deptId=${encodeURIComponent(String(row.deptId))}`,
  })
}

/** 二次确认后删除部门并刷新列表 */
function onDelete(row: SysDept) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除部门「${row.deptName || ''}」吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await delDept(row.deptId!)
        toastOk('已删除')
        load()
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

onShow(() => {
  load()
})

onPullDownRefresh(() => {
  load()
})
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="部门名称"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd()"
    />
    <QbListFilters v-model="filters.status" label="状态" :options="sys_normal_disable" />

    <QbListCard
      v-for="row in flatRows"
      :key="String(row.deptId)"
      :title="row.deptName || '—'"
      :subtitle="`排序 ${row.orderNum ?? '—'}`"
      :depth="row.depth"
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
        <text v-if="canAdd" class="qb-link" @click="goAdd(row.deptId)">新增下级</text>
        <text v-if="canEdit" class="qb-link" @click="goEdit(row)">编辑</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onDelete(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :empty="!flatRows.length"
      :has-rows="!!flatRows.length"
      empty-text="暂无部门"
    />
  </view>
</template>
