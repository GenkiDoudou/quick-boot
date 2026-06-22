<template>
  <div class="mcp-tool-invoke">
    <div class="mcp-tool-invoke__section-label">试跑</div>

    <el-form
      v-if="tool.parameters?.length"
      label-position="top"
      class="mcp-tool-invoke__form"
      @submit.prevent="handleInvoke"
    >
      <el-form-item
        v-for="param in tool.parameters"
        :key="param.name"
        :required="param.required"
      >
        <template #label>
          <span class="mcp-tool-invoke__label">
            <code>{{ param.name }}</code>
            <el-tag size="small" type="warning" effect="plain">{{ param.type || 'string' }}</el-tag>
          </span>
        </template>
        <el-select
          v-if="param.enumValues?.length"
          v-model="formArgs[param.name]"
          clearable
          :placeholder="param.description || '请选择'"
          style="width: 100%"
        >
          <el-option v-for="ev in param.enumValues" :key="ev" :label="ev" :value="ev" />
        </el-select>
        <el-input
          v-else-if="param.type === 'boolean'"
          v-model="formArgs[param.name]"
          placeholder="true / false"
        />
        <el-input
          v-else-if="param.type === 'integer' || param.type === 'number'"
          v-model="formArgs[param.name]"
          type="number"
          :placeholder="param.description || param.defaultValue || ''"
        />
        <el-input
          v-else
          v-model="formArgs[param.name]"
          :placeholder="param.description || param.defaultValue || ''"
        />
        <div v-if="param.description" class="mcp-tool-invoke__hint">{{ param.description }}</div>
      </el-form-item>
    </el-form>

    <div v-else class="mcp-tool-invoke__json-mode">
      <div class="mcp-tool-invoke__hint">该工具无声明参数，可直接执行或填写 JSON 入参：</div>
      <el-input
        v-model="jsonArgs"
        type="textarea"
        :rows="3"
        placeholder='{"key": "value"}'
      />
    </div>

    <div class="mcp-tool-invoke__actions">
      <el-button
        type="primary"
        :loading="invoking"
        v-hasPermi="['ai:mcp:test']"
        @click="handleInvoke"
      >
        <el-icon class="mcp-tool-invoke__run-icon"><VideoPlay /></el-icon>
        执行工具
      </el-button>
    </div>

    <div v-if="invokeResult" class="mcp-tool-invoke__result">
      <el-alert
        :type="resultAlertType"
        :title="resultTitle"
        show-icon
        :closable="false"
      />
      <div v-if="invokeResult.durationMs != null" class="mcp-tool-invoke__duration">
        耗时 {{ invokeResult.durationMs }} ms
      </div>
      <div v-if="invokeResult.textOutput" class="mcp-tool-invoke__output-wrap">
        <div class="mcp-tool-invoke__output-label">文本输出</div>
        <pre class="mcp-tool-invoke__output">{{ invokeResult.textOutput }}</pre>
      </div>
      <div v-if="invokeResult.structuredContent" class="mcp-tool-invoke__output-wrap">
        <div class="mcp-tool-invoke__output-label">Structured Content</div>
        <pre class="mcp-tool-invoke__output">{{ formatJson(invokeResult.structuredContent) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import { invokeMcpTool } from '@/api/ai/mcp'

/**
 * 单个 MCP 工具的试跑面板：根据 parameters 生成表单并展示执行结果。
 */
const props = defineProps({
  mcpId: { type: [String, Number], required: true },
  /** @type {import('vue').PropType<Record<string, unknown>>} */
  tool: { type: Object, required: true },
})

const formArgs = reactive({})
const jsonArgs = ref('{}')
const invoking = ref(false)
const invokeResult = ref(null)

watch(
  () => props.tool,
  (tool) => {
    invokeResult.value = null
    Object.keys(formArgs).forEach((k) => delete formArgs[k])
    for (const param of tool?.parameters || []) {
      formArgs[param.name] = param.defaultValue ?? ''
    }
    jsonArgs.value = '{}'
  },
  { immediate: true, deep: true },
)

const resultAlertType = computed(() => {
  if (!invokeResult.value?.success) return 'error'
  if (invokeResult.value?.isError) return 'warning'
  return 'success'
})

const resultTitle = computed(() => invokeResult.value?.message || '—')

function formatJson(obj) {
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

function buildArguments() {
  if (!props.tool.parameters?.length) {
    const raw = (jsonArgs.value || '').trim()
    if (!raw || raw === '{}') return {}
    try {
      const parsed = JSON.parse(raw)
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        return parsed
      }
      throw new Error('须为 JSON 对象')
    } catch (e) {
      throw new Error(`入参 JSON 无效: ${e.message}`)
    }
  }

  const args = {}
  for (const param of props.tool.parameters) {
    const raw = formArgs[param.name]
    const empty = raw === '' || raw === null || raw === undefined
    if (empty) {
      if (param.required) {
        throw new Error(`请填写必填参数: ${param.name}`)
      }
      continue
    }
    args[param.name] = coerceValue(raw, param.type)
  }
  return args
}

function coerceValue(raw, type) {
  if (type === 'integer') {
    const n = parseInt(String(raw), 10)
    if (Number.isNaN(n)) throw new Error(`参数须为整数: ${raw}`)
    return n
  }
  if (type === 'number') {
    const n = Number(raw)
    if (Number.isNaN(n)) throw new Error(`参数须为数字: ${raw}`)
    return n
  }
  if (type === 'boolean') {
    const s = String(raw).trim().toLowerCase()
    if (s === 'true' || s === '1') return true
    if (s === 'false' || s === '0') return false
    throw new Error(`参数须为 true/false: ${raw}`)
  }
  if (type === 'object' || type === 'array') {
    try {
      return JSON.parse(String(raw))
    } catch {
      throw new Error(`参数须为合法 JSON: ${raw}`)
    }
  }
  return String(raw)
}

async function handleInvoke() {
  try {
    const argumentsPayload = buildArguments()
    invoking.value = true
    const res = await invokeMcpTool({
      mcpId: props.mcpId,
      toolName: props.tool.name,
      arguments: argumentsPayload,
    })
    invokeResult.value = res?.data || { success: false, message: '无响应数据' }
    if (!invokeResult.value.success) {
      ElMessage.error(invokeResult.value.message || '执行失败')
    } else if (invokeResult.value.isError) {
      ElMessage.warning(invokeResult.value.message || '工具返回错误')
    } else {
      ElMessage.success('执行成功')
    }
  } catch (e) {
    ElMessage.error(e.message || '执行失败')
  } finally {
    invoking.value = false
  }
}
</script>

<style scoped>
.mcp-tool-invoke {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--el-border-color);
}

.mcp-tool-invoke__section-label {
  margin-bottom: 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-primary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.mcp-tool-invoke__form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.mcp-tool-invoke__label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.mcp-tool-invoke__label code {
  font-size: 12px;
  color: var(--el-color-primary);
}

.mcp-tool-invoke__hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.mcp-tool-invoke__json-mode {
  margin-bottom: 12px;
}

.mcp-tool-invoke__actions {
  margin-bottom: 12px;
}

.mcp-tool-invoke__run-icon {
  margin-right: 4px;
}

.mcp-tool-invoke__result {
  margin-top: 8px;
}

.mcp-tool-invoke__duration {
  margin: 8px 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.mcp-tool-invoke__output-wrap {
  margin-top: 10px;
}

.mcp-tool-invoke__output-label {
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.mcp-tool-invoke__output {
  margin: 0;
  padding: 12px;
  max-height: 240px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.55;
  font-family: Consolas, Monaco, monospace;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
