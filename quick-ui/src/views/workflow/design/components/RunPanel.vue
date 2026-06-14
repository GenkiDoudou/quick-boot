<template>
  <div class="wf-run" :class="{ 'wf-run--open': visible }">
    <div class="wf-run__bar" @click="toggle">
      <span class="wf-run__bar-title">运行调试</span>
      <span v-if="running" class="wf-run__bar-status wf-run__bar-status--running">运行中…</span>
      <span v-else-if="traceSteps.length && !running" class="wf-run__bar-status">
        {{ traceSteps.length }} 步 · {{ successCount }}/{{ traceSteps.length }} 成功
      </span>
      <el-icon class="wf-run__bar-icon">
        <component :is="visible ? ArrowDown : ArrowUp" />
      </el-icon>
    </div>

    <div v-show="visible" class="wf-run__body">
      <!-- 左侧：运行入参 -->
      <div class="wf-run__col wf-run__col--input">
        <div class="wf-run__section-title">运行输入</div>
        <el-form label-position="top" size="small" class="wf-run__form">
          <p v-if="!startInputs.length" class="wf-run__hint">
            请先在开始节点添加字段后再运行
          </p>
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
              show-word-limit
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
              :rows="3"
              :placeholder="resolveRunFieldType(field) === 'array' ? 'JSON 数组' : 'JSON 对象'"
            />
            <el-input
              v-else-if="resolveRunFieldType(field) === 'file'"
              disabled
              placeholder="文件类型暂不支持在设计器调试中上传"
            />
            <el-input v-else v-model="localInputs[field.key]" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="localStream">流式输出（async + SSE）</el-checkbox>
          </el-form-item>
          <el-button type="primary" :loading="running" class="wf-run__start-btn" @click="emitRun">
            <el-icon><VideoPlay /></el-icon>
            开始运行
          </el-button>
        </el-form>
      </div>

      <!-- 中间：步骤 Trace（可展开查看每步 I/O） -->
      <div class="wf-run__col wf-run__col--trace">
        <div class="wf-run__section-header">
          <div class="wf-run__section-title">执行步骤</div>
          <el-button
            v-if="traceSteps.length"
            link
            size="small"
            type="primary"
            @click="toggleExpandAll"
          >
            {{ allExpanded ? '全部收起' : '全部展开' }}
          </el-button>
        </div>

        <div v-if="traceSteps.length" class="wf-run__steps">
          <div
            v-for="(step, idx) in traceSteps"
            :key="traceStepKey(step, idx)"
            class="wf-run__step"
            :class="{
              'wf-run__step--failed': step.status === 'FAILED',
              'wf-run__step--running': step.status === 'RUNNING',
              'wf-run__step--expanded': isExpanded(step, idx)
            }"
          >
            <div
              class="wf-run__step-head"
              @click="toggleStep(step, idx)"
            >
              <el-icon class="wf-run__step-caret">
                <ArrowRight />
              </el-icon>
              <span class="wf-run__step-order">{{ idx + 1 }}</span>
              <span class="wf-run__step-type">{{ nodeTypeLabel(step.nodeType) }}</span>
              <code class="wf-run__step-id">{{ step.nodeId }}</code>
              <el-tag v-if="loopIterationBadge(step)" size="small" type="info" effect="plain">
                {{ loopIterationBadge(step) }}
              </el-tag>
              <el-tag
                size="small"
                :type="statusTagType(step.status)"
                class="wf-run__step-tag"
              >
                {{ statusLabel(step.status) }}
              </el-tag>
              <span v-if="step.durationMs != null" class="wf-run__step-dur">
                {{ step.durationMs }} ms
              </span>
              <el-button
                link
                size="small"
                class="wf-run__step-focus"
                title="定位到画布节点"
                @click.stop="$emit('focus-step', step)"
              >
                定位
              </el-button>
            </div>

            <div v-show="isExpanded(step, idx)" class="wf-run__step-body">
              <div class="wf-run__step-io">
                <div class="wf-run__step-io-label">输入</div>
                <pre class="wf-run__step-io-pre">{{ formatStepIo(step.inputs) }}</pre>
              </div>
              <div class="wf-run__step-io">
                <div class="wf-run__step-io-label">输出</div>
                <pre
                  v-if="step.outputs != null"
                  class="wf-run__step-io-pre"
                >{{ formatStepIo(step.outputs) }}</pre>
                <p v-else-if="step.status === 'RUNNING'" class="wf-run__step-io-pending">
                  执行中…
                </p>
                <p v-else class="wf-run__step-io-pending">无输出</p>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="点击「开始运行」后，此处展示每一步的输入与输出" :image-size="56" />
      </div>

      <!-- 右侧：最终输出 -->
      <div class="wf-run__col wf-run__col--output">
        <div class="wf-run__section-title">最终输出</div>
        <div v-if="running && localStream && !streamText" class="wf-run__streaming">
          <span class="wf-run__streaming-dot" />
          流式生成中…
        </div>
        <pre v-else-if="streamText" class="wf-run__plain-output">{{ streamText }}</pre>
        <el-empty
          v-else-if="!running && traceSteps.length"
          description="运行完成，但输出节点结果为空（请检查连线、输出变量与入参）"
          :image-size="56"
        />
        <el-empty v-else description="运行完成后在此展示输出节点结果" :image-size="56" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowUp, ArrowRight, VideoPlay } from '@element-plus/icons-vue'
import { migrateFieldType, parseRunInputValue } from './forms/startFieldTypes'
import { getNodeLabel } from '../nodeMeta'
import { formatLoopIterationBadge, formatStepIo, traceStepKey } from '../utils/runTraceUtils'

defineOptions({ name: 'RunPanel' })

const props = defineProps({
  visible: { type: Boolean, default: false },
  startInputs: { type: Array, default: () => [] },
  streamEnabled: { type: Boolean, default: false },
  running: { type: Boolean, default: false },
  traceSteps: { type: Array, default: () => [] },
  streamText: { type: String, default: '' }
})

const emit = defineEmits(['update:visible', 'update:streamEnabled', 'run', 'focus-step'])

const localInputs = reactive({})
/** @type {import('vue').Ref<Set<string>>} */
const expandedKeys = ref(new Set())
const allExpanded = ref(false)

const localStream = computed({
  get: () => props.streamEnabled,
  set: (v) => emit('update:streamEnabled', v)
})

const visibleStartInputs = computed(() =>
  (props.startInputs || []).filter((f) => f.key && !f.hidden)
)

const successCount = computed(() =>
  props.traceSteps.filter((s) => s.status === 'SUCCESS').length
)

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
      if (!validKeys.has(key)) {
        delete localInputs[key]
      }
    })
  },
  { immediate: true, deep: true }
)

/** 新步骤到达时自动展开最新一步 */
watch(
  () => props.traceSteps.length,
  (len, prev) => {
    if (len > (prev || 0) && len > 0) {
      const last = props.traceSteps[len - 1]
      const key = traceStepKey(last, len - 1)
      expandedKeys.value = new Set([...expandedKeys.value, key])
    }
  }
)

/** 运行结束后默认展开全部步骤 */
watch(
  () => props.running,
  (isRunning, wasRunning) => {
    if (wasRunning && !isRunning && props.traceSteps.length) {
      expandAllSteps()
    }
    if (isRunning) {
      expandedKeys.value = new Set()
      allExpanded.value = false
    }
  }
)

function toggle() {
  emit('update:visible', !props.visible)
}

function nodeTypeLabel(type) {
  return getNodeLabel(type) || type || '节点'
}

function statusLabel(status) {
  if (status === 'FAILED') return '失败'
  if (status === 'SUCCESS') return '成功'
  if (status === 'RUNNING') return '运行中'
  return status || '—'
}

function statusTagType(status) {
  if (status === 'FAILED') return 'danger'
  if (status === 'SUCCESS') return 'success'
  if (status === 'RUNNING') return 'warning'
  return 'info'
}

function loopIterationBadge(step) {
  return formatLoopIterationBadge(step)
}

function isExpanded(step, idx) {
  return expandedKeys.value.has(traceStepKey(step, idx))
}

function toggleStep(step, idx) {
  const key = traceStepKey(step, idx)
  const next = new Set(expandedKeys.value)
  if (next.has(key)) {
    next.delete(key)
  } else {
    next.add(key)
  }
  expandedKeys.value = next
  allExpanded.value = next.size === props.traceSteps.length
}

function expandAllSteps() {
  expandedKeys.value = new Set(
    props.traceSteps.map((step, idx) => traceStepKey(step, idx))
  )
  allExpanded.value = true
}

function toggleExpandAll() {
  if (allExpanded.value) {
    expandedKeys.value = new Set()
    allExpanded.value = false
  } else {
    expandAllSteps()
  }
}

/** @param {object} field */
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
.wf-run {
  flex-shrink: 0;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.wf-run__bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 36px;
  cursor: pointer;
  user-select: none;
  color: #606266;
  font-size: 13px;

  &:hover {
    background: #f5f7fa;
  }
}

.wf-run__bar-title {
  font-weight: 600;
  color: #0a2463;
}

.wf-run__bar-status {
  font-size: 12px;
  color: #909399;

  &--running {
    color: #e6a23c;
  }
}

.wf-run--open .wf-run__body {
  height: 44vh;
}

.wf-run__body {
  display: grid;
  grid-template-columns: 260px 1fr 1fr;
  gap: 0;
  height: 0;
  overflow: hidden;
  border-top: 1px solid #ebeef5;
  transition: height 0.2s ease;
}

.wf-run__col {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 12px 14px;
  overflow: hidden;
  border-right: 1px solid #ebeef5;

  &:last-child {
    border-right: none;
  }

  &--input {
    overflow-y: auto;
  }

  &--trace,
  &--output {
    overflow-y: auto;
  }
}

.wf-run__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  flex-shrink: 0;
}

.wf-run__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 10px;
}

.wf-run__section-header .wf-run__section-title {
  margin-bottom: 0;
}

.wf-run__hint {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.wf-run__start-btn {
  width: 100%;
}

.wf-run__steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.wf-run__step {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: border-color 0.15s;

  &--failed {
    border-color: #fab6b6;
    background: #fef0f0;
  }

  &--running {
    border-color: #f3d19e;
    background: #fdf6ec;
  }

  &--expanded .wf-run__step-caret {
    transform: rotate(90deg);
  }
}

.wf-run__step-head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  cursor: pointer;
  font-size: 12px;
  flex-wrap: wrap;

  &:hover {
    background: rgba(64, 158, 255, 0.06);
  }
}

.wf-run__step-caret {
  flex-shrink: 0;
  font-size: 12px;
  color: #909399;
  transition: transform 0.15s;
}

.wf-run__step-order {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  font-size: 11px;
  font-weight: 600;
}

.wf-run__step-type {
  font-weight: 600;
  color: #303133;
}

.wf-run__step-id {
  font-size: 11px;
  color: #909399;
  background: #f5f7fa;
  padding: 1px 4px;
  border-radius: 3px;
}

.wf-run__step-tag {
  flex-shrink: 0;
}

.wf-run__step-dur {
  font-size: 11px;
  color: #909399;
  margin-left: auto;
}

.wf-run__step-focus {
  flex-shrink: 0;
  padding: 0 4px;
}

.wf-run__step-body {
  border-top: 1px solid #ebeef5;
  padding: 8px 10px 10px;
  background: #fafafa;
}

.wf-run__step-io {
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.wf-run__step-io-label {
  font-size: 11px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
}

.wf-run__step-io-pre {
  margin: 0;
  padding: 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  font-size: 11px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 160px;
  overflow-y: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  user-select: text;
}

.wf-run__step-io-pending {
  margin: 0;
  font-size: 12px;
  color: #909399;
  padding: 8px;
  background: #fff;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.wf-run__streaming {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  font-size: 13px;
  color: #606266;
}

.wf-run__streaming-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: wf-run-pulse 1s ease-in-out infinite;
}

@keyframes wf-run-pulse {
  0%,
  100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}

.wf-run__plain-output {
  margin: 0;
  flex: 1;
  min-height: 80px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-y: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  user-select: text;
  cursor: text;
  border: none;
}
</style>
