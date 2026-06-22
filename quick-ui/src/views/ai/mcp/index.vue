<template>
  <div class="app-container">
    <C7CardGrid
      ref="gridRef"
      row-key="mcpId"
      :list-function="listFunction"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :show-toolbar="false"
      :show-add-card="true"
      :add-card-permi="['ai:mcp:add']"
      add-card-text="新增 MCP"
      :on-add="openAdd"
      :page-sizes="[12, 24, 48]"
      :default-page-size="12"
      empty-text="暂无 MCP 服务"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #card="{ row, refreshData }">
        <McpServerCard
          :item="row"
          @open="openTools"
          @test="handleTest"
          @edit="openEdit"
          @delete="(item) => handleDeleteRow(item, refreshData)"
        />
      </template>
    </C7CardGrid>

    <c7-dialog v-model="visible" :title="form.mcpId ? '编辑 MCP' : '新增 MCP'" :on-confirm="submit" width="720px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="编码" prop="code">
              <el-input v-model="form.code" maxlength="64" :disabled="!!form.mcpId" placeholder="导出 mcp.json 的 key" />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="传输方式" prop="transport">
              <el-select v-model="form.transport" style="width: 100%">
                <el-option label="STDIO（本地进程）" value="STDIO" />
                <el-option label="SSE（远程）" value="SSE" />
                <el-option label="Streamable HTTP" value="STREAMABLE_HTTP" />
              </el-select>
            </el-form-item>
            <el-form-item label="超时(ms)" prop="requestTimeoutMs">
              <el-input-number v-model="form.requestTimeoutMs" :min="1000" :max="300000" :step="1000" controls-position="right" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">正常</el-radio>
                <el-radio :label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="连接" name="conn">
          <template v-if="form.transport === 'STDIO'">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              title="Windows 下 npx 需使用 cmd.exe + /c 包装，详见 Spring AI MCP 文档。"
              class="ai-mcp-page__tip"
            />
            <el-form :model="form" label-width="110px">
              <el-form-item label="命令" required>
                <el-input v-model="form.command" placeholder="npx 或 cmd.exe" />
              </el-form-item>
              <el-form-item label="参数">
                <div class="ai-mcp-page__args">
                  <div v-for="(arg, idx) in form.args" :key="idx" class="ai-mcp-page__arg-row">
                    <el-input v-model="form.args[idx]" placeholder="参数" />
                    <el-button link type="danger" @click="removeArg(idx)">删除</el-button>
                  </div>
                  <el-button link type="primary" @click="addArg">+ 添加参数</el-button>
                </div>
              </el-form-item>
            </el-form>
          </template>
          <template v-else>
            <el-form :model="form" label-width="110px">
              <el-form-item label="URL" required>
                <el-input
                  v-model="form.url"
                  :placeholder="form.transport === 'STREAMABLE_HTTP'
                    ? 'https://mcp.api-inference.modelscope.net/xxx/mcp'
                    : 'https://mcp.example.com/sse'"
                />
                <div v-if="form.transport === 'STREAMABLE_HTTP'" class="ai-mcp-page__url-tip">
                  ModelScope 等托管 MCP 请使用 Streamable HTTP，URL 通常以 /mcp 结尾。
                </div>
              </el-form-item>
              <el-form-item label="请求头">
                <div class="ai-mcp-page__args">
                  <div v-for="(h, idx) in form.headers" :key="idx" class="ai-mcp-page__header-row">
                    <el-input v-model="h.headerKey" placeholder="Header" style="width: 160px" />
                    <el-select v-model="h.valueType" style="width: 120px">
                      <el-option label="明文" value="PLAIN" />
                      <el-option label="密钥" value="SECRET" />
                      <el-option label="环境变量" value="ENV_REF" />
                    </el-select>
                    <el-input v-model="h.headerValue" placeholder="值" />
                    <el-button link type="danger" @click="removeHeader(idx)">删除</el-button>
                  </div>
                  <el-button link type="primary" @click="addHeader">+ 添加请求头</el-button>
                </div>
              </el-form-item>
            </el-form>
          </template>
        </el-tab-pane>

        <el-tab-pane label="环境变量" name="env">
          <div class="ai-mcp-page__args">
            <div v-for="(env, idx) in form.envs" :key="idx" class="ai-mcp-page__env-row">
              <el-input v-model="env.envKey" placeholder="变量名" style="width: 160px" />
              <el-select v-model="env.valueType" style="width: 120px">
                <el-option label="明文" value="PLAIN" />
                <el-option label="密钥" value="SECRET" />
                <el-option label="环境变量" value="ENV_REF" />
              </el-select>
              <el-input v-model="env.envValue" :placeholder="env.valueType === 'SECRET' && form.mcpId ? '留空表示不修改' : '值'" />
              <el-button link type="danger" @click="removeEnv(idx)">删除</el-button>
            </div>
            <el-button link type="primary" @click="addEnv">+ 添加环境变量</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </c7-dialog>

    <McpToolsDrawer
      v-model="toolsVisible"
      :mcp="selectedMcp"
      :loading="toolsLoading"
      :result="toolsResult"
      @refresh="reloadTools"
    />

    <el-dialog v-model="testVisible" title="连接测试结果" width="720px">
      <div v-if="testResult">
        <el-alert :type="testResult.success ? 'success' : 'error'" :title="testResult.message" show-icon :closable="false" />
        <p v-if="testResult.toolCount != null" class="ai-mcp-page__test-count">工具数量：{{ testResult.toolCount }}</p>
        <el-collapse v-if="testResult.tools?.length" class="ai-mcp-page__test-collapse">
          <el-collapse-item
            v-for="(tool, idx) in testResult.tools"
            :key="tool.name || idx"
            :title="`${tool.name}（${tool.parameters?.length || 0} 个参数）`"
          >
            <p v-if="tool.description" class="ai-mcp-page__test-tool-desc">{{ tool.description }}</p>
            <el-table v-if="tool.parameters?.length" :data="tool.parameters" size="small" border>
              <el-table-column prop="name" label="参数" width="120" />
              <el-table-column prop="type" label="类型" width="90" />
              <el-table-column prop="required" label="必填" width="60">
                <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
              </el-table-column>
              <el-table-column prop="description" label="说明" show-overflow-tooltip />
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { c7Confirm } from '@/packages/C7MessageBox/index.js'
import {
  addMcp,
  getMcpInfo,
  listMcp,
  listMcpTools,
  removeMcp,
  testMcp,
  updateMcp
} from '@/api/ai/mcp'
import McpServerCard from './components/McpServerCard.vue'
import McpToolsDrawer from './components/McpToolsDrawer.vue'

defineOptions({ name: 'AiMcp' })

const gridRef = ref(null)
const visible = ref(false)
const activeTab = ref('basic')
const formRef = ref(null)
const testVisible = ref(false)
const testResult = ref(null)
const toolsVisible = ref(false)
const toolsLoading = ref(false)
const toolsResult = ref(null)
const selectedMcp = ref(null)

const defaultSearchParam = { name: '', transport: '' }

const searchColumns = [
  { prop: 'name', label: '名称', type: 'input' },
  {
    prop: 'transport',
    label: '类型',
    type: 'select',
    options: [
      { label: 'STDIO（本地）', value: 'STDIO' },
      { label: 'SSE（联网）', value: 'SSE' },
      { label: 'Streamable HTTP', value: 'STREAMABLE_HTTP' }
    ]
  }
]

const emptyForm = () => ({
  mcpId: null,
  name: '',
  code: '',
  description: '',
  transport: 'STDIO',
  command: 'npx',
  args: ['--yes'],
  url: '',
  headers: [],
  requestTimeoutMs: 30000,
  status: 0,
  envs: []
})

const form = ref(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  transport: [{ required: true, message: '请选择传输方式', trigger: 'change' }]
}

function listFunction(params) {
  return listMcp(params)
}

async function handleDeleteRow(row, refreshData) {
  try {
    await c7Confirm(`确认删除 ${row.name} 吗？`)
    await removeMcp([row.mcpId])
    ElMessage.success('删除成功')
    if (typeof refreshData === 'function') {
      await refreshData()
    } else {
      gridRef.value?.refreshData()
    }
  } catch {
    /* 用户取消 */
  }
}

function refreshGridAfterMutation() {
  if (gridRef.value?.getDataList) {
    return gridRef.value.getDataList()
  }
  return gridRef.value?.refreshData?.()
}

function openAdd() {
  form.value = emptyForm()
  activeTab.value = 'basic'
  visible.value = true
}

function openEdit(row) {
  getMcpInfo(row.mcpId, false).then((res) => {
    const d = res?.data || {}
    form.value = {
      mcpId: d.mcpId,
      name: d.name || '',
      code: d.code || '',
      description: d.description || '',
      transport: d.transport || 'STDIO',
      command: d.command || '',
      args: Array.isArray(d.args) && d.args.length ? [...d.args] : [],
      url: d.url || '',
      headers: Array.isArray(d.headers) ? d.headers.map((h) => ({ ...h })) : [],
      requestTimeoutMs: d.requestTimeoutMs ?? 30000,
      status: d.status ?? 0,
      envs: Array.isArray(d.envs) ? d.envs.map((e) => ({ ...e })) : []
    }
    activeTab.value = 'basic'
    visible.value = true
  })
}

function addArg() {
  form.value.args.push('')
}

function removeArg(idx) {
  form.value.args.splice(idx, 1)
}

function addHeader() {
  form.value.headers.push({ headerKey: '', valueType: 'PLAIN', headerValue: '' })
}

function removeHeader(idx) {
  form.value.headers.splice(idx, 1)
}

function addEnv() {
  form.value.envs.push({ envKey: '', valueType: 'PLAIN', envValue: '', sortOrder: form.value.envs.length })
}

function removeEnv(idx) {
  form.value.envs.splice(idx, 1)
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('validate'))
      const payload = { ...form.value }
      if (payload.transport === 'STDIO') {
        payload.args = (payload.args || []).map((s) => String(s || '').trim()).filter(Boolean)
        payload.url = ''
        payload.headers = []
      } else {
        payload.command = ''
        payload.args = []
      }
      const req = payload.mcpId ? updateMcp(payload) : addMcp(payload)
      req
        .then(() => {
          ElMessage.success(payload.mcpId ? '修改成功' : '新增成功')
          visible.value = false
          refreshGridAfterMutation()
          resolve()
        })
        .catch(reject)
    })
  })
}

function handleTest(row) {
  testMcp(row.mcpId).then((res) => {
    testResult.value = res?.data || {}
    testVisible.value = true
    gridRef.value?.refreshData()
  })
}

function openTools(row) {
  selectedMcp.value = { ...row }
  toolsResult.value = null
  toolsVisible.value = true
  reloadTools()
}

async function reloadTools() {
  if (!selectedMcp.value?.mcpId) return
  toolsLoading.value = true
  try {
    const res = await listMcpTools(selectedMcp.value.mcpId)
    toolsResult.value = res?.data || { success: false, message: '无响应数据' }
    gridRef.value?.refreshData()
  } finally {
    toolsLoading.value = false
  }
}
</script>

<style scoped>
.ai-mcp-page__tip {
  margin-bottom: 12px;
}

.ai-mcp-page__url-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.ai-mcp-page__args {
  width: 100%;
}

.ai-mcp-page__arg-row,
.ai-mcp-page__header-row,
.ai-mcp-page__env-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.ai-mcp-page__test-count {
  margin: 12px 0 8px;
  font-size: 13px;
}

.ai-mcp-page__test-collapse {
  margin-top: 8px;
  border: none;
}

.ai-mcp-page__test-tool-desc {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
