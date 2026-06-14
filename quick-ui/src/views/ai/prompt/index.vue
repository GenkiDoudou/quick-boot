<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="promptId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['ai:prompt:add']"
      :show-edit-button="true"
      :edit-button-permi="['ai:prompt:edit']"
      :show-delete-button="true"
      :delete-button-permi="['ai:prompt:remove']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #action="{ row }">
        <el-button link type="primary" @click="openEdit(row)" v-hasPermi="['ai:prompt:edit']">编辑</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除 ${row.name} 吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['ai:prompt:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.promptId ? '编辑提示词' : '新增提示词'" :on-confirm="submit" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="提示词名称" prop="name">
          <el-input v-model="form.name" maxlength="100" show-word-limit placeholder="请输入提示词名称" />
        </el-form-item>
        <el-form-item label="提示词分类" prop="category">
          <el-input v-model="form.category" maxlength="64" placeholder="如：客服、RAG、通用" />
        </el-form-item>
        <el-form-item label="提示词描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="简要说明用途" />
        </el-form-item>
        <el-form-item label="提示词内容" prop="content">
          <div class="ai-prompt-page__content-wrap">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              placeholder="请输入提示词正文"
            />
            <div class="ai-prompt-page__content-actions">
              <el-button
                type="primary"
                plain
                :loading="optimizing"
                :disabled="!form.content?.trim()"
                @click="handleOptimize"
                v-hasPermi="['ai:prompt:optimize']"
              >
                AI 优化
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addPrompt,
  getPromptInfo,
  listPrompt,
  optimizePromptContent,
  removePrompt,
  updatePrompt
} from '@/api/ai/prompt'

const tableRef = ref(null)
const formRef = ref(null)
const visible = ref(false)
const optimizing = ref(false)

const defaultSearchParam = { pageNum: 1, pageSize: 10 }

const searchColumns = [
  { prop: 'name', label: '提示词名称', type: 'input' },
  { prop: 'category', label: '提示词分类', type: 'input' }
]

const tableColumns = [
  { prop: 'name', label: '提示词名称', minWidth: 160 },
  { prop: 'category', label: '提示词分类', width: 140 },
  { prop: 'description', label: '提示词描述', minWidth: 200, showOverflowTooltip: true },
  { prop: 'updateTime', label: '更新时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 140, fixed: 'right' }
]

const emptyForm = () => ({
  promptId: null,
  name: '',
  category: '',
  description: '',
  content: ''
})

const form = reactive(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入提示词名称', trigger: 'blur' }]
}

function listFunction(params) {
  return listPrompt(params)
}

function batchDeleteFunction(ids) {
  return removePrompt(ids)
}

function refreshTable() {
  if (tableRef.value?.getDataList) {
    return tableRef.value.getDataList()
  }
  return tableRef.value?.refreshData?.()
}

function openAdd() {
  Object.assign(form, emptyForm())
  visible.value = true
}

async function openEdit(row) {
  const res = await getPromptInfo(row.promptId)
  const data = res.data || {}
  Object.assign(form, emptyForm(), {
    promptId: data.promptId,
    name: data.name || '',
    category: data.category || '',
    description: data.description || '',
    content: data.content || ''
  })
  visible.value = true
}

async function submit() {
  await formRef.value?.validate?.()
  const payload = {
    promptId: form.promptId,
    name: form.name,
    category: form.category,
    description: form.description,
    content: form.content
  }
  if (form.promptId) {
    await updatePrompt(payload)
    ElMessage.success('修改成功')
  } else {
    await addPrompt(payload)
    ElMessage.success('新增成功')
  }
  visible.value = false
  await refreshTable()
}

async function handleOptimize() {
  if (!form.content?.trim()) {
    ElMessage.warning('请先输入提示词内容')
    return
  }
  optimizing.value = true
  try {
    const res = await optimizePromptContent({ content: form.content.trim() })
    const data = res.data
    if (data?.success && data.optimizedContent) {
      form.content = data.optimizedContent
      ElMessage.success('优化完成，已更新内容')
    } else {
      ElMessage.error(data?.errorMsg || '优化失败')
    }
  } finally {
    optimizing.value = false
  }
}

async function removeRow(row) {
  await removePrompt([row.promptId])
  ElMessage.success('删除成功')
  await refreshTable()
}
</script>

<style scoped>
.ai-prompt-page__content-wrap {
  width: 100%;
}
.ai-prompt-page__content-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
