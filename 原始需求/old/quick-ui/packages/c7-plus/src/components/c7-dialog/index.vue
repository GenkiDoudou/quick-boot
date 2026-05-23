<template>
  <component
      :is="currentComponent"
      :model-value="visible"
      v-bind="mergedProps"
      @update:model-value="handleVisibilityChange"
      @close="handleClose"
  >
    <slot />

    <!-- 仅当为 Dialog 时才支持 footer 插槽 -->
    <template v-if="$slots.footer && mode === 'dialog' && footer" #footer>
      <slot name="footer" />
    </template>

    <template v-if="!$slots.footer && mode === 'dialog' && footer" #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submit">确 定</el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>

    <!-- 仅当为 Drawer 时才支持 footer 插槽 -->
    <template v-if="$slots.footer && mode !== 'dialog'" #footer>
      <slot name="footer" />
    </template>
  </component>
</template>

<script setup lang="ts">
import { computed, defineOptions, watch, onUnmounted } from 'vue'

defineOptions({
  name: 'C7Dialog'
})

/**
 * 弹窗模式类型
 */
type Mode = 'dialog' | 'drawer'

/**
 * 组件属性接口
 */
interface Props {
  footer?: boolean                    // 是否显示底部操作栏
  mode?: Mode                         // 弹窗模式：dialog 或 drawer
  visible?: boolean                   // 是否显示（可选，默认 false）
  modalProps?: Record<string, any>    // 传递给 el-dialog 或 el-drawer 的属性
}

const props = withDefaults(defineProps<Props>(), {
  footer: true,
  mode: 'dialog',
  visible: false,
  modalProps: () => ({})
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'close': []
  'submit': []
}>()

/**
 * 计算属性：动态选择 Dialog 或 Drawer 组件
 */
const currentComponent = computed(() => {
  return props.mode === 'drawer' ? 'el-drawer' : 'el-dialog'
})

/**
 * 计算属性：合并用户传入的配置与默认配置
 */
const mergedProps = computed(() => ({
  ...defaultProps.value,
  ...props.modalProps
}))

/**
 * 计算属性：根据模式返回默认配置
 * - dialog 模式：默认宽度 50%
 * - drawer 模式：默认方向 rtl（从右侧弹出）
 */
const defaultProps = computed(() => {
  return props.mode === 'drawer'
      ? { direction: 'rtl' }
      : { width: '50%' }
})

/**
 * 处理显示状态变化
 * @param newValue 新的显示状态
 */
const handleVisibilityChange = (newValue: boolean) => {
  emit('update:visible', newValue)
}

/**
 * 处理关闭事件
 */
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

/**
 * 处理提交事件
 */
const submit = () => {
  emit('submit')
}

/**
 * 组件卸载时确保弹窗关闭
 * 防止路由切换时弹窗仍然存在导致白屏
 * 注意：使用 try-catch 防止父组件已卸载时 emit 报错
 */
onUnmounted(() => {
  try {
    if (props.visible) {
      // 强制关闭弹窗
      emit('update:visible', false)
      emit('close')
    }
  } catch (error) {
    // 忽略错误，因为父组件可能已经卸载
    // console.warn('C7Dialog: 组件卸载时关闭弹窗失败', error)
  }
})
</script>

