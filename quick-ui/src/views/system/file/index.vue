<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="fileId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :delete-button-permi="['system:file:remove']"
      :show-export-button="false"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left>
        <el-button type="primary" plain @click="openUpload" v-hasPermi="['system:file:upload']">上传</el-button>
        <el-button plain @click="classifyInfoVisible = true">分类说明</el-button>
      </template>

      <template #classify="{ row }">
        <el-button link type="primary" @click="openClassifyDetail(row.classify)">{{ row.classify || '—' }}</el-button>
      </template>

      <template #sizeBytes="{ row }">
        {{ formatFileSize(row.sizeBytes) }}
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="handlePreview(row)" v-hasPermi="['system:file:view']">预览</el-button>
        <el-button link type="primary" @click="handleDownload(row)" v-hasPermi="['system:file:download']">下载</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除文件「${row.originalName}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:file:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog
      v-model="uploadVisible"
      title="上传文件"
      :on-confirm="submitUpload"
      width="560px"
      @closed="resetUploadDialog"
    >
      <el-form label-width="90px">
        <el-form-item label="分类" required>
          <el-select
            v-model="uploadClassify"
            placeholder="请选择上传分类"
            filterable
            style="width: 100%"
            @change="onUploadClassifyChange"
          >
            <el-option
              v-for="item in classifyOptions"
              :key="item.classify"
              :label="item.classify"
              :value="item.classify"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <C7Upload
            v-if="uploadVisible && uploadClassify"
            ref="c7UploadRef"
            :key="uploadSessionKey + '-' + uploadClassify"
            :classify="uploadClassify"
            :auto-upload="false"
            :upload-fn="systemUploadFn"
            v-model:results="uploadResults"
          />
          <div v-else class="sys-file-upload__placeholder">请先选择分类</div>
        </el-form-item>
      </el-form>
    </c7-dialog>

    <c7-dialog v-model="classifyInfoVisible" title="上传分类说明" :show-confirm="false" width="720px">
      <el-table :data="classifyOptions" border stripe size="small" empty-text="暂无分类配置">
        <el-table-column prop="classify" label="分类" width="100" />
        <el-table-column label="大小上限" width="100">
          <template #default="{ row }">{{ formatClassifySize(row) }}</template>
        </el-table-column>
        <el-table-column prop="limitCount" label="最多文件数" width="100" align="center" />
        <el-table-column label="允许类型" min-width="160">
          <template #default="{ row }">{{ row.limitExt?.trim() ? row.limitExt : '内置默认白名单' }}</template>
        </el-table-column>
        <el-table-column label="匿名上传" width="90" align="center">
          <template #default="{ row }">{{ row.anonymous ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
    </c7-dialog>

    <c7-dialog v-model="classifyDetailVisible" :title="`分类：${classifyDetail?.classify || ''}`" :show-confirm="false" width="480px">
      <el-descriptions v-if="classifyDetail" :column="1" border size="small">
        <el-descriptions-item label="分类名">{{ classifyDetail.classify }}</el-descriptions-item>
        <el-descriptions-item label="大小上限">{{ formatClassifySize(classifyDetail) }}</el-descriptions-item>
        <el-descriptions-item label="最多文件数">{{ classifyDetail.limitCount }}</el-descriptions-item>
        <el-descriptions-item label="允许类型">
          {{ classifyDetail.limitExt?.trim() ? classifyDetail.limitExt : '内置默认白名单' }}
        </el-descriptions-item>
        <el-descriptions-item label="匿名上传">{{ classifyDetail.anonymous ? '是' : '否' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未找到该分类配置" />
    </c7-dialog>

    <c7-dialog v-model="previewVisible" title="预览" :show-confirm="false" width="720px" @closed="handlePreviewClosed">
      <div v-if="previewType === 'image'" class="preview-body">
        <el-image :src="previewUrl" fit="contain" style="width: 100%; height: 520px" />
      </div>
      <div v-else-if="previewType === 'video'" class="preview-body">
        <video ref="videoRef" :src="previewUrl" controls style="width: 100%; max-height: 520px" />
      </div>
      <div v-else class="preview-body">
        <el-empty description="该类型不支持弹窗预览，将使用新窗口打开。" />
      </div>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import { formatFileSize, listFileClassifies } from '@/api/common/file'
import { buildFileViewUrl, downloadFile, listFile, removeFile, uploadFile } from '@/api/system/file'

/**
 * 文件管理页：上传、分页列表、预览、下载、删除；分类规则从 /file/classifies 动态加载。
 */
defineOptions({ name: 'SysFile' })

const tableRef = ref(null)
const classifyOptions = ref([])

const defaultSearchParam = {
  originalName: '',
  uploaderUserName: '',
  classify: ''
}

const classifySelectOptions = computed(() =>
  classifyOptions.value.map((c) => ({ label: c.classify, value: c.classify }))
)

const searchColumns = computed(() => [
  { prop: 'originalName', label: '文件名', type: 'input', span: 8, props: { placeholder: '请输入文件名', clearable: true } },
  { prop: 'uploaderUserName', label: '上传人', type: 'input', span: 8, props: { placeholder: '请输入上传人', clearable: true } },
  {
    prop: 'classify',
    label: '分类',
    type: 'select',
    span: 8,
    options: classifySelectOptions.value,
    props: { placeholder: '全部分类', clearable: true }
  }
])

const tableColumns = computed(() => [
  { prop: 'originalName', label: '文件名', minWidth: 180, showOverflowTooltip: true },
  { prop: 'classify', label: '分类', width: 110, columnType: 'slot', slotName: 'classify' },
  { prop: 'relativePath', label: '文件路径', minWidth: 220, showOverflowTooltip: true },
  { prop: 'sizeBytes', label: '大小', width: 100, columnType: 'slot', slotName: 'sizeBytes' },
  { prop: 'ext', label: '扩展名', width: 90 },
  { prop: 'uploaderUserName', label: '上传人', width: 120 },
  { prop: 'uploadTime', label: '上传时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 200, fixed: 'right' }
])

function listFunction(params) {
  return listFile(params)
}

function batchDeleteFunction(ids) {
  return removeFile(ids)
}

function removeRow(row) {
  return removeFile([row.fileId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function loadClassifies() {
  return listFileClassifies()
    .then((res) => {
      classifyOptions.value = Array.isArray(res?.data) ? res.data : []
    })
    .catch(() => {
      classifyOptions.value = []
    })
}

onMounted(() => {
  loadClassifies()
})

function formatClassifySize(row) {
  if (row?.limitSizeBytes > 0) return formatFileSize(row.limitSizeBytes)
  if (row?.limitSize != null && typeof row.limitSize === 'string') return row.limitSize
  return '—'
}

// 上传
const uploadVisible = ref(false)
const uploadClassify = ref('')
const uploadSessionKey = ref(0)
const c7UploadRef = ref(null)
const uploadResults = ref([])

function systemUploadFn(file, classify) {
  return uploadFile(file, classify)
}

/** 关闭上传弹窗或重新打开时重置列表，避免上次已成功文件仍展示 */
function resetUploadDialog() {
  uploadResults.value = []
  c7UploadRef.value?.clearFiles()
}

function openUpload() {
  uploadSessionKey.value += 1
  uploadClassify.value = classifyOptions.value[0]?.classify || ''
  uploadResults.value = []
  uploadVisible.value = true
}

function onUploadClassifyChange() {
  uploadResults.value = []
  c7UploadRef.value?.clearFiles()
}

function submitUpload() {
  if (!uploadClassify.value) {
    ElMessage.warning('请选择上传分类')
    return Promise.reject(new Error('no classify'))
  }
  return c7UploadRef.value?.submit().then(() => {
    ElMessage.success('上传成功')
    uploadVisible.value = false
    tableRef.value?.refreshData()
  })
}

// 分类说明
const classifyInfoVisible = ref(false)
const classifyDetailVisible = ref(false)
const classifyDetail = ref(null)

function openClassifyDetail(classify) {
  if (!classify) {
    ElMessage.warning('该记录无分类信息')
    return
  }
  classifyDetail.value = classifyOptions.value.find((c) => c.classify === classify) || null
  classifyDetailVisible.value = true
}

// 预览
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('other')
const videoRef = ref(null)

function isImageExt(ext) {
  const e = String(ext || '').toLowerCase()
  return ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes(e)
}

function isVideoExt(ext) {
  const e = String(ext || '').toLowerCase()
  return ['mp4', 'webm', 'ogg'].includes(e)
}

function handlePreview(row) {
  if (!row?.relativePath) {
    ElMessage.error('文件路径为空')
    return
  }
  const url = buildFileViewUrl(row.relativePath)
  if (!url) {
    ElMessage.error('无法构建预览地址')
    return
  }
  if (isImageExt(row.ext)) {
    previewType.value = 'image'
    previewUrl.value = url
    previewVisible.value = true
    return
  }
  if (isVideoExt(row.ext)) {
    previewType.value = 'video'
    previewUrl.value = url
    previewVisible.value = true
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

function handlePreviewClosed() {
  try {
    if (videoRef.value) {
      videoRef.value.pause()
      videoRef.value.currentTime = 0
    }
  } catch {
    // ignore
  }
}

// 下载
function decodeDownloadFilename(raw) {
  if (!raw) return ''
  try {
    return decodeURIComponent(String(raw))
  } catch {
    return String(raw)
  }
}

function resolveFileNameFromHeaders(headers, fallback) {
  const raw = headers?.['download-filename'] || headers?.['Download-Filename']
  const decoded = decodeDownloadFilename(raw)
  if (decoded && decoded.trim() !== '') return decoded
  return fallback || 'download'
}

function handleDownload(row) {
  if (!row) return
  return downloadFile(row.fileId).then(({ data, headers }) => {
    const fileName = resolveFileNameFromHeaders(headers, row.originalName)
    saveAs(new Blob([data]), fileName)
  })
}
</script>

<style scoped>
.preview-body {
  padding: 4px 0;
}

.sys-file-upload__placeholder {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
