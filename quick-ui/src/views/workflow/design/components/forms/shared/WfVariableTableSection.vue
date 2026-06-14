<template>
  <div class="wf-vt-section">
    <div class="wf-vt-section__header">
      <div class="wf-vt-section__title-wrap">
        <span class="wf-vt-section__title">{{ title }}</span>
        <el-tooltip v-if="tooltip" :content="tooltip" placement="top">
          <el-icon class="wf-vt-section__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="wf-vt-section__header-actions">
        <slot name="header-extra" />
        <el-button
          v-if="showAdd"
          link
          class="wf-vt-section__add-btn"
          :title="addTitle"
          @click.stop="$emit('add')"
        >
          <el-icon :size="16"><Plus /></el-icon>
        </el-button>
      </div>
    </div>

    <div v-if="hasRows" class="wf-vt-section__table">
      <div class="wf-vt-section__thead">
        <span
          v-for="col in columns"
          :key="col.key"
          class="wf-vt-section__col"
          :class="col.class || `wf-vt-section__col--${col.key}`"
        >
          {{ col.label }}
        </span>
        <span v-if="showActions" class="wf-vt-section__col wf-vt-section__col--actions" />
      </div>
      <slot />
    </div>
    <div v-else class="wf-vt-section__empty">{{ emptyText }}</div>
  </div>
</template>

<script setup>
import { InfoFilled, Plus } from '@element-plus/icons-vue'

defineOptions({ name: 'WfVariableTableSection' })

defineProps({
  title: { type: String, required: true },
  tooltip: { type: String, default: '' },
  columns: {
    type: Array,
    default: () => []
  },
  hasRows: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无数据，点击右上角 + 添加' },
  showAdd: { type: Boolean, default: true },
  addTitle: { type: String, default: '添加' },
  showActions: { type: Boolean, default: true }
})

defineEmits(['add'])
</script>

<style scoped lang="scss">
.wf-vt-section {
  margin-bottom: 16px;
}

.wf-vt-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 8px;
}

.wf-vt-section__title-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.wf-vt-section__title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.wf-vt-section__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.wf-vt-section__header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.wf-vt-section__add-btn {
  color: #409eff;
  padding: 4px;
}

.wf-vt-section__table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.wf-vt-section__thead {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

:deep(.wf-vt-section__row) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }
}

:deep(.wf-vt-section__row--error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}

:deep(.wf-vt-section__col--name),
:deep(.wf-vt-section__col--key) {
  width: 96px;
  flex-shrink: 0;
}

:deep(.wf-vt-section__col--type) {
  width: 110px;
  flex-shrink: 0;
}

:deep(.wf-vt-section__col--operator) {
  width: 100px;
  flex-shrink: 0;
}

:deep(.wf-vt-section__col--required) {
  width: 44px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}

:deep(.wf-vt-section__col--value),
:deep(.wf-vt-section__col--flex) {
  flex: 1;
  min-width: 0;
}

:deep(.wf-vt-section__col--actions) {
  width: 28px;
  flex-shrink: 0;
  padding: 0;
}

.wf-vt-section__thead .wf-vt-section__col--name,
.wf-vt-section__thead .wf-vt-section__col--key {
  width: 96px;
  flex-shrink: 0;
}

.wf-vt-section__thead .wf-vt-section__col--type {
  width: 110px;
  flex-shrink: 0;
}

.wf-vt-section__thead .wf-vt-section__col--operator {
  width: 100px;
  flex-shrink: 0;
}

.wf-vt-section__thead .wf-vt-section__col--required {
  width: 44px;
  flex-shrink: 0;
}

.wf-vt-section__thead .wf-vt-section__col--flex {
  flex: 1;
}

.wf-vt-section__thead .wf-vt-section__col--actions {
  width: 28px;
  flex-shrink: 0;
}

.wf-vt-section__empty {
  padding: 20px 12px;
  text-align: center;
  font-size: 12px;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
}
</style>
