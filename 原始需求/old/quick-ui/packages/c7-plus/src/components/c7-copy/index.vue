<template>
  <div class="c7-copy" :class="{ 'c7-copy--inline': inline }">
    <!-- 文本内容 -->
    <span v-if="showText" class="c7-copy__text">{{ computedDisplayText }}</span>
    
    <!-- 复制按钮 -->
    <el-button
      v-if="showButton"
      :type="buttonType"
      :size="buttonSize"
      :icon="copyIcon"
      :plain="buttonPlain"
      :link="buttonLink"
      :circle="buttonCircle"
      :disabled="disabled"
      @click="handleCopy"
      class="c7-copy__button"
    >
      <slot name="button">{{ buttonText || '复制' }}</slot>
    </el-button>
    
    <!-- 图标按钮 -->
    <el-icon
      v-else-if="showIcon"
      :size="iconSize"
      :color="iconColor"
      class="c7-copy__icon"
      @click="handleCopy"
    >
      <component :is="copyIconComponent" />
    </el-icon>
    
    <!-- 纯文本模式（点击复制） -->
    <span
      v-else
      class="c7-copy__clickable"
      :class="{ 'c7-copy__clickable--disabled': disabled }"
      @click="handleCopy"
    >
      <slot>{{ computedDisplayText }}</slot>
    </span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineOptions } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { logger } from '../../utils/logger'

defineOptions({
  name: 'C7Copy'
})

/**
 * 组件属性接口
 */
interface Props {
  // 要复制的文本
  text: string | number
  // 显示模式：button（按钮）、icon（图标）、text（文本）、none（仅复制功能）
  mode?: 'button' | 'icon' | 'text' | 'none'
  // 是否内联显示
  inline?: boolean
  // 是否禁用
  disabled?: boolean
  // 按钮类型（mode 为 button 时生效）
  buttonType?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default'
  // 按钮大小（mode 为 button 时生效）
  buttonSize?: 'large' | 'default' | 'small'
  // 按钮文本（mode 为 button 时生效）
  buttonText?: string
  // 按钮是否为文本按钮（mode 为 button 时生效）
  buttonPlain?: boolean
  // 按钮是否为链接按钮（mode 为 button 时生效）
  buttonLink?: boolean
  // 按钮是否为圆形（mode 为 button 时生效）
  buttonCircle?: boolean
  // 图标大小（mode 为 icon 时生效）
  iconSize?: number | string
  // 图标颜色（mode 为 icon 时生效）
  iconColor?: string
  // 是否显示文本（mode 为 text 或 none 时生效）
  showText?: boolean
  // 显示文本（如果为空则显示原始文本）
  displayText?: string
  // 复制成功提示文本
  successMessage?: string
  // 复制失败提示文本
  errorMessage?: string
  // 是否显示提示
  showMessage?: boolean
  // 复制前的回调（返回 false 可阻止复制）
  beforeCopy?: () => boolean | Promise<boolean>
  // 复制后的回调
  afterCopy?: (text: string) => void
}

const props = withDefaults(defineProps<Props>(), {
  text: '',
  mode: 'icon',
  inline: false,
  disabled: false,
  buttonType: 'default',
  buttonSize: 'default',
  buttonText: '',
  buttonPlain: false,
  buttonLink: false,
  buttonCircle: false,
  iconSize: 16,
  iconColor: '#409EFF',
  showText: true,
  displayText: '',
  successMessage: '复制成功',
  errorMessage: '复制失败',
  showMessage: true,
  beforeCopy: undefined,
  afterCopy: undefined
})

const emit = defineEmits<{
  'copy': [text: string]
  'success': [text: string]
  'error': [error: Error]
}>()

/**
 * 计算属性：显示文本
 */
const computedDisplayText = computed(() => {
  if (props.displayText) {
    return props.displayText
  }
  return String(props.text)
})

/**
 * 计算属性：是否显示按钮
 */
const showButton = computed(() => props.mode === 'button')

/**
 * 计算属性：是否显示图标
 */
const showIcon = computed(() => props.mode === 'icon')

/**
 * 计算属性：复制图标
 */
const copyIcon = computed(() => {
  if (props.mode === 'button') {
    return 'DocumentCopy'
  }
  return undefined
})

/**
 * 计算属性：图标组件
 */
const copyIconComponent = computed(() => DocumentCopy)

/**
 * 复制到剪贴板
 */
const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    // 优先使用 Clipboard API
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
      return true
    }
    
    // 降级方案：使用 execCommand
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    
    try {
      const successful = document.execCommand('copy')
      document.body.removeChild(textArea)
      return successful
    } catch (error) {
      document.body.removeChild(textArea)
      throw error
    }
  } catch (error) {
    logger.error('复制失败:', error)
    throw error
  }
}

/**
 * 处理复制
 */
const handleCopy = async () => {
  if (props.disabled) {
    return
  }
  
  try {
    // 复制前回调
    if (props.beforeCopy) {
      const canCopy = await props.beforeCopy()
      if (canCopy === false) {
        return
      }
    }
    
    const text = String(props.text)
    const success = await copyToClipboard(text)
    
    if (success) {
      emit('copy', text)
      emit('success', text)
      
      if (props.showMessage) {
        ElMessage.success(props.successMessage)
      }
      
      // 复制后回调
      if (props.afterCopy) {
        props.afterCopy(text)
      }
    } else {
      throw new Error('复制操作失败')
    }
  } catch (error: any) {
    logger.error('复制失败:', error)
    emit('error', error)
    
    if (props.showMessage) {
      ElMessage.error(props.errorMessage)
    }
  }
}
</script>

<style scoped lang="scss">
.c7-copy {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  
  &--inline {
    display: inline-flex;
  }
  
  &__text {
    user-select: all;
  }
  
  &__button {
    flex-shrink: 0;
  }
  
  &__icon {
    cursor: pointer;
    transition: color 0.3s;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
  
  &__clickable {
    cursor: pointer;
    user-select: all;
    transition: color 0.3s;
    
    &:hover {
      color: var(--el-color-primary);
    }
    
    &--disabled {
      cursor: not-allowed;
      opacity: 0.6;
      
      &:hover {
        color: inherit;
      }
    }
  }
}
</style>

