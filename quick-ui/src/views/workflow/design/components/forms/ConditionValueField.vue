<template>
  <div class="wf-cond-value-field">
    <el-input
      ref="inputRef"
      :model-value="modelValue"
      size="small"
      :placeholder="placeholder"
      clearable
      @update:model-value="onInput"
      @focus="onFocus"
    />
    <el-popover
      v-model:visible="visible"
      placement="bottom-end"
      :width="340"
      trigger="click"
      popper-class="wf-cond-value-field-popper"
    >
      <template #reference>
        <el-button
          size="small"
          class="wf-cond-value-field__pick"
          title="插入变量"
          @click.stop
        >
          <span class="wf-cond-value-field__x">(x)</span>
        </el-button>
      </template>
      <div class="wf-cond-value-field-panel">
        <div v-if="!groups.length" class="wf-cond-value-field-panel__empty">暂无可引用变量</div>
        <div v-for="group in groups" :key="group.id" class="wf-cond-value-field-panel__group">
          <div class="wf-cond-value-field-panel__group-title">{{ group.label }}</div>
          <div
            v-for="item in group.children"
            :key="item.id"
            class="wf-cond-value-field-panel__item"
            @click="onPick(item)"
          >
            <span class="wf-cond-value-field-panel__x">(x)</span>
            <span class="wf-cond-value-field-panel__path">{{ displayPath(item.insert) }}</span>
            <span class="wf-cond-value-field-panel__tag">{{ formatType(item.type) }}</span>
          </div>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

/**
 * 变量值输入框：支持直接输入文本，或通过 (x) 插入 {{变量}} 引用。
 * 用于输出变量值、输入参数、变量赋值、条件对比等场景。
 */
defineOptions({ name: 'ConditionValueField' })

const props = defineProps({
  modelValue: { type: String, default: '' },
  variableTree: { type: Array, default: () => [] },
  placeholder: { type: String, default: '输入或引用变量' }
})

const emit = defineEmits(['update:modelValue'])

const inputRef = ref(null)
const visible = ref(false)
const cursorPos = ref(0)

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

function onInput(val) {
  emit('update:modelValue', val)
}

function onFocus(e) {
  cursorPos.value = e.target.selectionStart ?? 0
}

function insertAtCursor(text) {
  const el = inputRef.value?.input || inputRef.value?.textarea
  const current = props.modelValue ?? ''
  const pos = el?.selectionStart ?? cursorPos.value
  const next = current.slice(0, pos) + text + current.slice(pos)
  emit('update:modelValue', next)
  cursorPos.value = pos + text.length
}

function onPick(item) {
  if (!item?.insert) return
  insertAtCursor(item.insert)
  visible.value = false
}
</script>

<style scoped lang="scss">
.wf-cond-value-field {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.wf-cond-value-field__pick {
  flex-shrink: 0;
  padding: 5px 8px;
  height: 24px;
  color: #409eff;
  border: 1px solid #d9ecff;
  background: #ecf5ff;

  &:hover {
    background: #d9ecff;
    border-color: #b3d8ff;
  }
}

.wf-cond-value-field__x {
  font-size: 12px;
  font-weight: 600;
  font-family: Consolas, Monaco, monospace;
}

.wf-cond-value-field-panel {
  max-height: 320px;
  overflow-y: auto;
}

.wf-cond-value-field-panel__empty {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

.wf-cond-value-field-panel__group + .wf-cond-value-field-panel__group {
  margin-top: 12px;
}

.wf-cond-value-field-panel__group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 6px;
  padding: 0 4px;
}

.wf-cond-value-field-panel__item {
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

.wf-cond-value-field-panel__x {
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  font-family: Consolas, Monaco, monospace;
  flex-shrink: 0;
}

.wf-cond-value-field-panel__path {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: Consolas, Monaco, monospace;
}

.wf-cond-value-field-panel__tag {
  flex-shrink: 0;
  font-size: 11px;
  color: #909399;
}
</style>

<style lang="scss">
.wf-cond-value-field-popper {
  padding: 8px !important;
}
</style>
