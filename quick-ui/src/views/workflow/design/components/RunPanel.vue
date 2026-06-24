<template>
  <aside v-show="visible" class="wf-run-chat">
    <header class="wf-run-chat__head">
      <div class="wf-run-chat__head-main">
        <span class="wf-run-chat__title">试运行</span>
        <span v-if="running" class="wf-run-chat__badge wf-run-chat__badge--running">运行中</span>
        <span v-else-if="runStats.stepCount" class="wf-run-chat__badge" :class="`wf-run-chat__badge--${statusTone}`">
          {{ runStatusLabel }}
        </span>
      </div>
      <button type="button" class="wf-run-chat__close" title="关闭" @click="close">
        <el-icon><Close /></el-icon>
      </button>
    </header>

    <div v-if="runStats.stepCount || running" class="wf-run-chat__stats">
      <span>{{ runStats.stepCount || 0 }} 节点</span>
      <span>{{ formatDurationMs(runStats.totalDurationMs) }}</span>
      <span v-if="runStats.totalTokens">{{ runStats.totalTokens.toLocaleString() }} tokens</span>
    </div>

    <div ref="scrollRef" class="wf-run-chat__messages">
      <div v-if="!hasMessages" class="wf-run-chat__welcome">
        <el-icon :size="36"><ChatDotRound /></el-icon>
        <p>填写下方参数并点击「运行」，结果将在此展示；各节点的输入/输出请在画布节点上查看。</p>
      </div>

      <div v-if="userMessageText" class="wf-run-chat__msg wf-run-chat__msg--user">
        <div class="wf-run-chat__bubble">{{ userMessageText }}</div>
        <C7Copy mode="icon" :text="userMessageText" success-message="已复制" class="wf-run-chat__copy" />
      </div>

      <div v-if="running && !assistantMessageText" class="wf-run-chat__msg wf-run-chat__msg--assistant">
        <div class="wf-run-chat__bubble wf-run-chat__bubble--loading">
          <span class="wf-run-chat__dot" />
          <span class="wf-run-chat__dot" />
          <span class="wf-run-chat__dot" />
        </div>
      </div>

      <div v-if="assistantMessageText" class="wf-run-chat__msg wf-run-chat__msg--assistant">
        <div class="wf-run-chat__bubble wf-run-chat__bubble--result">
          <pre class="wf-run-chat__result-pre">{{ assistantMessageText }}</pre>
        </div>
        <C7Copy mode="icon" :text="assistantMessageText" success-message="已复制" class="wf-run-chat__copy" />
      </div>
    </div>

    <footer class="wf-run-chat__composer">
      <div v-if="!startInputs.length" class="wf-run-chat__composer-empty">
        请先在开始节点添加输入字段
      </div>
      <el-form v-else label-position="top" size="small" class="wf-run-chat__form">
        <el-form-item
          v-for="field in visibleStartInputs"
          :key="field.key"
          :label="field.label || field.key"
          :required="field.required"
        >
          <el-input
            v-if="resolveRunFieldType(field) === 'string' || resolveRunFieldType(field) === 'time'"
            v-model="localInputs[field.key]"
            :placeholder="field.label || field.key"
            :maxlength="field.maxLength || undefined"
          />
          <el-input-number
            v-else-if="resolveRunFieldType(field) === 'integer'"
            v-model="localInputs[field.key]"
            :precision="0"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
          <el-input-number
            v-else-if="resolveRunFieldType(field) === 'number'"
            v-model="localInputs[field.key]"
            controls-position="right"
            style="width: 100%"
          />
          <el-switch
            v-else-if="resolveRunFieldType(field) === 'boolean'"
            v-model="localInputs[field.key]"
          />
          <el-input
            v-else-if="resolveRunFieldType(field) === 'object' || resolveRunFieldType(field) === 'array'"
            v-model="localInputs[field.key]"
            type="textarea"
            :rows="2"
            :placeholder="resolveRunFieldType(field) === 'array' ? 'JSON 数组' : 'JSON 对象'"
          />
          <el-input
            v-else-if="resolveRunFieldType(field) === 'file'"
            disabled
            placeholder="文件类型暂不支持"
          />
          <el-input v-else v-model="localInputs[field.key]" />
        </el-form-item>
        <div class="wf-run-chat__options">
          <el-checkbox v-model="localStream">流式输出</el-checkbox>
        </div>
      </el-form>
      <el-button type="primary" :loading="running" class="wf-run-chat__submit" @click="emitRun">
        <el-icon><VideoPlay /></el-icon>
        运行
      </el-button>
    </footer>
  </aside>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, VideoPlay } from '@element-plus/icons-vue'
import C7Copy from '@/packages/C7Copy/index.vue'
import { migrateFieldType, parseRunInputValue } from './forms/startFieldTypes'
import {
  computeRunStats,
  formatDurationMs,
  formatRunStatusLabel,
  runStatusTagType as resolveRunStatusTagType
} from '../utils/runTraceUtils'

defineOptions({ name: 'RunPanel' })

const props = defineProps({
  visible: { type: Boolean, default: false },
  startInputs: { type: Array, default: () => [] },
  streamEnabled: { type: Boolean, default: false },
  running: { type: Boolean, default: false },
  traceSteps: { type: Array, default: () => [] },
  streamText: { type: String, default: '' },
  runInfo: { type: Object, default: () => ({}) },
  lastRunInputs: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'update:streamEnabled', 'run'])

const localInputs = reactive({})
const scrollRef = ref(null)

const localStream = computed({
  get: () => props.streamEnabled,
  set: (v) => emit('update:streamEnabled', v)
})

const visibleStartInputs = computed(() =>
  (props.startInputs || []).filter((f) => f.key && !f.hidden)
)

const runStats = computed(() => computeRunStats(props.traceSteps, props.runInfo))

const runStatusLabel = computed(() => {
  if (props.running) return '运行中'
  return formatRunStatusLabel(runStats.value.finalStatus)
})

const statusTone = computed(() => {
  if (props.running) return 'running'
  const t = resolveRunStatusTagType(runStats.value.finalStatus)
  if (t === 'success') return 'success'
  if (t === 'danger') return 'danger'
  return 'neutral'
})

const userMessageText = computed(() => formatInputsMessage(props.lastRunInputs))

const assistantMessageText = computed(() => {
  if (props.running && props.streamEnabled && props.streamText) return props.streamText
  if (!props.running && props.streamText) return props.streamText
  return ''
})

const hasMessages = computed(() => !!(userMessageText.value || assistantMessageText.value || props.running))

watch(
  () => props.startInputs,
  (fields) => {
    const validKeys = new Set()
    ;(fields || []).forEach((f) => {
      if (!f.key) return
      validKeys.add(f.key)
      if (localInputs[f.key] === undefined) {
        if (f.hidden && f.defaultValue !== undefined && f.defaultValue !== '') {
          localInputs[f.key] = f.defaultValue
        } else if (f.defaultValue !== undefined && f.defaultValue !== '' && f.defaultValue !== null) {
          localInputs[f.key] = f.defaultValue
        } else {
          localInputs[f.key] = f.type === 'boolean' ? false : f.type === 'number' ? 0 : ''
        }
      }
    })
    Object.keys(localInputs).forEach((key) => {
      if (!validKeys.has(key)) delete localInputs[key]
    })
  },
  { immediate: true, deep: true }
)

watch(
  () => [props.streamText, props.running, userMessageText.value],
  () => {
    nextTick(() => {
      const el = scrollRef.value
      if (el) el.scrollTop = el.scrollHeight
    })
  }
)

function close() {
  emit('update:visible', false)
}

function formatInputsMessage(inputs) {
  if (!inputs || typeof inputs !== 'object') return ''
  const keys = Object.keys(inputs).filter((k) => inputs[k] !== undefined && inputs[k] !== '')
  if (!keys.length) return ''
  if (keys.length === 1 && typeof inputs[keys[0]] === 'string') {
    return String(inputs[keys[0]])
  }
  try {
    return JSON.stringify(inputs, null, 2)
  } catch {
    return keys.map((k) => `${k}: ${inputs[k]}`).join('\n')
  }
}

function resolveRunFieldType(field) {
  return migrateFieldType(field?.fieldType)
}

function emitRun() {
  if (!props.startInputs?.length) {
    ElMessage.warning('请先在开始节点添加至少一个输入字段')
    return
  }
  for (const field of props.startInputs || []) {
    if (!field.key || field.hidden) continue
    if (field.required) {
      const val = localInputs[field.key]
      if (val === undefined || val === null || val === '') {
        ElMessage.warning(`请填写必填项：${field.label || field.key}`)
        return
      }
    }
  }

  const inputs = {}
  for (const f of props.startInputs || []) {
    if (!f.key) continue
    const raw = f.hidden ? f.defaultValue ?? '' : localInputs[f.key]
    const ft = resolveRunFieldType(f)
    if (ft === 'object' || ft === 'array') {
      if (raw !== undefined && raw !== null && raw !== '' && typeof raw === 'string') {
        try {
          JSON.parse(raw.trim())
        } catch {
          ElMessage.warning(`${f.label || f.key} 不是合法的 JSON`)
          return
        }
      }
    }
    inputs[f.key] = parseRunInputValue(f, raw)
  }
  emit('run', inputs)
}
</script>

<style scoped lang="scss">
.wf-run-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  flex-shrink: 0;
  background: #fff;
  border-left: 1px solid #e5e6eb;
  overflow: hidden;
}

.wf-run-chat__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid #e5e6eb;
  flex-shrink: 0;
}

.wf-run-chat__head-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wf-run-chat__title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.wf-run-chat__badge {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  background: #f2f3f5;
  color: #86909c;

  &--success {
    background: #e8ffea;
    color: #00b42a;
  }

  &--danger {
    background: #ffece8;
    color: #f53f3f;
  }

  &--running {
    background: #fff7e8;
    color: #ff7d00;
  }
}

.wf-run-chat__close {
  border: none;
  background: #f2f3f5;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  color: #86909c;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: #e5e6eb;
    color: #1d2129;
  }
}

.wf-run-chat__stats {
  display: flex;
  gap: 12px;
  padding: 8px 14px;
  font-size: 11px;
  color: #86909c;
  border-bottom: 1px solid #f2f3f5;
  flex-shrink: 0;
}

.wf-run-chat__messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px;
  background: #f7f8fa;
}

.wf-run-chat__welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 24px 12px;
  color: #86909c;

  .el-icon {
    color: #c9cdd4;
    margin-bottom: 10px;
  }

  p {
    margin: 0;
    font-size: 12px;
    line-height: 1.6;
  }
}

.wf-run-chat__msg {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 12px;

  &--user {
    flex-direction: row-reverse;

    .wf-run-chat__bubble {
      background: #3370ff;
      color: #fff;
      border-bottom-right-radius: 4px;
    }
  }

  &--assistant {
    .wf-run-chat__bubble {
      background: #fff;
      border: 1px solid #e5e6eb;
      border-bottom-left-radius: 4px;
    }
  }
}

.wf-run-chat__bubble {
  max-width: calc(100% - 32px);
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;

  &--loading {
    display: flex;
    gap: 4px;
    padding: 14px 16px;
  }

  &--result {
    padding: 0;
    overflow: hidden;
    width: 100%;
  }
}

.wf-run-chat__result-pre {
  margin: 0;
  padding: 10px 12px;
  max-height: 280px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'SF Mono', Consolas, Monaco, 'Courier New', monospace;
  color: #1d2129;
}

.wf-run-chat__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c9cdd4;
  animation: wf-chat-dot 1.2s ease-in-out infinite;

  &:nth-child(2) {
    animation-delay: 0.15s;
  }

  &:nth-child(3) {
    animation-delay: 0.3s;
  }
}

.wf-run-chat__copy {
  flex-shrink: 0;
  margin-top: 4px;
}

.wf-run-chat__composer {
  flex-shrink: 0;
  padding: 12px 14px 14px;
  border-top: 1px solid #e5e6eb;
  background: #fff;
}

.wf-run-chat__composer-empty {
  font-size: 12px;
  color: #86909c;
  margin-bottom: 10px;
}

.wf-run-chat__form {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 10px;

  :deep(.el-form-item) {
    margin-bottom: 10px;
  }

  :deep(.el-form-item__label) {
    font-size: 12px;
    color: #1d2129;
    padding-bottom: 4px;
  }
}

.wf-run-chat__options {
  margin-bottom: 4px;

  :deep(.el-checkbox__label) {
    font-size: 12px;
    color: #86909c;
  }
}

.wf-run-chat__submit {
  width: 100%;
  height: 36px;
  border-radius: 8px;
}

@keyframes wf-chat-dot {
  0%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-3px);
  }
}
</style>
