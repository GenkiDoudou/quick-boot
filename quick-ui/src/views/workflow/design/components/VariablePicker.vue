<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-start"
    :width="340"
    trigger="click"
    popper-class="wf-var-picker-popper"
  >
    <template #reference>
      <el-button size="small" class="wf-var-picker__trigger" @click.stop>
        <span class="wf-var-picker__x">(x)</span>
        插入变量
      </el-button>
    </template>

    <div class="wf-var-picker">
      <div v-if="!groups.length" class="wf-var-picker__empty">暂无可引用变量</div>
      <div v-for="group in groups" :key="group.id" class="wf-var-picker__group">
        <div class="wf-var-picker__group-title">{{ group.label }}</div>
        <div
          v-for="item in group.children"
          :key="item.id"
          class="wf-var-picker__item"
          @click="onPick(item)"
        >
          <span class="wf-var-picker__x">(x)</span>
          <span class="wf-var-picker__path">{{ displayPath(item.insert) }}</span>
          <span class="wf-var-picker__tag">{{ formatType(item.type) }}</span>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { computed, ref } from 'vue'

defineOptions({ name: 'VariablePicker' })

const props = defineProps({
  variableTree: { type: Array, default: () => [] }
})

const emit = defineEmits(['insert'])

const visible = ref(false)

const groups = computed(() =>
  (props.variableTree || [])
    .map((group) => ({
      ...group,
      children: (group.children || []).filter((item) => item.insert)
    }))
    .filter((group) => group.children.length)
)

function displayPath(insert) {
  if (!insert) return ''
  return insert.replace(/^\{\{|\}\}$/g, '')
}

function formatType(type) {
  const map = {
    string: 'String',
    integer: 'Integer',
    number: 'Number',
    boolean: 'Boolean',
    time: 'Time',
    object: 'Object',
    array: 'Array',
    file: 'File'
  }
  const key = String(type || '').toLowerCase()
  return map[key] || 'String'
}

function onPick(item) {
  if (!item?.insert) return
  emit('insert', item.insert)
  visible.value = false
}
</script>

<style scoped lang="scss">
.wf-var-picker__trigger {
  color: #409eff;
  border: 1px solid #d9ecff;
  background: #ecf5ff;
  padding: 4px 10px;
  height: auto;

  &:hover {
    background: #d9ecff;
    border-color: #b3d8ff;
  }
}

.wf-var-picker__x {
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  font-family: Consolas, Monaco, monospace;
  margin-right: 4px;
}

.wf-var-picker {
  max-height: 320px;
  overflow-y: auto;
}

.wf-var-picker__empty {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

.wf-var-picker__group {
  & + & {
    margin-top: 12px;
  }
}

.wf-var-picker__group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 6px;
  padding: 0 4px;
}

.wf-var-picker__item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  margin-bottom: 4px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;

  &:hover {
    border-color: #c6e2ff;
    background: #fafcff;
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.wf-var-picker__path {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: Consolas, Monaco, monospace;
}

.wf-var-picker__tag {
  flex-shrink: 0;
  font-size: 11px;
  color: #909399;
}
</style>

<style lang="scss">
.wf-var-picker-popper {
  padding: 8px !important;
}
</style>
