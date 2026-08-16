<script setup lang="ts">
/**
 * 定时任务列表：名称关键词 + 状态筛选；启停、执行一次、跳转调度日志、查看与删除。
 * 无 Cron 编辑 UI（仅展示 cronExpression）；详情走只读 form。
 */
import { computed, ref } from 'vue'
import { changeJobStatus, pageJobs, removeJob, runJob, type SysJob } from '@/api/monitor/job'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'
import type { DictOption } from '@/api/system/dict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'

const canChange = computed(() => hasPermi('monitor:job:changeStatus'))
const canRemove = computed(() => hasPermi('monitor:job:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['monitor:job:query', 'monitor:job:list']))

/** 任务状态：0 正常 / 1 暂停（与后端一致，非 sys_normal_disable） */
const jobStatusOptions: DictOption[] = [
  { label: '正常', value: '0' },
  { label: '暂停', value: '1' },
]

/** 列表筛选：空串 = 全部 */
const filters = ref({ status: '' })

/** 卡片字段：组名 / Cron / 调用目标（长文本 stack） */
const cardColumns: QbCardColumn[] = [
  { prop: 'jobGroup', label: '分组', span: 12, kv: 'row' },
  { prop: 'cronExpression', label: 'Cron', span: 12, kv: 'row' },
  { prop: 'invokeTarget', label: '调用目标', span: 24, kv: 'stack' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysJob>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageJobs(current, size, kw || undefined, f.status),
})

async function onToggle(row: SysJob) {
  const next = row.status === '0' ? '1' : '0'
  try {
    await changeJobStatus({ jobId: row.jobId!, status: next })
    row.status = next
    toastOk(next === '0' ? '已启用' : '已暂停')
  }
  catch (e) {
    toastErr(e)
  }
}

function onRun(row: SysJob) {
  uni.showModal({
    title: '立即执行',
    content: `执行任务「${row.jobName}」一次？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await runJob(row.jobId!)
        toastOk('已触发执行')
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

/** 只读详情：job/form?jobId= */
function goView(row: SysJob) {
  uni.navigateTo({
    url: `/pages/monitor/job/form?jobId=${encodeURIComponent(String(row.jobId))}`,
  })
}

function goLogs(row: SysJob) {
  uni.navigateTo({
    url: `/pages/monitor/jobLog/index?jobName=${encodeURIComponent(String(row.jobName || ''))}`,
  })
}

/** 单行删除：确认后调用 removeJob */
function onRemove(row: SysJob) {
  uni.showModal({
    title: '确认删除',
    content: `删除任务「${row.jobName || ''}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeJob([row.jobId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

function statusText(s?: string) {
  return s === '0' ? '正常' : '暂停'
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="任务名称"
      :show-add="false"
      @search="onSearch"
    />
    <QbListFilters
      v-model="filters.status"
      label="状态"
      :options="jobStatusOptions"
    />

    <QbListCard
      v-for="row in rows"
      :key="String(row.jobId)"
      :title="row.jobName || '-'"
    >
      <template #status>
        <text class="qb-muted">{{ statusText(row.status) }}</text>
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text v-if="canChange" class="qb-link" @click="onToggle(row)">
          {{ row.status === '0' ? '暂停' : '启用' }}
        </text>
        <text v-if="canChange" class="qb-link" @click="onRun(row)">执行</text>
        <text class="qb-link" @click="goLogs(row)">日志</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无任务"
    />
  </view>
</template>
