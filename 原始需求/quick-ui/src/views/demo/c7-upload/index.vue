<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Upload 文件上传</h2>
      <p class="demo-desc">基于 el-upload 封装，v-model 绑定逗号分隔 URL 字符串，支持自定义上传函数（httpRequest）、响应解析（responseParser）、文件类型/大小校验、uploading 状态暴露。</p>
    </div>

    <demo-section title="图片上传（picture-card 模式，模拟上传）">
      <C7Upload
        v-model="val1"
        :http-request="mockUpload"
        :response-parser="res => res.url"
        file-type="jpg,png,gif,webp"
        :limit="3"
      />
      <val-display :value="val1" />
      <demo-code :code="code1" />
    </demo-section>

    <demo-section title="文件上传（text 模式）">
      <C7Upload
        v-model="val2"
        :http-request="mockUpload"
        :response-parser="res => res.url"
        file-type="pdf,doc,docx,xlsx"
        :file-size="10"
        :limit="5"
        list-type="text"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          点击上传文件
        </el-button>
      </C7Upload>
      <val-display :value="val2" />
      <demo-code :code="code2" />
    </demo-section>

    <demo-section title="文件大小限制（最大 1MB）">
      <C7Upload
        v-model="val3"
        :http-request="mockUpload"
        :response-parser="res => res.url"
        :file-size="1"
        file-type="jpg,png"
      />
      <val-display :value="val3" />
      <demo-code :code="code3" />
    </demo-section>

    <demo-section title="最多上传1张（limit=1）">
      <C7Upload
        v-model="val4"
        :http-request="mockUpload"
        :response-parser="res => res.url"
        :limit="1"
        file-type="jpg,png"
      />
      <val-display :value="val4" />
      <demo-code :code="code4" />
    </demo-section>

    <demo-section title="事件监听（success / error / remove）">
      <C7Upload
        v-model="val5"
        :http-request="mockUpload"
        :response-parser="res => res.url"
        :limit="3"
        @success="onSuccess"
        @error="onError"
        @remove="onRemove"
      />
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">上传/删除文件后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code5" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Upload } from '@element-plus/icons-vue'

defineOptions({ name: 'C7UploadDemo' })

const val1 = ref('')
const val2 = ref('')
const val3 = ref('')
const val4 = ref('')
const val5 = ref('')

const eventLogs = ref([])

// 模拟上传函数，延迟 800ms 返回假 URL
async function mockUpload({ file }) {
  await new Promise(r => setTimeout(r, 800))
  const fakeUrl = `https://example.com/uploads/${Date.now()}_${file.name}`
  return { url: fakeUrl }
}

function onSuccess(url) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] success: ${url}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onError(err) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] error: ${err?.message}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onRemove(url) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] remove: ${url}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}

const code1 = `<!-- 推荐：使用自定义上传函数，对接项目 axios 拦截器 -->
<C7Upload
  v-model="form.avatar"
  :http-request="uploadFile"
  :response-parser="res => res.data.fileUrl"
  file-type="jpg,png,gif,webp"
  :limit="3"
/>
<!-- form.avatar 输出示例: 'https://cdn.com/a.jpg,https://cdn.com/b.png' -->`

const code2 = `<C7Upload
  v-model="form.attachments"
  :http-request="uploadFile"
  :response-parser="res => res.data.fileUrl"
  file-type="pdf,doc,docx,xlsx"
  :file-size="10"
  :limit="5"
  list-type="text"
>
  <el-button type="primary">点击上传文件</el-button>
</C7Upload>`

const code3 = `<!-- 限制文件最大 1MB -->
<C7Upload
  v-model="form.image"
  :http-request="uploadFile"
  :file-size="1"
  file-type="jpg,png"
/>`

const code4 = `<!-- 最多上传 1 张，超出提示 -->
<C7Upload v-model="form.cover" :http-request="uploadFile" :limit="1" />`

const code5 = `<C7Upload
  v-model="form.files"
  :http-request="uploadFile"
  :response-parser="res => res.data.url"
  @success="(url, res) => console.log('上传成功', url)"
  @error="(err) => console.log('上传失败', err)"
  @remove="(url) => console.log('删除文件', url)"
/>`
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
.event-log {
  margin-top: 12px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 10px 14px;
  min-height: 48px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  .log-item { margin: 2px 0; color: #a6e3a1; }
  .log-empty { margin: 0; color: #6c7086; font-style: italic; }
}
</style>

<script>
import { defineComponent, ref, h } from 'vue'
export const DemoSection = defineComponent({
  name: 'DemoSection',
  props: { title: String },
  setup(props, { slots }) {
    return () => h('div', { class: 'demo-section' }, [
      h('h3', { class: 'section-title' }, props.title),
      h('div', { class: 'section-body' }, slots.default?.())
    ])
  }
})
export const ValDisplay = defineComponent({
  name: 'ValDisplay',
  props: { value: { default: undefined } },
  setup(props) {
    return () => h('div', { class: 'val-display' }, `当前值：${JSON.stringify(props.value)}`)
  }
})
export const DemoCode = defineComponent({
  name: 'DemoCode',
  props: { code: String },
  setup(props) {
    const open = ref(false)
    return () => h('div', { class: 'code-toggle' }, [
      h('span', { class: 'code-toggle-btn', onClick: () => { open.value = !open.value } }, open.value ? '▲ 收起代码' : '▶ 查看示例代码'),
      open.value ? h('pre', { class: 'code-block' }, h('code', {}, props.code)) : null
    ])
  }
})
</script>

<style>
.demo-section { margin-bottom: 36px; background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 20px 24px; box-shadow: 0 1px 4px rgba(0,0,0,.04); }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 12px; padding-bottom: 10px; border-bottom: 1px dashed #ebeef5; }
.section-body { padding-top: 4px; display: flex; flex-direction: column; gap: 10px; }
.val-display { font-size: 12px; color: #909399; font-family: 'JetBrains Mono', 'Fira Code', monospace; background: #f5f7fa; padding: 4px 10px; border-radius: 4px; display: inline-block; }
.code-toggle { margin-top: 8px; }
.code-toggle-btn { font-size: 12px; color: #409eff; cursor: pointer; user-select: none; }
.code-toggle-btn:hover { text-decoration: underline; }
.code-block { margin-top: 8px; background: #282c34; color: #abb2bf; border-radius: 6px; padding: 14px 16px; font-size: 12px; font-family: 'JetBrains Mono', 'Fira Code', monospace; overflow-x: auto; line-height: 1.6; white-space: pre; }
</style>
