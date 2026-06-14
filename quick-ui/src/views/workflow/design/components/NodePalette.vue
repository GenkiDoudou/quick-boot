<template>
  <aside class="wf-palette" :class="{ 'wf-palette--popup': variant === 'popup' }">
    <div class="wf-palette__sticky">
      <div v-if="variant !== 'popup'" class="wf-palette__title">节点库</div>
      <el-input
        v-model="keyword"
        placeholder="搜索节点、描述或示例"
        clearable
        :prefix-icon="Search"
        class="wf-palette__search"
      />
    </div>

    <div class="wf-palette__scroll">
      <div v-for="cat in visibleCategories" :key="cat.key" class="wf-palette__group">
        <div class="wf-palette__group-title">{{ cat.label }}</div>
        <div
          v-for="item in cat.items"
          :key="item.type"
          class="wf-palette__item"
          draggable="true"
          :style="{ '--item-color': item.color }"
          @dragstart="onDragStart($event, item.type)"
          @click="onItemClick(item.type)"
        >
          <span class="wf-palette__item-dot" />
          <div class="wf-palette__item-body">
            <div class="wf-palette__item-name">{{ item.label }}</div>
            <div class="wf-palette__item-desc">{{ item.description }}</div>
            <div v-if="item.example" class="wf-palette__item-example">
              <span class="wf-palette__item-example-label">示例</span>
              <span class="wf-palette__item-example-text">{{ item.example }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!hasResults" description="无匹配节点" :image-size="48" />
    </div>
  </aside>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { CATEGORY_LABELS, WORKFLOW_NODE_TYPES } from '../nodeMeta'
import { isFixedWorkflowNodeType } from '../utils/workflowNodePolicy'
import { isLoopBodyOnlyNodeType } from '../utils/loopUtils'
import { isForbiddenInBatchBody } from '../utils/batchUtils'

defineOptions({ name: 'NodePalette' })

const props = defineProps({
  /** sidebar：左侧栏；popup：底部添加节点弹层内 */
  variant: { type: String, default: 'sidebar' },
  /** 当前容器上下文：null | loop | batch */
  containerKind: { type: String, default: null }
})

const emit = defineEmits(['drag-start', 'pick'])

const keyword = ref('')

const visibleCategories = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const cats = ['basic', 'logic', 'ai', 'knowledge', 'tool']
  return cats
    .map((key) => {
      const items = WORKFLOW_NODE_TYPES.filter((item) => {
        if (item.category !== key) return false
        if (isFixedWorkflowNodeType(item.type)) return false
        if (item.type === 'loop-body' || item.type === 'batch-body') return false
        if (item.type === 'loop-body-start' || item.type === 'loop-body-end') return false
        if (isLoopBodyOnlyNodeType(item.type)) return props.containerKind === 'loop'
        if (props.containerKind === 'loop' && (item.type === 'loop' || item.type === 'batch')) return false
        if (props.containerKind === 'batch') {
          if (item.type === 'loop' || item.type === 'batch') return false
          if (isForbiddenInBatchBody(item.type)) return false
        }
        if (!kw) return true
        return (
          item.label.toLowerCase().includes(kw) ||
          item.type.toLowerCase().includes(kw) ||
          (item.description || '').toLowerCase().includes(kw) ||
          (item.example || '').toLowerCase().includes(kw)
        )
      })
      return { key, label: CATEGORY_LABELS[key], items }
    })
    .filter((g) => g.items.length)
})

const hasResults = computed(() => visibleCategories.value.length > 0)

function onDragStart(event, type) {
  event.dataTransfer.setData('application/vueflow', type)
  event.dataTransfer.effectAllowed = 'move'
  emit('drag-start', type)
}

/** 点击节点项：popup 模式下直接添加到画布 */
function onItemClick(type) {
  if (props.variant === 'popup') {
    emit('pick', type)
  }
}
</script>

<style scoped lang="scss">
.wf-palette {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  background: #fff;
  border-right: 1px solid #ebeef5;

  &--popup {
    height: auto;
    max-height: min(480px, 70vh);
    border-right: none;

    .wf-palette__sticky {
      padding: 12px 12px 8px;
    }

    .wf-palette__scroll {
      max-height: calc(min(480px, 70vh) - 52px);
      padding: 8px 12px 12px;
    }
  }
}

.wf-palette__sticky {
  flex-shrink: 0;
  padding: 12px 12px 10px;
  border-bottom: 1px solid #f0f2f5;
  background: #fff;
}

.wf-palette__title {
  font-size: 14px;
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 10px;
}

.wf-palette__search {
  width: 100%;
}

.wf-palette__scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 12px 16px;
}

.wf-palette__group {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.wf-palette__group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.wf-palette__item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px;
  margin-bottom: 8px;
  border-radius: 8px;
  background: #f5f7fa;
  border: 1px solid transparent;
  cursor: grab;
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;

  &:hover {
    background: #ecf5ff;
    border-color: #d9ecff;
    transform: translateY(-1px);
  }

  &:active {
    cursor: grabbing;
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.wf-palette__item-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--item-color, #409eff);
  margin-top: 5px;
  flex-shrink: 0;
}

.wf-palette__item-body {
  min-width: 0;
  flex: 1;
}

.wf-palette__item-name {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
  line-height: 1.4;
}

.wf-palette__item-desc {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  line-height: 1.45;
}

.wf-palette__item-example {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 6px;
  padding: 6px 8px;
  border-radius: 6px;
  background: #fff;
  border: 1px solid #ebeef5;
}

.wf-palette__item-example-label {
  font-size: 10px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.wf-palette__item-example-text {
  font-size: 11px;
  color: #606266;
  line-height: 1.45;
  word-break: break-word;
  font-family: Consolas, Monaco, 'Courier New', monospace;
}
</style>
