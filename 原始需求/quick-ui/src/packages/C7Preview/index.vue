<template>
  <div class="c7-preview" :style="previewStyle">
    <!-- coverType: none —— 直接展示 -->
    <template v-if="normalizedCoverType === 'none'">
      <!-- 图片 -->
      <template v-if="resolvedDisplayType === 'image'">
        <el-image
          v-for="(url, idx) in fileUrls"
          :key="idx"
          :src="url"
          :preview-src-list="fileUrls"
          :initial-index="idx"
          fit="cover"
          :style="previewStyle"
          class="c7-preview__image"
          preview-teleported
        />
      </template>

      <!-- 视频 -->
      <template v-else-if="resolvedDisplayType === 'video'">
        <div
          v-for="(url, idx) in fileUrls"
          :key="idx"
          class="c7-preview__video-cover"
          :style="previewStyle"
          @click="openVideo(url, idx)"
        >
          <img :src="props.defaultVideoImage" alt="video" class="c7-preview__cover-img" />
          <div class="c7-preview__play-icon">
            <svg viewBox="0 0 24 24" fill="white" width="32" height="32">
              <path d="M8 5v14l11-7z" />
            </svg>
          </div>
        </div>
      </template>

      <!-- 文件 -->
      <template v-else>
        <div
          v-for="(url, idx) in fileUrls"
          :key="idx"
          class="c7-preview__file-cover"
          :style="previewStyle"
          @click="openFile(url, idx)"
        >
          <img :src="props.defaultFileImage" alt="file" class="c7-preview__cover-img" />
          <span class="c7-preview__file-name">{{ extractFileName(url) }}</span>
        </div>
      </template>
    </template>

    <!-- coverType: button —— 按钮触发 -->
    <template v-else-if="normalizedCoverType === 'button'">
      <el-button type="primary" plain size="small" @click="handleButtonClick">
        {{ buttonText }}
        <span v-if="showCount" class="c7-preview__count-badge">{{ fileUrls.length }}</span>
      </el-button>
    </template>

    <!-- coverType: file —— 文件列表触发 -->
    <template v-else-if="normalizedCoverType === 'file'">
      <div class="c7-preview__file-list">
        <div
          v-for="(url, idx) in fileUrls"
          :key="idx"
          class="c7-preview__file-item"
          @click="handleFileItemClick(url, idx)"
        >
          <el-icon class="c7-preview__file-icon">
            <component :is="fileItemIcon" />
          </el-icon>
          <span class="c7-preview__file-item-name">{{ extractFileName(url) }}</span>
        </div>
        <span v-if="showCount" class="c7-preview__file-count">共 {{ fileUrls.length }} 个</span>
      </div>
    </template>

    <!-- 视频播放对话框 -->
    <el-dialog
      v-model="videoDialogVisible"
      :title="videoDialogTitle"
      width="700px"
      append-to-body
      destroy-on-close
      :before-close="handleVideoDialogClose"
    >
      <video
        v-if="videoDialogVisible"
        ref="videoRef"
        :src="currentVideoUrl"
        controls
        autoplay
        style="width: 100%; max-height: 480px; display: block;"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Document, VideoPlay, Picture } from '@element-plus/icons-vue'

defineOptions({ name: 'C7Preview' })

// ── 内置 SVG 占位图（避免依赖外部 URL）──
const DEFAULT_VIDEO_SVG = `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 80 80"><rect width="80" height="80" rx="8" fill="%23f0f2f5"/><path d="M28 24v32l28-16z" fill="%23909399"/></svg>`
const DEFAULT_FILE_SVG  = `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 80 80"><rect width="80" height="80" rx="8" fill="%23f0f2f5"/><path d="M22 16h24l14 14v38a2 2 0 01-2 2H22a2 2 0 01-2-2V18a2 2 0 012-2z" fill="%23dcdfe6" stroke="%23909399" stroke-width="1.5"/><path d="M46 16v14h14" fill="none" stroke="%23909399" stroke-width="1.5"/><line x1="28" y1="40" x2="52" y2="40" stroke="%23909399" stroke-width="2" stroke-linecap="round"/><line x1="28" y1="50" x2="46" y2="50" stroke="%23909399" stroke-width="2" stroke-linecap="round"/></svg>`

// 自动识别扩展名
const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg']
const VIDEO_EXTS = ['mp4', 'webm', 'ogg', 'mov', 'avi']

const props = defineProps({
  /** 逗号分隔的 URL 字符串 */
  urls: {
    type: String,
    default: ''
  },
  /** 容器宽度 */
  width: {
    type: [String, Number],
    default: undefined
  },
  /** 容器高度 */
  height: {
    type: [String, Number],
    default: undefined
  },
  /** 展示类型: image | video | file */
  displayType: {
    type: String,
    default: 'image',
    validator: (v) => ['image', 'video', 'file'].includes(v)
  },
  /** 自动根据 URL 扩展名识别类型 */
  autoDetect: {
    type: Boolean,
    default: false
  },
  /** 封面模式: none | button | file（兼容大写 None） */
  coverType: {
    type: String,
    default: 'none'
  },
  /** button 模式下按钮文字 */
  buttonText: {
    type: String,
    default: '预览'
  },
  /** 视频默认封面图 */
  defaultVideoImage: {
    type: String,
    default: () => DEFAULT_VIDEO_SVG
  },
  /** 文件默认封面图 */
  defaultFileImage: {
    type: String,
    default: () => DEFAULT_FILE_SVG
  },
  /** 是否显示文件数量角标 */
  showCount: {
    type: Boolean,
    default: false
  },
  /**
   * 预览前钩子，返回 false 可阻止预览
   * (url: string, index: number) => boolean | void
   */
  onPreview: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['preview', 'close'])

// ── 解析 URL 列表 ──
const fileUrls = computed(() => {
  if (!props.urls) return []
  return props.urls
    .split(',')
    .map((u) => u.trim())
    .filter(Boolean)
})

// ── 宽高样式 ──
const previewStyle = computed(() => {
  const style = {}
  if (props.width !== undefined) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  if (props.height !== undefined) {
    style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  return style
})

// ── 规范化 coverType（兼容 'None'）──
const normalizedCoverType = computed(() =>
  (props.coverType || 'none').toLowerCase()
)

// ── 自动识别或使用指定的 displayType ──
const resolvedDisplayType = computed(() => {
  if (!props.autoDetect) return props.displayType
  const ext = fileUrls.value[0]?.split('.').pop()?.toLowerCase() || ''
  if (IMAGE_EXTS.includes(ext)) return 'image'
  if (VIDEO_EXTS.includes(ext)) return 'video'
  return 'file'
})

// ── file 列表模式下的图标 ──
const fileItemIcon = computed(() => {
  if (resolvedDisplayType.value === 'video') return VideoPlay
  if (resolvedDisplayType.value === 'image') return Picture
  return Document
})

// ── 视频对话框 ──
const videoDialogVisible = ref(false)
const currentVideoUrl = ref('')
const videoRef = ref(null)
const videoDialogTitle = ref('视频预览')

function openVideo(url, index) {
  if (triggerPreviewHook(url, index) === false) return
  currentVideoUrl.value = url
  videoDialogTitle.value = extractFileName(url) || '视频预览'
  videoDialogVisible.value = true
  emit('preview', url, index)
}

function handleVideoDialogClose(done) {
  if (videoRef.value) {
    videoRef.value.pause()
    videoRef.value.currentTime = 0
  }
  currentVideoUrl.value = ''
  videoDialogVisible.value = false
  emit('close')
  done()
}

// ── 文件新窗口打开 ──
function openFile(url, index) {
  if (triggerPreviewHook(url, index) === false) return
  window.open(url, '_blank')
  emit('preview', url, index)
}

// ── button / file-item 点击 ──
function handleButtonClick() {
  if (!fileUrls.value.length) return
  if (resolvedDisplayType.value === 'image') {
    // 触发第一张图片的 el-image 预览（通过隐藏触发器）
    triggerImagePreview(0)
  } else if (resolvedDisplayType.value === 'video') {
    openVideo(fileUrls.value[0], 0)
  } else {
    fileUrls.value.forEach((url, idx) => openFile(url, idx))
  }
}

function handleFileItemClick(url, index) {
  if (resolvedDisplayType.value === 'image') {
    triggerImagePreview(index)
  } else if (resolvedDisplayType.value === 'video') {
    openVideo(url, index)
  } else {
    openFile(url, index)
  }
}

// ── 图片预览（button/file-cover 模式通过隐藏 el-image 触发）──
const hiddenImageTrigger = ref(null)
const hiddenInitialIndex = ref(0)

function triggerImagePreview(index) {
  if (triggerPreviewHook(fileUrls.value[index], index) === false) return
  hiddenInitialIndex.value = index
  // 使用 el-image 内部的 clickHandler
  if (hiddenImageTrigger.value) {
    const elImage = hiddenImageTrigger.value
    if (elImage.clickHandler) {
      elImage.clickHandler()
    } else {
      // fallback: 直接触发 click 事件
      elImage.$el?.querySelector('img')?.click()
    }
  }
  emit('preview', fileUrls.value[index], index)
}

// ── onPreview 钩子 ──
function triggerPreviewHook(url, index) {
  if (typeof props.onPreview === 'function') {
    return props.onPreview(url, index)
  }
  return true
}

// ── 工具函数：提取文件名 ──
function extractFileName(url) {
  if (!url) return ''
  try {
    const pathname = new URL(url).pathname
    return decodeURIComponent(pathname.split('/').pop() || url)
  } catch {
    return url.split('/').pop() || url
  }
}
</script>

<style scoped>
.c7-preview {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-start;
  vertical-align: middle;
}

/* ── 图片 ── */
.c7-preview__image {
  border-radius: 4px;
  cursor: pointer;
  display: block;
}

/* ── 视频封面 ── */
.c7-preview__video-cover {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  background: #f0f2f5;
  min-width: 80px;
  min-height: 80px;
}

.c7-preview__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.c7-preview__play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.45);
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.c7-preview__video-cover:hover .c7-preview__play-icon {
  background: rgba(0, 0, 0, 0.65);
}

/* ── 文件封面（none 模式）── */
.c7-preview__file-cover {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  background: #f0f2f5;
  min-width: 80px;
  min-height: 80px;
  padding: 8px;
  gap: 4px;
  transition: background 0.2s;
}

.c7-preview__file-cover:hover {
  background: #e6e8eb;
}

.c7-preview__file-name {
  font-size: 11px;
  color: #606266;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

/* ── button 模式角标 ── */
.c7-preview__count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  margin-left: 4px;
  line-height: 1;
}

/* ── file 列表模式 ── */
.c7-preview__file-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.c7-preview__file-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
  max-width: 100%;
}

.c7-preview__file-item:hover {
  background: #f0f2f5;
}

.c7-preview__file-icon {
  flex-shrink: 0;
  color: #409eff;
  font-size: 16px;
}

.c7-preview__file-item-name {
  font-size: 13px;
  color: #409eff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.c7-preview__file-count {
  font-size: 12px;
  color: #909399;
  padding-left: 8px;
}
</style>
