<template>
  <el-dialog
    :model-value="modelValue"
    width="840px"
    class="mcp-tools-dialog"
    destroy-on-close
    align-center
    :show-close="true"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="mcp-tools-dialog__header">
        <div class="mcp-tools-dialog__header-icon" :class="`mcp-tools-dialog__header-icon--${iconType}`">
          <el-icon :size="28"><component :is="iconComponent" /></el-icon>
        </div>
        <div class="mcp-tools-dialog__header-main">
          <div class="mcp-tools-dialog__title">{{ mcp?.name || 'MCP 服务' }}</div>
          <div class="mcp-tools-dialog__subtitle">
            <el-tag size="small" effect="dark" :type="transportTagType">{{ transportLabel }}</el-tag>
            <span v-if="mcp?.code" class="mcp-tools-dialog__code">{{ mcp.code }}</span>
          </div>
        </div>
        <div v-if="result?.success" class="mcp-tools-dialog__count">
          <span class="mcp-tools-dialog__count-num">{{ toolCount }}</span>
          <span class="mcp-tools-dialog__count-label">个工具</span>
        </div>
      </div>
    </template>

    <div v-loading="loading" class="mcp-tools-dialog__body">
      <p v-if="mcp?.description" class="mcp-tools-dialog__desc">{{ mcp.description }}</p>

      <div v-if="result && !result.success" class="mcp-tools-dialog__error">
        <el-icon class="mcp-tools-dialog__error-icon"><CircleCloseFilled /></el-icon>
        <div class="mcp-tools-dialog__error-text">{{ result.message || '获取工具列表失败' }}</div>
        <el-button type="primary" plain :loading="loading" @click="emit('refresh')">重新连接</el-button>
      </div>

      <template v-else-if="result?.success">
        <el-collapse v-if="result.tools?.length" v-model="expandedTools" class="mcp-tools-dialog__collapse">
          <el-collapse-item
            v-for="(tool, index) in result.tools"
            :key="tool.name || index"
            :name="tool.name || String(index)"
          >
            <template #title>
              <div class="mcp-tool-collapse__title">
                <el-icon class="mcp-tool-collapse__icon"><SetUp /></el-icon>
                <span class="mcp-tool-collapse__name">{{ tool.name }}</span>
                <el-tag v-if="tool.title" size="small" type="info" class="mcp-tool-collapse__title-tag">{{ tool.title }}</el-tag>
                <el-tag size="small" :type="paramCount(tool) ? 'primary' : 'info'" effect="plain">
                  {{ paramCount(tool) }} 个参数
                </el-tag>
              </div>
            </template>

            <p v-if="tool.description" class="mcp-tool-collapse__desc">{{ tool.description }}</p>

            <div v-if="tool.parameters?.length" class="mcp-tool-collapse__section">
              <div class="mcp-tool-collapse__section-label">入参</div>
              <el-table :data="tool.parameters" size="small" border stripe class="mcp-tool-collapse__table">
                <el-table-column prop="name" label="参数名" width="140">
                  <template #default="{ row }">
                    <code class="mcp-tool-collapse__code">{{ row.name }}</code>
                  </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="100">
                  <template #default="{ row }">
                    <el-tag size="small" type="warning" effect="plain">{{ row.type || '—' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="required" label="必填" width="72" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.required" size="small" type="danger" effect="plain">是</el-tag>
                    <span v-else class="mcp-tool-collapse__optional">否</span>
                  </template>
                </el-table-column>
                <el-table-column label="说明" min-width="160" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.description || '—' }}
                  </template>
                </el-table-column>
                <el-table-column label="约束" min-width="140">
                  <template #default="{ row }">
                    <span v-if="row.defaultValue" class="mcp-tool-collapse__constraint">默认: {{ row.defaultValue }}</span>
                    <template v-if="row.enumValues?.length">
                      <el-tag
                        v-for="ev in row.enumValues"
                        :key="ev"
                        size="small"
                        class="mcp-tool-collapse__enum"
                      >{{ ev }}</el-tag>
                    </template>
                    <span v-if="!row.defaultValue && !row.enumValues?.length">—</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-alert
              v-else
              type="info"
              :closable="false"
              show-icon
              title="该工具未声明入参（inputSchema 为空）"
              class="mcp-tool-collapse__no-params"
            />

            <div v-if="tool.inputSchema && Object.keys(tool.inputSchema).length" class="mcp-tool-collapse__section">
              <div class="mcp-tool-collapse__section-label">Input Schema（JSON）</div>
              <pre class="mcp-tool-collapse__schema">{{ formatJson(tool.inputSchema) }}</pre>
            </div>

            <div v-if="tool.outputSchema && Object.keys(tool.outputSchema).length" class="mcp-tool-collapse__section">
              <div class="mcp-tool-collapse__section-label">Output Schema（JSON）</div>
              <pre class="mcp-tool-collapse__schema">{{ formatJson(tool.outputSchema) }}</pre>
            </div>

            <McpToolInvokePanel
              v-if="mcp?.mcpId"
              :mcp-id="mcp.mcpId"
              :tool="tool"
            />
          </el-collapse-item>
        </el-collapse>
        <el-empty v-else description="该 MCP 未暴露任何工具" />
      </template>

      <div v-else-if="!loading" class="mcp-tools-dialog__placeholder">
        <el-icon :size="40" class="mcp-tools-dialog__placeholder-icon"><Loading /></el-icon>
        <span>正在连接 MCP 并获取工具…</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" :loading="loading" @click="emit('refresh')">
        <el-icon class="mcp-tools-dialog__refresh-icon"><Refresh /></el-icon>
        刷新工具
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import {
  CircleCloseFilled,
  Connection,
  Cpu,
  Loading,
  Monitor,
  Refresh,
  SetUp,
} from '@element-plus/icons-vue'
import McpToolInvokePanel from './McpToolInvokePanel.vue'

/**
 * MCP 工具列表弹窗：居中宽弹窗 + 工具卡片网格，比侧栏抽屉更易读。
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** @type {import('vue').PropType<Record<string, unknown> | null>} */
  mcp: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  /** @type {import('vue').PropType<{ success?: boolean, toolCount?: number, message?: string, tools?: Array<{name:string,description:string}> } | null>} */
  result: { type: Object, default: null },
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const expandedTools = ref([])

watch(
  () => props.result?.tools,
  (tools) => {
    if (tools?.length) {
      expandedTools.value = [tools[0].name || '0']
    } else {
      expandedTools.value = []
    }
  },
  { immediate: true },
)

function paramCount(tool) {
  return tool?.parameters?.length ?? 0
}

function formatJson(obj) {
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

const transportLabel = computed(() => {
  const map = {
    STDIO: '本地 STDIO',
    SSE: '联网 SSE',
    STREAMABLE_HTTP: 'Streamable HTTP',
  }
  return map[props.mcp?.transport] || props.mcp?.transport || '—'
})

const transportTagType = computed(() => {
  if (props.mcp?.transport === 'STDIO') return 'info'
  if (props.mcp?.transport === 'SSE') return ''
  return 'primary'
})

const iconType = computed(() => {
  if (props.mcp?.transport === 'STDIO') return 'stdio'
  if (props.mcp?.transport === 'SSE') return 'sse'
  return 'http'
})

const iconComponent = computed(() => {
  if (props.mcp?.transport === 'STDIO') return Cpu
  if (props.mcp?.transport === 'SSE') return Connection
  return Monitor
})

const toolCount = computed(() => props.result?.toolCount ?? props.result?.tools?.length ?? 0)
</script>

<style scoped>
.mcp-tools-dialog__header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-right: 24px;
}

.mcp-tools-dialog__header-icon {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.mcp-tools-dialog__header-icon--stdio {
  background: linear-gradient(135deg, #606266, #909399);
}

.mcp-tools-dialog__header-icon--sse {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.mcp-tools-dialog__header-icon--http {
  background: linear-gradient(135deg, #0a2463, #409eff);
}

.mcp-tools-dialog__header-main {
  flex: 1;
  min-width: 0;
}

.mcp-tools-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.3;
  margin-bottom: 6px;
}

.mcp-tools-dialog__subtitle {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.mcp-tools-dialog__code {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-family: Consolas, Monaco, monospace;
}

.mcp-tools-dialog__count {
  flex-shrink: 0;
  text-align: center;
  min-width: 72px;
  padding: 8px 12px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--el-color-primary-light-8), var(--el-color-primary-light-9));
  border: 1px solid var(--el-color-primary-light-5);
}

.mcp-tools-dialog__count-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--el-color-primary);
}

.mcp-tools-dialog__count-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.mcp-tools-dialog__body {
  min-height: 220px;
  max-height: 58vh;
  overflow-y: auto;
}

.mcp-tools-dialog__desc {
  margin: 0 0 16px;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border-left: 3px solid var(--el-color-primary);
}

.mcp-tools-dialog__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px 16px;
  text-align: center;
}

.mcp-tools-dialog__error-icon {
  font-size: 48px;
  color: var(--el-color-danger);
}

.mcp-tools-dialog__error-text {
  max-width: 520px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  word-break: break-word;
}

.mcp-tools-dialog__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mcp-tools-dialog__collapse {
  border: none;
}

.mcp-tools-dialog__collapse :deep(.el-collapse-item) {
  margin-bottom: 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  overflow: hidden;
}

.mcp-tools-dialog__collapse :deep(.el-collapse-item__header) {
  height: auto;
  min-height: 48px;
  padding: 10px 14px;
  line-height: 1.4;
  background: var(--el-fill-color-blank);
  border-bottom: none;
}

.mcp-tools-dialog__collapse :deep(.el-collapse-item__wrap) {
  border-top: 1px solid var(--el-border-color-lighter);
}

.mcp-tools-dialog__collapse :deep(.el-collapse-item__content) {
  padding: 12px 14px 16px;
}

.mcp-tool-collapse__title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  width: 100%;
  padding-right: 8px;
}

.mcp-tool-collapse__icon {
  color: var(--el-color-primary);
  font-size: 16px;
}

.mcp-tool-collapse__name {
  font-size: 14px;
  font-weight: 600;
  font-family: Consolas, Monaco, monospace;
  color: var(--el-text-color-primary);
}

.mcp-tool-collapse__title-tag {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mcp-tool-collapse__desc {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

.mcp-tool-collapse__section {
  margin-top: 12px;
}

.mcp-tool-collapse__section-label {
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.mcp-tool-collapse__table {
  width: 100%;
}

.mcp-tool-collapse__code {
  font-size: 12px;
  color: var(--el-color-primary);
}

.mcp-tool-collapse__optional {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.mcp-tool-collapse__constraint {
  display: inline-block;
  margin-right: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.mcp-tool-collapse__enum {
  margin: 2px 4px 2px 0;
}

.mcp-tool-collapse__no-params {
  margin-top: 4px;
}

.mcp-tool-collapse__schema {
  margin: 0;
  padding: 12px;
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
  font-family: Consolas, Monaco, monospace;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 640px) {
  .mcp-tools-dialog__grid {
    grid-template-columns: 1fr;
  }
}

.mcp-tool-item {
  position: relative;
  display: flex;
  gap: 10px;
  padding: 14px 14px 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.15s;
}

.mcp-tool-item:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.12);
  transform: translateY(-1px);
}

.mcp-tool-item__badge {
  position: absolute;
  top: 8px;
  right: 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-placeholder);
}

.mcp-tool-item__icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.mcp-tool-item__content {
  flex: 1;
  min-width: 0;
  padding-right: 20px;
}

.mcp-tool-item__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
  word-break: break-all;
}

.mcp-tool-item__desc {
  font-size: 12px;
  line-height: 1.55;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mcp-tools-dialog__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 180px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.mcp-tools-dialog__placeholder-icon {
  color: var(--el-color-primary);
  animation: mcp-tools-spin 1.2s linear infinite;
}

.mcp-tools-dialog__refresh-icon {
  margin-right: 4px;
}

@keyframes mcp-tools-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

<style>
.mcp-tools-dialog .el-dialog__header {
  margin-right: 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.mcp-tools-dialog .el-dialog__body {
  padding-top: 16px;
}
</style>
