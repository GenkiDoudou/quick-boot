<template>
  <div class="app-container">
    <C7CardGrid
      ref="gridRef"
      row-key="id"
      :list-function="listFunction"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :show-toolbar="false"
      :show-add-card="true"
      :add-card-permi="['aiapp:add']"
      add-card-text="创建应用"
      :on-add="openAdd"
      :page-sizes="[12, 24, 48]"
      :default-page-size="12"
      empty-text="暂无 AI 应用"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #card="{ row, refreshData }">
        <AiAppCard
          :item="row"
          @design="goDesign"
          @chat="goChat"
          @publish="openPublish"
          @delete="(item) => handleDeleteRow(item, refreshData)"
        />
      </template>
    </C7CardGrid>

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
import { onActivated, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { c7Confirm } from '@/packages/C7MessageBox/index.js'
import { addAiApp, listAiApp, removeAiApp } from '@/api/ai/app'
import PublishDialog from '../components/PublishDialog.vue'
import AiAppCard from './components/AiAppCard.vue'

defineOptions({ name: 'AiAppList' })

const router = useRouter()
const gridRef = ref(null)
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

const searchColumns = [
  { prop: 'name', label: '名称', type: 'input', props: { placeholder: '请输入名称', clearable: true } },
  {
    prop: 'appType',
    label: '类型',
    type: 'select',
    options: [
      { label: '智能体', value: 'agent' },
      { label: '高级编排', value: 'workflow' }
    ]
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '草稿', value: 'draft' },
      { label: '已发布', value: 'published' }
    ]
  }
]

function listFunction(params) {
  return listAiApp(params)
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
  await gridRef.value?.refreshData?.()
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
  gridRef.value?.refreshData?.()
}

async function handleDeleteRow(row, refreshData) {
  try {
    await c7Confirm(`确认删除应用「${row.name}」吗？`)
    await removeAiApp([row.id])
    ElMessage.success('删除成功')
    if (typeof refreshData === 'function') {
      await refreshData()
    } else {
      await gridRef.value?.refreshData?.()
    }
  } catch {
    /* 用户取消 */
  }
}

/** 从编排/演示页返回时刷新列表 */
onActivated(() => {
  gridRef.value?.refreshData?.()
})
</script>
