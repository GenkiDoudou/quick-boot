<template>
  <el-dialog v-model="visible" :title="title" width="640px" destroy-on-close append-to-body @closed="onClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="任务名称" prop="jobName">
        <el-input v-model="form.jobName" placeholder="请输入任务名称" />
      </el-form-item>
      <el-form-item label="任务组名" prop="jobGroup">
        <el-select v-model="form.jobGroup" placeholder="请选择" style="width: 100%">
          <el-option v-for="d in sys_job_group" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="调用目标" prop="invokeTarget">
        <el-select
          v-model="form.invokeTarget"
          placeholder="请选择 ITask Bean"
          filterable
          clearable
          style="width: 100%"
          :loading="invokeTargetLoading"
        >
          <el-option
            v-for="item in invokeTargetOptions"
            :key="item.beanName"
            :label="item.label"
            :value="item.beanName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="参数" prop="params">
        <el-input v-model="form.params" type="textarea" :rows="2" placeholder="可选" />
      </el-form-item>
      <el-form-item label="Cron 表达式" prop="cronExpression">
        <el-input v-model="form.cronExpression" placeholder="Quartz 六段：秒 分 时 日 月 周，每分钟示例 0 0/1 * * * ?">
          <template #append>
            <el-button @click="cronOpen = true">生成</el-button>
          </template>
        </el-input>
        <div v-if="cronHint" class="cron-hint">{{ cronHint }}</div>
      </el-form-item>
      <el-form-item label="错失策略" prop="misfirePolicy">
        <el-radio-group v-model="form.misfirePolicy">
          <el-radio v-for="d in sys_job_misfire_policy" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="并发执行" prop="concurrent">
        <el-radio-group v-model="form.concurrent">
          <el-radio v-for="d in sys_job_concurrent" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.jobId" label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="d in sys_job_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item v-if="detailNextTimes" label="下次执行">
        <el-input :model-value="detailNextTimes" type="textarea" :rows="3" readonly />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="!readOnly" type="primary" :loading="submitting" @click="submit">确定</el-button>
    </template>
  </el-dialog>
  <JobCronPicker
    v-model="cronOpen"
    :expression="form.cronExpression"
    @confirm="(v) => (form.cronExpression = v)"
  />
</template>

<script setup>
/**
 * 定时任务新增/编辑弹窗：封装任务表单、Cron 选择器与 ITask Bean 下拉；支持只读查看。
 *
 * @prop {boolean} modelValue 弹窗显隐（v-model）
 * @prop {number|null} jobId 编辑时传入任务 ID；为空则新增
 * @prop {boolean} [readOnly=false] 只读模式（隐藏提交按钮）
 * @emits update:modelValue 关闭弹窗
 * @emits success 保存成功
 */
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import { addJob, getJob, listJobInvokeTargets, updateJob } from '@/api/monitor/job'
import JobCronPicker from '@/components/monitor/JobCronPicker.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  jobId: { type: Number, default: null },
  readOnly: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'success'])

const { sys_job_group, sys_job_status, sys_job_misfire_policy, sys_job_concurrent } = useDict(
  'sys_job_group',
  'sys_job_status',
  'sys_job_misfire_policy',
  'sys_job_concurrent'
)

const visible = ref(false)
const submitting = ref(false)
const cronOpen = ref(false)
const formRef = ref(null)
const detailNextTimes = ref('')
const detailCronDescription = ref('')
const invokeTargetOptions = ref([])
const invokeTargetLoading = ref(false)

/** Quartz 六段 Cron 本地提示（与后端 CronUtils.describe 规则一致）。 */
const cronHint = computed(() => {
  if (detailCronDescription.value) {
    return detailCronDescription.value
  }
  const cron = (form.cronExpression || '').trim()
  if (!cron) return ''
  const parts = cron.split(/\s+/)
  if (parts.length < 6) return ''
  if (parts[0] === '*' && parts[1] !== '*') {
    const fixed = ['0', ...parts.slice(1)].join(' ')
    return `当前为每秒触发（秒=*）。若需每分钟执行请改为：${fixed}`
  }
  if (parts[0] === '*' && parts[1] === '*') {
    return '当前为每秒触发（秒、分均为 *）'
  }
  if (parts[0] === '0' && parts[1].includes('/')) {
    return '每分钟的第 0 秒执行'
  }
  return ''
})

const defaultForm = () => ({
  jobId: null,
  jobName: '',
  jobGroup: 'DEFAULT',
  invokeTarget: '',
  cronExpression: '',
  misfirePolicy: '3',
  concurrent: '1',
  status: '1',
  params: '',
  remark: '',
})

const form = reactive(defaultForm())

const rules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  jobGroup: [{ required: true, message: '请选择任务组', trigger: 'change' }],
  invokeTarget: [{ required: true, message: '请选择调用目标', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入 Cron 表达式', trigger: 'blur' }],
  misfirePolicy: [{ required: true, message: '请选择错失策略', trigger: 'change' }],
  concurrent: [{ required: true, message: '请选择并发策略', trigger: 'change' }],
}

const title = computed(() => {
  if (props.readOnly) return '任务详情'
  return props.jobId ? '修改任务' : '新增任务'
})

watch(
  () => props.modelValue,
  async (v) => {
    visible.value = v
    if (v) {
      Object.assign(form, defaultForm())
      detailNextTimes.value = ''
      detailCronDescription.value = ''
      if (props.jobId) {
        const res = await getJob(props.jobId)
        const data = res.data || res
        Object.assign(form, data)
        detailNextTimes.value = data.nextTimes || ''
        detailCronDescription.value = data.cronDescription || ''
      }
      await loadInvokeTargets()
    }
  }
)

watch(visible, (v) => emit('update:modelValue', v))

/** 加载容器内所有 ITask Bean 供下拉选择。 */
async function loadInvokeTargets() {
  invokeTargetLoading.value = true
  try {
    const res = await listJobInvokeTargets()
    invokeTargetOptions.value = res.data || []
    if (form.invokeTarget && !invokeTargetOptions.value.some((o) => o.beanName === form.invokeTarget)) {
      invokeTargetOptions.value = [
        { beanName: form.invokeTarget, label: form.invokeTarget + '（已配置）' },
        ...invokeTargetOptions.value,
      ]
    }
  } finally {
    invokeTargetLoading.value = false
  }
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.jobId) {
      await updateJob({ ...form })
    } else {
      await addJob({ ...form })
    }
    ElMessage.success('保存成功')
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

function onClosed() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.cron-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-color-warning);
}
</style>
