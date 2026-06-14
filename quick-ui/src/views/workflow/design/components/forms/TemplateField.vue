<template>
  <div class="wf-template-field">
    <div class="wf-template-field__toolbar">
      <VariablePicker :variable-tree="variableTree" @insert="insertAtCursor" />
      <el-button
        v-if="expandable"
        link
        class="wf-template-field__expand-btn"
        :title="expandTitle || '放大编辑'"
        @click="openDialog"
      >
        <el-icon :size="16"><FullScreen /></el-icon>
      </el-button>
    </div>
    <p v-if="hint" class="wf-template-field__hint">{{ hint }}</p>
    <el-input
      ref="inputRef"
      :model-value="modelValue"
      type="textarea"
      :rows="rows"
      :placeholder="placeholder"
      @update:model-value="$emit('update:modelValue', $event)"
      @focus="onFocus"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="expandTitle || '编辑模板'"
      width="min(960px, 92vw)"
      class="wf-template-field-dialog"
      append-to-body
      destroy-on-close
      @closed="onDialogClosed"
    >
      <div class="wf-template-field__dialog-toolbar">
        <VariablePicker :variable-tree="variableTree" @insert="insertInDialog" />
      </div>
      <p v-if="hint" class="wf-template-field__hint">{{ hint }}</p>
      <el-input
        ref="dialogInputRef"
        v-model="dialogDraft"
        type="textarea"
        :rows="dialogRows"
        :placeholder="placeholder"
        class="wf-template-field__dialog-input"
        @focus="onDialogFocus"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDialog">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { FullScreen } from '@element-plus/icons-vue'
import VariablePicker from '../VariablePicker.vue'

defineOptions({ name: 'TemplateField' })

const props = defineProps({
  modelValue: { type: String, default: '' },
  variableTree: { type: Array, default: () => [] },
  rows: { type: Number, default: 3 },
  placeholder: { type: String, default: '' },
  /** 模板用法说明，显示在变量选择器下方 */
  hint: { type: String, default: '' },
  /** 是否显示放大编辑（Coze 式弹窗） */
  expandable: { type: Boolean, default: false },
  /** 放大编辑弹窗标题 */
  expandTitle: { type: String, default: '' },
  /** 弹窗内文本域行数（实际高度由 CSS min-height 控制） */
  dialogRows: { type: Number, default: 18 }
})

const emit = defineEmits(['update:modelValue'])

const inputRef = ref(null)
const dialogInputRef = ref(null)
const cursorPos = ref(0)
const dialogCursorPos = ref(0)
const dialogVisible = ref(false)
const dialogDraft = ref('')

function onFocus(e) {
  cursorPos.value = e.target.selectionStart ?? 0
}

function onDialogFocus(e) {
  dialogCursorPos.value = e.target.selectionStart ?? 0
}

function openDialog() {
  dialogDraft.value = props.modelValue ?? ''
  dialogVisible.value = true
  nextTick(() => {
    dialogInputRef.value?.textarea?.focus()
  })
}

function confirmDialog() {
  emit('update:modelValue', dialogDraft.value)
  dialogVisible.value = false
}

function onDialogClosed() {
  dialogDraft.value = ''
}

function insertAtCursor(text) {
  const el = inputRef.value?.textarea || inputRef.value?.input
  const current = el?.value ?? props.modelValue ?? ''
  const pos = el?.selectionStart ?? cursorPos.value
  const next = current.slice(0, pos) + text + current.slice(pos)
  emit('update:modelValue', next)
  cursorPos.value = pos + text.length
}

function insertInDialog(text) {
  const el = dialogInputRef.value?.textarea || dialogInputRef.value?.input
  const current = el?.value ?? dialogDraft.value ?? ''
  const pos = el?.selectionStart ?? dialogCursorPos.value
  dialogDraft.value = current.slice(0, pos) + text + current.slice(pos)
  dialogCursorPos.value = pos + text.length
  nextTick(() => {
    const textarea = dialogInputRef.value?.textarea
    if (textarea) {
      textarea.focus()
      textarea.setSelectionRange(dialogCursorPos.value, dialogCursorPos.value)
    }
  })
}
</script>

<style scoped>
.wf-template-field__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.wf-template-field__expand-btn {
  flex-shrink: 0;
  color: #909399;
  padding: 4px;
}

.wf-template-field__expand-btn:hover {
  color: #409eff;
}

.wf-template-field__hint {
  margin: 0 0 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.wf-template-field__dialog-toolbar {
  margin-bottom: 8px;
}

.wf-template-field__dialog-input :deep(.el-textarea__inner) {
  min-height: 52vh;
  font-family: inherit;
  line-height: 1.6;
}
</style>

<style>
.wf-template-field-dialog .el-dialog__body {
  padding-top: 8px;
}
</style>
