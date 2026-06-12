<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="workflowId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['workflow:add']"
      :show-edit-button="true"
      :edit-button-permi="['workflow:edit']"
      :show-delete-button="true"
      :delete-button-permi="['workflow:remove']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #status="{ row }">
        <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
      </template>

      <template #action="{ row }">
        <el-button link @click="openEdit(row)" v-hasPermi="['workflow:edit']">编辑</el-button>
        <el-button link type="primary" @click="goDesign(row)" v-hasPermi="['workflow:edit']">设计</el-button>
        <el-button link type="success" @click="handlePublish(row)" v-hasPermi="['workflow:publish']">发布</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除工作流「${row.name}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['workflow:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.workflowId ? '编辑工作流' : '新增工作流'" :on-confirm="submit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入工作流名称" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="Chat 模型">
          <el-select v-model="form.chatModelId" filterable clearable placeholder="留空则使用全局默认" style="width: 100%">
            <el-option
              v-for="item in chatModelOptions"
              :key="item.modelId"
              :label="`${item.name} (${item.code})${item.defaultSlot ? ' ★' : ''}`"
              :value="item.modelId"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listModelOptions } from '@/api/ai/model'
import { addWorkflow, getWorkflow, listWorkflow, publishWorkflow, removeWorkflow, updateWorkflow } from '@/api/workflow'

defineOptions({ name: 'WfWorkflowList' })

const router = useRouter()
const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)

const form = ref({
  workflowId: null,
  name: '',
  description: '',
  chatModelId: null
})

const chatModelOptions = ref([])

const defaultSearchParam = {
  name: '',
  status: ''
}

const searchColumns = computed(() => [
  { prop: 'name', label: '名称', type: 'input', span: 8, props: { placeholder: '请输入名称', clearable: true } },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: [
      { label: '草稿', value: 'DRAFT' },
      { label: '已发布', value: 'PUBLISHED' },
      { label: '已停用', value: 'DISABLED' }
    ],
    props: { placeholder: '请选择状态', clearable: true, style: 'width: 240px' }
  }
])

const tableColumns = computed(() => [
  { prop: 'name', label: '名称', minWidth: 160 },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 110 },
  { prop: 'description', label: '描述', minWidth: 220, showOverflowTooltip: true },
  { prop: 'updateTime', label: '更新时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 260, fixed: 'right' }
])

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

/** @param {string} status */
function statusLabel(status) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', DISABLED: '已停用' }
  return map[status] || status || '-'
}

/** @param {string} status */
function statusTagType(status) {
  const map = { DRAFT: 'info', PUBLISHED: 'success', DISABLED: 'danger' }
  return map[status] || 'info'
}

function listFunction(params) {
  return listWorkflow(params)
}

function openAdd() {
  form.value = { workflowId: null, name: '', description: '', chatModelId: null }
  visible.value = true
}

function openEdit(row) {
  if (!row) return
  getWorkflow(row.workflowId).then((res) => {
    const d = res?.data || row
    form.value = {
      workflowId: d.workflowId,
      name: d.name || '',
      description: d.description || '',
      chatModelId: d.chatModelId ?? null
    }
    visible.value = true
  })
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const req = form.value.workflowId ? updateWorkflow(form.value) : addWorkflow(form.value)
      req.then(() => {
        ElMessage.success('操作成功')
        visible.value = false
        tableRef.value?.refreshData()
        resolve()
      }).catch(reject)
    })
  })
}

function removeRow(row) {
  return removeWorkflow([row.workflowId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function batchDeleteFunction(ids) {
  return removeWorkflow(ids || []).then(() => {
    ElMessage.success('删除成功')
  })
}

/** @param {{ workflowId: number|string, name?: string }} row */
function goDesign(row) {
  router.push({ path: `/workflow/design/${row.workflowId}` })
}

/** @param {{ workflowId: number|string, name?: string }} row */
function handlePublish(row) {
  ElMessageBox.confirm(`确认发布工作流「${row.name}」吗？`, '发布确认', { type: 'warning' })
    .then(() => publishWorkflow({ workflowId: row.workflowId }))
    .then(() => {
      ElMessage.success('发布成功')
      tableRef.value?.refreshData()
    })
    .catch(() => {})
}

function loadChatModelOptions() {
  listModelOptions('LANGUAGE')
    .then((res) => {
      chatModelOptions.value = res?.data || []
    })
    .catch(() => {
      chatModelOptions.value = []
    })
}

onMounted(loadChatModelOptions)
</script>
