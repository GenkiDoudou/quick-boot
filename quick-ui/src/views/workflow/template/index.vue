<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="templateId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['workflow:template:add']"
      :show-edit-button="true"
      :edit-button-permi="['workflow:template:edit']"
      :show-delete-button="true"
      :delete-button-permi="['workflow:template:remove']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left>
        <el-button type="primary" plain @click="openImport" v-hasPermi="['workflow:template:add']">
          从工作流导入
        </el-button>
      </template>

      <template #status="{ row }">
        <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
          {{ row.status === 'ENABLED' ? '启用' : '停用' }}
        </el-tag>
      </template>

      <template #builtin="{ row }">
        <el-tag :type="row.builtin === 1 ? 'warning' : ''">{{ row.builtin === 1 ? '内置' : '自定义' }}</el-tag>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="goDesign(row)" v-hasPermi="['workflow:template:edit']">设计</el-button>
        <el-button link @click="openEdit(row)" v-hasPermi="['workflow:template:edit']">编辑</el-button>
        <c7-button
          v-if="row.builtin !== 1 || isAdmin"
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除模板「${row.name}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['workflow:template:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog
      v-model="visible"
      :title="form.templateId ? '编辑模板' : '新增模板'"
      :on-confirm="submit"
      width="860px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板编码" prop="code">
          <el-input
            v-model="form.code"
            :disabled="!!form.templateId && form.builtin === 1"
            maxlength="64"
            placeholder="小写字母、数字、连字符，如 loop-count-test"
          />
        </el-form-item>
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" maxlength="128" show-word-limit placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            maxlength="512"
            show-word-limit
            placeholder="简要说明用途"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ENABLED">启用</el-radio>
            <el-radio value="DISABLED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="!form.templateId && isAdmin" label="模板类型" prop="builtin">
          <el-radio-group v-model="form.builtin">
            <el-radio :value="0">自定义</el-radio>
            <el-radio :value="1">内置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-else-if="form.templateId" label="模板类型">
          <el-tag :type="form.builtin === 1 ? 'warning' : ''">{{ form.builtin === 1 ? '内置' : '自定义' }}</el-tag>
        </el-form-item>
        <el-form-item label="图 JSON" prop="graphJson">
          <div class="wf-template-page__graph-wrap">
            <el-input
              v-model="form.graphJson"
              type="textarea"
              :rows="14"
              placeholder="留空则使用最小 start/end 图；也可通过「设计」按钮可视化编辑"
              class="wf-template-page__graph-input"
            />
            <div class="wf-template-page__graph-actions">
              <el-button :loading="validating" @click="handleValidateGraph" v-hasPermi="['workflow:template:edit']">
                校验 JSON
              </el-button>
              <el-button v-if="form.templateId" type="primary" plain @click="goDesign(form)" v-hasPermi="['workflow:template:edit']">
                打开设计器
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </c7-dialog>

    <c7-dialog v-model="importVisible" title="从工作流导入模板" :on-confirm="submitImport" width="520px">
      <el-form ref="importFormRef" :model="importForm" :rules="importRules" label-width="100px">
        <el-form-item label="工作流" prop="workflowId">
          <el-select
            v-model="importForm.workflowId"
            filterable
            placeholder="选择来源工作流"
            style="width: 100%"
            :loading="workflowLoading"
          >
            <el-option
              v-for="item in workflowOptions"
              :key="item.workflowId"
              :label="item.name"
              :value="item.workflowId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="importForm.code" maxlength="64" placeholder="留空则自动生成" />
        </el-form-item>
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="importForm.name" maxlength="128" placeholder="留空则自动生成" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="importForm.description" type="textarea" :rows="2" maxlength="512" />
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import auth from '@/plugins/auth'
import { listWorkflow } from '@/api/workflow'
import {
  addWorkflowTemplate,
  getWorkflowTemplateInfo,
  importWorkflowTemplateFromWorkflow,
  pageWorkflowTemplate,
  removeWorkflowTemplate,
  updateWorkflowTemplate,
  validateWorkflowTemplateGraph
} from '@/api/workflow/template'

defineOptions({ name: 'WfWorkflowTemplate' })

const router = useRouter()
const isAdmin = computed(() => auth.hasRole('admin'))
const tableRef = ref(null)
const formRef = ref(null)
const importFormRef = ref(null)
const visible = ref(false)
const importVisible = ref(false)
const validating = ref(false)
const workflowLoading = ref(false)
const workflowOptions = ref([])

const defaultSearchParam = { pageNum: 1, pageSize: 10, name: '', code: '', status: '' }

const searchColumns = [
  { prop: 'name', label: '模板名称', type: 'input' },
  { prop: 'code', label: '模板编码', type: 'input' },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '启用', value: 'ENABLED' },
      { label: '停用', value: 'DISABLED' }
    ],
    props: { clearable: true, style: 'width: 160px' }
  }
]

const tableColumns = [
  { prop: 'name', label: '模板名称', minWidth: 160 },
  { prop: 'code', label: '模板编码', minWidth: 140 },
  { prop: 'builtin', label: '类型', columnType: 'slot', slotName: 'builtin', width: 90 },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 90 },
  { prop: 'sortOrder', label: '排序', width: 80 },
  { prop: 'description', label: '描述', minWidth: 200, showOverflowTooltip: true },
  { prop: 'updateTime', label: '更新时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 180, fixed: 'right' }
]

const emptyForm = () => ({
  templateId: null,
  code: '',
  name: '',
  description: '',
  status: 'ENABLED',
  sortOrder: 0,
  builtin: 0,
  graphJson: ''
})

const form = reactive(emptyForm())

const importForm = reactive({
  workflowId: null,
  code: '',
  name: '',
  description: ''
})

const rules = {
  code: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }]
}

const importRules = {
  workflowId: [{ required: true, message: '请选择工作流', trigger: 'change' }]
}

function listFunction(params) {
  return pageWorkflowTemplate(params)
}

function batchDeleteFunction(ids) {
  return removeWorkflowTemplate(ids)
}

function refreshTable() {
  return tableRef.value?.getDataList?.() || tableRef.value?.refreshData?.()
}

function parseGraphJson(text) {
  const trimmed = (text || '').trim()
  if (!trimmed) return null
  return JSON.parse(trimmed)
}

function openAdd() {
  Object.assign(form, emptyForm())
  visible.value = true
}

async function openEdit(row) {
  const res = await getWorkflowTemplateInfo(row.templateId)
  const data = res.data || {}
  Object.assign(form, emptyForm(), {
    templateId: data.templateId,
    code: data.code || '',
    name: data.name || '',
    description: data.description || '',
    status: data.status || 'ENABLED',
    sortOrder: data.sortOrder ?? 0,
    builtin: data.builtin ?? 0,
    graphJson: data.graph ? JSON.stringify(data.graph, null, 2) : ''
  })
  visible.value = true
}

async function submit() {
  await formRef.value?.validate?.()
  let graph = null
  if (form.graphJson?.trim()) {
    try {
      graph = parseGraphJson(form.graphJson)
    } catch {
      ElMessage.error('图 JSON 格式无效')
      throw new Error('invalid graph json')
    }
  }
  const payload = {
    templateId: form.templateId,
    code: form.code,
    name: form.name,
    description: form.description,
    status: form.status,
    sortOrder: form.sortOrder,
    graph
  }
  if (form.templateId) {
    await updateWorkflowTemplate(payload)
    ElMessage.success('修改成功')
  } else {
    await addWorkflowTemplate({
      ...payload,
      builtin: isAdmin.value ? form.builtin : 0
    })
    ElMessage.success('新增成功')
  }
  visible.value = false
  await refreshTable()
}

async function handleValidateGraph() {
  if (!form.graphJson?.trim()) {
    ElMessage.warning('请先输入图 JSON')
    return
  }
  validating.value = true
  try {
    const graph = parseGraphJson(form.graphJson)
    await validateWorkflowTemplateGraph({ graph })
    ElMessage.success('校验通过')
  } catch (err) {
    if (err instanceof SyntaxError) {
      ElMessage.error('JSON 格式无效')
    }
  } finally {
    validating.value = false
  }
}

async function removeRow(row) {
  await removeWorkflowTemplate([row.templateId])
  ElMessage.success('删除成功')
  await refreshTable()
}

function goDesign(row) {
  const id = row.templateId
  if (!id) return
  router.push({ path: `/workflow/design/template/${id}` })
}

async function loadWorkflowOptions() {
  workflowLoading.value = true
  try {
    const res = await listWorkflow({ pageNum: 1, pageSize: 200 })
    workflowOptions.value = res.data?.records || []
  } finally {
    workflowLoading.value = false
  }
}

function openImport() {
  importForm.workflowId = null
  importForm.code = ''
  importForm.name = ''
  importForm.description = ''
  importVisible.value = true
  if (!workflowOptions.value.length) {
    loadWorkflowOptions()
  }
}

async function submitImport() {
  await importFormRef.value?.validate?.()
  const payload = {
    workflowId: importForm.workflowId,
    template: {
      code: importForm.code || undefined,
      name: importForm.name || undefined,
      description: importForm.description || undefined
    }
  }
  const res = await importWorkflowTemplateFromWorkflow(payload)
  ElMessage.success('导入成功')
  importVisible.value = false
  await refreshTable()
  const templateId = res.data
  if (templateId) {
    router.push({ path: `/workflow/design/template/${templateId}` })
  }
}

onMounted(() => {
  loadWorkflowOptions()
})
</script>

<style scoped>
.wf-template-page__graph-wrap {
  width: 100%;
}

.wf-template-page__graph-input :deep(textarea) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 12px;
}

.wf-template-page__graph-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
