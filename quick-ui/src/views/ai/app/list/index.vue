<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="id"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['aiapp:add']"
      :show-edit-button="false"
      :show-delete-button="true"
      :delete-button-permi="['aiapp:remove']"
      :on-add="openAdd"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #appType="{ row }">
        <el-tag :type="row.appType === 'agent' ? 'primary' : 'warning'">
          {{ row.appType === 'agent' ? '智能体' : '高级编排' }}
        </el-tag>
      </template>

      <template #status="{ row }">
        <el-tag :type="row.status === 'published' ? 'success' : 'info'">
          {{ row.status === 'published' ? '已发布' : '草稿' }}
        </el-tag>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="goDesign(row)" v-hasPermi="['aiapp:edit']">编排</el-button>
        <el-button link @click="goChat(row)" v-hasPermi="['aiapp:chat']">演示</el-button>
        <el-button link type="success" @click="openPublish(row)" v-hasPermi="['aiapp:publish']">发布</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除应用「${row.name}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['aiapp:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" title="创建 AI 应用" :on-confirm="submitAdd">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="128" show-word-limit placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="类型" prop="appType">
          <el-radio-group v-model="form.appType">
            <el-radio value="agent">智能体</el-radio>
            <el-radio value="workflow">高级编排</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="介绍" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="512" show-word-limit />
        </el-form-item>
      </el-form>
    </c7-dialog>

    <PublishDialog
      v-model="publishVisible"
      :app-id="publishApp?.id"
      :app-name="publishApp?.name"
      :status="publishApp?.status"
      @published="onPublished"
    />
  </div>
</template>

<script setup>
import { computed, onActivated, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addAiApp, listAiApp, removeAiApp } from '@/api/ai/app'
import PublishDialog from '../components/PublishDialog.vue'

defineOptions({ name: 'AiAppList' })

const router = useRouter()
const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)
const publishVisible = ref(false)
const publishApp = ref(null)

const form = ref({ name: '', appType: 'agent', description: '' })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  appType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const defaultSearchParam = { name: '', appType: '', status: '' }

const searchColumns = computed(() => [
  { prop: 'name', label: '名称', type: 'input', span: 8, props: { placeholder: '请输入名称', clearable: true } },
  {
    prop: 'appType',
    label: '类型',
    type: 'select',
    span: 8,
    options: [
      { label: '智能体', value: 'agent' },
      { label: '高级编排', value: 'workflow' }
    ]
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: [
      { label: '草稿', value: 'draft' },
      { label: '已发布', value: 'published' }
    ]
  }
])

const tableColumns = computed(() => [
  { prop: 'name', label: '名称', minWidth: 160 },
  { prop: 'appType', label: '类型', width: 110, columnType: 'slot', slotName: 'appType' },
  { prop: 'status', label: '状态', width: 100, columnType: 'slot', slotName: 'status' },
  { prop: 'description', label: '介绍', minWidth: 200, showOverflowTooltip: true },
  { prop: 'updateTime', label: '更新时间', width: 170 },
  { prop: 'action', label: '操作', width: 260, columnType: 'slot', slotName: 'action', fixed: 'right' }
])

function listFunction(params) {
  return listAiApp(params)
}

function batchDeleteFunction(ids) {
  return removeAiApp(ids)
}

function openAdd() {
  form.value = { name: '', appType: 'agent', description: '' }
  visible.value = true
}

async function submitAdd() {
  await formRef.value?.validate()
  const res = await addAiApp(form.value)
  ElMessage.success('创建成功')
  visible.value = false
  await tableRef.value?.refreshData?.()
  const id = res.data
  if (id) {
    goDesign({ id, appType: form.value.appType })
  }
}

function goDesign(row) {
  const path = row.appType === 'workflow'
    ? `/ai/app/workflow/${row.id}`
    : `/ai/app/agent/${row.id}`
  router.push(path)
}

function goChat(row) {
  router.push(`/ai/app/chat/${row.id}`)
}

function openPublish(row) {
  publishApp.value = { ...row }
  publishVisible.value = true
}

function onPublished() {
  publishApp.value = { ...publishApp.value, status: 'published' }
  tableRef.value?.refreshData?.()
}

async function removeRow(row) {
  await removeAiApp([row.id])
  ElMessage.success('删除成功')
  tableRef.value?.refreshData?.()
}

/** 从编排/演示页返回时刷新列表 */
onActivated(() => {
  tableRef.value?.refreshData?.()
})
</script>
