<script setup lang="ts">
/**
 * 字典数据表单：归属 dictType 由路由带入；mode=view 时全部只读。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addDictData, getDictData, updateDictData } from '@/api/system/dictData'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

const { sys_normal_disable } = useDict('sys_normal_disable')
const dictCode = ref('')
const dictType = ref('')
const isEdit = computed(() => !!dictCode.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:dict:edit') : hasPermi('system:dict:add')),
)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: '0',
  remark: '',
})

onLoad(async (query) => {
  dictCode.value = query?.dictCode ? String(query.dictCode) : ''
  dictType.value = query?.dictType ? String(query.dictType) : ''
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看字典数据' : dictCode.value ? '编辑字典数据' : '新增字典数据',
  })
  if (!dictCode.value) return
  loading.value = true
  try {
    const d = await getDictData(dictCode.value)
    form.dictLabel = d.dictLabel || ''
    form.dictValue = d.dictValue || ''
    form.dictSort = Number(d.dictSort ?? 0)
    form.status = d.status ?? '0'
    form.remark = (d as { remark?: string }).remark || ''
    if (d.dictType) dictType.value = String(d.dictType)
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

/** 标签与键值必填；dictType 由路由带入不可缺 */
async function onSubmit() {
  if (!dictType.value) {
    toastInfo('缺少字典类型')
    return
  }
  if (!assert([
    () => required('标签', form.dictLabel),
    () => required('键值', form.dictValue),
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      dictType: dictType.value,
      dictLabel: form.dictLabel.trim(),
      dictValue: String(form.dictValue).trim(),
      dictSort: Number(form.dictSort) || 0,
      status: form.status,
      remark: form.remark.trim() || undefined,
    }
    if (isEdit.value) {
      await updateDictData({ ...payload, dictCode: dictCode.value })
    }
    else {
      await addDictData(payload)
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
        <text class="qb-form-label">字典类型</text>
        <u-input
          :model-value="dictType"
          disabled
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">标签</text>
        <u-input
          v-model="form.dictLabel"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">键值</text>
        <u-input
          v-model="form.dictValue"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">排序</text>
        <u-input
          v-model="form.dictSort"
          type="number"
          :disabled="isView"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <QbStatusChips v-model="form.status" :options="sys_normal_disable" :disabled="isView" />
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
