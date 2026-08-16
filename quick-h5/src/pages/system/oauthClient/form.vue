<script setup lang="ts">
/**
 * OAuth 客户端表单：secret 默认掩码，有权限可揭示；clientId 编辑只读。
 * mode=view 时全部只读并隐藏保存。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addOauthClient, getOauthClient, updateOauthClient } from '@/api/system/oauthClient'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

const { sys_normal_disable, sys_yes_no } = useDict('sys_normal_disable', 'sys_yes_no')
const id = ref('')
const isEdit = computed(() => !!id.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:oauthClient:edit') : hasPermi('system:oauthClient:add')),
)
const canSecret = computed(() => hasPermi('system:oauthClient:secret'))
const loading = ref(false)
const submitting = ref(false)
const revealSecret = ref(false)
const secretRaw = ref('')

const form = reactive({
  clientId: '',
  clientName: '',
  apiPathPatterns: '/**',
  tokenTimeout: '' as string | number,
  checkCaptcha: '0',
  status: '0',
  remark: '',
})

onLoad(async (query) => {
  const pk = query?.id ? String(query.id) : ''
  id.value = pk
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看客户端' : pk ? '编辑客户端' : '新增客户端',
  })
  if (!pk) return
  loading.value = true
  try {
    const d = await getOauthClient(pk)
    form.clientId = d.clientId || ''
    form.clientName = d.clientName || ''
    form.apiPathPatterns = d.apiPathPatterns || '/**'
    form.tokenTimeout = d.tokenTimeout == null ? '' : Number(d.tokenTimeout)
    form.checkCaptcha = d.checkCaptcha ?? '0'
    form.status = d.status ?? '0'
    form.remark = d.remark || ''
    secretRaw.value = d.clientSecret || ''
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    loading.value = false
  }
})

function goBack() {
  uni.navigateBack()
}

const secretDisplay = computed(() => {
  if (!secretRaw.value) return '—'
  if (!canSecret.value || !revealSecret.value) return '••••••••'
  return secretRaw.value
})

function toggleSecret() {
  if (!canSecret.value) {
    toastInfo('无查看密钥权限')
    return
  }
  revealSecret.value = !revealSecret.value
}

function copySecret() {
  if (!canSecret.value || !revealSecret.value || !secretRaw.value) return
  uni.setClipboardData({ data: secretRaw.value })
}

/** clientId / 名称 / 接口路径必填；secret 掩码逻辑不变 */
async function onSubmit() {
  if (!assert([
    () => required('客户端 ID', form.clientId),
    () => required('客户端名称', form.clientName),
    () => required('接口路径', form.apiPathPatterns),
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      clientId: form.clientId.trim(),
      clientName: form.clientName.trim(),
      apiPathPatterns: form.apiPathPatterns.trim(),
      tokenTimeout: form.tokenTimeout === '' ? null : Number(form.tokenTimeout),
      checkCaptcha: form.checkCaptcha,
      status: form.status,
      remark: form.remark.trim() || undefined,
    }
    if (isEdit.value) {
      await updateOauthClient({ ...payload, id: id.value })
    }
    else {
      const newId = await addOauthClient(payload)
      toastOk('保存成功')
      // 新增后跳转编辑页以便查看 secret
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/system/oauthClient/form?id=${encodeURIComponent(String(newId))}`,
        })
      }, 400)
      return
    }
    toastOk('保存成功')
    setTimeout(() => uni.navigateBack(), 400)
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <view v-if="loading" class="qb-form-loading qb-muted">加载中…</view>
    <view v-else class="qb-form-panel">
      <view class="qb-form-field">
        <text class="qb-form-label">客户端 ID</text>
        <u-input
          v-model="form.clientId"
          placeholder="必填"
          border="surround"
          :disabled="isView || isEdit"
          :custom-style="qbInputStyle"
        />
      </view>
      <view v-if="isEdit" class="qb-form-field">
        <text class="qb-form-label">客户端密钥</text>
        <view class="secret-row">
          <text class="secret-text">{{ secretDisplay }}</text>
          <text v-if="canSecret && !isView" class="qb-link" @click="toggleSecret">
            {{ revealSecret ? '隐藏' : '显示' }}
          </text>
          <text
            v-if="canSecret && !isView && revealSecret && secretRaw"
            class="qb-link"
            @click="copySecret"
          >
            复制
          </text>
        </view>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">客户端名称</text>
        <u-input
          v-model="form.clientName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">允许访问的接口</text>
        <u-input
          v-model="form.apiPathPatterns"
          :disabled="isView"
          placeholder="如 /**"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">Token 有效秒数</text>
        <u-input
          v-model="form.tokenTimeout"
          type="number"
          :disabled="isView"
          placeholder="空则用全局配置"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">校验验证码</text>
        <QbStatusChips v-model="form.checkCaptcha" :options="sys_yes_no" :disabled="isView" />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <QbStatusChips v-model="form.status" :options="sys_normal_disable" :disabled="isView" />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">备注</text>
        <u-input
          v-model="form.remark"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <u-button
        v-if="canSave"
        type="primary"
        :loading="submitting"
        :custom-style="qbPrimaryBtnStyle"
        @click="onSubmit"
      >
        保存
      </u-button>
      <u-button
        v-else-if="isView"
        :custom-style="qbPrimaryBtnStyle"
        @click="goBack"
      >
        返回
      </u-button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.secret-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: #f1f5f9;
}
.secret-text {
  flex: 1;
  min-width: 0;
  font-size: 28rpx;
  color: #0f172a;
  word-break: break-all;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}
</style>
