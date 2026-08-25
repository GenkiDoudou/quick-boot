<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="classifyId"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-add-button="true"
      :add-button-permi="['system:fileClassify:add']"
      :on-add="openAdd"
      :show-delete-button="true"
      :delete-button-permi="['system:fileClassify:remove']"
      :delete-function="batchDeleteFunction"
      :check-delete-success="() => true"
      :show-export-button="false"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #limitSizeBytes="{ row }">
        {{ formatFileSize(row.limitSizeBytes) }}
      </template>
      <template #compressEnabled="{ row }">
        <C7DictTag :model-value="row.compressEnabled" :options="sys_yes_no" />
      </template>
      <template #anonymous="{ row }">
        <C7DictTag :model-value="row.anonymous" :options="sys_yes_no" />
      </template>
      <template #status="{ row }">
        <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:fileClassify:edit']" @click="openEdit(row)">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除分类「${row.classify}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:fileClassify:remove']"
        />
      </template>
    </C7JsonTable>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增文件分类' : '修改文件分类'" width="600px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="分类键" prop="classify">
          <el-input v-model="form.classify" :disabled="!isAdd" placeholder="如 avatar、default（不可含 /）" />
        </el-form-item>
        <el-form-item label="展示名" prop="classifyName">
          <el-input v-model="form.classifyName" placeholder="展示名称" />
        </el-form-item>
        <el-form-item label="允许后缀" prop="limitExt">
          <el-input v-model="form.limitExt" placeholder="如 png,jpg,pdf；空=内置白名单" />
        </el-form-item>
        <el-form-item label="大小上限(MB)" prop="limitSizeMb">
          <el-input-number v-model="form.limitSizeMb" :min="0.1" :max="1024" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="最多文件数" prop="limitCount">
          <el-input-number v-model="form.limitCount" :min="1" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="开启压缩" prop="compressEnabled">
          <el-switch v-model="form.compressEnabled" active-value="1" inactive-value="0" />
          <span class="form-tip">开启后前端按下方参数压缩；服务端另受 qc.file.compress.enabled 控制</span>
        </el-form-item>
        <template v-if="form.compressEnabled === '1'">
          <el-form-item label="压缩阈值(KB)" prop="compressMinSizeKb">
            <el-input-number
              v-model="form.compressMinSizeKb"
              :min="1"
              :max="102400"
              :step="50"
              controls-position="right"
            />
            <span class="form-tip">超过该大小才压缩</span>
          </el-form-item>
          <el-form-item label="JPEG 质量" prop="compressQuality">
            <el-input-number
              v-model="form.compressQuality"
              :min="0.1"
              :max="1"
              :step="0.05"
              :precision="2"
              controls-position="right"
            />
            <span class="form-tip">0.10–1.00，越大越清晰</span>
          </el-form-item>
          <el-form-item label="最长边(px)" prop="compressMaxEdge">
            <el-input-number
              v-model="form.compressMaxEdge"
              :min="0"
              :max="10000"
              :step="100"
              controls-position="right"
            />
            <span class="form-tip">0 表示不限制边长</span>
          </el-form-item>
        </template>
        <el-form-item label="匿名上传" prop="anonymous">
          <el-switch v-model="form.anonymous" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in (sys_normal_disable || [])" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
    </C7Dialog>
  </div>
</template>

<script setup>
/**
 * 文件分类配置：定义上传 classify 键、大小/数量/后缀限制及压缩参数。
 */
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import { formatFileSize } from '@/api/common/file'
import {
  addFileClassify,
  getFileClassify,
  listFileClassify,
  removeFileClassify,
  updateFileClassify
} from '@/api/system/fileClassify'

defineOptions({ name: 'SysFileClassify' })

const { sys_yes_no, sys_normal_disable } = useDict('sys_yes_no', 'sys_normal_disable')

const tableRef = ref(null)
const formRef = ref(null)
const formVisible = ref(false)
const isAdd = ref(true)

const form = reactive({
  classifyId: null,
  classify: '',
  classifyName: '',
  limitExt: '',
  limitSizeMb: 10,
  limitCount: 1,
  compressEnabled: '0',
  compressMinSizeKb: 200,
  compressQuality: 0.85,
  compressMaxEdge: 1920,
  anonymous: '0',
  status: '0',
  remark: ''
})

const rules = {
  classify: [
    { required: true, message: '必填', trigger: 'blur' },
    {
      validator: (_r, v, cb) => {
        if (v != null && String(v).includes('/')) cb(new Error('分类键不能包含 /'))
        else cb()
      },
      trigger: 'blur'
    }
  ],
  classifyName: [{ required: true, message: '必填', trigger: 'blur' }],
  limitSizeMb: [{ required: true, message: '必填', trigger: 'change' }],
  limitCount: [{ required: true, message: '必填', trigger: 'change' }]
}

const defaultSearch = { classify: '', classifyName: '', status: '' }

const searchColumns = computed(() => [
  { prop: 'classify', label: '分类键', type: 'input', span: 8, props: { clearable: true } },
  { prop: 'classifyName', label: '展示名', type: 'input', span: 8, props: { clearable: true } },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    props: { clearable: true, options: sys_normal_disable.value || [] }
  }
])

const tableColumns = [
  { prop: 'classify', label: '分类键', minWidth: 120 },
  { prop: 'classifyName', label: '展示名', minWidth: 120 },
  { prop: 'limitExt', label: '允许后缀', minWidth: 140, showOverflowTooltip: true },
  { prop: 'limitSizeBytes', label: '大小上限', width: 110, columnType: 'slot', slotName: 'limitSizeBytes' },
  { prop: 'limitCount', label: '数量', width: 80, align: 'center' },
  { prop: 'compressEnabled', label: '压缩', width: 80, columnType: 'slot', slotName: 'compressEnabled' },
  { prop: 'anonymous', label: '匿名', width: 80, columnType: 'slot', slotName: 'anonymous' },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'action', label: '操作', width: 140, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

function listFunction(params) {
  return listFileClassify(params)
}

function batchDeleteFunction(ids) {
  return removeFileClassify(ids)
}

/** MB 与字节互转：表单用 MB 展示，提交 limitSizeBytes */
function mbToBytes(mb) {
  const n = Number(mb)
  if (!Number.isFinite(n) || n <= 0) return 10 * 1024 * 1024
  return Math.round(n * 1024 * 1024)
}

function bytesToMb(bytes) {
  const n = Number(bytes)
  if (!Number.isFinite(n) || n <= 0) return 10
  return Math.round((n / (1024 * 1024)) * 10) / 10
}

function resetForm() {
  Object.assign(form, {
    classifyId: null,
    classify: '',
    classifyName: '',
    limitExt: '',
    limitSizeMb: 10,
    limitCount: 1,
    compressEnabled: '0',
    compressMinSizeKb: 200,
    compressQuality: 0.85,
    compressMaxEdge: 1920,
    anonymous: '0',
    status: '0',
    remark: ''
  })
}

function openAdd() {
  isAdd.value = true
  resetForm()
  formVisible.value = true
}

async function openEdit(row) {
  isAdd.value = false
  const res = await getFileClassify(row.classifyId)
  const data = res?.data || row
  Object.assign(form, {
    classifyId: data.classifyId,
    classify: data.classify,
    classifyName: data.classifyName,
    limitExt: data.limitExt || '',
    limitSizeMb: bytesToMb(data.limitSizeBytes),
    limitCount: data.limitCount > 0 ? data.limitCount : 1,
    compressEnabled: data.compressEnabled === '1' ? '1' : '0',
    compressMinSizeKb: Number(data.compressMinSizeKb) > 0 ? Number(data.compressMinSizeKb) : 200,
    compressQuality: (() => {
      const q = Number(data.compressQuality)
      if (!Number.isFinite(q)) return 0.85
      return Math.min(1, Math.max(0.1, q))
    })(),
    compressMaxEdge: Number.isFinite(Number(data.compressMaxEdge)) ? Math.max(0, Number(data.compressMaxEdge)) : 1920,
    anonymous: data.anonymous === '1' ? '1' : '0',
    status: data.status === '1' ? '1' : '0',
    remark: data.remark || ''
  })
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  const payload = {
    classifyName: form.classifyName,
    limitExt: form.limitExt,
    limitSizeBytes: mbToBytes(form.limitSizeMb),
    limitCount: form.limitCount,
    compressEnabled: form.compressEnabled,
    compressMinSizeKb: form.compressMinSizeKb,
    compressQuality: form.compressQuality,
    compressMaxEdge: form.compressMaxEdge,
    anonymous: form.anonymous,
    status: form.status,
    remark: form.remark
  }
  if (isAdd.value) {
    await addFileClassify({ ...payload, classify: form.classify })
  } else {
    await updateFileClassify({ ...payload, classifyId: form.classifyId })
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  tableRef.value?.refreshData?.()
}

function removeRow(row) {
  return removeFileClassify([row.classifyId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData?.()
  })
}
</script>

<style scoped>
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
