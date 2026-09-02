<template>
  <el-dialog v-model="visible" :title="title" width="720px" destroy-on-close append-to-body @closed="onClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="任务名称" prop="jobName">
        <el-input v-model="form.jobName" placeholder="请输入任务名称" />
      </el-form-item>
      <el-form-item label="任务组名" prop="jobGroup">
        <el-select v-model="form.jobGroup" placeholder="请选择" style="width: 100%">
          <el-option v-for="d in sys_job_group" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="jobType">
        <el-select v-model="form.jobType" placeholder="请选择任务类型" style="width: 100%" @change="onJobTypeChange">
          <el-option v-for="d in sys_job_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>

      <!-- Bean 任务 -->
      <template v-if="form.jobType === '0'">
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
          <el-input v-model="form.params" type="textarea" :rows="2" placeholder="可选，传给 ITask.execute 的字符串参数" />
        </el-form-item>
      </template>

      <!-- HTTP 任务 -->
      <template v-else-if="form.jobType === '1'">
        <el-form-item label="请求 URL" prop="httpConfig.url">
          <el-input v-model="form.httpConfig.url" placeholder="https://example.com/api/health" />
        </el-form-item>
        <el-form-item label="请求方法" prop="httpConfig.method">
          <el-select v-model="form.httpConfig.method" style="width: 100%">
            <el-option v-for="m in httpMethods" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求头">
          <div class="header-list">
            <div v-for="(h, idx) in form.httpConfig.headers" :key="idx" class="header-row">
              <el-input v-model="h.key" placeholder="Header 名" style="width: 40%" />
              <el-input v-model="h.value" placeholder="Header 值" style="width: 50%; margin-left: 8px" />
              <el-button link type="danger" @click="removeHeader(idx)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addHeader">+ 添加请求头</el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="httpBodyVisible" label="请求体">
          <el-input v-model="form.httpConfig.body" type="textarea" :rows="3" placeholder="JSON 或纯文本" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="form.httpConfig.timeoutMs" :min="1000" :max="300000" :step="1000" />
        </el-form-item>
        <el-form-item label="期望状态码">
          <el-input v-model="form.httpConfig.expectStatus" placeholder="200 或 200,201" />
        </el-form-item>
      </template>

      <!-- 脚本任务 -->
      <template v-else-if="form.jobType === '2'">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="脚本任务需在服务端开启 qc.monitor.job.script.enabled 并配置 allowed-dirs 白名单"
          style="margin-bottom: 12px"
        />
        <el-form-item label="脚本路径" prop="scriptConfig.scriptPath">
          <el-input v-model="form.scriptConfig.scriptPath" placeholder="白名单目录内的绝对路径，如 /opt/scripts/backup.sh" />
        </el-form-item>
        <el-form-item label="脚本参数">
          <div class="header-list">
            <div v-for="(arg, idx) in form.scriptConfig.args" :key="idx" class="header-row">
              <el-input v-model="form.scriptConfig.args[idx]" placeholder="参数" style="width: 85%" />
              <el-button link type="danger" @click="removeArg(idx)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addArg">+ 添加参数</el-button>
          </div>
        </el-form-item>
        <el-form-item label="工作目录">
          <el-input v-model="form.scriptConfig.workDir" placeholder="可选，默认同脚本目录" />
        </el-form-item>
        <el-form-item label="超时(秒)">
          <el-input-number v-model="form.scriptConfig.timeoutSec" :min="1" :max="3600" />
        </el-form-item>
      </template>

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
 * 定时任务新增/编辑弹窗：支持 Bean / HTTP / Script 结构化表单。
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

const { sys_job_group, sys_job_status, sys_job_misfire_policy, sys_job_concurrent, sys_job_type } = useDict(
  'sys_job_group',
  'sys_job_status',
  'sys_job_misfire_policy',
  'sys_job_concurrent',
  'sys_job_type'
)

const httpMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS']

const visible = ref(false)
const submitting = ref(false)
const cronOpen = ref(false)
const formRef = ref(null)
const detailNextTimes = ref('')
const detailCronDescription = ref('')
const invokeTargetOptions = ref([])
const invokeTargetLoading = ref(false)

const defaultHttpConfig = () => ({
  url: '',
  method: 'GET',
  headers: [],
  body: '',
  timeoutMs: 30000,
  expectStatus: '200',
})

const defaultScriptConfig = () => ({
  scriptPath: '',
  args: [],
  workDir: '',
  timeoutSec: 60,
})

const defaultForm = () => ({
  jobId: null,
  jobName: '',
  jobGroup: 'DEFAULT',
  jobType: '0',
  invokeTarget: '',
  cronExpression: '',
  misfirePolicy: '3',
  concurrent: '1',
  status: '1',
  params: '',
  remark: '',
  httpConfig: defaultHttpConfig(),
  scriptConfig: defaultScriptConfig(),
})

const form = reactive(defaultForm())

/** POST/PUT/PATCH 才展示请求体。 */
const httpBodyVisible = computed(() => ['POST', 'PUT', 'PATCH'].includes(form.httpConfig?.method))

const cronHint = computed(() => {
  if (detailCronDescription.value) return detailCronDescription.value
  const cron = (form.cronExpression || '').trim()
  if (!cron) return ''
  const parts = cron.split(/\s+/)
  if (parts.length < 6) return ''
  if (parts[0] === '*' && parts[1] !== '*') {
    return `当前为每秒触发（秒=*）。若需每分钟执行请改为：${['0', ...parts.slice(1)].join(' ')}`
  }
  if (parts[0] === '*' && parts[1] === '*') return '当前为每秒触发（秒、分均为 *）'
  if (parts[0] === '0' && parts[1].includes('/')) return '每分钟的第 0 秒执行'
  return ''
})

const rules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  jobGroup: [{ required: true, message: '请选择任务组', trigger: 'change' }],
  jobType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  invokeTarget: [
    {
      validator: (_rule, value, callback) => {
        if (form.jobType !== '0') return callback()
        if (!value) return callback(new Error('请选择调用目标'))
        callback()
      },
      trigger: 'change',
    },
  ],
  'httpConfig.url': [
    {
      validator: (_rule, value, callback) => {
        if (form.jobType !== '1') return callback()
        if (!value || !String(value).trim()) return callback(new Error('请输入请求 URL'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  'scriptConfig.scriptPath': [
    {
      validator: (_rule, value, callback) => {
        if (form.jobType !== '2') return callback()
        if (!value || !String(value).trim()) return callback(new Error('请输入脚本路径'))
        callback()
      },
      trigger: 'blur',
    },
  ],
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
        Object.assign(form, defaultForm(), data)
        form.jobType = data.jobType || '0'
        form.httpConfig = { ...defaultHttpConfig(), ...(data.httpConfig || {}) }
        if (!form.httpConfig.headers) form.httpConfig.headers = []
        form.scriptConfig = { ...defaultScriptConfig(), ...(data.scriptConfig || {}) }
        if (!form.scriptConfig.args) form.scriptConfig.args = []
        detailNextTimes.value = data.nextTimes || ''
        detailCronDescription.value = data.cronDescription || ''
      }
      await loadInvokeTargets()
    }
  }
)

watch(visible, (v) => emit('update:modelValue', v))

function onJobTypeChange() {
  if (form.jobType === '0') {
    form.httpConfig = defaultHttpConfig()
    form.scriptConfig = defaultScriptConfig()
  } else if (form.jobType === '1') {
    form.invokeTarget = ''
    form.params = ''
    form.scriptConfig = defaultScriptConfig()
  } else if (form.jobType === '2') {
    form.invokeTarget = ''
    form.params = ''
    form.httpConfig = defaultHttpConfig()
  }
}

function addHeader() {
  form.httpConfig.headers.push({ key: '', value: '' })
}

function removeHeader(idx) {
  form.httpConfig.headers.splice(idx, 1)
}

function addArg() {
  form.scriptConfig.args.push('')
}

function removeArg(idx) {
  form.scriptConfig.args.splice(idx, 1)
}

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

/** 组装提交载荷：按 jobType 携带结构化字段。 */
function buildPayload() {
  const payload = {
    jobId: form.jobId,
    jobName: form.jobName,
    jobGroup: form.jobGroup,
    jobType: form.jobType,
    cronExpression: form.cronExpression,
    misfirePolicy: form.misfirePolicy,
    concurrent: form.concurrent,
    status: form.status,
    remark: form.remark,
  }
  if (form.jobType === '0') {
    payload.invokeTarget = form.invokeTarget
    payload.params = form.params
  } else if (form.jobType === '1') {
    payload.httpConfig = {
      ...form.httpConfig,
      headers: (form.httpConfig.headers || []).filter((h) => h.key && h.key.trim()),
    }
  } else if (form.jobType === '2') {
    payload.scriptConfig = {
      ...form.scriptConfig,
      args: (form.scriptConfig.args || []).filter((a) => a != null && String(a).trim()),
    }
  }
  return payload
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = buildPayload()
    if (form.jobId) {
      await updateJob(payload)
    } else {
      await addJob(payload)
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
.header-list {
  width: 100%;
}
.header-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
</style>
