<script setup lang="ts">
/**
 * 字典类型表单：编辑时 dictType 只读；mode=view 时全部只读。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addDictType, getDictType, updateDictType } from '@/api/system/dictType'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

const { sys_normal_disable } = useDict('sys_normal_disable')
const dictId = ref('')
const isEdit = computed(() => !!dictId.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:dict:edit') : hasPermi('system:dict:add')),
)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  dictName: '',
  dictType: '',
  status: '0',
  remark: '',
})

onLoad(async (query) => {
  const id = query?.dictId ? String(query.dictId) : ''
  dictId.value = id
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看字典类型' : id ? '编辑字典类型' : '新增字典类型',
  })
  if (!id) return
  loading.value = true
  try {
    const d = await getDictType(id)
    form.dictName = d.dictName || ''
    form.dictType = d.dictType || ''
    form.status = d.status ?? '0'
    form.remark = d.remark || ''
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

/** 字典名称与类型编码必填 */
async function onSubmit() {
  if (!assert([
    () => required('字典名称', form.dictName),
    () => required('字典类型', form.dictType),
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      dictName: form.dictName.trim(),
      dictType: form.dictType.trim(),
      status: form.status,
      remark: form.remark.trim() || undefined,
    }
    if (isEdit.value) {
      await updateDictType({ ...payload, dictId: dictId.value })
    }
    else {
      await addDictType(payload)
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
        <text class="qb-form-label">字典名称</text>
        <u-input
          v-model="form.dictName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">字典类型</text>
        <u-input
          v-model="form.dictType"
          placeholder="如 sys_user_sex"
          border="surround"
          :disabled="isView || isEdit"
          :custom-style="qbInputStyle"
        />
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
