<template>
  <div class="c7-preview" v-bind="$attrs">
    <!-- 空 urls：none/file 不渲染内容；button 展示禁用按钮（spec 二选一之「button 禁用」） -->
    <template v-if="isEmpty && coverType === 'button'">
      <el-button disabled>{{ previewText }}</el-button>
    </template>

    <template v-else-if="!isEmpty && coverType === 'none'">
      <div class="c7-preview__grid">
        <template v-for="(url, index) in parsedUrls" :key="`${index}-${url}`">
          <div
              v-if="kindAtIndex(index) === 'image'"
              class="c7-preview__cell c7-preview__cell--image"
              :style="coverStyle"
              @click.capture.stop="onNoneImageClick(index, $event)"
          >
            <el-image
                :ref="(el) => setImageRef(index, el)"
                :src="url"
                fit="cover"
                :preview-src-list="previewSrcList"
                :initial-index="imageInitialIndexFor(index)"
                preview-teleported
            />
          </div>
          <div
              v-else-if="kindAtIndex(index) === 'video'"
              class="c7-preview__cell c7-preview__cell--video"
              :style="coverStyle"
              role="button"
              tabindex="0"
              @click.stop="onNoneVideoClick(index)"
              @keydown.enter.prevent="onNoneVideoClick(index)"
              @keydown.space.prevent="onNoneVideoClick(index)"
          >
            <el-icon class="c7-preview__play" :size="40">
              <VideoPlay/>
            </el-icon>
          </div>
          <div
              v-else
              class="c7-preview__cell c7-preview__cell--file"
              :style="coverStyle"
              role="button"
              tabindex="0"
              @click.stop="onNoneFileClick(index)"
              @keydown.enter.prevent="onNoneFileClick(index)"
              @keydown.space.prevent="onNoneFileClick(index)"
          >
            <el-icon class="c7-preview__doc" :size="28">
              <Document/>
            </el-icon>
            <span class="c7-preview__fname">{{ fileBasename(url) }}</span>
          </div>
        </template>
      </div>
    </template>

    <template v-else-if="!isEmpty && coverType === 'button'">
      <el-badge :value="parsedUrls.length" :hidden="parsedUrls.length === 0">
        <el-button type="primary" @click="onButtonClick">{{ previewText }}</el-button>
      </el-badge>
      <el-image
          v-if="previewSrcList.length > 0"
          ref="progImageRef"
          class="c7-preview__prog-image"
          :src="previewSrcList[0]"
          fit="cover"
          :preview-src-list="previewSrcList"
          :initial-index="progImageInitialIndex"
          preview-teleported
      />
    </template>

    <template v-else-if="!isEmpty && coverType === 'file'">
      <el-table
          class="c7-preview__table"
          :data="tableRows"
          :show-header="true"
          size="small"
          @row-click="onFileRowClick"
      >
        <el-table-column label="类型" width="88">
          <template #default="{ row }">
            <span>{{ kindLabel(row.kind) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="文件名" prop="name" min-width="160"/>
      </el-table>
      <el-image
          v-if="previewSrcList.length > 0"
          ref="progImageRef"
          class="c7-preview__prog-image"
          :src="previewSrcList[0]"
          fit="cover"
          :preview-src-list="previewSrcList"
          :initial-index="progImageInitialIndex"
          preview-teleported
      />
    </template>

    <C7Dialog
        v-model="videoDialogVisible"
        :footer="false"
        :title="videoDialogTitle"
        width="720px"
    >
      <div class="c7-preview__video-body">
        <div v-if="videoUrls.length > 1" class="c7-preview__video-nav">
          <el-button text :disabled="currentVideoIndex <= 0" @click="onPrevVideo">上一条</el-button>
          <span class="c7-preview__video-idx">{{ currentVideoIndex + 1 }} / {{ videoUrls.length }}</span>
          <el-button
              text
              :disabled="currentVideoIndex >= videoUrls.length - 1"
              @click="onNextVideo"
          >下一条</el-button>
        </div>
        <video
            ref="videoPlayerRef"
            class="c7-preview__video-el"
            controls
            autoplay
            :src="currentVideoUrl"
        />
      </div>
    </C7Dialog>
  </div>
</template>

<script setup>
import {computed, nextTick, ref, shallowReactive, watch} from 'vue'
import {Document, VideoPlay} from '@element-plus/icons-vue'
import C7Dialog from '../C7Dialog/index.vue'
import {parseUrls} from './parseUrls'
import {inferMediaKind} from './inferMediaKind'

defineOptions({
  name: 'C7Preview',
  inheritAttrs: false,
})

/**
 * 附件预览：逗号分隔 `urls`，支持 **图片大图 / 视频弹窗 / 文件新窗口**。
 *
 * **钩子**：**`onPreview`** 返回 **`false` 或 reject** 时 **不**打开预览且 **不** **`emit('preview')`**；通过时 **先** **`emit('preview')`** 再打开。
 *
 * @emits preview — `(url: string, index: number)`
 * @emits close — 视频弹窗关闭且已 **pause + 进度归零** 后触发一次
 */
const props = defineProps({
  /** 逗号分隔 URL；空段在解析时丢弃 */
  urls: {type: String, default: ''},
  /** `autoDetect=false` 时整条列表按此类型渲染 */
  displayType: {
    type: String,
    default: 'image',
    validator: (v) => ['image', 'video', 'file'].includes(v),
  },
  /** 为 `true` 时按扩展名推断每条类型 */
  autoDetect: {type: Boolean, default: true},
  /** `none` 平铺、`button` 聚合、`file` 表格 */
  coverType: {
    type: String,
    default: 'none',
    validator: (v) => ['none', 'button', 'file'].includes(v),
  },
  /** 封面宽度（px） */
  width: {type: Number, default: undefined},
  /** 封面高度（px） */
  height: {type: Number, default: undefined},
  /** `coverType=button` 时按钮文案 */
  previewText: {type: String, default: '预览'},
  /** 视频弹窗标题 */
  videoDialogTitle: {type: String, default: '视频预览'},
  /**
   * 预览前拦截；返回 **`false` / `Promise<false>` / reject** 阻止。
   * @type {(url: string, index: number) => boolean | Promise<boolean> | undefined}
   */
  onPreview: {type: Function, default: null},
})

const emit = defineEmits(['preview', 'close'])

const parsedUrls = computed(() => parseUrls(props.urls))

const isEmpty = computed(() => parsedUrls.value.length === 0)

const coverStyle = computed(() => {
  const s = {}
  if (props.width != null && !Number.isNaN(Number(props.width))) {
    s.width = `${Number(props.width)}px`
  }
  if (props.height != null && !Number.isNaN(Number(props.height))) {
    s.height = `${Number(props.height)}px`
  }
  return s
})

function kindAtIndex(index) {
  const u = parsedUrls.value[index]
  if (!u) {
    return 'file'
  }
  return props.autoDetect ? inferMediaKind(u) : props.displayType
}

const previewSrcList = computed(() =>
    parsedUrls.value.filter((_, i) => kindAtIndex(i) === 'image'),
)

function imageInitialIndexFor(parsedIndex) {
  let n = 0
  for (let j = 0; j < parsedIndex; j++) {
    if (kindAtIndex(j) === 'image') {
      n++
    }
  }
  return n
}

const imageRefMap = shallowReactive({})

function setImageRef(index, el) {
  if (el) {
    imageRefMap[index] = el
  } else {
    delete imageRefMap[index]
  }
}

const progImageRef = ref(null)
const progImageInitialIndex = ref(0)

const videoDialogVisible = ref(false)
const videoUrls = ref([])
const currentVideoIndex = ref(0)
const videoPlayerRef = ref(null)

const currentVideoUrl = computed(() => videoUrls.value[currentVideoIndex.value] || '')

const tableRows = computed(() =>
    parsedUrls.value.map((url, index) => ({
      __index: index,
      name: fileBasename(url),
      kind: kindAtIndex(index),
    })),
)

function kindLabel(kind) {
  if (kind === 'image') {
    return '图片'
  }
  if (kind === 'video') {
    return '视频'
  }
  return '文件'
}

function fileBasename(url) {
  try {
    const noHash = url.split('#')[0]
    const path = noHash.split('?')[0]
    const seg = path.includes('/') ? path.split('/').pop() : path
    if (!seg) {
      return '文件'
    }
    return decodeURIComponent(seg)
  } catch {
    return '文件'
  }
}

/**
 * @param {string} url
 * @param {number} index
 * @returns {Promise<boolean>}
 */
async function runGuard(url, index) {
  if (typeof props.onPreview !== 'function') {
    return true
  }
  try {
    const r = await props.onPreview(url, index)
    return r !== false
  } catch {
    return false
  }
}

function resetVideoEl() {
  const v = videoPlayerRef.value
  if (v) {
    v.pause()
    v.currentTime = 0
  }
}

watch(videoDialogVisible, (isOpen, wasOpen) => {
  if (wasOpen === true && isOpen === false) {
    resetVideoEl()
    emit('close')
  }
})

async function openVideoDialog(urlList, startIndex, emitUrl, emitIndex) {
  if (!(await runGuard(emitUrl, emitIndex))) {
    return
  }
  emit('preview', emitUrl, emitIndex)
  videoUrls.value = [...urlList]
  currentVideoIndex.value = startIndex
  videoDialogVisible.value = true
}

async function onNoneImageClick(index, _evt) {
  const url = parsedUrls.value[index]
  if (!(await runGuard(url, index))) {
    return
  }
  emit('preview', url, index)
  await nextTick()
  const inst = imageRefMap[index]
  if (typeof inst?.showPreview === 'function') {
    inst.showPreview()
  }
}

async function onNoneVideoClick(index) {
  const url = parsedUrls.value[index]
  await openVideoDialog([url], 0, url, index)
}

async function onNoneFileClick(index) {
  const url = parsedUrls.value[index]
  if (!(await runGuard(url, index))) {
    return
  }
  emit('preview', url, index)
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function onButtonClick() {
  const urls = parsedUrls.value
  if (urls.length === 0) {
    return
  }
  const imgs = previewSrcList.value
  if (imgs.length > 0) {
    const firstIdx = urls.findIndex((_, i) => kindAtIndex(i) === 'image')
    const u0 = urls[firstIdx]
    if (!(await runGuard(u0, firstIdx))) {
      return
    }
    emit('preview', u0, firstIdx)
    progImageInitialIndex.value = 0
    await nextTick()
    progImageRef.value?.showPreview?.()
    return
  }
  const vIdx = urls.findIndex((_, i) => kindAtIndex(i) === 'video')
  if (vIdx !== -1) {
    const list = urls.filter((_, i) => kindAtIndex(i) === 'video')
    const u = urls[vIdx]
    await openVideoDialog(list, 0, u, vIdx)
    return
  }
  const u = urls[0]
  if (!(await runGuard(u, 0))) {
    return
  }
  emit('preview', u, 0)
  window.open(u, '_blank', 'noopener,noreferrer')
}

async function onFileRowClick(row) {
  const index = row.__index
  const k = kindAtIndex(index)
  const url = parsedUrls.value[index]
  if (k === 'image') {
    if (!(await runGuard(url, index))) {
      return
    }
    emit('preview', url, index)
    progImageInitialIndex.value = imageInitialIndexFor(index)
    await nextTick()
    progImageRef.value?.showPreview?.()
    return
  }
  if (k === 'video') {
    await openVideoDialog([url], 0, url, index)
    return
  }
  if (!(await runGuard(url, index))) {
    return
  }
  emit('preview', url, index)
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function onPrevVideo() {
  if (currentVideoIndex.value <= 0) {
    return
  }
  resetVideoEl()
  currentVideoIndex.value -= 1
  await nextTick()
  try {
    await videoPlayerRef.value?.play?.()
  } catch {
    /* autoplay 可能被策略拦截，忽略 */
  }
}

async function onNextVideo() {
  if (currentVideoIndex.value >= videoUrls.value.length - 1) {
    return
  }
  resetVideoEl()
  currentVideoIndex.value += 1
  await nextTick()
  try {
    await videoPlayerRef.value?.play?.()
  } catch {
    /* 同上 */
  }
}
</script>

<style scoped lang="scss">
.c7-preview__grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-start;
}

.c7-preview__cell {
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.c7-preview__cell--image :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.c7-preview__cell--image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
}

.c7-preview__cell--video {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--el-fill-color-dark);
  min-height: 120px;
  min-width: 120px;
}

.c7-preview__cell--file {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px;
  cursor: pointer;
  background: var(--el-fill-color-light);
  min-height: 120px;
  min-width: 120px;
}

.c7-preview__fname {
  font-size: 12px;
  color: var(--el-text-color-regular);
  text-align: center;
  word-break: break-all;
  max-width: 200px;
}

.c7-preview__play {
  color: var(--el-color-primary);
}

.c7-preview__doc {
  color: var(--el-text-color-secondary);
}

.c7-preview__prog-image {
  position: fixed;
  left: -9999px;
  top: 0;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
  overflow: hidden;
}

.c7-preview__video-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.c7-preview__video-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.c7-preview__video-idx {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.c7-preview__video-el {
  width: 100%;
  max-height: 60vh;
  background: #000;
}

.c7-preview__table {
  width: 100%;
}

:deep(.c7-preview__table .el-table__row) {
  cursor: pointer;
}
</style>
