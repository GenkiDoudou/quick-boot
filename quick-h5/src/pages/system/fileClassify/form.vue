<script setup lang="ts">
/**
 * 文件分类表单：新增可填 classify，编辑时键只读；mode=view 时全部只读。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  addFileClassify,
  getFileClassify,
  updateFileClassify,
} from '@/api/system/fileClassify'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

const { sys_normal_disable } = useDict('sys_normal_disable')
const classifyId = ref('')
const isEdit = computed(() => !!classifyId.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:fileClassify:edit') : hasPermi('system:fileClassify:add')),
)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  classify: '',
  classifyName: '',
  limitExt: '',
  limitSizeBytes: '' as string | number,
  limitCount: '' as string | number,
  status: '0',
  remark: '',
})

onLoad(async (query) => {
  const id = query?.classifyId ? String(query.classifyId) : ''
  classifyId.value = id
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看文件分类' : id ? '编辑文件分类' : '新增文件分类',
  })
  if (!id) return
  loading.value = true
  try {
    const d = await getFileClassify(id)
    form.classify = d.classify || ''
    form.classifyName = d.classifyName || ''
    form.limitExt = d.limitExt || ''
    form.limitSizeBytes = d.limitSizeBytes == null ? '' : Number(d.limitSizeBytes)
    form.limitCount = d.limitCount == null ? '' : Number(d.limitCount)
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

/** 分类键与展示名称必填；编辑时键只读 */
async function onSubmit() {
  if (!assert([
    () => required('分类键', form.classify),
    () => required('展示名称', form.classifyName),
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      classify: form.classify.trim(),
      classifyName: form.classifyName.trim(),
      limitExt: form.limitExt.trim() || undefined,
      limitSizeBytes: form.limitSizeBytes === '' ? null : Number(form.limitSizeBytes),
      limitCount: form.limitCount === '' ? null : Number(form.limitCount),
      status: form.status,
      remark: form.remark.trim() || undefined,
    }
    if (isEdit.value) {
      await updateFileClassify({ ...payload, classifyId: classifyId.value })
    }
    else {
      await addFileClassify(payload)
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
        <text class="qb-form-label">分类键</text>
        <u-input
          v-model="form.classify"
          placeholder="唯一键，创建后不可改"
          border="surround"
          :disabled="isView || isEdit"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">展示名称</text>
        <u-input
          v-model="form.classifyName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">允许后缀</text>
        <u-input
          v-model="form.limitExt"
          :disabled="isView"
          placeholder="如 png,jpg,pdf"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">大小上限（字节）</text>
        <u-input
          v-model="form.limitSizeBytes"
          type="number"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">单次数量上限</text>
        <u-input
          v-model="form.limitCount"
          type="number"
          :disabled="isView"
          placeholder="选填"
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
