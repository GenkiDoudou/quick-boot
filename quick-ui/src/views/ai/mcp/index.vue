<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="mcpId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['ai:mcp:add']"
      :show-edit-button="true"
      :edit-button-permi="['ai:mcp:edit']"
      :show-delete-button="true"
      :delete-button-permi="['ai:mcp:remove']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left>
        <el-button plain @click="handleExportSelected" v-hasPermi="['ai:mcp:export']">导出所选</el-button>
      </template>

      <template #transport="{ row }">
        <el-tag size="small">{{ transportLabel(row.transport) }}</el-tag>
      </template>

      <template #status="{ row }">
        <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
      </template>

      <template #lastTestStatus="{ row }">
        <el-tag v-if="row.lastTestStatus" size="small" :type="testTagType(row.lastTestStatus)">
          {{ row.lastTestStatus }}
        </el-tag>
        <span v-else>—</span>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="handleTest(row)" v-hasPermi="['ai:mcp:test']">测试</el-button>
        <el-button link @click="openEdit(row)" v-hasPermi="['ai:mcp:edit']">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除 ${row.name} 吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['ai:mcp:remove']"
        />
      </template>
    </C7JsonTable>

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
                <el-input v-model="form.url" placeholder="https://mcp.example.com/sse" />
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

    <el-dialog v-model="testVisible" title="连接测试结果" width="560px">
      <div v-if="testResult">
        <el-alert :type="testResult.success ? 'success' : 'error'" :title="testResult.message" show-icon :closable="false" />
        <p v-if="testResult.toolCount != null" class="ai-mcp-page__test-count">工具数量：{{ testResult.toolCount }}</p>
        <el-table v-if="testResult.tools?.length" :data="testResult.tools" size="small" class="ai-mcp-page__test-table">
          <el-table-column prop="name" label="工具名" width="180" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addMcp,
  exportMcp,
  getMcpInfo,
  listMcp,
  removeMcp,
  testMcp,
  updateMcp
} from '@/api/ai/mcp'

defineOptions({ name: 'AiMcp' })

const tableRef = ref(null)
const visible = ref(false)
const activeTab = ref('basic')
const formRef = ref(null)
const testVisible = ref(false)
const testResult = ref(null)

const defaultSearchParam = { name: '', code: '', transport: '', status: null }

const searchColumns = [
  { prop: 'name', label: '名称', type: 'input' },
  { prop: 'code', label: '编码', type: 'input' },
  {
    prop: 'transport',
    label: '传输',
    type: 'select',
    options: [
      { label: 'STDIO', value: 'STDIO' },
      { label: 'SSE', value: 'SSE' },
      { label: 'STREAMABLE_HTTP', value: 'STREAMABLE_HTTP' }
    ]
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '正常', value: 0 },
      { label: '停用', value: 1 }
    ]
  }
]

const tableColumns = [
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'code', label: '编码', minWidth: 120 },
  { prop: 'transport', label: '传输', width: 130, columnType: 'slot', slotName: 'transport' },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'lastTestStatus', label: '最近测试', width: 110, columnType: 'slot', slotName: 'lastTestStatus' },
  { prop: 'updateTime', label: '更新时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 200, fixed: 'right' }
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

function transportLabel(t) {
  const map = { STDIO: 'STDIO', SSE: 'SSE', STREAMABLE_HTTP: 'HTTP' }
  return map[t] || t || '—'
}

function testTagType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'info'
}

function listFunction(params) {
  return listMcp(params)
}

function batchDeleteFunction(ids) {
  return removeMcp(ids)
}

function removeRow(row) {
  return removeMcp([row.mcpId]).then(() => {
    tableRef.value?.refreshData()
  })
}

function refreshTableAfterMutation() {
  if (tableRef.value?.getDataList) {
    return tableRef.value.getDataList()
  }
  return tableRef.value?.refreshData?.()
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
          refreshTableAfterMutation()
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
    tableRef.value?.refreshData()
  })
}

function handleExportSelected() {
  const rows = tableRef.value?.getSelectionRows?.() || []
  const ids = rows.map((r) => r.mcpId).join(',')
  if (!ids) {
    ElMessage.warning('请先勾选要导出的 MCP')
    return
  }
  exportMcp(ids, false).then((res) => {
    const text = JSON.stringify(res?.data || {}, null, 2)
    navigator.clipboard?.writeText(text).then(() => {
      ElMessage.success('已复制 mcp.json 片段到剪贴板')
    }).catch(() => {
      ElMessage.success('导出成功，请从网络响应查看 JSON')
    })
  })
}
</script>

<style scoped>
.ai-mcp-page__tip {
  margin-bottom: 12px;
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

.ai-mcp-page__test-table {
  margin-top: 8px;
}
</style>
