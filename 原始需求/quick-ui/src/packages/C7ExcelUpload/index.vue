<template>
  <div class="c7-excel-upload">
    <!-- 重复数据处理策略 -->
    <div class="c7-excel-upload__strategy">
      <span class="c7-excel-upload__strategy-label">重复数据处理策略：</span>
      <el-radio-group v-model="duplicateStrategy" :disabled="uploading">
        <el-radio value="overwrite">{{ overwriteLabel }}</el-radio>
        <el-radio value="ignore">{{ ignoreLabel }}</el-radio>
      </el-radio-group>
    </div>

    <!-- 文件选择区 -->
    <div class="c7-excel-upload__file-row">
      <!-- 隐藏的原生 input -->
      <input
        ref="inputRef"
        type="file"
        accept=".xls,.xlsx"
        style="display:none"
        @change="handleFileChange"
      />
      <el-button :disabled="uploading" @click="inputRef.click()">
        <el-icon><UploadFilled /></el-icon>
        选择文件
      </el-button>
      <span class="c7-excel-upload__file-name">
        {{ selectedFile ? selectedFile.name : '未选择文件' }}
      </span>
      <el-button
        type="primary"
        :loading="uploading"
        :disabled="!selectedFile"
        style="margin-left:auto"
        @click="handleUpload"
      >
        <el-icon v-if="!uploading"><Upload /></el-icon>
        开始导入
      </el-button>
    </div>

    <!-- 导入结果区（上传完成后显示）-->
    <transition name="c7-upload-fade">
      <div v-if="uploadResult" class="c7-excel-upload__result">
        <div class="c7-excel-upload__stats">
          <span class="stat-item">
            <span class="stat-label">总数</span>
            <span class="stat-value total">{{ uploadResult.total }}</span>
          </span>
          <span class="stat-divider">/</span>
          <span class="stat-item">
            <span class="stat-label">成功</span>
            <span class="stat-value success">{{ uploadResult.successCount }}</span>
          </span>
          <span class="stat-divider">/</span>
          <span class="stat-item">
            <span class="stat-label">失败</span>
            <span class="stat-value" :class="uploadResult.failCount > 0 ? 'fail' : 'success'">
              {{ uploadResult.failCount }}
            </span>
          </span>
        </div>

        <!-- 失败记录下载 -->
        <div v-if="uploadResult.failCount > 0 && uploadResult.errorFileUrl" class="c7-excel-upload__error-download">
          <el-icon><Warning /></el-icon>
          <span>存在失败记录，请下载错误详情文件查看原因：</span>
          <a
            :href="uploadResult.errorFileUrl"
            target="_blank"
            download
            class="c7-excel-upload__error-link"
          >
            <el-icon><Download /></el-icon>
            {{ errorFileLabel }}
          </a>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Upload, Warning, Download } from '@element-plus/icons-vue'

defineOptions({ name: 'C7ExcelUpload' })

const props = defineProps({
  /**
   * 上传函数（必填）
   * (file: File, duplicateStrategy: 'overwrite' | 'ignore') => Promise<ExcelUploadResult>
   */
  uploadFn: {
    type: Function,
    required: true
  },
  /** 覆盖策略文案 */
  overwriteLabel: {
    type: String,
    default: '覆盖更新'
  },
  /** 忽略策略文案 */
  ignoreLabel: {
    type: String,
    default: '忽略跳过'
  },
  /** 文件大小限制（MB） */
  fileSize: {
    type: Number,
    default: 10
  },
  /** 错误文件下载按钮文案 */
  errorFileLabel: {
    type: String,
    default: '下载错误详情文件'
  },
  /**
   * 自定义通知函数，替代内置 ElMessage
   * (type: 'success' | 'error' | 'warning', message: string) => void
   */
  notify: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['success', 'error'])

// ── 内部状态 ──
const inputRef = ref(null)
const selectedFile = ref(null)
const duplicateStrategy = ref('overwrite')
const uploading = ref(false)
const uploadResult = ref(null)

// ── 暴露给父组件 ──
defineExpose({ uploading, reset })

/**
 * 触发通知
 */
function triggerNotify(type, message) {
  if (props.notify) {
    props.notify(type, message)
    return
  }
  if (type === 'error') ElMessage.error(message)
  else if (type === 'warning') ElMessage.warning(message)
  else ElMessage.success(message)
}

/**
 * 校验并选择文件
 */
function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // 重置 input，允许重复选同一文件
  e.target.value = ''

  // 类型校验
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!['xls', 'xlsx'].includes(ext)) {
    triggerNotify('error', '仅支持上传 .xls / .xlsx 格式的文件')
    return
  }

  // 大小校验
  const limitBytes = props.fileSize * 1024 * 1024
  if (file.size > limitBytes) {
    triggerNotify('error', `文件大小不能超过 ${props.fileSize}MB`)
    return
  }

  selectedFile.value = file
  // 重新选文件时清除上次结果
  uploadResult.value = null
}

/**
 * 执行上传
 */
async function handleUpload() {
  if (!selectedFile.value) {
    triggerNotify('warning', '请先选择文件')
    return
  }
  if (uploading.value) return

  uploading.value = true
  try {
    const result = await props.uploadFn(selectedFile.value, duplicateStrategy.value)
    uploadResult.value = result
    emit('success', result)
  } catch (err) {
    const error = err instanceof Error ? err : new Error(String(err))
    triggerNotify('error', error.message || '导入失败')
    emit('error', error)
  } finally {
    uploading.value = false
  }
}

/**
 * 重置组件状态（清除文件和结果）
 */
function reset() {
  selectedFile.value = null
  uploadResult.value = null
  duplicateStrategy.value = 'overwrite'
  if (inputRef.value) inputRef.value.value = ''
}
</script>

<style scoped>
.c7-excel-upload {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.c7-excel-upload__strategy {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.c7-excel-upload__strategy-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.c7-excel-upload__file-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.c7-excel-upload__file-name {
  font-size: 13px;
  color: #606266;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── 结果区 ── */
.c7-excel-upload__result {
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.c7-excel-upload__stats {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.stat-value.total  { color: #303133; }
.stat-value.success { color: #67c23a; }
.stat-value.fail   { color: #f56c6c; }

.stat-divider {
  color: #dcdfe6;
  font-size: 18px;
  line-height: 1;
}

/* ── 失败下载 ── */
.c7-excel-upload__error-download {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #e6a23c;
}

.c7-excel-upload__error-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.c7-excel-upload__error-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* ── 过渡动画 ── */
.c7-upload-fade-enter-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.c7-upload-fade-enter-from  { opacity: 0; transform: translateY(-6px); }
</style>
