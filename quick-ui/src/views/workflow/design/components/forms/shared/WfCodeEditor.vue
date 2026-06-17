<template>
  <div class="wf-code-editor" :class="{ 'wf-code-editor--readonly': readonly }">
    <div class="wf-code-editor__toolbar">
      <span class="wf-code-editor__lang">{{ languageLabel }}</span>
      <div class="wf-code-editor__actions">
        <el-button
          link
          type="primary"
          size="small"
          :disabled="readonly"
          @click="formatCode('inline')"
        >
          <el-icon><MagicStick /></el-icon>
          格式化
        </el-button>
        <el-button
          link
          type="primary"
          size="small"
          @click="openFullscreen"
        >
          <el-icon><FullScreen /></el-icon>
          全屏
        </el-button>
      </div>
    </div>

    <!-- 内联：直接 create，避免 VueMonacoEditor 在侧栏异步挂载时内容空白 -->
    <div
      v-show="!fullscreenVisible"
      ref="inlineContainerRef"
      class="wf-code-editor__surface"
      :style="surfaceStyle"
    />

    <el-dialog
      v-model="fullscreenVisible"
      fullscreen
      destroy-on-close
      append-to-body
      class="wf-code-editor-dialog"
      @opened="onFullscreenOpened"
      @closed="onFullscreenClosed"
    >
      <template #header>
        <div class="wf-code-editor-dialog__header">
          <div class="wf-code-editor-dialog__title">
            <span>代码编辑</span>
            <el-tag size="small" effect="plain">{{ languageLabel }}</el-tag>
          </div>
          <div class="wf-code-editor-dialog__actions">
            <el-button size="small" :disabled="readonly" @click="formatCode('fullscreen')">
              <el-icon><MagicStick /></el-icon>
              格式化
            </el-button>
            <el-button type="primary" size="small" @click="fullscreenVisible = false">完成</el-button>
          </div>
        </div>
      </template>
      <div class="wf-code-editor-dialog__body">
        <VueMonacoEditor
          ref="fullscreenEditorRef"
          v-model:value="codeValue"
          :path="fullscreenPath"
          :default-value="modelValue || ''"
          :language="monacoLanguage"
          theme="vs"
          width="100%"
          height="100%"
          :save-view-state="false"
          :options="fullscreenEditorOptions"
          @mount="(editor, monacoInstance) => onFullscreenMount(editor, monacoInstance)"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { FullScreen, MagicStick } from '@element-plus/icons-vue'
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import { monaco, setupMonacoEditor } from '@/utils/wfMonacoSetup.js'
import { basicFormatPython, ensurePythonFormatProvider } from './wfMonacoFormat.js'

defineOptions({ name: 'WfCodeEditor' })

setupMonacoEditor()
ensurePythonFormatProvider(monaco)

const props = defineProps({
  /** 编辑器内容 */
  modelValue: { type: String, default: '' },
  /** 语法模式：javascript / python */
  language: { type: String, default: 'javascript' },
  /** 编辑器高度（px） */
  minHeight: { type: Number, default: 280 },
  /** 是否只读 */
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change', 'focus', 'blur'])

const instanceId = `wf_code_${Math.random().toString(36).slice(2, 10)}`
const fullscreenPath = `${instanceId}_fullscreen`

const inlineContainerRef = ref(null)
const fullscreenVisible = ref(false)
const fullscreenEditorRef = ref(null)
const fullscreenEditor = shallowRef(null)
let inlineEditor = null
let layoutTimer = null
let syncingFromProps = false

const monacoLanguage = computed(() => (props.language === 'python' ? 'python' : 'javascript'))
const languageLabel = computed(() => (props.language === 'python' ? 'Python' : 'JavaScript'))
const surfaceStyle = computed(() => ({
  height: `${props.minHeight}px`,
  minHeight: `${props.minHeight}px`
}))

const codeValue = computed({
  get: () => props.modelValue ?? '',
  set: (val) => {
    emit('update:modelValue', val ?? '')
    emit('change', val ?? '')
  }
})

const baseEditorOptions = {
  automaticLayout: true,
  fontSize: 13,
  fontFamily: "Consolas, Monaco, 'Courier New', monospace",
  lineHeight: 22,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  wordWrap: 'on',
  tabSize: 2,
  insertSpaces: true,
  renderWhitespace: 'selection',
  bracketPairColorization: { enabled: true },
  padding: { top: 8, bottom: 8 },
  scrollbar: {
    verticalScrollbarSize: 8,
    horizontalScrollbarSize: 8
  }
}

const fullscreenEditorOptions = computed(() => ({
  ...baseEditorOptions,
  readOnly: props.readonly,
  minimap: { enabled: true }
}))

/**
 * 构建内联编辑器 options。
 */
function buildInlineOptions() {
  return {
    ...baseEditorOptions,
    value: props.modelValue ?? '',
    language: monacoLanguage.value,
    theme: 'vs',
    readOnly: props.readonly
  }
}

/**
 * 创建侧栏内联 Monaco 实例（同步，不依赖 loader 异步回调）。
 */
function createInlineEditor() {
  if (inlineEditor || !inlineContainerRef.value || fullscreenVisible.value) return

  inlineEditor = monaco.editor.create(inlineContainerRef.value, buildInlineOptions())
  bindEditorEvents(inlineEditor, 'inline')

  nextTick(() => {
    inlineEditor?.layout()
    applyEditorValue(inlineEditor, props.modelValue)
  })
}

/**
 * 销毁内联 Monaco 实例。
 */
function destroyInlineEditor() {
  if (!inlineEditor) return
  inlineEditor.dispose()
  inlineEditor = null
}

/**
 * 绑定编辑器事件与快捷键。
 * @param {import('monaco-editor').editor.IStandaloneCodeEditor} editor
 * @param {'inline'|'fullscreen'} slot
 */
function bindEditorEvents(editor, slot) {
  editor.onDidChangeModelContent(() => {
    if (syncingFromProps) return
    const value = editor.getValue()
    if (value !== props.modelValue) {
      codeValue.value = value
    }
  })
  editor.onDidFocusEditorText(() => emit('focus', editor.getValue()))
  editor.onDidBlurEditorText(() => emit('blur', editor.getValue()))
  editor.addAction({
    id: `wf-format-${slot}-${instanceId}`,
    label: '格式化代码',
    keybindings: [
      monaco.KeyMod.Shift | monaco.KeyMod.Alt | monaco.KeyCode.KeyF
    ],
    run: () => formatCode(slot)
  })
}

/**
 * 将外部代码写入编辑器。
 * @param {import('monaco-editor').editor.IStandaloneCodeEditor | null} editor
 * @param {string} value
 */
function applyEditorValue(editor, value) {
  if (!editor) return
  const next = value ?? ''
  if (editor.getValue() === next) return
  syncingFromProps = true
  editor.setValue(next)
  syncingFromProps = false
}

function scheduleRelayoutInline() {
  if (layoutTimer) clearTimeout(layoutTimer)
  layoutTimer = setTimeout(() => {
    nextTick(() => {
      inlineEditor?.layout()
      applyEditorValue(inlineEditor, props.modelValue)
    })
  }, 50)
}

useResizeObserver(inlineContainerRef, () => {
  scheduleRelayoutInline()
})

watch(
  () => props.modelValue,
  (val) => {
    if (!inlineEditor && inlineContainerRef.value && !fullscreenVisible.value) {
      createInlineEditor()
    }
    applyEditorValue(inlineEditor, val)
    applyEditorValue(fullscreenEditor.value, val)
  }
)

watch(
  () => props.language,
  (lang) => {
    const languageId = lang === 'python' ? 'python' : 'javascript'
    const inlineModel = inlineEditor?.getModel()
    if (inlineModel) {
      monaco.editor.setModelLanguage(inlineModel, languageId)
    }
    const fullscreenModel = fullscreenEditor.value?.getModel()
    if (fullscreenModel) {
      monaco.editor.setModelLanguage(fullscreenModel, languageId)
    }
  }
)

watch(
  () => props.readonly,
  (readonly) => {
    inlineEditor?.updateOptions({ readOnly: readonly })
    fullscreenEditor.value?.updateOptions({ readOnly: readonly })
  }
)

onMounted(() => {
  requestAnimationFrame(() => {
    createInlineEditor()
    scheduleRelayoutInline()
  })
})

onUnmounted(() => {
  if (layoutTimer) clearTimeout(layoutTimer)
  destroyInlineEditor()
})

/**
 * 全屏编辑器挂载回调。
 * @param {import('monaco-editor').editor.IStandaloneCodeEditor} editor
 * @param {typeof import('monaco-editor')} monacoInstance
 */
function onFullscreenMount(editor, monacoInstance) {
  ensurePythonFormatProvider(monacoInstance)
  fullscreenEditor.value = editor
  applyEditorValue(editor, props.modelValue)
  bindEditorEvents(editor, 'fullscreen')
  nextTick(() => editor.layout())
}

function openFullscreen() {
  fullscreenVisible.value = true
}

function onFullscreenOpened() {
  const editor = fullscreenEditor.value
  applyEditorValue(editor, props.modelValue)
  nextTick(() => {
    editor?.layout()
    editor?.focus()
  })
}

/** 关闭全屏后恢复内联编辑器。 */
function onFullscreenClosed() {
  fullscreenEditor.value = null
  nextTick(() => {
    if (!inlineEditor) {
      createInlineEditor()
    } else {
      applyEditorValue(inlineEditor, props.modelValue)
      scheduleRelayoutInline()
    }
  })
}

/**
 * 获取编辑器实例。
 * @param {'inline'|'fullscreen'} slot
 */
function resolveEditor(slot) {
  return slot === 'fullscreen' ? fullscreenEditor.value : inlineEditor
}

/**
 * 执行代码格式化。
 * @param {'inline'|'fullscreen'} slot
 */
async function formatCode(slot = 'inline') {
  if (props.readonly) return
  const editor = resolveEditor(slot)
  if (!editor) return

  if (props.language === 'python') {
    const formatted = basicFormatPython(editor.getValue())
    if (formatted === editor.getValue()) {
      ElMessage.info('代码格式已符合基本规范')
      return
    }
    editor.pushUndoStop()
    editor.executeEdits('wf-python-format', [{
      range: editor.getModel().getFullModelRange(),
      text: formatted
    }])
    codeValue.value = editor.getValue()
    ElMessage.success('已整理 Python 缩进与空行')
    return
  }

  const action = editor.getAction('editor.action.formatDocument')
  if (!action) {
    ElMessage.warning('当前语言暂不支持自动格式化')
    return
  }
  await action.run()
  codeValue.value = editor.getValue()
  ElMessage.success('格式化完成')
}
</script>

<style scoped lang="scss">
.wf-code-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  transition: border-color 0.2s;

  &:hover {
    border-color: #c0c4cc;
  }

  &--readonly {
    background: #f5f7fa;
  }
}

.wf-code-editor__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 6px 10px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
}

.wf-code-editor__lang {
  font-size: 12px;
  color: #606266;
}

.wf-code-editor__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.wf-code-editor__surface {
  width: 100%;
}

.wf-code-editor-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.wf-code-editor-dialog__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.wf-code-editor-dialog__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wf-code-editor-dialog__body {
  height: calc(100vh - 120px);
}
</style>

<style lang="scss">
.wf-code-editor-dialog {
  .el-dialog__body {
    padding: 0 16px 16px;
  }
}
</style>
