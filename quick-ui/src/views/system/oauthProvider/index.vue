<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="providerCode"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :add-button-permi="['system:oauthProvider:add']"
      :show-edit-button="true"
      :edit-button-permi="['system:oauthProvider:edit']"
      :show-delete-button="true"
      :delete-button-permi="['system:oauthProvider:remove']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #enabled="{ row }">
        <el-tag :type="row.enabled === '1' ? 'success' : 'info'">{{ row.enabled === '1' ? '启用' : '停用' }}</el-tag>
      </template>
      <template #action="{ row }">
        <el-button link @click="openEdit(row)" v-hasPermi="['system:oauthProvider:edit']">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:oauthProvider:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="isAdd ? '新增外部 IdP' : '修改外部 IdP'" :on-confirm="submit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="编码" prop="providerCode">
          <el-input v-model="form.providerCode" :disabled="!isAdd" placeholder="如 github" />
        </el-form-item>
        <el-form-item label="名称" prop="providerName">
          <el-input v-model="form.providerName" />
        </el-form-item>
        <el-form-item label="Client ID" prop="clientId">
          <el-input v-model="form.clientId" />
        </el-form-item>
        <el-form-item label="Client Secret" prop="clientSecret">
          <el-input v-model="form.clientSecret" type="password" show-password />
        </el-form-item>
        <el-form-item label="Authorize URL" prop="authorizeUrl">
          <el-input v-model="form.authorizeUrl" />
        </el-form-item>
        <el-form-item label="Token URL" prop="tokenUrl">
          <el-input v-model="form.tokenUrl" />
        </el-form-item>
        <el-form-item label="Userinfo URL" prop="userinfoUrl">
          <el-input v-model="form.userinfoUrl" />
        </el-form-item>
        <el-form-item label="回调地址" prop="redirectUri">
          <el-input v-model="form.redirectUri" />
        </el-form-item>
        <el-form-item label="启用" prop="enabled">
          <el-radio-group v-model="form.enabled">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addOauthProvider, listOauthProvider, removeOauthProvider, updateOauthProvider } from '@/api/system/oauthProvider'

defineOptions({ name: 'SysOauthProvider' })

const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)
const isAdd = ref(true)

const form = ref({
  providerCode: '',
  providerName: '',
  clientId: '',
  clientSecret: '',
  authorizeUrl: '',
  tokenUrl: '',
  userinfoUrl: '',
  redirectUri: '',
  enabled: '0',
  autoRegister: '0',
  remark: ''
})

const defaultSearchParam = { providerName: '' }

const searchColumns = computed(() => [
  { prop: 'providerName', label: '名称', type: 'input', span: 8, props: { clearable: true } }
])

const tableColumns = computed(() => [
  { prop: 'providerCode', label: '编码', width: 120 },
  { prop: 'providerName', label: '名称', minWidth: 140 },
  { prop: 'enabled', label: '状态', columnType: 'slot', slotName: 'enabled', width: 90 },
  { prop: 'redirectUri', label: '回调', minWidth: 200, showOverflowTooltip: true },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 150, fixed: 'right' }
])

const rules = {
  providerCode: [{ required: true, message: '必填', trigger: 'blur' }],
  providerName: [{ required: true, message: '必填', trigger: 'blur' }],
  clientId: [{ required: true, message: '必填', trigger: 'blur' }],
  authorizeUrl: [{ required: true, message: '必填', trigger: 'blur' }],
  tokenUrl: [{ required: true, message: '必填', trigger: 'blur' }],
  redirectUri: [{ required: true, message: '必填', trigger: 'blur' }]
}

function listFunction(params) {
  return listOauthProvider(params).then((res) => {
    const records = res.data || []
    return { data: { records, total: records.length } }
  })
}

function openAdd() {
  isAdd.value = true
  form.value = {
    providerCode: '',
    providerName: '',
    clientId: '',
    clientSecret: '',
    authorizeUrl: '',
    tokenUrl: '',
    userinfoUrl: 'https://api.github.com/user',
    redirectUri: '',
    enabled: '0',
    autoRegister: '0',
    remark: ''
  }
  visible.value = true
}

function openEdit(row) {
  isAdd.value = false
  form.value = { ...row, clientSecret: '' }
  visible.value = true
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const req = isAdd.value ? addOauthProvider(form.value) : updateOauthProvider(form.value)
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
  return removeOauthProvider([row.providerCode]).then(() => tableRef.value?.refreshData())
}

function batchDeleteFunction(ids) {
  return removeOauthProvider(ids || [])
}
</script>
