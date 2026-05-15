<template>
  <div class="c7-switch-form">
    <!-- header: true 时显示 el-page-header -->
    <el-page-header
      v-if="currentConfig && currentConfig.header"
      class="c7-switch-form__page-header"
      @back="handleBack"
    >
      <template #title>
        <span>{{ currentConfig.title || '' }}</span>
      </template>
      <template #content>
        <slot name="header-content" :config="currentConfig" />
      </template>
    </el-page-header>

    <!-- 视图内容区 -->
    <transition :name="transitionName">
      <div :key="String(modelValue)" class="c7-switch-form__view">
        <!-- 有匹配视图时渲染对应 slot -->
        <template v-if="currentConfig">
          <slot
            :name="currentConfig.name"
            :config="currentConfig"
            :switch-to="switchTo"
            :go-back="goBack"
          />
        </template>

        <!-- 无匹配视图时渲染 empty slot -->
        <template v-else>
          <slot name="empty">
            <div class="c7-switch-form__empty">视图 "{{ modelValue }}" 未找到</div>
          </slot>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

defineOptions({ name: 'C7SwitchForm' })

const props = defineProps({
  /** 当前视图名（v-model） */
  modelValue: {
    type: [String, Number],
    required: true
  },
  /** 视图配置列表（推荐名称） */
  views: {
    type: Array,
    default: null
  },
  /** 视图配置列表（向后兼容别名） */
  showIndexs: {
    type: Array,
    default: null
  },
  /** 默认视图名（back 时无历史栈则跳转此视图） */
  defaultView: {
    type: [String, Number],
    default: undefined
  },
  /**
   * 切换过渡动画名
   * 传 false 则无动画，传字符串则使用对应动画名
   * 默认: 'c7-switch-fade'
   */
  transition: {
    type: [String, Boolean],
    default: 'c7-switch-fade'
  },
  /** 是否缓存已访问视图，默认: false */
  keepAlive: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'back',
  'change',
  'not-found'
])

// ── 合并 views / showIndexs，views 优先 ──
const configs = computed(() => props.views ?? props.showIndexs ?? [])

// ── 当前视图配置 ──
const currentConfig = computed(() =>
  configs.value.find((c) => c.name === props.modelValue) ?? null
)

// ── 过渡动画名 ──
const transitionName = computed(() => {
  if (props.transition === false || props.transition === '') return ''
  return typeof props.transition === 'string' ? props.transition : 'c7-switch-fade'
})

// ── 视图历史栈 ──
const viewHistory = ref([])

// ── 切换视图 ──
function switchTo(viewName) {
  if (props.modelValue === viewName) return
  // 校验视图是否存在
  const exists = configs.value.some((c) => c.name === viewName)
  if (!exists) {
    emit('not-found', viewName)
  }
  viewHistory.value.push(props.modelValue)
  emit('update:modelValue', viewName)
  const config = configs.value.find((c) => c.name === viewName) ?? null
  emit('change', viewName, config)
}

// ── 返回上一视图 ──
function goBack() {
  const prev = viewHistory.value.pop()
  if (prev !== undefined) {
    emit('update:modelValue', prev)
    const config = configs.value.find((c) => c.name === prev) ?? null
    emit('change', prev, config)
    emit('back', prev, config)
    return
  }
  // 无历史栈：使用 closeIndex 或 defaultView
  const closeTarget = currentConfig.value?.closeIndex ?? props.defaultView
  if (closeTarget !== undefined) {
    emit('update:modelValue', closeTarget)
    const config = configs.value.find((c) => c.name === closeTarget) ?? null
    emit('change', closeTarget, config)
    emit('back', closeTarget, config)
  }
}

// ── el-page-header 返回按钮 ──
function handleBack() {
  goBack()
}

// ── modelValue 变化时，若无匹配视图则触发 not-found ──
watch(
  () => props.modelValue,
  (val) => {
    if (configs.value.length > 0 && !configs.value.some((c) => c.name === val)) {
      emit('not-found', val)
    }
  },
  { immediate: false }
)

// ── 暴露 ──
defineExpose({
  switchTo,
  goBack,
  currentConfig,
  viewHistory
})
</script>

<style scoped>
.c7-switch-form {
  width: 100%;
}

.c7-switch-form__page-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.c7-switch-form__view {
  width: 100%;
}

.c7-switch-form__empty {
  padding: 32px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

/* ── 默认 fade 过渡 ── */
.c7-switch-fade-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.c7-switch-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.c7-switch-fade-enter-from {
  opacity: 0;
  transform: translateX(12px);
}
.c7-switch-fade-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}
</style>
