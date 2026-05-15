<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="noticeId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      :show-export-button="false"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-right>
        <el-tooltip content="通知公告暂不支持导出" placement="top">
          <span class="notice-toolbar-export-wrap">
            <el-button type="warning" plain disabled>导出</el-button>
          </span>
        </el-tooltip>
      </template>
      <template #noticeType="{ row }">
        <c7-dict-tag :model-value="row.noticeType" :options="sys_notice_type" />
      </template>
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_notice_status" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" @click="openEdit(row)" v-hasPermi="['system:notice:edit']">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除公告「${row.noticeTitle}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:notice:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.noticeId ? '修改公告' : '新增公告'" :on-confirm="submit" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择公告类型" style="width: 100%">
            <el-option v-for="d in sys_notice_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in sys_notice_status" :key="d.value" :label="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" prop="noticeContent">
          <el-input
            v-model="form.noticeContent"
            type="textarea"
            :rows="10"
            placeholder="支持 HTML，提交后由服务端白名单消毒；可为空"
          />
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import { addNotice, getNotice, listNotice, removeNotice, updateNotice } from '@/api/system/notice'

/**
 * 通知公告管理页：列表分页、字典展示、富文本以 HTML 文本域编辑（服务端消毒）。
 */
defineOptions({ name: 'SysNotice' })

const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)

const { sys_notice_type, sys_notice_status } = useDict('sys_notice_type', 'sys_notice_status')

const form = ref({
  noticeId: null,
  noticeTitle: '',
  noticeType: '',
  status: '0',
  noticeContent: ''
})

const defaultSearchParam = {
  noticeTitle: '',
  noticeType: '',
  createBy: ''
}

const searchColumns = computed(() => [
  { prop: 'noticeTitle', label: '公告标题', type: 'input', span: 8, props: { placeholder: '请输入公告标题', clearable: true } },
  {
    prop: 'noticeType',
    label: '公告类型',
    type: 'select',
    span: 8,
    options: sys_notice_type.value,
    props: { placeholder: '请选择公告类型', clearable: true, style: 'width: 240px' }
  },
  { prop: 'createBy', label: '创建人', type: 'input', span: 8, props: { placeholder: '请输入创建人', clearable: true } }
])

const tableColumns = computed(() => [
  { prop: 'noticeId', label: '公告编号', width: 120 },
  { prop: 'noticeTitle', label: '公告标题', minWidth: 180, showOverflowTooltip: true },
  { prop: 'noticeType', label: '公告类型', columnType: 'slot', slotName: 'noticeType', width: 100 },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 100 },
  { prop: 'createBy', label: '创建人', width: 120 },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 160, fixed: 'right' }
])

const rules = {
  noticeTitle: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }]
}

function listFunction(params) {
  return listNotice(params)
}

function openAdd() {
  form.value = { noticeId: null, noticeTitle: '', noticeType: '', status: '0', noticeContent: '' }
  visible.value = true
}

function openEdit(row) {
  if (!row) return
  getNotice(row.noticeId).then((res) => {
    const d = res.data || {}
    form.value = {
      noticeId: d.noticeId,
      noticeTitle: d.noticeTitle || '',
      noticeType: d.noticeType || '',
      status: d.status != null ? String(d.status) : '0',
      noticeContent: d.noticeContent || ''
    }
    visible.value = true
  })
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const req = form.value.noticeId ? updateNotice(form.value) : addNotice(form.value)
      req
        .then(() => {
          ElMessage.success('操作成功')
          visible.value = false
          tableRef.value?.refreshData()
          resolve()
        })
        .catch(reject)
    })
  })
}

function removeRow(row) {
  return removeNotice([row.noticeId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function batchDeleteFunction(ids) {
  return removeNotice(ids || []).then(() => {
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped>
.notice-toolbar-export-wrap {
  display: inline-block;
  margin-right: 8px;
  vertical-align: middle;
}
</style>
