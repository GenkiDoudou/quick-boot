<script setup lang="ts">
/**
 * 角色新增/编辑/查看表单：权限字符 roleKey 编辑时不可改；管理员角色禁止选「停用」。
 * mode=view 时全部只读并隐藏保存。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addRole, getRole, updateRole } from '@/api/system/role'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

const { sys_normal_disable } = useDict('sys_normal_disable')

const roleId = ref('')
const isEdit = computed(() => !!roleId.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const isAdmin = computed(() => roleId.value === '1')
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:role:edit') : hasPermi('system:role:add')),
)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: '',
})

/** 管理员角色禁用「停用」选项 */
const disabledStatus = computed(() => (isAdmin.value ? ['1'] : []))

async function loadDetail(id: string) {
  const d = await getRole(id)
  form.roleName = d.roleName || ''
  form.roleKey = d.roleKey || ''
  form.roleSort = Number(d.roleSort ?? 0)
  form.status = d.status ?? '0'
  form.remark = d.remark || ''
}

onLoad(async (query) => {
  const id = query?.roleId ? String(query.roleId) : ''
  roleId.value = id
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看角色' : id ? '编辑角色' : '新增角色',
  })
  if (!id) return
  loading.value = true
  try {
    await loadDetail(id)
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

/** 名称与权限字符必填（文案由 required 的 label 生成） */
async function onSubmit() {
  if (!assert([
    () => required('角色名称', form.roleName),
    () => required('权限字符', form.roleKey),
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      roleName: form.roleName.trim(),
      roleKey: form.roleKey.trim(),
      roleSort: Number(form.roleSort) || 0,
      status: form.status,
      remark: form.remark.trim() || undefined,
    }
    if (isEdit.value) {
      await updateRole({ ...payload, roleId: roleId.value })
    }
    else {
      await addRole(payload)
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
        <text class="qb-form-label">角色名称</text>
        <u-input
          v-model="form.roleName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          maxlength="64"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">权限字符</text>
        <u-input
          v-model="form.roleKey"
          placeholder="如 admin、common"
          border="surround"
          maxlength="64"
          :disabled="isView || isEdit"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">显示顺序</text>
        <u-input
          v-model="form.roleSort"
          type="number"
          :disabled="isView"
          placeholder="0"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <QbStatusChips
          v-model="form.status"
          :options="sys_normal_disable"
          :disabled="isView"
          :disabled-values="disabledStatus"
        />
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
      <view v-else class="qb-muted" style="margin-top: 24rpx; text-align: center">无保存权限</view>
    </view>
  </view>
</template>
