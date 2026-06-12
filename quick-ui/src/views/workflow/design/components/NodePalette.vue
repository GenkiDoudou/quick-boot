<template>
  <aside class="wf-palette">
    <el-input
      v-model="keyword"
      placeholder="搜索节点"
      clearable
      :prefix-icon="Search"
      class="wf-palette__search"
    />
    <div v-for="cat in visibleCategories" :key="cat.key" class="wf-palette__group">
      <div class="wf-palette__group-title">{{ cat.label }}</div>
      <div
        v-for="item in cat.items"
        :key="item.type"
        class="wf-palette__item"
        draggable="true"
        :style="{ '--item-color': item.color }"
        @dragstart="onDragStart($event, item.type)"
      >
        <span class="wf-palette__item-dot" />
        <div class="wf-palette__item-text">
          <div class="wf-palette__item-name">{{ item.label }}</div>
          <div class="wf-palette__item-desc">{{ item.description }}</div>
        </div>
      </div>
    </div>
    <el-empty v-if="!hasResults" description="无匹配节点" :image-size="48" />
  </aside>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { CATEGORY_LABELS, WORKFLOW_NODE_TYPES } from '../nodeMeta'

defineOptions({ name: 'NodePalette' })

const emit = defineEmits(['drag-start'])

const keyword = ref('')

const visibleCategories = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const cats = ['basic', 'logic', 'ai', 'tool']
  return cats
    .map((key) => {
      const items = WORKFLOW_NODE_TYPES.filter((item) => {
        if (item.category !== key) return false
        if (!kw) return true
        return (
          item.label.toLowerCase().includes(kw) ||
          item.type.toLowerCase().includes(kw) ||
          (item.description || '').toLowerCase().includes(kw)
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
</script>

<style scoped lang="scss">
.wf-palette {
  padding: 12px;
  overflow-y: auto;
  background: #fff;
  border-right: 1px solid #ebeef5;
}

.wf-palette__search {
  margin-bottom: 12px;
}

.wf-palette__group {
  margin-bottom: 16px;
}

.wf-palette__group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.wf-palette__item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 10px;
  margin-bottom: 6px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: grab;
  transition: transform 0.15s ease, background 0.15s ease;

  &:hover {
    background: #ecf5ff;
    transform: translateY(-1px);
  }

  &:active {
    cursor: grabbing;
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

.wf-palette__item-name {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
}

.wf-palette__item-desc {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  line-height: 1.3;
}
</style>
