<template>
  <div class="icon-select">
    <el-input
      v-model="filterText"
      placeholder="搜索图标名称"
      clearable
      size="small"
      class="icon-select__search"
    />
    <div class="icon-select__grid">
      <div
        v-for="name in filteredIcons"
        :key="name"
        class="icon-select__item"
        :class="{ 'is-active': name === activeIcon }"
        :title="name"
        @click="pick(name)"
      >
        <svg-icon :icon-class="name" />
      </div>
    </div>
    <div v-if="!filteredIcons.length" class="icon-select__empty">无匹配图标，可将 SVG 放入 src/assets/icons/svg</div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/**
 * 菜单图标点选器：根据 {@code import.meta.glob} 解析到的 SVG 路径得到文件名（不含扩展名），与 {@link SvgIcon} 的 {@code icon-class} 一致；不使用 {@code eager}，避免把所有 SVG 打进本组件 JS。
 *
 * @emits selected 选中图标名称
 */
defineProps({
  /** 当前已选图标名，用于高亮 */
  activeIcon: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['selected'])

const filterText = ref('')

const iconList = Object.keys(import.meta.glob('@/assets/icons/svg/*.svg', { eager: false }))
  .map((path) => {
    const base = path.split('/').pop()
    return base ? base.replace(/\.svg$/i, '') : ''
  })
  .filter(Boolean)
  .sort((a, b) => a.localeCompare(b))

const filteredIcons = computed(() => {
  const q = filterText.value.trim().toLowerCase()
  if (!q) {
    return iconList
  }
  return iconList.filter((n) => n.toLowerCase().includes(q))
})

function pick(name) {
  emit('selected', name)
}

/** 清空搜索（表单失焦时由父级调用，对齐 RuoYi 行为） */
function reset() {
  filterText.value = ''
}

defineExpose({ reset })
</script>

<style scoped>
.icon-select {
  max-height: 380px;
  overflow: auto;
}

.icon-select__search {
  margin-bottom: 10px;
}

.icon-select__grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.icon-select__item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  transition: border-color 0.15s, background 0.15s;
}

.icon-select__item:hover {
  border-color: var(--el-color-primary);
}

.icon-select__item.is-active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.icon-select__item :deep(.svg-icon) {
  width: 22px;
  height: 22px;
}

.icon-select__empty {
  padding: 16px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
