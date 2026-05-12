<template>
  <div class="c7-switch-form">
    <template v-if="currentConfig">
      <!-- 带头部的视图 -->
      <el-page-header 
        v-if="currentConfig.header"
        @back="handleBack(currentConfig.closeIndex)"
      >
        <template #content>
          <span class="switch-form-title">{{ currentConfig.title }}</span>
        </template>
        <div class="switch-form-content">
          <slot :name="currentConfig.name" :config="currentConfig" />
        </div>
      </el-page-header>

      <!-- 无头部的视图 -->
      <div v-else class="switch-form-content">
        <slot :name="currentConfig.name" :config="currentConfig" />
      </div>
    </template>

    <!-- 默认内容 -->
    <div v-else class="switch-form-empty">
      <slot name="empty">
        <p>没有找到对应的视图配置</p>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineOptions } from 'vue'

defineOptions({
  name: 'C7SwitchForm'
})

/**
 * 视图配置接口
 */
interface SwitchConfig {
  name: string                      // 视图名称（必填）
  title?: string                    // 视图标题
  header?: boolean                  // 是否显示头部
  closeIndex?: string | number      // 关闭时跳转的视图
  [key: string]: any                // 其他自定义属性
}

/**
 * 组件属性接口
 */
interface Props {
  showIndexs: SwitchConfig[]        // 视图配置列表
  modelValue: string | number       // 当前显示的视图名称
  defaultView?: string              // 默认视图（返回时使用）
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'back': [closeIndex: string | number, config: SwitchConfig]
  'change': [currentValue: string | number, config: SwitchConfig]
}>()

/**
 * 计算属性：当前视图配置
 * 根据 modelValue 查找对应的视图配置
 */
const currentConfig = computed((): SwitchConfig | null => {
  const config = props.showIndexs.find(item => item.name === props.modelValue)
  return config || null
})

/**
 * 处理返回操作
 * @param closeIndex 要跳转的视图索引
 */
const handleBack = (closeIndex: string | number = '') => {
  // 确定目标视图：优先使用 closeIndex，其次使用 defaultView
  const targetIndex = closeIndex || props.defaultView || ''
  
  // 更新当前视图
  emit('update:modelValue', targetIndex)
  
  // 触发 back 事件
  if (currentConfig.value) {
    emit('back', closeIndex, currentConfig.value)
  }
}

/**
 * 切换到指定视图
 * @param viewName 视图名称
 */
const switchTo = (viewName: string | number) => {
  // 避免重复切换
  if (props.modelValue !== viewName) {
    emit('update:modelValue', viewName)
    
    // 查找目标视图配置
    const config = props.showIndexs.find(item => item.name === viewName)
    if (config) {
      emit('change', viewName, config)
    }
  }
}

/**
 * 暴露方法供父组件调用
 */
defineExpose({
  switchTo,
  currentConfig: currentConfig.value
})
</script>

<style scoped>
.c7-switch-form {
  width: 100%;
  height: 100%;
}

.switch-form-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.switch-form-content {
  margin-top: 16px;
}

.switch-form-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--el-text-color-placeholder);
  font-size: 14px;
}

@media (max-width: 768px) {
  .switch-form-title {
    font-size: 16px;
  }
  
  .switch-form-content {
    margin-top: 12px;
  }
}
</style>

