<template>

  <div class="app-container">

    <C7JsonTable

      ref="tableRef"

      row-key="clientId"

      :show-index="false"

      :show-selection="true"

      :list-function="listFunction"

      :table-columns="tableColumns"

      :search-columns="searchColumns"

      :default-search-param="defaultSearchParam"

      :delete-function="batchDeleteFunction"

      :show-add-button="true"

      :add-button-permi="['system:oauthClient:add']"

      :show-edit-button="true"

      :edit-button-permi="['system:oauthClient:edit']"

      :show-delete-button="true"

      :delete-button-permi="['system:oauthClient:remove']"

      :on-add="openAdd"

      :on-edit="openEdit"

      :check-delete-success="() => true"

      rows-key="data.records"

      total-key="data.total"

    >

      <template #status="{ row }">

        <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>

      </template>

      <template #signVerify="{ row }">

        <el-tag :type="row.signVerify === '1' ? 'success' : 'info'">{{ row.signVerify === '1' ? '是' : '否' }}</el-tag>

      </template>

      <template #action="{ row }">

        <el-button link type="primary" @click="openView(row)" v-hasPermi="['system:oauthClient:query']">查看</el-button>

        <el-button link @click="openEdit(row)" v-hasPermi="['system:oauthClient:edit']">修改</el-button>

        <c7-button

          btn-type="delete"

          link

          confirm

          :confirm-message="`确认删除 ${row.clientName} 吗？`"

          :click-function="() => removeRow(row)"

          v-hasPermi="['system:oauthClient:remove']"

        />

      </template>

    </C7JsonTable>



    <c7-dialog

      v-model="visible"

      width="640px"

      :title="form.clientId && !isAdd ? '修改 OAuth 客户端' : '新增 OAuth 客户端'"

      :on-confirm="submit"

    >

      <el-form ref="formRef" :model="form" :rules="rules" label-width="128px" class="oauth-client-form">

        <el-form-item prop="clientId">

          <template #label>

            <FormFieldLabel label="Client ID" :hint="fieldHints.clientId" />

          </template>

          <el-input v-model="form.clientId" :disabled="!isAdd" placeholder="第三方应用唯一标识">

            <template v-if="isAdd" #append>

              <el-button @click="generateClientId">生成</el-button>

            </template>

          </el-input>

        </el-form-item>



        <el-form-item prop="clientSecret">

          <template #label>

            <FormFieldLabel label="Client Secret" :hint="fieldHints.clientSecret" />

          </template>

          <el-input

            v-model="form.clientSecret"

            type="password"

            show-password

            :placeholder="isAdd ? '必填，可点击生成' : '留空不修改，可点击生成新密钥'"

          >

            <template #append>

              <el-button @click="generateClientSecret">生成</el-button>

            </template>

          </el-input>

        </el-form-item>



        <el-form-item prop="clientName">

          <template #label>

            <FormFieldLabel label="应用名称" :hint="fieldHints.clientName" />

          </template>

          <el-input v-model="form.clientName" placeholder="管理端展示名称" />

        </el-form-item>



        <el-form-item prop="redirectUris">

          <template #label>

            <FormFieldLabel label="回调地址" :hint="fieldHints.redirectUris" />

          </template>

          <el-input

            v-model="form.redirectUris"

            type="textarea"

            :rows="2"

            placeholder="授权码/隐式模式必填；仅 Client 签名或 client_credentials 可留空"

          />

        </el-form-item>



        <el-form-item prop="grantTypeList">

          <template #label>

            <FormFieldLabel label="授权模式" :hint="fieldHints.grantTypes" />

          </template>

          <el-select

            v-model="form.grantTypeList"

            multiple

            collapse-tags

            collapse-tags-tooltip

            placeholder="请选择允许的 Grant Type"

            style="width: 100%"

          >

            <el-option

              v-for="opt in grantTypeOptions"

              :key="opt.value"

              :label="opt.label"

              :value="opt.value"

            />

          </el-select>

        </el-form-item>



        <el-form-item prop="apiPathPatterns">

          <template #label>

            <FormFieldLabel label="接口授权" :hint="fieldHints.apiPathPatterns" />

          </template>

          <el-input

            v-model="form.apiPathPatterns"

            type="textarea"

            :rows="6"

            placeholder="每行一条 Ant 路径（Spring AntPathMatcher），例如：&#10;/open-api/v1/userinfo&#10;/system/**"

          />

        </el-form-item>



        <el-form-item prop="signVerify">

          <template #label>

            <FormFieldLabel label="客户端验签" :hint="fieldHints.signVerify" />

          </template>

          <el-select v-model="form.signVerify" placeholder="请选择" style="width: 100%">

            <el-option

              v-for="opt in signVerifyOptions"

              :key="opt.value"

              :label="opt.label"

              :value="opt.value"

            />

          </el-select>

        </el-form-item>



        <el-form-item prop="isConfidential">

          <template #label>

            <FormFieldLabel label="机密客户端" :hint="fieldHints.isConfidential" />

          </template>

          <el-select v-model="form.isConfidential" placeholder="请选择" style="width: 100%">

            <el-option

              v-for="opt in confidentialOptions"

              :key="opt.value"

              :label="opt.label"

              :value="opt.value"

            />

          </el-select>

        </el-form-item>



        <el-form-item prop="status">

          <template #label>

            <FormFieldLabel label="状态" :hint="fieldHints.status" />

          </template>

          <el-select v-model="form.status" placeholder="请选择" style="width: 100%">

            <el-option

              v-for="opt in statusOptions"

              :key="opt.value"

              :label="opt.label"

              :value="opt.value"

            />

          </el-select>

        </el-form-item>



        <el-form-item prop="remark">

          <template #label>

            <FormFieldLabel label="备注" :hint="fieldHints.remark" />

          </template>

          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />

        </el-form-item>

      </el-form>

    </c7-dialog>



    <c7-dialog

      v-model="pwdConfirmVisible"

      title="验证身份"

      width="440px"

      :on-confirm="submitRevealSecret"

    >

      <el-alert

        type="info"

        :closable="false"

        show-icon

        title="查看 Client Secret 需输入当前登录用户的密码"

        class="oauth-client-view-alert"

      />

      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="96px" class="oauth-client-pwd-form">

        <el-form-item label="登录密码" prop="password">

          <el-input

            v-model="pwdForm.password"

            type="password"

            show-password

            autocomplete="current-password"

            placeholder="请输入您的登录密码"

            @keyup.enter="pwdFormRef?.validate?.()"

          />

        </el-form-item>

      </el-form>

    </c7-dialog>



    <c7-dialog v-model="viewVisible" title="查看客户端凭证" width="560px" :footer="false">

      <el-alert

        type="warning"

        :closable="false"

        show-icon

        title="请妥善保管 Client Secret，勿泄露或提交到版本库。"

        class="oauth-client-view-alert"

      />

      <el-descriptions :column="1" border class="oauth-client-view-desc">

        <el-descriptions-item label="Client ID">

          <span class="oauth-client-view-value">{{ viewDetail.clientId }}</span>

          <el-button link type="primary" @click="copyText(viewDetail.clientId, 'Client ID')">复制</el-button>

        </el-descriptions-item>

        <el-descriptions-item label="Client Secret">

          <el-input

            v-model="viewDetail.clientSecret"

            readonly

            type="password"

            show-password

            class="oauth-client-view-secret"

          />

          <el-button link type="primary" @click="copyText(viewDetail.clientSecret, 'Client Secret')">复制</el-button>

        </el-descriptions-item>

      </el-descriptions>

    </c7-dialog>

  </div>

</template>



<script setup>

import { computed, defineComponent, h, ref } from 'vue'

import { ElIcon, ElMessage, ElTooltip } from 'element-plus'

import { View } from '@element-plus/icons-vue'

import {

  addOauthClient,

  listOauthClient,

  revealOauthClientSecret,

  removeOauthClient,

  updateOauthClient

} from '@/api/system/oauthClient'



defineOptions({ name: 'SysOauthClient' })



/**

 * 表单项标签 + 小眼睛（悬停查看字段说明）。

 */

const FormFieldLabel = defineComponent({

  name: 'OauthClientFormFieldLabel',

  props: {

    label: { type: String, required: true },

    hint: { type: String, required: true }

  },

  setup(props) {

    return () =>

      h('span', { class: 'oauth-client-form__label' }, [

        props.label,

        h(

          ElTooltip,

          { content: props.hint, placement: 'top', effect: 'dark' },

          {

            default: () =>

              h(ElIcon, { class: 'oauth-client-form__hint', tabindex: 0 }, () => h(View))

          }

        )

      ])

  }

})



/** 各字段说明（OAuth2 授权服务器登记第三方应用） */

const fieldHints = {

  clientId:

    '第三方应用在授权请求中携带的 client_id，创建后不可修改。可点击「生成」自动创建（前缀 oauth_ + 随机串）。',

  clientSecret:

    '客户端密钥，用于 token 端点校验身份；服务端加密存储。可点击「生成」得到 64 位十六进制随机串；修改时留空表示不修改。',

  clientName: '在管理端与授权确认页展示的应用名称，便于用户识别。',

  redirectUris:

    '授权码（authorization_code）或隐式（implicit）模式必填；仅 Client HMAC 签名、client_credentials 等不涉及浏览器回调时可留空（如首方 quick-ui）。多个地址用英文逗号分隔，须精确匹配、不支持 *。',

  grantTypes:

    '决定该应用能用哪种 OAuth2 流程换 token：授权码（浏览器跳转，最安全）、刷新令牌、客户端凭证（纯机机）、密码/隐式（一般不推荐）。仅做 Client HMAC 签名、不走 OAuth 换 token 时，可只选 client_credentials 或按需勾选。',

  apiPathPatterns:

    '启用「客户端验签」时必填：每行一条 Ant 路径（与 Spring AntPathMatcher 一致），对 servlet path 匹配。示例：/open-api/v1/userinfo、/system/**',

  signVerify:

    '为「是」时：请求头带该 Client ID 须通过 HMAC 验签，且 path 须命中接口授权规则。为「否」时：带该 Client ID 的请求跳过验签（仍可能需用户登录 Token）；仅 OAuth token 访问 /open-api 时仍按接口授权校验。',

  isConfidential:

    'OAuth2 协议字段：能否在服务端安全保存 client_secret。「是」= 机密客户端（后端、可保密的环境）；「否」= 公共客户端（无法保密 secret 的纯浏览器应用）。本系统 Client HMAC 签名与是否机密无直接关系，首方 quick-ui 仍建议选「是」。',

  status: '停用后该 client_id 将无法发起新的授权与换 token 请求。',

  remark: '内部备注，不影响 OAuth2 协议行为。'

}



const grantTypeOptions = [

  { value: 'authorization_code', label: 'authorization_code（授权码）' },

  { value: 'refresh_token', label: 'refresh_token（刷新令牌）' },

  { value: 'client_credentials', label: 'client_credentials（客户端凭证）' },

  { value: 'password', label: 'password（密码模式）' },

  { value: 'implicit', label: 'implicit（隐式）' }

]



const signVerifyOptions = [

  { value: '1', label: '是（校验 HMAC 与接口授权）' },

  { value: '0', label: '否（不验签）' }

]



const confidentialOptions = [

  { value: '1', label: '是（服务端可保管 Secret）' },

  { value: '0', label: '否（公共客户端）' }

]



const statusOptions = [

  { value: '0', label: '正常' },

  { value: '1', label: '停用' }

]



const tableRef = ref(null)

const visible = ref(false)

const viewVisible = ref(false)

const pwdConfirmVisible = ref(false)

const pendingRevealClientId = ref('')

const pwdFormRef = ref(null)

const pwdForm = ref({ password: '' })

const pwdRules = {

  password: [{ required: true, message: '请输入当前用户登录密码', trigger: 'blur' }]

}

const viewDetail = ref({ clientId: '', clientSecret: '' })

const formRef = ref(null)

const isAdd = ref(true)



/** 启用验签时须配置接口授权 */

function validateApiPathPatterns(_rule, value, callback) {

  if (form.value.signVerify !== '1') {

    callback()

    return

  }

  if (!String(value || '').trim()) {

    callback(new Error('启用验签时请配置接口授权（Ant 路径）'))

    return

  }

  callback()

}



/** 授权码/隐式模式须填写回调地址 */

function validateRedirectUris(_rule, value, callback) {

  const grants = form.value.grantTypeList || []

  const needs = grants.some((g) => g === 'authorization_code' || g === 'implicit')

  if (needs && !String(value || '').trim()) {

    callback(new Error('启用授权码或隐式模式时须填写回调地址'))

    return

  }

  callback()

}



const form = ref({

  clientId: '',

  clientSecret: '',

  clientName: '',

  redirectUris: '',

  grantTypes: 'authorization_code,refresh_token',

  apiPathPatterns: '',

  signVerify: '1',

  grantTypeList: ['authorization_code', 'refresh_token'],

  status: '0',

  isConfidential: '1',

  remark: ''

})



const defaultSearchParam = { clientName: '' }



const searchColumns = computed(() => [

  { prop: 'clientName', label: '应用名称', type: 'input', span: 8, props: { clearable: true } }

])



const tableColumns = computed(() => [

  { prop: 'clientId', label: 'Client ID', minWidth: 140 },

  { prop: 'clientName', label: '应用名称', minWidth: 160 },

  { prop: 'redirectUris', label: '回调地址', minWidth: 220, showOverflowTooltip: true },

  { prop: 'grantTypes', label: 'Grant', minWidth: 180 },

  { prop: 'apiPathPatterns', label: '接口授权', minWidth: 160, showOverflowTooltip: true },

  { prop: 'signVerify', label: '验签', width: 72, columnType: 'slot', slotName: 'signVerify' },

  { prop: 'status', label: '状态', width: 80, columnType: 'slot', slotName: 'status' },

  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 200, fixed: 'right' }

])



const rules = computed(() => ({

  clientId: [{ required: true, message: '请输入或生成 Client ID', trigger: 'blur' }],

  clientSecret: isAdd.value ? [{ required: true, message: '请输入或生成 Secret', trigger: 'blur' }] : [],

  clientName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],

  redirectUris: [{ validator: validateRedirectUris, trigger: ['blur', 'change'] }],

  grantTypeList: [

    {

      type: 'array',

      required: true,

      min: 1,

      message: '请至少选择一种授权模式',

      trigger: 'change'

    }

  ],

  apiPathPatterns: [

    {

      validator: validateApiPathPatterns,

      trigger: ['blur', 'change']

    }

  ],

  signVerify: [{ required: true, message: '请选择是否验签', trigger: 'change' }],

  isConfidential: [{ required: true, message: '请选择是否机密客户端', trigger: 'change' }],

  status: [{ required: true, message: '请选择状态', trigger: 'change' }]

}))



/**

 * 使用 Web Crypto 生成随机十六进制字符串。

 * @param {number} byteLength 字节长度

 * @returns {string}

 */

function randomHex(byteLength) {

  const bytes = new Uint8Array(byteLength)

  crypto.getRandomValues(bytes)

  return Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('')

}



/** 生成 Client ID（仅新增时可用，长度适配 VARCHAR(64)） */

function generateClientId() {

  form.value.clientId = `oauth_${randomHex(12)}`

  formRef.value?.clearValidate('clientId')

  ElMessage.success('已生成 Client ID')

}



/** 生成 Client Secret（新增必填；修改时生成即表示轮换密钥） */

function generateClientSecret() {

  form.value.clientSecret = randomHex(32)

  formRef.value?.clearValidate('clientSecret')

  ElMessage.success('已生成 Client Secret，请妥善保存')

}



/**

 * 将逗号分隔字符串拆为数组（Grant / Scope）。

 * @param {string} [raw]

 * @returns {string[]}

 */

function splitCsv(raw) {

  if (!raw) return []

  return String(raw)

    .split(/[,，\s]+/)

    .map((s) => s.trim())

    .filter(Boolean)

}



/**

 * 将数组拼为后端存储用的逗号分隔字符串。

 * @param {string[]} list

 * @returns {string}

 */

function joinCsv(list) {

  return (list || []).filter(Boolean).join(',')

}



function syncListsFromForm() {

  form.value.grantTypeList = splitCsv(form.value.grantTypes)

}



function listFunction(params) {

  return listOauthClient(params).then((res) => {

    const records = res.data || []

    return { data: { records, total: records.length } }

  })

}



function openAdd() {

  isAdd.value = true

  form.value = {

    clientId: '',

    clientSecret: '',

    clientName: '',

    redirectUris: '',

    grantTypes: 'authorization_code,refresh_token',

    apiPathPatterns: '',

    signVerify: '1',

    grantTypeList: ['authorization_code', 'refresh_token'],

    status: '0',

    isConfidential: '1',

    remark: ''

  }

  visible.value = true

}



/** 查看凭证：先弹窗校验当前用户密码，再拉取明文 Secret */

function openView(row) {

  if (!row?.clientId) return

  pendingRevealClientId.value = row.clientId

  pwdForm.value.password = ''

  pwdConfirmVisible.value = true

  pwdFormRef.value?.clearValidate?.()

}



/** 密码校验通过后展示 Client ID / Secret */

function submitRevealSecret() {

  return new Promise((resolve, reject) => {

    pwdFormRef.value?.validate((valid) => {

      if (!valid) {

        reject(new Error('校验失败'))

        return

      }

      revealOauthClientSecret(pendingRevealClientId.value, pwdForm.value.password)

        .then((res) => {

          const data = res.data || {}

          viewDetail.value = {

            clientId: data.clientId || pendingRevealClientId.value,

            clientSecret: data.clientSecret || ''

          }

          pwdConfirmVisible.value = false

          pwdForm.value.password = ''

          viewVisible.value = true

          resolve()

        })

        .catch(reject)

    })

  })

}



/** 复制到剪贴板 */

async function copyText(text, label) {

  if (!text) {

    ElMessage.warning(`${label} 为空`)

    return

  }

  try {

    await navigator.clipboard.writeText(text)

    ElMessage.success(`已复制 ${label}`)

  } catch {

    ElMessage.error('复制失败，请手动选择复制')

  }

}



function openEdit(row) {

  if (!row) return

  isAdd.value = false

  form.value = {

    ...row,

    clientSecret: '',

    isConfidential: row.isConfidential ?? '1',

    status: row.status ?? '0',

    grantTypeList: [],

    apiPathPatterns: row.apiPathPatterns ?? '',

    signVerify: row.signVerify ?? '1'

  }

  syncListsFromForm()

  visible.value = true

}



function buildPayload() {

  const { grantTypeList, clientSecret, ...rest } = form.value

  const payload = {

    ...rest,

    redirectUris: String(rest.redirectUris || '').trim(),

    apiPathPatterns: String(rest.apiPathPatterns || '').trim(),

    grantTypes: joinCsv(grantTypeList)

  }

  // 修改时留空表示不修改密钥，避免提交空串触发防火墙误扫
  if (isAdd.value || String(clientSecret || '').trim() !== '') {
    payload.clientSecret = clientSecret
  }

  return payload

}



function submit() {

  return new Promise((resolve, reject) => {

    formRef.value.validate((valid) => {

      if (!valid) return reject(new Error('校验失败'))

      const payload = buildPayload()

      const req = isAdd.value ? addOauthClient(payload) : updateOauthClient(payload)

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

  return removeOauthClient([row.clientId]).then(() => {

    ElMessage.success('删除成功')

    return tableRef.value?.refreshData()

  })

}



function batchDeleteFunction(ids) {

  return removeOauthClient(ids || []).then(() => ElMessage.success('删除成功'))

}

</script>



<style scoped lang="scss">

.oauth-client-form {

  :deep(.oauth-client-form__label) {

    display: inline-flex;

    align-items: center;

    gap: 4px;

  }



  :deep(.oauth-client-form__hint) {

    cursor: help;

    color: var(--el-text-color-secondary);

    font-size: 14px;

    vertical-align: middle;



    &:hover {

      color: var(--el-color-primary);

    }

  }

}



.oauth-client-view-alert {

  margin-bottom: 16px;

}



.oauth-client-pwd-form {

  margin-top: 12px;

}



.oauth-client-view-desc {

  :deep(.el-descriptions__cell) {

    vertical-align: middle;

  }

}



.oauth-client-view-value {

  margin-right: 8px;

  word-break: break-all;

}



.oauth-client-view-secret {

  max-width: 360px;

  margin-right: 8px;

}

</style>


