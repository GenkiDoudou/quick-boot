<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7ExcelDownload Excel 下载组件</h2>
      <p class="demo-desc">专用于 Excel 文件下载的轻量按钮组件，调用方传入下载函数，组件自动管理 loading、解析文件名、触发浏览器下载。</p>
    </div>

    <DemoSection title="基础用法（文件名从响应头解析）">
      <div class="row">
        <C7ExcelDownload :download-fn="downloadBasic" @success="onSuccess" @error="onError" />
      </div>
      <ResultLog :result="log" />
      <DemoCode :code="code1" />
    </DemoSection>

    <DemoSection title="指定文件名（file-name prop）">
      <div class="row">
        <C7ExcelDownload
          :download-fn="downloadBasic"
          file-name="用户数据导出.xlsx"
          @success="onSuccess"
        />
      </div>
      <ResultLog :result="log" />
      <DemoCode :code="code2" />
    </DemoSection>

    <DemoSection title="自定义按钮样式（透传 el-button 属性）">
      <div class="row">
        <C7ExcelDownload :download-fn="downloadBasic" label="导出 Excel" type="success" @success="onSuccess" />
        <C7ExcelDownload :download-fn="downloadBasic" label="导出" type="warning" plain @success="onSuccess" />
        <C7ExcelDownload :download-fn="downloadBasic" label="下载" type="info" plain size="small" @success="onSuccess" />
        <C7ExcelDownload :download-fn="downloadBasic" label="下载模板" type="primary" link @success="onSuccess" />
      </div>
      <DemoCode :code="code3" />
    </DemoSection>

    <DemoSection title="下载失败处理（@error 事件）">
      <div class="row">
        <C7ExcelDownload
          :download-fn="downloadFail"
          label="触发失败"
          type="danger"
          plain
          @error="onError"
        />
      </div>
      <ResultLog :result="log" />
      <DemoCode :code="code4" />
    </DemoSection>

    <DemoSection title="自定义通知函数（notify prop）">
      <div class="row">
        <C7ExcelDownload
          :download-fn="downloadBasic"
          label="自定义通知下载"
          :notify="customNotify"
          @success="onSuccess"
        />
      </div>
      <DemoCode :code="code5" />
    </DemoSection>

    <DemoSection title="兜底文件名（defaultFileName）">
      <div class="row">
        <C7ExcelDownload
          :download-fn="downloadNoHeader"
          default-file-name="备用文件名.xlsx"
          label="下载（无响应头）"
          @success="onSuccess"
        />
      </div>
      <ResultLog :result="log" />
      <DemoCode :code="code6" />
    </DemoSection>

    <DemoSection title="expose: downloading 状态">
      <div class="row">
        <C7ExcelDownload
          ref="dlRef"
          :download-fn="downloadSlow"
          label="慢速下载"
          @success="onSuccess"
        />
        <el-tag :type="dlRef?.downloading ? 'warning' : 'info'" size="small">
          {{ dlRef?.downloading ? '下载中...' : '空闲' }}
        </el-tag>
      </div>
      <DemoCode :code="code7" />
    </DemoSection>
  </div>
</template>

<script setup>
import { ref, defineComponent, h } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

defineOptions({ name: 'C7ExcelDownloadDemo' })

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

const log = ref(null)
const dlRef = ref(null)

function onSuccess(name) {
  log.value = { event: 'success', fileName: name }
  ElMessage.success(`${name} 下载成功`)
}
function onError(err) {
  log.value = { event: 'error', message: err.message }
}

/** 生成测试 Blob（模拟 xlsx 内容） */
function makeFakeBlob() {
  return new Blob(['fake-excel-content'], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
}

/** 基础下载：返回带响应头的结构 */
async function downloadBasic() {
  await new Promise(r => setTimeout(r, 800))
  return {
    data: makeFakeBlob(),
    headers: { 'content-disposition': 'attachment; filename="demo-export.xlsx"' }
  }
}

/** 无响应头：只返回 Blob */
async function downloadNoHeader() {
  await new Promise(r => setTimeout(r, 800))
  return makeFakeBlob()
}

/** 模拟下载失败 */
async function downloadFail() {
  await new Promise(r => setTimeout(r, 800))
  throw new Error('服务器返回 500，导出失败')
}

/** 慢速下载（用于演示 expose downloading 状态） */
async function downloadSlow() {
  await new Promise(r => setTimeout(r, 2500))
  return {
    data: makeFakeBlob(),
    headers: { 'content-disposition': 'attachment; filename="slow-export.xlsx"' }
  }
}

/** 自定义通知 */
function customNotify(type, msg) {
  ElNotification({ type, title: type === 'success' ? '下载成功' : '下载失败', message: msg, duration: 2000 })
}

const code1 = `async function handleDownload() {
  const res = await request.get('/api/export', { responseType: 'blob' })
  return { data: res.data, headers: res.headers }
}
<C7ExcelDownload :download-fn="handleDownload" />`

const code2 = `<C7ExcelDownload
  :download-fn="handleDownload"
  file-name="用户数据导出.xlsx"
/>`

const code3 = `<C7ExcelDownload :download-fn="fn" label="导出 Excel" type="success" />
<C7ExcelDownload :download-fn="fn" label="导出" type="warning" plain />
<C7ExcelDownload :download-fn="fn" label="下载" size="small" />`

const code4 = `<C7ExcelDownload
  :download-fn="handleDownload"
  @success="(name) => ElMessage.success(name + ' 下载成功')"
  @error="(err) => ElMessage.error(err.message)"
/>`

const code5 = `<C7ExcelDownload
  :download-fn="handleDownload"
  :notify="(type, msg) => ElNotification({ type, title: msg })"
/>`

const code6 = `<!-- 直接返回 Blob 时无法解析文件名，使用 defaultFileName -->
<C7ExcelDownload
  :download-fn="() => fetchBlob()"
  default-file-name="备用文件名.xlsx"
/>`

const code7 = `<C7ExcelDownload ref="dlRef" :download-fn="handleDownload" />
<span>{{ dlRef?.downloading ? '下载中' : '空闲' }}</span>`
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
.result-log {
  margin-top: 10px; padding: 8px 12px; background: #f4f4f5; border-radius: 4px; font-size: 13px;
  .result-label { color: #909399; margin-right: 6px; }
  .result-code { color: #303133; font-family: 'JetBrains Mono', monospace; }
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
