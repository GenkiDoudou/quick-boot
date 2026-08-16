<script setup lang="ts">
/**
 * 文件管理：文件名关键词 + 分类筛选、必选分类后上传、预览/下载/删除。
 * 分类筛选项由 listFileClassifies 动态组装为 DictOption，经 QbListFilters 展示。
 */
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  downloadFileTemp,
  listFile,
  listFileClassifies,
  removeFile,
  uploadFile,
  type FileClassifyOption,
  type SysFile,
} from '@/api/system/file'
import type { DictOption } from '@/api/system/dict'
import { toGetPageQuery } from '@/api/pageQuery'
import { usePagedList } from '@/composables/usePagedList'
import { stashDetailRow } from '@/utils/detailStash'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const canUpload = computed(() => hasPermi('system:file:upload'))
/** 元数据查看：query / list / view 任一 */
const canView = computed(() =>
  hasPermi(['system:file:query', 'system:file:list', 'system:file:view']),
)
/** 预览文件内容，沿用 view 权限 */
const canPreview = computed(() => hasPermi('system:file:view'))
const canDownload = computed(() => hasPermi('system:file:download'))
const canRemove = computed(() => hasPermi('system:file:remove'))

const classifyOptions = ref<FileClassifyOption[]>([])
/** 供 QbListFilters 使用的动态分类芯片（不含「全部」，组件会自动 prepend） */
const classifyFilterOptions = ref<DictOption[]>([])
const uploadClassify = ref('')
/** 分类筛选；空串=全部 */
const filters = ref({ classify: '' })

const cardColumns: QbCardColumn[] = [
  { prop: 'classify', label: '分类', span: 12, kv: 'row' },
  { prop: 'sizeText', label: '大小', span: 12, kv: 'row' },
  { prop: 'uploaderUserName', label: '上传人', span: 24, kv: 'row', showIfProp: true },
]

const { keyword, rows, loading, finished, onSearch, load } = usePagedList<SysFile>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    listFile({
      ...toGetPageQuery(current, size),
      originalName: kw || undefined,
      classify: f.classify || undefined,
    }),
})

/** 加载启用中分类；同时刷新筛选项与上传 ActionSheet 数据源 */
async function ensureClassifies() {
  try {
    classifyOptions.value = (await listFileClassifies()) || []
    classifyFilterOptions.value = classifyOptions.value
      .filter((c) => c.classify)
      .map((c) => ({
        label: String(c.classifyName || c.classify),
        value: String(c.classify),
      }))
  }
  catch (e) {
    toastErr(e)
  }
}

onShow(() => {
  // usePagedList 已 onShow 拉列表；此处补分类选项供筛选芯片
  ensureClassifies()
})

function onUploadTap() {
  if (!canUpload.value) return
  ensureClassifies().then(() => {
    if (!classifyOptions.value.length) {
      toastInfo('暂无可用分类')
      return
    }
    // ActionSheet 选分类，避免各端 picker 回参不一致；上传仍强制分类
    uni.showActionSheet({
      itemList: classifyOptions.value.map(
        (c) => `${c.classifyName || c.classify}（${c.classify}）`,
      ),
      success: (res) => {
        const hit = classifyOptions.value[res.tapIndex]
        const key = String(hit?.classify || '').trim()
        if (!key) {
          toastInfo('请选择分类')
          return
        }
        uploadClassify.value = key
        pickAndUpload(key)
      },
    })
  })
}

function pickAndUpload(classify: string) {
  // #ifdef H5
  uni.chooseFile({
    count: 1,
    success: async (res) => {
      const path = (res.tempFilePaths && res.tempFilePaths[0])
        || (res.tempFiles && (res.tempFiles[0] as { path?: string })?.path)
      if (!path) {
        toastInfo('未选择文件')
        return
      }
      try {
        await uploadFile(String(path), classify)
        toastOk('上传成功')
        load(true)
      }
      catch (err) {
        toastErr(err)
      }
    },
    fail: () => toastInfo('已取消选择'),
  })
  // #endif
  // #ifndef H5
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const path = res.tempFilePaths?.[0]
      if (!path) return
      try {
        await uploadFile(path, classify)
        toastOk('上传成功')
        load(true)
      }
      catch (err) {
        toastErr(err)
      }
    },
  })
  // #endif
}

/** 暂存行后进入元数据只读页（后端无 get-by-id） */
function goView(row: SysFile) {
  stashDetailRow('file', row)
  uni.navigateTo({ url: '/pages/system/file/detail' })
}

async function onPreview(row: SysFile) {
  try {
    const path = await downloadFileTemp(row.fileId!, 'preview')
    const ext = String(row.ext || '').toLowerCase()
    if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp'].includes(ext)) {
      uni.previewImage({ urls: [path] })
      return
    }
    uni.openDocument({
      filePath: path,
      showMenu: true,
      fail: () => toastInfo('当前端无法预览该类型，请下载'),
    })
  }
  catch (e) {
    toastErr(e)
  }
}

async function onDownload(row: SysFile) {
  try {
    const path = await downloadFileTemp(row.fileId!, 'download')
    toastOk('已下载到临时文件')
    uni.openDocument({
      filePath: path,
      showMenu: true,
      fail: () => toastInfo(path),
    })
  }
  catch (e) {
    toastErr(e)
  }
}

function onRemove(row: SysFile) {
  uni.showModal({
    title: '确认删除',
    content: `删除「${row.originalName}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeFile([row.fileId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

function fmtSize(bytes?: number) {
  const n = Number(bytes)
  if (!Number.isFinite(n) || n <= 0) return '—'
  if (n < 1024) return `${n}B`
  if (n < 1024 * 1024) return `${Math.round(n / 1024)}KB`
  return `${(n / (1024 * 1024)).toFixed(1)}MB`
}

/** 扩展 sizeText 供 columns 展示 */
function cardRow(row: SysFile) {
  return {
    ...row,
    sizeText: fmtSize(row.sizeBytes),
  }
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="文件名"
      :show-add="canUpload"
      add-text="上传"
      @search="onSearch"
      @add="onUploadTap"
    />
    <!-- 选项未就绪时不渲染，避免 QbListFilters 空 options 回退成正常/停用 -->
    <QbListFilters
      v-if="classifyFilterOptions.length"
      v-model="filters.classify"
      label="分类"
      :options="classifyFilterOptions"
    />

    <QbListCard
      v-for="row in rows"
      :key="String(row.fileId)"
      :title="row.originalName || '—'"
      :subtitle="row.classify || '—'"
    >
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="cardRow(row)" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text v-if="canPreview" class="qb-link" @click="onPreview(row)">预览</text>
        <text v-if="canDownload" class="qb-link" @click="onDownload(row)">下载</text>
        <text v-if="canRemove" class="qb-link qb-link--danger" @click="onRemove(row)">删除</text>
      </template>
    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无文件"
    />
  </view>
</template>
