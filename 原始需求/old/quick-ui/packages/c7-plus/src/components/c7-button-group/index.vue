<template>
  <div class="c7-button-group" :class="groupClasses" :style="groupStyle" :key="renderKey">
    <!-- 显示可见的按钮 -->
    <template v-for="(button, index) in visibleButtons" :key="`visible-${index}-${renderKey}`">
      <component :is="button" :size="size" />
    </template>
    
    <!-- 更多按钮下拉菜单 -->
    <el-dropdown 
      v-if="hiddenButtons.length > 0" 
      :trigger="triggerType"
      @command="handleDropdownCommand"
      placement="bottom-end"
    >
      <el-button 
        type="info" 
        :size="size"
        plain
        class="c7-button-group__more"
      >
        更多
        <el-icon class="el-icon--right"><arrow-down /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <template v-for="(button, index) in hiddenButtons" :key="`hidden-${index}-${renderKey}`">
            <el-dropdown-item 
              :command="`button-${index}`"
              @click="triggerButtonAction(button)"
            >
              <component :is="button" :size="size" />
            </el-dropdown-item>
          </template>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, defineOptions, useSlots, ref } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { logger } from '../../utils/logger'

defineOptions({
  name: 'C7ButtonGroup'
})

/**
 * 布局模式类型
 */
type Mode = 'auto' | 'inline' | 'dropdown'

/**
 * 间距类型
 */
type Spacing = 'tight' | 'normal' | 'loose' | number

/**
 * 按钮大小类型
 */
type Size = 'large' | 'default' | 'small'

/**
 * 触发方式类型
 */
type Trigger = 'click' | 'hover'

/**
 * 断点配置接口
 */
interface Breakpoints {
  xs?: number
  sm?: number
  md?: number
  lg?: number
}

/**
 * 组件属性接口
 */
interface Props {
  mode?: Mode                           // 布局模式
  maxVisible?: number                   // 最大显示按钮数量（auto 模式下）
  spacing?: Spacing                     // 按钮间距
  size?: Size                           // 按钮大小
  responsive?: boolean                  // 是否响应式
  trigger?: Trigger                     // 触发方式
  breakpoints?: Breakpoints             // 断点配置
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'auto',
  maxVisible: 2,
  spacing: 'loose',
  size: 'small',
  responsive: true,
  trigger: 'click',
  breakpoints: () => ({
    xs: 1,
    sm: 2,
    md: 3,
    lg: 4
  })
})

const slots = useSlots()

/**
 * 用于强制重新渲染的响应式 key
 * 当 slot 内容变化时，这个 key 会变化，从而强制组件重新渲染
 */
const renderKey = ref(0)

/**
 * 获取所有按钮组件
 * 每次计算时都会访问 slot，Vue 会自动追踪变化
 */
const allButtons = computed(() => {
  if (!slots.default) return []
  
  // 访问 renderKey 以建立依赖关系
  // 当外部调用 forceUpdate 时，会更新 renderKey，从而触发重新计算
  const _ = renderKey.value
  
  return slots.default().filter(vnode => 
    vnode.type && 
    (vnode.type.__name === 'C7Button' || vnode.type.name === 'C7Button')
  )
})

/**
 * 强制更新方法，供外部调用
 */
const forceUpdate = () => {
  renderKey.value++
}

// 暴露 forceUpdate 方法
defineExpose({
  forceUpdate
})

/**
 * 可见按钮
 */
const visibleButtons = computed(() => {
  if (props.mode === 'inline' || allButtons.value.length <= props.maxVisible) {
    return allButtons.value
  }
  return allButtons.value.slice(0, props.maxVisible)
})

/**
 * 隐藏按钮
 */
const hiddenButtons = computed(() => {
  if (props.mode === 'inline' || allButtons.value.length <= props.maxVisible) {
    return []
  }
  return allButtons.value.slice(props.maxVisible)
})

/**
 * 触发类型
 */
const triggerType = computed(() => {
  return props.trigger
})

/**
 * 计算实际间距值
 */
const actualSpacing = computed(() => {
  if (typeof props.spacing === 'number') {
    return `${props.spacing}px`
  }
  
  // 预设间距值
  const spacingMap: Record<string, string> = {
    tight: '0px',
    normal: '1px', 
    loose: '2px'
  }
  
  return spacingMap[props.spacing] || '0px'
})

/**
 * 处理下拉菜单命令
 * @param command 命令
 */
const handleDropdownCommand = (command: string) => {
  logger.log('Dropdown command:', command)
}

/**
 * 触发按钮动作
 * @param buttonVnode 按钮虚拟节点
 */
const triggerButtonAction = (buttonVnode: any) => {
  // 手动触发按钮的点击事件
  if (buttonVnode && buttonVnode.props && buttonVnode.props.onClick) {
    buttonVnode.props.onClick()
  }
}

/**
 * 组件样式类
 */
const groupClasses = computed(() => {
  return [
    `c7-button-group--${props.mode}`,
    `c7-button-group--size-${props.size}`,
    {
      'c7-button-group--responsive': props.responsive
    }
  ]
})

/**
 * 动态样式
 */
const groupStyle = computed(() => {
  return {
    gap: actualSpacing.value
  }
})
</script>

<style scoped>
.c7-button-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  /* gap 通过 :style 动态设置 */
}

/* 大小配置 */
.c7-button-group--size-small {
  --button-size: small;
}

.c7-button-group--size-default {
  --button-size: default;
}

.c7-button-group--size-large {
  --button-size: large;
}

/* 更多按钮样式 */
.c7-button-group__more {
  position: relative;
}

.c7-button-group__more:hover {
  background-color: var(--el-color-info-light-9);
  border-color: var(--el-color-info-light-7);
}

/* 下拉菜单项样式 */
:deep(.el-dropdown-menu__item) {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: var(--el-color-primary-light-9);
}

/* 响应式配置 */
@media (max-width: 768px) {
  .c7-button-group--responsive {
    flex-direction: column;
    align-items: stretch;
  }
  
  .c7-button-group--responsive :deep(.el-button) {
    width: 100%;
  }
  
  .c7-button-group--responsive .c7-button-group__more {
    width: 100%;
  }
}

/* 内联模式 */
.c7-button-group--inline {
  display: flex;
  align-items: center;
}

/* 下拉模式 */
.c7-button-group--dropdown {
  position: relative;
}

/* 自动模式 */
.c7-button-group--auto {
  display: flex;
  align-items: center;
}

/* 按钮组内按钮样式统一 */
.c7-button-group :deep(.el-button) {
  margin: 0 !important;
}

/* 下拉菜单样式优化 */
:deep(.el-dropdown-menu) {
  min-width: 120px;
  max-width: 200px;
}

:deep(.el-dropdown-menu__item) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

