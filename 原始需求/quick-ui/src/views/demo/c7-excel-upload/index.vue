<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7ExcelUpload Excel 导入组件</h2>
      <p class="demo-desc">专用于 Excel 批量导入，支持重复数据处理策略选择，上传后展示结果统计，失败记录提供错误文件下载入口。</p>
    </div>

    <DemoSection title="基础用法（覆盖/忽略策略 + 结果统计）">
      <C7ExcelUpload :upload-fn="uploadSuccess" @success="onSuccess" @error="onError" />
      <DemoCode :code="code1" />
    </DemoSection>

    <DemoSection title="含失败记录 + 错误文件下载链接">
      <C7ExcelUpload :upload-fn="uploadWithFail" @success="onSuccess" />
      <DemoCode :code="code2" />
    </DemoSection>

    <DemoSection title="上传失败（接口异常）">
      <C7ExcelUpload :upload-fn="uploadFail" @error="onError" />
      <ResultLog :result="errorLog" />
      <DemoCode :code="code3" />
    </DemoSection>

    <DemoSection title="自定义策略文案">
      <C7ExcelUpload
        :upload-fn="uploadSuccess"
        overwrite-label="更新已有数据"
        ignore-label="保留已有数据"
        @success="onSuccess"
      />
      <DemoCode :code="code4" />
    </DemoSection>

    <DemoSection title="自定义错误文件下载文案（errorFileLabel）">
      <C7ExcelUpload
        :upload-fn="uploadWithFail"
        error-file-label="下载失败数据报告"
        @success="onSuccess"
      />
      <DemoCode :code="code5" />
    </DemoSection>

    <DemoSection title="自定义通知函数（notify）">
      <C7ExcelUpload
        :upload-fn="uploadFail"
        :notify="customNotify"
        @error="onError"
      />
      <DemoCode :code="code6" />
    </DemoSection>

    <DemoSection title="expose: reset 重置状态">
      <C7ExcelUpload ref="uploadRef" :upload-fn="uploadSuccess" @success="onSuccess" />
      <div class="row" style="margin-top:12px">
        <el-button @click="uploadRef?.reset()">重置组件</el-button>
        <el-tag :type="uploadRef?.uploading ? 'warning' : 'info'" size="small">
          {{ uploadRef?.uploading ? '上传中...' : '空闲' }}
        </el-tag>
      </div>
      <DemoCode :code="code7" />
    </DemoSection>

    <DemoSection title="搭配 C7ExcelDownload（下载模板 + 导入）">
      <div class="row">
        <C7ExcelDownload
          :download-fn="downloadTemplate"
          file-name="用户导入模板.xlsx"
          label="下载模板"
          type="info"
          plain
        />
        <C7ExcelUpload :upload-fn="uploadSuccess" @success="onSuccess" />
      </div>
      <DemoCode :code="code8" />
    </DemoSection>

    <div v-if="successLog" class="event-log">
      <p class="log-item">✅ success 事件：{{ JSON.stringify(successLog) }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, defineComponent, h } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

defineOptions({ name: 'C7ExcelUploadDemo' })

const DemoSection = defineComponent({
  name: 'DemoSection',
  props: { title: String },
  setup(props, { slots }) {
    return () => h('div', { class: 'demo-section' }, [
      h('h3', { class: 'section-title' }, props.title),
      h('div', { class: 'section-body' }, slots.default?.())
    ])
  }
})

const DemoCode = defineComponent({
  name: 'DemoCode',
  props: { code: String },
  setup(props) {
    const open = ref(false)
    return () => h('div', { class: 'code-toggle' }, [
      h('span', { class: 'code-toggle-btn', onClick: () => { open.value = !open.value } },
        open.value ? '▲ 收起代码' : '▶ 查看示例代码'),
      open.value ? h('pre', { class: 'code-block' }, h('code', {}, props.code)) : null
    ])
  }
})

const ResultLog = defineComponent({
  name: 'ResultLog',
  props: { result: Object },
  setup(props) {
    return () => props.result
      ? h('div', { class: 'result-log' }, [
          h('span', { class: 'result-label' }, '事件结果：'),
          h('code', { class: 'result-code' }, JSON.stringify(props.result))
        ])
      : null
  }
})

const uploadRef = ref(null)
const successLog = ref(null)
const errorLog = ref(null)

function onSuccess(result) {
  successLog.value = result
}
function onError(err) {
  errorLog.value = { message: err.message }
}

// ── 模拟上传函数 ──
async function uploadSuccess(file, strategy) {
  await new Promise(r => setTimeout(r, 1200))
  return { total: 100, successCount: 100, failCount: 0 }
}

async function uploadWithFail(file, strategy) {
  await new Promise(r => setTimeout(r, 1200))
  return {
    total: 100,
    successCount: 95,
    failCount: 5,
    errorFileUrl: 'https://example.com/files/import-errors-demo.xlsx'
  }
}

async function uploadFail(file, strategy) {
  await new Promise(r => setTimeout(r, 800))
  throw new Error('服务器返回 500，导入失败')
}

async function downloadTemplate() {
  await new Promise(r => setTimeout(r, 500))
  return new Blob(['template'], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
}

function customNotify(type, msg) {
  ElNotification({ type, title: type === 'error' ? '导入失败' : '提示', message: msg, duration: 3000 })
}

// ── 示例代码 ──
const code1 = `async function handleUpload(file, duplicateStrategy) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('duplicateStrategy', duplicateStrategy)
  const res = await request.post('/api/user/import', formData)
  return res.data
}
<C7ExcelUpload :upload-fn="handleUpload" @success="onSuccess" />`

const code2 = `// 后端返回含 errorFileUrl 时自动显示下载链接
// return { total: 100, successCount: 95, failCount: 5,
//   errorFileUrl: 'https://xxx/import-errors.xlsx' }`

const code3 = `<C7ExcelUpload
  :upload-fn="handleUpload"
  @error="(err) => ElMessage.error(err.message)"
/>`

const code4 = `<C7ExcelUpload
  :upload-fn="handleUpload"
  overwrite-label="更新已有数据"
  ignore-label="保留已有数据"
/>`

const code5 = `<C7ExcelUpload
  :upload-fn="handleUpload"
  error-file-label="下载失败数据报告"
/>`

const code6 = `<C7ExcelUpload
  :upload-fn="handleUpload"
  :notify="(type, msg) => ElNotification({ type, title: msg })"
/>`

const code7 = `<C7ExcelUpload ref="uploadRef" :upload-fn="handleUpload" />
<el-button @click="uploadRef.reset()">重置</el-button>`

const code8 = `<C7ExcelDownload :download-fn="downloadTemplate" file-name="模板.xlsx" label="下载模板" type="info" plain />
<C7ExcelUpload :upload-fn="handleUpload" />`
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.demo-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
  .demo-title { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0 0 8px; }
  .demo-desc { color: #606266; font-size: 14px; margin: 0; line-height: 1.6; }
}
.row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.event-log {
  margin-top: 16px; background: #1e1e2e; border-radius: 6px; padding: 10px 14px;
  .log-item { margin: 2px 0; color: #a6e3a1; font-size: 12px; font-family: monospace; }
}
.result-log {
  margin-top: 10px; padding: 8px 12px; background: #f4f4f5; border-radius: 4px; font-size: 13px;
  .result-label { color: #909399; margin-right: 6px; }
  .result-code { color: #303133; font-family: monospace; }
}
</style>

<style>
.demo-section { margin-bottom: 36px; background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 20px 24px; box-shadow: 0 1px 4px rgba(0,0,0,.04); }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 14px; padding-bottom: 10px; border-bottom: 1px dashed #ebeef5; }
.section-body { display: flex; flex-direction: column; gap: 12px; }
.code-toggle { margin-top: 10px; }
.code-toggle-btn { font-size: 12px; color: #409eff; cursor: pointer; user-select: none; }
.code-toggle-btn:hover { text-decoration: underline; }
.code-block { margin-top: 8px; background: #282c34; color: #abb2bf; border-radius: 6px; padding: 14px 16px; font-size: 12px; font-family: 'JetBrains Mono', monospace; overflow-x: auto; line-height: 1.6; white-space: pre; }
</style>
