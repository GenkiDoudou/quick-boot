<template>
  <div class="file-preview">
    <!-- 根据封面类型选择展示形式 -->
    <template v-if="coverType === 'None'">
      <!-- 直接展示所有预览项 -->
      <template v-for="(url, index) in fileUrls" :key="index">
        <template v-if="displayType === 'image'">
          <el-image
              :src="url"
              :style="previewStyle"
              :preview-src-list="fileUrls"
              v-bind="$attrs"
          ></el-image>
        </template>
        <template v-else-if="displayType === 'video'">
          <el-image
              :src="defaultVideoImage"
              :style="previewStyle"
              @click="openVideoDialog(url)"
              v-bind="$attrs"
          ></el-image>
        </template>
        <template v-else-if="displayType === 'file'">
          <el-link
              type="primary"
              @click="openFile(url)"
              style="display: block; margin-bottom: 8px;"
          >
            {{ extractFileName(url) }}
          </el-link>
        </template>
      </template>
    </template>

    <template v-else-if="coverType === 'button'">
      <!-- 封面为按钮 -->
      <el-button type="primary" @click="coverDialogVisible = true">
        {{ buttonText }}
      </el-button>
    </template>

    <template v-else-if="coverType === 'file'">
      <!-- 封面为文件 -->
      <template v-if="displayType === 'image'">
        <el-image
            preview-teleported
            close-on-press-escape
            :src="fileUrls[0]"
            :style="previewStyle"
            @click="coverDialogVisible = true"
            v-bind="$attrs"
        ></el-image>
      </template>
      <template v-else-if="displayType === 'video'">
        <el-image
            preview-teleported
            close-on-press-escape
            :src="defaultVideoImage"
            :style="previewStyle"
            @click="coverDialogVisible = true"
            v-bind="$attrs"
        ></el-image>
      </template>
      <template v-else-if="displayType === 'file'">
        <el-image
            preview-teleported
            close-on-press-escape
            :src="defaultFileImage"
            :style="previewStyle"
            @click="coverDialogVisible = true"
            v-bind="$attrs"
        ></el-image>
      </template>
    </template>

    <!-- 预览对话框 -->
    <el-dialog
        v-model="coverDialogVisible"
        width="80%"
        title="文件预览"
        :before-close="handleCoverDialogClose"
    >
      <div class="dialog-content">
        <template v-for="(url, index) in fileUrls" :key="index">
          <template v-if="displayType === 'image'">
            <el-image
                preview-teleported
                close-on-press-escape
                :src="url"
                :style="previewStyle"
                :preview-src-list="fileUrls"
                v-bind="$attrs"
            ></el-image>
          </template>
          <template v-else-if="displayType === 'video'">
            <el-image
                :src="defaultVideoImage"
                preview-teleported
                close-on-press-escape
                :style="previewStyle"
                @click="openVideoDialog(url)"
                v-bind="$attrs"
            ></el-image>
          </template>
          <template v-else-if="displayType === 'file'">
            <el-link
                type="primary"
                @click="openFile(url)"
                style="display: block; margin-bottom: 8px;"
            >
              {{ extractFileName(url) }}
            </el-link>
          </template>
        </template>
      </div>
      <template #footer>
        <el-button @click="coverDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 视频播放对话框 -->
    <el-dialog
        append-to-body
        v-model="videoDialogVisible"
        width="80%"
        title="视频预览"
        :before-close="handleVideoDialogClose"
    >
      <video
          v-if="currentVideoUrl"
          :src="currentVideoUrl"
          controls
          autoplay
          style="width: 100%; height: 100%;"
      ></video>
      <template #footer>
        <el-button @click="videoDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineOptions } from 'vue'
import { logger } from '../../utils/logger'

defineOptions({
  name: 'C7Preview'
})

/**
 * 显示类型
 */
type DisplayType = 'image' | 'video' | 'file'

/**
 * 封面类型
 */
type CoverType = 'None' | 'button' | 'file'

/**
 * 组件属性接口
 */
interface Props {
  urls: string                      // 文件URL列表（逗号分隔，必填）
  width?: string | number           // 预览区域宽度
  height?: string | number          // 预览区域高度
  displayType?: DisplayType         // 文件显示类型
  coverType?: CoverType             // 封面类型
  buttonText?: string               // 按钮封面的文本
  defaultVideoImage?: string        // 视频默认缩略图
  defaultFileImage?: string         // 文件默认封面图
}

const props = withDefaults(defineProps<Props>(), {
  width: '200px',
  height: '200px',
  displayType: 'image',
  coverType: 'None',
  buttonText: '预览文件',
  defaultVideoImage: 'https://via.placeholder.com/200?text=Video',
  defaultFileImage: 'https://via.placeholder.com/200?text=File'
})

/**
 * 计算属性：将 urls 字符串按逗号分割为数组
 * 过滤掉空字符串
 */
const fileUrls = computed(() => {
  if (!props.urls) return []
  return props.urls
      .split(',')
      .map(url => url.trim())
      .filter(url => url)
})

/**
 * 计算属性：生成预览组件的样式对象
 */
const previewStyle = computed(() => ({
  width: typeof props.width === 'number' ? props.width + 'px' : props.width,
  height: typeof props.height === 'number' ? props.height + 'px' : props.height,
  cursor: 'pointer'
}))

// 对话框状态
const coverDialogVisible = ref(false)    // 封面对话框显示状态
const videoDialogVisible = ref(false)    // 视频对话框显示状态
const currentVideoUrl = ref('')          // 当前播放的视频URL

/**
 * 打开视频预览对话框
 * @param url 视频URL
 */
const openVideoDialog = (url: string) => {
  currentVideoUrl.value = url
  videoDialogVisible.value = true
}

/**
 * 视频对话框关闭前回调
 * @param done 关闭回调函数
 */
const handleVideoDialogClose = (done: () => void) => {
  currentVideoUrl.value = ''
  done()
}

/**
 * 封面对话框关闭前回调
 * @param done 关闭回调函数
 */
const handleCoverDialogClose = (done: () => void) => {
  done()
}

/**
 * 在新窗口打开文件
 * @param url 文件URL
 */
const openFile = (url: string) => {
  window.open(url, '_blank')
}

/**
 * 从 URL 中提取文件名
 * @param url 文件URL
 * @returns 文件名
 */
const extractFileName = (url: string): string => {
  try {
    const segments = url.split('/')
    return segments[segments.length - 1] || url
  } catch (error) {
    logger.error('提取文件名失败:', error)
    return url
  }
}
</script>

<style scoped>
.file-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.dialog-content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
</style>

