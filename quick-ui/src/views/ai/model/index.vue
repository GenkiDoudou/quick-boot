<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="modelId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['ai:model:add']"
      :show-edit-button="true"
      :edit-button-permi="['ai:model:edit']"
      :show-delete-button="true"
      :delete-button-permi="['ai:model:remove']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left>
        <el-button plain @click="handleImportYaml" v-hasPermi="['ai:model:add']">从 YAML 导入草稿</el-button>
        <el-button plain @click="handleExportSelected" v-hasPermi="['ai:model:export']">导出所选</el-button>
      </template>

      <template #modelType="{ row }">
        <el-tag size="small" :type="modelTypeTagType(row.modelType)">{{ modelTypeLabel(row.modelType) }}</el-tag>
      </template>

      <template #provider="{ row }">
        <el-tag size="small">{{ providerLabel(row.provider) }}</el-tag>
      </template>

      <template #defaultSlot="{ row }">
        <el-tag v-if="row.defaultSlot" size="small" type="success">{{ row.defaultSlot }}</el-tag>
        <span v-else>—</span>
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
        <el-button link type="primary" @click="handleTest(row)" v-hasPermi="['ai:model:test']">测试</el-button>
        <el-dropdown v-if="canSetDefault(row)" trigger="click" @command="(slot) => handleSetDefault(row, slot)" v-hasPermi="['ai:model:edit']">
          <el-button link>设默认</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="isLanguageModel(row.modelType)" command="CHAT">语言模型默认</el-dropdown-item>
              <el-dropdown-item v-if="isLanguageModel(row.modelType)" command="WORKFLOW_CHAT">工作流语言模型默认</el-dropdown-item>
              <el-dropdown-item v-if="isVectorModel(row.modelType)" command="EMBEDDING">向量模型默认</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button link @click="openEdit(row)" v-hasPermi="['ai:model:edit']">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除 ${row.name} 吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['ai:model:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.modelId ? '编辑大模型' : '新增大模型'" :on-confirm="submit" width="760px">
      <template #extra>
        <el-button
          plain
          :loading="formTestLoading"
          @click="handleFormTest"
          v-hasPermi="['ai:model:test']"
        >
          测试连接
        </el-button>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="模型配置" name="main">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="编码" prop="code">
              <el-input v-model="form.code" maxlength="64" :disabled="!!form.modelId" placeholder="唯一标识，如 deepseek-chat-prod" />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="模型类型" prop="modelType">
              <el-select v-model="form.modelType" style="width: 100%" :disabled="!!form.modelId">
                <el-option v-for="item in AI_MODEL_TYPES" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="厂商" prop="provider">
              <el-select v-model="form.provider" style="width: 100%" @change="onProviderChange">
                <el-option v-for="item in AI_PROVIDER_PRESETS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="Base URL" required>
              <el-input v-model="form.baseUrl" placeholder="选择厂商后自动填充，可手动修改" />
            </el-form-item>
            <el-form-item label="模型名" required>
              <el-input v-model="form.modelName" :placeholder="modelNamePlaceholder" />
            </el-form-item>
            <el-form-item label="API Key 类型">
              <el-select v-model="form.apiKeyType" style="width: 100%">
                <el-option label="明文" value="PLAIN" />
                <el-option label="密钥（SM4）" value="SECRET" />
                <el-option label="环境变量引用" value="ENV_REF" />
              </el-select>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input
                v-model="form.apiKey"
                :placeholder="apiKeyPlaceholder"
                show-password
              />
            </el-form-item>
            <el-form-item label="超时(ms)">
              <el-input-number v-model="form.requestTimeoutMs" :min="1000" :max="600000" :step="1000" controls-position="right" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">正常</el-radio>
                <el-radio :label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="高级" name="adv">
          <el-form :model="form" label-width="120px">
            <el-form-item v-if="isLanguageModel(form.modelType)" label="Completions 路径">
              <el-input v-model="form.completionsPath" placeholder="DeepSeek 默认 /chat/completions" />
            </el-form-item>
            <el-form-item v-if="isVectorModel(form.modelType)" label="Embeddings 路径">
              <el-input v-model="form.embeddingsPath" placeholder="默认 /v1/embeddings" />
            </el-form-item>
            <el-form-item v-if="isVectorModel(form.modelType)" label="向量维度">
              <el-input-number v-model="form.dimensions" :min="1" :max="8192" controls-position="right" />
            </el-form-item>
            <el-form-item v-if="isLanguageModel(form.modelType)" label="温度">
              <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" :precision="1" controls-position="right" />
            </el-form-item>
            <el-form-item v-if="isLanguageModel(form.modelType)" label="最大 Token">
              <el-input-number v-model="form.maxTokens" :min="1" :max="128000" controls-position="right" />
            </el-form-item>
            <el-alert
              v-if="form.modelType === 'IMAGE'"
              type="info"
              :closable="false"
              show-icon
              title="图像模型配置可保存，连接测试与运行时调用能力后续版本接入。"
            />
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </c7-dialog>

    <el-dialog v-model="testVisible" title="连接测试结果" width="560px">
      <div v-if="testResult">
        <el-alert :type="testResult.success ? 'success' : 'error'" :title="testResult.message" show-icon :closable="false" />
        <p v-if="testResult.actualDimensions != null" class="ai-model-page__test-extra">实测维度：{{ testResult.actualDimensions }}</p>
        <p v-if="testResult.replyPreview" class="ai-model-page__test-extra">回复摘要：{{ testResult.replyPreview }}</p>
      </div>
    </el-dialog>

    <el-dialog v-model="importVisible" title="YAML 导入草稿" width="720px">
      <el-alert type="info" :closable="false" show-icon title="以下为从当前 spring.ai 配置生成的草稿，确认后可逐条编辑并保存。" class="ai-model-page__import-tip" />
      <el-table :data="importDrafts" size="small" max-height="400">
        <el-table-column prop="name" label="名称" width="140" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="modelType" label="类型" width="100">
          <template #default="{ row }">{{ modelTypeLabel(row.modelType) }}</template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型名" min-width="120" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click="applyImportDraft(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addModel,
  exportModel,
  getModelInfo,
  importModelFromYaml,
  listModel,
  removeModel,
  setModelDefault,
  testModel,
  updateModel
} from '@/api/ai/model'
import {
  AI_MODEL_TYPES,
  AI_PROVIDER_PRESETS,
  applyProviderPreset,
  findProviderPreset,
  isLanguageModel,
  isVectorModel,
  modelTypeLabel,
  providerLabel
} from '@/constants/aiModel'

defineOptions({ name: 'AiModel' })

const tableRef = ref(null)
const visible = ref(false)
const activeTab = ref('main')
const formRef = ref(null)
const testVisible = ref(false)
const testResult = ref(null)
const importVisible = ref(false)
const importDrafts = ref([])
const formTestLoading = ref(false)

const defaultSearchParam = { name: '', code: '', modelType: '', provider: '', status: null, defaultSlot: '' }

const searchColumns = [
  { prop: 'name', label: '名称', type: 'input' },
  { prop: 'code', label: '编码', type: 'input' },
  {
    prop: 'modelType',
    label: '类型',
    type: 'select',
    options: AI_MODEL_TYPES.map((t) => ({ label: t.label, value: t.value }))
  },
  {
    prop: 'provider',
    label: '厂商',
    type: 'select',
    options: AI_PROVIDER_PRESETS.map((p) => ({ label: p.label, value: p.value }))
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '正常', value: 0 },
      { label: '停用', value: 1 }
    ]
  },
  {
    prop: 'defaultSlot',
    label: '默认槽位',
    type: 'select',
    options: [
      { label: 'CHAT', value: 'CHAT' },
      { label: 'EMBEDDING', value: 'EMBEDDING' },
      { label: 'WORKFLOW_CHAT', value: 'WORKFLOW_CHAT' }
    ]
  }
]

const tableColumns = [
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'code', label: '编码', minWidth: 120 },
  { prop: 'modelType', label: '类型', width: 110, columnType: 'slot', slotName: 'modelType' },
  { prop: 'provider', label: '厂商', width: 120, columnType: 'slot', slotName: 'provider' },
  { prop: 'modelName', label: '模型名', minWidth: 140 },
  { prop: 'defaultSlot', label: '全局默认', width: 130, columnType: 'slot', slotName: 'defaultSlot' },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'lastTestStatus', label: '最近测试', width: 110, columnType: 'slot', slotName: 'lastTestStatus' },
  { prop: 'updateTime', label: '更新时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 260, fixed: 'right' }
]

const emptyForm = () => {
  const form = {
    modelId: null,
    name: '',
    code: '',
    description: '',
    modelType: 'LANGUAGE',
    provider: 'DEEPSEEK',
    baseUrl: '',
    apiKeyType: 'SECRET',
    apiKey: '',
    modelName: '',
    completionsPath: '',
    embeddingsPath: '',
    dimensions: null,
    temperature: 0.7,
    maxTokens: 4096,
    requestTimeoutMs: 60000,
    status: 0
  }
  applyProviderPreset(form, form.provider)
  return form
}

const form = ref(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
  provider: [{ required: true, message: '请选择厂商', trigger: 'change' }]
}

const modelNamePlaceholder = computed(() => findProviderPreset(form.value.provider)?.modelNameHint || '请输入厂商模型名')

const apiKeyPlaceholder = computed(() => {
  if (form.value.apiKeyType === 'SECRET' && form.value.modelId) return '留空表示不修改'
  if (findProviderPreset(form.value.provider)?.apiKeyOptional) return 'Ollama 本地可留空'
  return 'sk-... 或 ${OPENAI_API_KEY}'
})

function modelTypeTagType(modelType) {
  if (isLanguageModel(modelType)) return 'primary'
  if (isVectorModel(modelType)) return 'warning'
  if (modelType === 'IMAGE') return 'success'
  return 'info'
}

function testTagType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'info'
}

function canSetDefault(row) {
  return row.status === 0 && (isLanguageModel(row.modelType) || isVectorModel(row.modelType))
}

function onProviderChange(provider) {
  applyProviderPreset(form.value, provider)
}

function listFunction(params) {
  return listModel(params)
}

function batchDeleteFunction(ids) {
  return removeModel(ids)
}

function removeRow(row) {
  return removeModel([row.modelId]).then(() => {
    tableRef.value?.refreshData()
  })
}

/** 保存后刷新列表；新增时回到第一页以便看到新记录 */
function refreshTableAfterMutation() {
  if (tableRef.value?.getDataList) {
    return tableRef.value.getDataList()
  }
  return tableRef.value?.refreshData?.()
}

function openAdd() {
  form.value = emptyForm()
  activeTab.value = 'main'
  visible.value = true
}

function openEdit(row) {
  getModelInfo(row.modelId, false).then((res) => {
    const d = res?.data || {}
    form.value = {
      modelId: d.modelId,
      name: d.name || '',
      code: d.code || '',
      description: d.description || '',
      modelType: normalizeModelTypeForForm(d.modelType),
      provider: normalizeProviderForForm(d.provider),
      baseUrl: d.baseUrl || '',
      apiKeyType: d.apiKeyType || 'SECRET',
      apiKey: '',
      modelName: d.modelName || '',
      completionsPath: d.completionsPath || '',
      embeddingsPath: d.embeddingsPath || '',
      dimensions: d.dimensions ?? null,
      temperature: d.temperature ?? 0.7,
      maxTokens: d.maxTokens ?? 4096,
      requestTimeoutMs: d.requestTimeoutMs ?? 60000,
      status: d.status ?? 0
    }
    activeTab.value = 'main'
    visible.value = true
  })
}

/** 旧 CHAT/EMBEDDING 映射到新表单枚举 */
function normalizeModelTypeForForm(modelType) {
  if (modelType === 'CHAT') return 'LANGUAGE'
  if (modelType === 'EMBEDDING') return 'VECTOR'
  return modelType || 'LANGUAGE'
}

/** 旧 OPENAI_COMPAT 映射为 OpenAI */
function normalizeProviderForForm(provider) {
  if (provider === 'OPENAI_COMPAT') return 'OPENAI'
  return provider || 'DEEPSEEK'
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('validate'))
      if (!form.value.baseUrl?.trim()) {
        ElMessage.warning('请填写 Base URL')
        return reject(new Error('baseUrl'))
      }
      if (!form.value.modelName?.trim()) {
        ElMessage.warning('请填写模型名')
        return reject(new Error('modelName'))
      }
      const payload = { ...form.value }
      const req = payload.modelId ? updateModel(payload) : addModel(payload)
      req
        .then(() => {
          ElMessage.success(payload.modelId ? '修改成功' : '新增成功')
          visible.value = false
          refreshTableAfterMutation()
          resolve()
        })
        .catch(reject)
    })
  })
}

function handleTest(row) {
  testModel(row.modelId).then((res) => {
    testResult.value = res?.data || {}
    testVisible.value = true
    tableRef.value?.refreshData()
  })
}

function handleSetDefault(row, defaultSlot) {
  setModelDefault({ modelId: row.modelId, defaultSlot }).then(() => {
    ElMessage.success(`已设为 ${defaultSlot} 默认`)
    tableRef.value?.refreshData()
  })
}

/** 校验连接测试所需字段（不要求 API Key，Ollama 等可无密钥） */
function validateConnectionFields() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('validate'))
      if (!form.value.baseUrl?.trim()) {
        ElMessage.warning('请填写 Base URL')
        return reject(new Error('baseUrl'))
      }
      if (!form.value.modelName?.trim()) {
        ElMessage.warning('请填写模型名')
        return reject(new Error('modelName'))
      }
      resolve()
    })
  })
}

/** 构建保存 payload（与 submit 一致） */
function buildSavePayload() {
  return { ...form.value }
}

/**
 * 弹窗内测试连接：未保存时先落库再测；已保存则先更新再测，确保使用最新配置。
 */
async function handleFormTest() {
  if (form.value.modelType === 'IMAGE') {
    ElMessage.info('图像模型连接测试暂未实现')
    return
  }
  try {
    await validateConnectionFields()
  } catch {
    return
  }
  formTestLoading.value = true
  try {
    const payload = buildSavePayload()
    if (payload.modelId) {
      await updateModel(payload)
    } else {
      await addModel(payload)
      const listRes = await listModel({ code: payload.code, pageNum: 1, pageSize: 1 })
      const saved = listRes?.data?.records?.[0]
      if (!saved?.modelId) {
        ElMessage.error('保存成功但未找到模型记录，请刷新后重试')
        return
      }
      form.value.modelId = saved.modelId
    }
    const res = await testModel(form.value.modelId)
    testResult.value = res?.data || {}
    testVisible.value = true
    refreshTableAfterMutation()
  } finally {
    formTestLoading.value = false
  }
}

function handleExportSelected() {
  const rows = tableRef.value?.getSelectionRows?.() || []
  const ids = rows.map((r) => r.modelId).join(',')
  if (!ids) {
    ElMessage.warning('请先勾选要导出的模型')
    return
  }
  exportModel(ids, 'yaml', false).then((res) => {
    const text = res?.data || ''
    navigator.clipboard?.writeText(text).then(() => {
      ElMessage.success('已复制 YAML 片段到剪贴板')
    }).catch(() => {
      ElMessage.success('导出成功，请从网络响应查看内容')
    })
  })
}

function handleImportYaml() {
  importModelFromYaml().then((res) => {
    importDrafts.value = res?.data || []
    importVisible.value = true
    if (!importDrafts.value.length) {
      ElMessage.info('未检测到可导入的 spring.ai 配置')
    }
  })
}

function applyImportDraft(row) {
  form.value = {
    ...emptyForm(),
    name: row.name || '',
    code: row.code || '',
    description: row.description || '',
    modelType: normalizeModelTypeForForm(row.modelType),
    provider: normalizeProviderForForm(row.provider),
    baseUrl: row.baseUrl || '',
    apiKeyType: row.apiKeyType || 'ENV_REF',
    apiKey: row.apiKey || '',
    modelName: row.modelName || '',
    completionsPath: row.completionsPath || '',
    embeddingsPath: row.embeddingsPath || '',
    dimensions: row.dimensions ?? null,
    temperature: row.temperature ?? 0.7,
    maxTokens: row.maxTokens ?? 4096,
    requestTimeoutMs: row.requestTimeoutMs ?? 60000,
    status: 0
  }
  if (!form.value.baseUrl) {
    applyProviderPreset(form.value, form.value.provider)
  }
  importVisible.value = false
  activeTab.value = 'main'
  visible.value = true
}
</script>

<style scoped>
.ai-model-page__test-extra {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.ai-model-page__import-tip {
  margin-bottom: 12px;
}
</style>
