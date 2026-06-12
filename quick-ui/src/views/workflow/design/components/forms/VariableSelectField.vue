<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-start"
    :width="420"
    trigger="click"
    popper-class="wf-var-select-popper"
  >
    <template #reference>
      <button type="button" class="wf-var-select" :class="{ 'wf-var-select--empty': !modelValue }" @click.stop>
        <template v-if="resolved">
          <div class="wf-var-select__picked">
            <div class="wf-var-select__picked-main">
              <span class="wf-var-select__group">{{ resolved.group.label }}</span>
              <span class="wf-var-select__sep">/</span>
              <span class="wf-var-select__x">(x)</span>
              <span class="wf-var-select__name">{{ displayName(resolved.item) }}</span>
              <span class="wf-var-select__type">{{ formatVariableType(resolved.item.type) }}</span>
            </div>
            <div v-if="resolved.item.description" class="wf-var-select__picked-desc">
              {{ resolved.item.description }}
            </div>
          </div>
        </template>
        <span v-else-if="modelValue" class="wf-var-select__raw">{{ displayRaw }}</span>
        <span v-else class="wf-var-select__placeholder">选择变量</span>
      </button>
    </template>

    <div class="wf-var-select-panel">
      <el-input
        v-model="keyword"
        size="small"
        clearable
        placeholder="搜索变量名、类型或描述"
        class="wf-var-select-panel__search"
      />
      <div v-if="!filteredRows.length" class="wf-var-select-panel__empty">暂无可引用变量</div>
      <div v-for="group in filteredGroups" :key="group.id" class="wf-var-select-panel__group">
        <div class="wf-var-select-panel__group-title">{{ group.label }}</div>
        <div
          v-for="item in group.children"
          :key="item.id"
          class="wf-var-select-panel__item"
          :class="{ 'wf-var-select-panel__item--active': item.insert === modelValue }"
          @click="onPick(item)"
        >
          <div class="wf-var-select-panel__item-main">
            <span class="wf-var-select-panel__x">(x)</span>
            <span class="wf-var-select-panel__name">{{ displayName(item) }}</span>
            <span class="wf-var-select-panel__type">{{ formatVariableType(item.type) }}</span>
          </div>
          <div v-if="item.description" class="wf-var-select-panel__desc">{{ item.description }}</div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { findVariableInTree, formatVariableType } from '../../utils/variableTreeUtils'

defineOptions({ name: 'VariableSelectField' })

const props = defineProps({
  modelValue: { type: String, default: '' },
  variableTree: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const keyword = ref('')

watch(visible, (open) => {
  if (!open) keyword.value = ''
})

const resolved = computed(() => findVariableInTree(props.variableTree, props.modelValue))

const displayRaw = computed(() =>
  props.modelValue ? props.modelValue.replace(/^\{\{|\}\}$/g, '') : ''
)

function displayName(item) {
  return item?.name || item?.label || ''
}

const filteredGroups = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return (props.variableTree || [])
    .map((group) => {
      const children = (group.children || []).filter((item) => {
        if (!item.insert) return false
        if (!kw) return true
        const hay = [
          group.label,
          item.label,
          item.name,
          item.description,
          item.insert,
          formatVariableType(item.type)
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
        return hay.includes(kw)
      })
      return { ...group, children }
    })
    .filter((group) => group.children.length)
})

const filteredRows = computed(() =>
  filteredGroups.value.flatMap((group) => group.children || [])
)

function onPick(item) {
  if (!item?.insert) return
  emit('update:modelValue', item.insert)
  visible.value = false
}
</script>

<style scoped lang="scss">
.wf-var-select {
  display: block;
  width: 100%;
  min-height: 32px;
  padding: 4px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  font-size: 13px;
  color: #303133;
  transition: border-color 0.15s ease;

  &:hover {
    border-color: #c0c4cc;
  }

  &--empty {
    color: #a8abb2;
  }
}

.wf-var-select__picked-main {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.wf-var-select__group {
  color: #606266;
  max-width: 38%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 1;
}

.wf-var-select__sep {
  color: #c0c4cc;
  flex-shrink: 0;
}

.wf-var-select__x {
  color: #409eff;
  font-weight: 600;
  font-family: Consolas, Monaco, monospace;
  flex-shrink: 0;
}

.wf-var-select__name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: Consolas, Monaco, monospace;
  font-weight: 500;
}

.wf-var-select__type {
  flex-shrink: 0;
  font-size: 11px;
  color: #909399;
  background: #f2f4f7;
  padding: 1px 6px;
  border-radius: 4px;
}

.wf-var-select__picked-desc {
  margin-top: 4px;
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wf-var-select__raw,
.wf-var-select__placeholder {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: Consolas, Monaco, monospace;
}

.wf-var-select-panel {
  max-height: 360px;
  overflow-y: auto;
}

.wf-var-select-panel__search {
  margin-bottom: 8px;
}

.wf-var-select-panel__empty {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

.wf-var-select-panel__group + .wf-var-select-panel__group {
  margin-top: 12px;
}

.wf-var-select-panel__group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 6px;
  padding: 0 4px;
}

.wf-var-select-panel__item {
  padding: 8px 10px;
  margin-bottom: 4px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;

  &:hover,
  &--active {
    border-color: #c6e2ff;
    background: #fafcff;
  }
}

.wf-var-select-panel__item-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wf-var-select-panel__x {
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  font-family: Consolas, Monaco, monospace;
  flex-shrink: 0;
}

.wf-var-select-panel__name {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: Consolas, Monaco, monospace;
}

.wf-var-select-panel__type {
  flex-shrink: 0;
  font-size: 11px;
  color: #909399;
  background: #f2f4f7;
  padding: 1px 6px;
  border-radius: 4px;
}

.wf-var-select-panel__desc {
  margin-top: 4px;
  padding-left: 22px;
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>

<style lang="scss">
.wf-var-select-popper {
  padding: 8px !important;
}
</style>
