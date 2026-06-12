<template>
  <div class="wf-run" :class="{ 'wf-run--open': visible }">
    <div class="wf-run__bar" @click="toggle">
      <span class="wf-run__bar-title">运行调试</span>
      <el-icon class="wf-run__bar-icon">
        <component :is="visible ? ArrowDown : ArrowUp" />
      </el-icon>
    </div>
    <div v-show="visible" class="wf-run__body">
      <div class="wf-run__col wf-run__col--input">
        <div class="wf-run__section-title">输入</div>
        <el-form label-position="top" size="small" class="wf-run__form">
          <p v-if="!startInputs.length" class="wf-run__no-inputs">
            请先在输入节点添加字段后再运行
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
          <el-form-item label="知识库 ID（可选）">
            <el-input v-model="localInputs.kbId" placeholder="注入 sys.kbId" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="localStream">流式输出（async + SSE）</el-checkbox>
          </el-form-item>
          <el-button type="primary" :loading="running" @click="emitRun">
            <el-icon><VideoPlay /></el-icon>
            开始运行
          </el-button>
        </el-form>
      </div>

      <div class="wf-run__col wf-run__col--trace">
        <div class="wf-run__section-title">步骤 Trace</div>
        <el-timeline v-if="traceSteps.length">
          <el-timeline-item
            v-for="(step, idx) in traceSteps"
            :key="step.nodeId + idx"
            :type="step.status === 'FAILED' ? 'danger' : step.status === 'SUCCESS' ? 'success' : 'primary'"
          >
            <div class="wf-run__trace-item" @click="$emit('focus-step', step)">
              <strong>{{ step.nodeId }}</strong>
              <el-tag size="small">{{ step.nodeType }}</el-tag>
              <el-tag size="small" :type="step.status === 'FAILED' ? 'danger' : 'success'">
                {{ step.status }}
              </el-tag>
              <span v-if="step.durationMs != null" class="wf-run__trace-dur">{{ step.durationMs }} ms</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="运行后将展示步骤 Trace" :image-size="56" />
      </div>

      <div class="wf-run__col wf-run__col--stream">
        <div class="wf-run__section-title">运行输出</div>
        <pre v-if="streamText" class="wf-run__plain-output">{{ streamText }}</pre>
        <el-empty
          v-else-if="!running && traceSteps.length"
          description="运行完成，但输出节点结果为空（请检查连线、输出变量与入参）"
          :image-size="56"
        />
        <el-empty v-else description="运行后将在此展示输出节点结果" :image-size="56" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowUp, VideoPlay } from '@element-plus/icons-vue'
import { migrateFieldType, parseRunInputValue } from './forms/startFieldTypes'

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

const localInputs = reactive({ kbId: '' })

const localStream = computed({
  get: () => props.streamEnabled,
  set: (v) => emit('update:streamEnabled', v)
})

/** 运行面板展示的非隐藏 Start 字段 */
const visibleStartInputs = computed(() =>
  (props.startInputs || []).filter((f) => f.key && !f.hidden)
)

watch(
  () => props.startInputs,
  (fields) => {
    const validKeys = new Set(['kbId'])
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

function toggle() {
  emit('update:visible', !props.visible)
}

/** @param {object} field */
function resolveRunFieldType(field) {
  return migrateFieldType(field?.fieldType)
}

function emitRun() {
  if (!props.startInputs?.length) {
    ElMessage.warning('请先在输入节点添加至少一个输入字段')
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

  const inputs = { kbId: localInputs.kbId || undefined }
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

.wf-run--open .wf-run__body {
  height: 40vh;
}

.wf-run__body {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0;
  height: 0;
  overflow: hidden;
  border-top: 1px solid #ebeef5;
  transition: height 0.2s ease;
}

.wf-run__col {
  padding: 12px 16px;
  overflow-y: auto;
  border-right: 1px solid #ebeef5;

  &:last-child {
    border-right: none;
  }
}

.wf-run__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 10px;
}

.wf-run__no-inputs {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.wf-run__trace-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;

  &:hover {
    background: #ecf5ff;
  }
}

.wf-run__trace-dur {
  font-size: 12px;
  color: #909399;
}

.wf-run__plain-output {
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: calc(40vh - 48px);
  overflow-y: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  user-select: text;
  -webkit-user-select: text;
  cursor: text;
  border: none;
}

.wf-run__markdown {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  max-height: calc(40vh - 48px);
  overflow-y: auto;

  :deep(pre) {
    background: #e8ecf1;
    padding: 8px;
    border-radius: 4px;
    overflow-x: auto;
  }

  :deep(code) {
    font-family: Consolas, Monaco, monospace;
  }
}
</style>
