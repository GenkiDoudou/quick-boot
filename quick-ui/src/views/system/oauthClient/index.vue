<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageOauthClient"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="removeOauthClient"
      :export-function="exportOauthClient"
      :export-button-permi="['system:oauthClient:export']"
      export-default-file-name="oauth-client.xlsx"
      :import-function="importOauthClient"
      :import-template-download-fn="downloadOauthClientImportTemplate"
      :import-button-permi="['system:oauthClient:import']"
      import-template-file-name="oauth-client-import-template.xlsx"
      :show-import-button="true"
      row-key="id"
      column-setting-key="sys-oauth-client"
      :show-add-button="true"
      :add-button-permi="['system:oauthClient:add']"
      :delete-button-permi="['system:oauthClient:remove']"
      :on-add="openAdd"
    >
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:oauthClient:query']" @click="openView(row)">查看</el-button>
        <el-button link type="primary" v-hasPermi="['system:oauthClient:edit']" @click="openEdit(row)">修改</el-button>
        <el-button link type="danger" v-hasPermi="['system:oauthClient:remove']" @click="removeRow(row)">删除</el-button>
        <el-button link type="primary" v-hasPermi="['system:oauthClient:secret']" @click="openViewSecret(row)">查看密钥</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog
      v-model="formVisible"
      :title="isAdd ? '新增客户端' : '修改客户端'"
      width="680px"
      :on-confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item prop="clientId">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.clientId">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>客户端id</span>
            </span>
          </template>
          <el-input v-model="form.clientId" :disabled="!isAdd" placeholder="唯一标识" maxlength="64" />
        </el-form-item>
        <el-form-item prop="clientName">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.clientName">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>客户端名称</span>
            </span>
          </template>
          <el-input v-model="form.clientName" placeholder="显示名称" maxlength="128" />
        </el-form-item>
        <el-form-item prop="apiPathList">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.apiPath">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>API 路径</span>
            </span>
          </template>
          <div class="path-list">
            <div v-for="(item, idx) in form.apiPathList" :key="idx" class="path-row">
              <el-input
                v-model="form.apiPathList[idx]"
                placeholder="如 /system/** 或 /**"
                maxlength="200"
              />
              <el-button
                type="danger"
                link
                :disabled="form.apiPathList.length <= 1"
                @click="removeApiPath(idx)"
              >删除</el-button>
            </div>
            <el-button type="primary" link :icon="Plus" @click="addApiPath">添加路径</el-button>
          </div>
        </el-form-item>
        <el-form-item prop="tokenTimeout">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.tokenTimeout">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>Token 有效期(秒)</span>
            </span>
          </template>
          <el-input-number
            v-model="form.tokenTimeout"
            :min="0"
            :controls="true"
            placeholder="空则用全局配置"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item prop="checkCaptcha">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.checkCaptcha">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>校验验证码</span>
            </span>
          </template>
          <C7Switch v-model="form.checkCaptcha" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item prop="status">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.status">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>状态</span>
            </span>
          </template>
          <C7Select
            v-model="form.status"
            :data-list="sys_normal_disable"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item prop="remark">
          <template #label>
            <span class="field-hint-label">
              <el-tooltip placement="top" :content="fieldTips.remark">
                <el-icon class="field-hint-eye"><View /></el-icon>
              </el-tooltip>
              <span>备注</span>
            </span>
          </template>
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="可选备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          v-if="isAdd"
          type="info"
          :closable="false"
          show-icon
          title="客户端密钥由系统自动生成，创建成功后可查看；之后可通过操作列「查看密钥」查看并一键复制。"
          class="mb12"
        />
      </el-form>
    </C7Dialog>

    <C7Dialog v-model="viewVisible" title="查看客户端" width="680px">
      <C7Descriptions :column="1" border :data="viewRow" :items="viewDescItems" />
      <template #footer>
        <el-button type="primary" @click="viewVisible = false">关闭</el-button>
      </template>
    </C7Dialog>

    <C7Dialog v-model="secretVisible" title="查看密钥" width="560px">
      <p class="hint-text">请妥善保管客户端密钥，勿泄露给无关人员。</p>
      <el-form label-width="120px">
        <el-form-item label="客户端id">
          <el-input :model-value="revealed.clientId" readonly />
        </el-form-item>
        <el-form-item label="客户端密钥">
          <el-input :model-value="revealed.clientSecret" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <C7Copy
          mode="button"
          button-text="一键全部复制"
          button-type="primary"
          :text="credentialsBlock"
        />
        <el-button @click="secretVisible = false">关闭</el-button>
      </template>
    </C7Dialog>
  </div>
</template>

<script setup>
/**
 * OAuth2 客户端管理：C7JsonTable 分页 CRUD、API 路径多行编辑、创建后展示密钥。
 */
import { Plus, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageOauthClient,
  getOauthClient,
  addOauthClient,
  updateOauthClient,
  removeOauthClient,
  exportOauthClient,
  importOauthClient,
  downloadOauthClientImportTemplate
} from '@/api/system/oauthClient'

/** 与菜单 component 路由 name（OauthClient）一致，供 keep-alive include 命中 */
defineOptions({ name: 'OauthClient' })

const { sys_normal_disable, sys_yes_no } = useDict('sys_normal_disable', 'sys_yes_no')

const tableRef = ref(null)
const formRef = ref(null)
const formVisible = ref(false)
const viewVisible = ref(false)
const secretVisible = ref(false)
const isAdd = ref(true)
const viewRow = ref({})

const fieldTips = {
  clientId: 'OAuth2 客户端唯一标识；创建后不可修改。',
  clientName: '管理后台展示用名称，便于识别业务方。',
  apiPath: '该客户端允许访问的接口 Ant 路径（如 /system/**）。新增默认 /**；可添加多条。',
  tokenTimeout: '用户通过本客户端登录后，访问令牌有效秒数。留空则使用全局 sa-token timeout。',
  checkCaptcha: '开启后，本客户端发起的登录请求必须完成行为验证码二次校验。',
  status: '停用后 Client Basic 校验将拒绝该客户端。',
  remark: '可选说明，仅管理展示，不影响鉴权。'
}

const defaultSearch = {
  clientId: '',
  clientName: '',
  status: ''
}

const searchColumns = computed(() => [
  { prop: 'clientId', label: '客户端id', type: 'input', order: 1, span: 6 },
  { prop: 'clientName', label: '客户端名称', type: 'input', order: 2, span: 6 },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    order: 3,
    span: 6,
    options: [
      { label: '全部', value: '' },
      ...(sys_normal_disable.value || [])
    ]
  }
])

const tableColumns = computed(() => [
  { prop: 'clientId', label: '客户端id', columnType: 'text', minWidth: 100 },
  { prop: 'clientName', label: '客户端名称', columnType: 'text', minWidth: 100 },
  { prop: 'apiPathPatterns', label: '放行的接口API', columnType: 'text', minWidth: 120, showOverflowTooltip: true },
  { prop: 'tokenTimeout', label: 'Token(秒)', columnType: 'text', width: 100 },
  {
    prop: 'checkCaptcha',
    label: '验证码',
    columnType: 'tag',
    width: 100,
    options: sys_yes_no.value || []
  },
  {
    prop: 'status',
    label: '状态',
    columnType: 'tag',
    width: 100,
    options: sys_normal_disable.value || []
  },
  { prop: 'createTime', label: '创建时间', columnType: 'text', minWidth: 160 },
  { prop: 'action', label: '操作', columnType: 'slot', width: 240, slotName: 'action' }
])

const viewDescItems = computed(() => [
  { prop: 'clientId', label: '客户端id' },
  { prop: 'clientName', label: '客户端名称' },
  { prop: 'apiPathPatterns', label: 'API 路径' },
  { prop: 'tokenTimeout', label: 'Token 有效期(秒)' },
  {
    prop: 'checkCaptcha',
    label: '校验验证码',
    columnType: 'tag',
    options: sys_yes_no.value || []
  },
  {
    prop: 'status',
    label: '状态',
    columnType: 'tag',
    options: sys_normal_disable.value || []
  },
  { prop: 'remark', label: '备注' },
  { prop: 'createTime', label: '创建时间' }
])

const form = reactive({
  id: undefined,
  clientId: '',
  clientName: '',
  apiPathList: ['/**'],
  tokenTimeout: undefined,
  checkCaptcha: '0',
  status: '0',
  remark: ''
})

const revealed = reactive({
  clientId: '',
  clientSecret: ''
})

const credentialsBlock = computed(() =>
  [
    `客户端id: ${revealed.clientId || ''}`,
    `客户端密钥: ${revealed.clientSecret || ''}`
  ].join('\n')
)

const rules = {
  clientId: [{ required: true, message: '请输入客户端id', trigger: 'blur' }],
  clientName: [{ required: true, message: '请输入客户端名称', trigger: 'blur' }]
}

function splitApiPaths(raw) {
  if (!raw || !String(raw).trim()) return ['/**']
  const list = String(raw)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
  return list.length ? list : ['/**']
}

function joinApiPaths(list) {
  return (list || [])
    .map((s) => String(s || '').trim())
    .filter(Boolean)
    .join(',')
}

function addApiPath() {
  form.apiPathList.push('')
}

function removeApiPath(idx) {
  if (form.apiPathList.length <= 1) return
  form.apiPathList.splice(idx, 1)
}

function resetForm() {
  form.id = undefined
  form.clientId = ''
  form.clientName = ''
  form.apiPathList = ['/**']
  form.tokenTimeout = undefined
  form.checkCaptcha = '0'
  form.status = '0'
  form.remark = ''
}

function openAdd() {
  isAdd.value = true
  resetForm()
  formVisible.value = true
}

function openView(row) {
  viewRow.value = { ...row }
  viewVisible.value = true
}

function openEdit(row) {
  isAdd.value = false
  form.id = row.id
  form.clientId = row.clientId || ''
  form.clientName = row.clientName || ''
  form.apiPathList = splitApiPaths(row.apiPathPatterns)
  form.tokenTimeout = row.tokenTimeout ?? undefined
  form.checkCaptcha = row.checkCaptcha === '1' || row.checkCaptcha === true || row.checkCaptcha === 1 ? '1' : '0'
  form.status = row.status != null ? String(row.status) : '0'
  form.remark = row.remark || ''
  formVisible.value = true
}

function showSecret(data) {
  revealed.clientId = data?.clientId || ''
  revealed.clientSecret = data?.clientSecret || ''
  secretVisible.value = true
}

async function openViewSecret(row) {
  try {
    const res = await getOauthClient(row.id)
    const data = res.data || {}
    if (!data.clientId && !data.clientSecret) {
      ElMessage.warning('未获取到凭证')
      return
    }
    showSecret({
      clientId: data.clientId || row.clientId,
      clientSecret: data.clientSecret
    })
  } catch {
    /* request 已提示 */
  }
}

function removeRow(row) {
  ElMessageBox.confirm(`确认删除客户端 ${row.clientId}？`, '提示', { type: 'warning' })
    .then(async () => {
      await removeOauthClient(row.id)
      ElMessage.success('已删除')
      tableRef.value?.refreshData?.()
    })
    .catch(() => {})
}

function submitForm() {
  return new Promise((resolve, reject) => {
    formRef.value?.validate(async (ok) => {
      if (!ok) {
        reject(new Error('校验未通过'))
        return
      }
      try {
        const payload = {
          id: isAdd.value ? undefined : form.id,
          clientId: form.clientId,
          clientName: form.clientName,
          apiPathPatterns: joinApiPaths(form.apiPathList) || '/**',
          tokenTimeout: form.tokenTimeout,
          checkCaptcha: form.checkCaptcha === '1' ? '1' : '0',
          status: form.status,
          remark: form.remark
        }
        if (isAdd.value) {
          const res = await addOauthClient(payload)
          // 后端 add 返回主键 id（字符串）；再拉详情取明文 secret
          const id = res.data != null && res.data !== '' ? String(res.data) : ''
          if (!id) {
            ElMessage.warning('创建成功')
            tableRef.value?.refreshData?.()
            resolve()
            return
          }
          const detail = await getOauthClient(id)
          ElMessage.success('创建成功，请保存凭证')
          showSecret(detail.data || { clientId: payload.clientId })
          tableRef.value?.refreshData?.()
          resolve()
        } else {
          await updateOauthClient(payload)
          ElMessage.success('更新成功')
          tableRef.value?.refreshData?.()
          resolve()
        }
      } catch (e) {
        reject(e)
      }
    })
  })
}
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}
.hint-text {
  margin: 0 0 12px;
  color: #606266;
  font-size: 13px;
}
.cred-row {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: center;
}
.cred-row .el-input {
  flex: 1;
}
.path-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.path-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.path-row .el-input {
  flex: 1;
}
.field-hint-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.field-hint-eye {
  cursor: help;
  color: #909399;
  vertical-align: middle;
}
</style>
