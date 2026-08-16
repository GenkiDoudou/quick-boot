<script setup lang="ts">
/**
 * 调度日志列表：任务名关键词 + 状态筛选；详情 / 删除 / 清空。
 * SysJobLog 无 costTime 字段，卡片不展示耗时。
 */
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  cleanJobLog,
  pageJobLogs,
  removeJobLog,
  type SysJobLog,
} from '@/api/monitor/jobLog'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'
import type { DictOption } from '@/api/system/dict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'

const presetJobName = ref('')
const canRemove = computed(() => hasPermi('monitor:job:remove'))

/** 执行结果：0 成功 / 1 失败 */
const jobLogStatusOptions: DictOption[] = [
  { label: '成功', value: '0' },
  { label: '失败', value: '1' },
]

const filters = ref({ status: '' })

/** 消息 / 分组 / 时间；无 costTime 故不配耗时列 */
const cardColumns: QbCardColumn[] = [
  { prop: 'jobMessage', label: '消息', span: 24, kv: 'stack' },
  { prop: 'jobGroup', label: '分组', span: 12, kv: 'row' },
  { prop: 'createTime', label: '时间', span: 12, kv: 'row' },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysJobLog>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageJobLogs(
      current,
      size,
      kw || presetJobName.value || undefined,
      f.status,
    ),
})

onLoad((query) => {
  const name = query?.jobName ? String(query.jobName) : ''
  presetJobName.value = name
  if (name) {
    keyword.value = name
    uni.setNavigationBarTitle({ title: `调度日志 · ${name}` })
  }
})

function goDetail(row: SysJobLog) {
  uni.navigateTo({
    url: `/pages/monitor/jobLog/detail?jobLogId=${encodeURIComponent(String(row.jobLogId))}`,
  })
}

function onRemove(row: SysJobLog) {
  uni.showModal({
    title: '确认删除',
    content: '删除该条调度日志？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeJobLog([row.jobLogId!])
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
    title: '清空日志',
    content: '清空全部调度日志？不可恢复',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await cleanJobLog()
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
      placeholder="任务名称"
      :show-add="false"
      @search="onSearch"
    />
    <QbListFilters
      v-model="filters.status"
      label="状态"
      :options="jobLogStatusOptions"
    />
    <view v-if="canRemove" class="qb-toolbar">
      <text class="qb-link qb-link--danger" @click="onClean">清空</text>
    </view>

    <QbListCard
      v-for="row in rows"
      :key="String(row.jobLogId)"
      :title="row.jobName || '-'"
    >
      <template #status>
        <text class="qb-muted">{{ row.status === '0' ? '成功' : '失败' }}</text>
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
      empty-text="暂无日志"
    />
  </view>
</template>
