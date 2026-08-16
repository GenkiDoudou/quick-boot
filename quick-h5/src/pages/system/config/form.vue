<script setup lang="ts">
/**
 * 参数新增/编辑/查看：内置参数锁定键名与类型；mode=view 时全部只读。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addConfig, getConfig, updateConfig } from '@/api/system/config'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

const { sys_yes_no } = useDict('sys_yes_no')
const configId = ref('')
const isEdit = computed(() => !!configId.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:config:edit') : hasPermi('system:config:add')),
)
const loading = ref(false)
const submitting = ref(false)
const builtin = ref(false)

const form = reactive({
  configName: '',
  configKey: '',
  configValue: '',
  configType: '0',
  remark: '',
})

async function loadDetail(id: string) {
  const d = await getConfig(id)
  form.configName = d.configName || ''
  form.configKey = d.configKey || ''
  form.configValue = d.configValue || ''
  form.configType = d.configType ?? '0'
  form.remark = d.remark || ''
  builtin.value = String(d.configType) === '1'
}

onLoad(async (query) => {
  const id = query?.configId ? String(query.configId) : ''
  configId.value = id
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看参数' : id ? '编辑参数' : '新增参数',
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

/** 名称 / 键名 / 键值必填 */
async function onSubmit() {
  if (!assert([
    () => required('参数名称', form.configName),
    () => required('参数键名', form.configKey),
    () => required('参数键值', form.configValue),
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      configName: form.configName.trim(),
      configKey: form.configKey.trim(),
      configValue: form.configValue.trim(),
      configType: form.configType,
      remark: form.remark.trim() || undefined,
    }
    if (isEdit.value) {
      await updateConfig({ ...payload, configId: configId.value })
    }
    else {
      await addConfig(payload)
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
        <text class="qb-form-label">参数名称</text>
        <u-input
          v-model="form.configName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          maxlength="100"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">参数键名</text>
        <u-input
          v-model="form.configKey"
          placeholder="必填"
          border="surround"
          maxlength="100"
          :disabled="isView || builtin"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">参数键值</text>
        <u-input
          v-model="form.configValue"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          maxlength="500"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">系统内置</text>
        <QbStatusChips
          v-model="form.configType"
          :options="sys_yes_no"
          :disabled="isView"
          :disabled-values="builtin ? ['0', '1'] : []"
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
