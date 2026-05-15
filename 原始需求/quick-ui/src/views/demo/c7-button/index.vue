<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Button 按钮组件</h2>
      <p class="demo-desc">基于 el-button 封装，提供预设类型、防抖、确认框、表单校验、loading 自动管理等能力。</p>
    </div>

    <!-- Section 1: 预设类型 -->
    <demo-section title="预设类型 btnType">
      <div class="btn-row">
        <C7Button btn-type="add" />
        <C7Button btn-type="edit" />
        <C7Button btn-type="delete" />
        <C7Button btn-type="query" />
        <C7Button btn-type="refresh" />
        <C7Button btn-type="upload" />
        <C7Button btn-type="download" />
        <C7Button btn-type="submit" />
        <C7Button btn-type="cancel" />
      </div>
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 自定义文字与类型 -->
    <demo-section title="自定义文字与类型">
      <div class="btn-row">
        <C7Button label="自定义文字" type="primary" />
        <C7Button label="朴素按钮" type="success" :plain="true" />
        <C7Button label="警告" type="warning" />
        <C7Button label="危险" type="danger" />
        <C7Button label="小号" type="info" size="small" />
        <C7Button label="大号" type="primary" size="large" />
      </div>
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 防抖 + loading 自动管理 -->
    <demo-section title="防抖 + Loading 自动管理">
      <div class="btn-row">
        <C7Button
          btn-type="query"
          label="模拟请求（1s）"
          :click-function="mockRequest"
          :is-success-callback="true"
          success-message="请求成功！"
        />
        <C7Button
          btn-type="query"
          label="快速连点测试（防抖300ms）"
          :click-function="mockRequest"
        />
      </div>
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 确认框前置 -->
    <demo-section title="确认框前置">
      <div class="btn-row">
        <C7Button
          btn-type="delete"
          :confirm="true"
          confirm-message="确认要删除这条记录吗？"
          :click-function="mockDelete"
          :is-success-callback="true"
          success-message="删除成功"
        />
        <C7Button
          btn-type="delete"
          label="自定义确认函数"
          :confirm="true"
          :confirm-fn="customConfirmFn"
          :click-function="mockDelete"
          :is-success-callback="true"
          success-message="删除成功（自定义确认）"
        />
      </div>
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 表单校验前置 -->
    <demo-section title="表单校验前置">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" style="max-width:400px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <C7Button
            btn-type="submit"
            :validate="true"
            :validate-ref="formRef"
            :click-function="mockSubmit"
            :is-success-callback="true"
            success-message="提交成功"
          />
          <C7Button
            btn-type="refresh"
            style="margin-left:8px"
            :click-function="() => formRef?.resetFields()"
          />
        </el-form-item>
      </el-form>
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: checkSuccess 自定义判断 -->
    <demo-section title="checkSuccess 自定义结果判断">
      <div class="btn-row">
        <C7Button
          btn-type="submit"
          label="业务成功（code=200）"
          :click-function="mockApiSuccess"
          :check-success="(res) => res.code === 200"
          :is-success-callback="true"
          success-message="业务处理成功"
          :show-error-toast="true"
          error-message="业务处理失败"
        />
        <C7Button
          btn-type="submit"
          label="业务失败（code=500）"
          :click-function="mockApiError"
          :check-success="(res) => res.code === 200"
          :is-success-callback="true"
          success-message="业务处理成功"
          :show-error-toast="true"
          error-message="服务端返回错误"
        />
      </div>
      <demo-code :code="code6" />
    </demo-section>

    <!-- Section 7: 事件回调 -->
    <demo-section title="事件回调 successCallback / errorCallback">
      <div class="btn-row">
        <C7Button
          btn-type="add"
          :click-function="mockRequest"
          :is-success-callback="true"
          @successCallback="onSuccess"
          @errorCallback="onError"
          @before-click="onBeforeClick"
          @after-click="onAfterClick"
        />
      </div>
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" :class="log.type">{{ log.msg }}</p>
        <p v-if="!eventLogs.length" class="empty">点击按钮查看事件日志...</p>
      </div>
      <demo-code :code="code7" />
    </demo-section>

    <!-- Section 8: 禁用状态（attrs 透传） -->
    <demo-section title="禁用状态（$attrs 透传）">
      <div class="btn-row">
        <C7Button btn-type="add" disabled />
        <C7Button btn-type="edit" disabled />
        <C7Button btn-type="delete" disabled />
      </div>
      <demo-code :code="code8" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessageBox } from 'element-plus'

defineOptions({ name: 'C7ButtonDemo' })

// ── 表单 ──
const formRef = ref(null)
const form = reactive({ username: '', email: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

// ── 事件日志 ──
const eventLogs = ref([])
function pushLog(msg, type = 'info') {
  eventLogs.value.unshift({ msg: `[${new Date().toLocaleTimeString()}] ${msg}`, type })
  if (eventLogs.value.length > 8) eventLogs.value.pop()
}
function onSuccess(result) { pushLog(`successCallback 触发，result: ${JSON.stringify(result)}`, 'success') }
function onError(error) { pushLog(`errorCallback 触发，error: ${error?.message}`, 'error') }
function onBeforeClick() { pushLog('before-click 触发', 'info') }
function onAfterClick(s) { pushLog(`after-click 触发，success=${s}`, s ? 'success' : 'error') }

// ── mock 函数 ──
function mockRequest() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 200 }), 1000))
}
function mockDelete() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 200 }), 600))
}
function mockSubmit() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 200 }), 800))
}
function mockApiSuccess() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 200, data: 'ok' }), 600))
}
function mockApiError() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 500, msg: 'server error' }), 600))
}
async function customConfirmFn() {
  try {
    await ElMessageBox.confirm('这是一个自定义的确认框内容，由外部 confirmFn 控制。', '自定义确认', {
      confirmButtonText: '我确认',
      cancelButtonText: '算了',
      type: 'info'
    })
    return true
  } catch {
    return false
  }
}

// ── 示例代码 ──
const code1 = `<C7Button btn-type="add" />
<C7Button btn-type="edit" />
<C7Button btn-type="delete" />
<C7Button btn-type="query" />
<C7Button btn-type="refresh" />
<C7Button btn-type="upload" />
<C7Button btn-type="download" />
<C7Button btn-type="submit" />
<C7Button btn-type="cancel" />`

const code2 = `<C7Button label="自定义文字" type="primary" />
<C7Button label="朴素按钮" type="success" :plain="true" />
<C7Button label="小号" type="info" size="small" />`

const code3 = `<C7Button
  btn-type="query"
  label="模拟请求（1s）"
  :click-function="mockRequest"
  :is-success-callback="true"
  success-message="请求成功！"
/>`

const code4 = `<!-- 内置 ElMessageBox 确认 -->
<C7Button
  btn-type="delete"
  :confirm="true"
  confirm-message="确认要删除这条记录吗？"
  :click-function="handleDelete"
/>

<!-- 自定义确认函数（优先级更高）-->
<C7Button
  btn-type="delete"
  :confirm="true"
  :confirm-fn="() => myCustomConfirm()"
  :click-function="handleDelete"
/>`

const code5 = `<C7Button
  btn-type="submit"
  :validate="true"
  :validate-ref="formRef"
  :click-function="handleSubmit"
  :is-success-callback="true"
  success-message="提交成功"
/>`

const code6 = `<C7Button
  btn-type="submit"
  :click-function="callApi"
  :check-success="(res) => res.code === 200"
  :is-success-callback="true"
  success-message="业务处理成功"
  error-message="服务端返回错误"
/>`

const code7 = `<C7Button
  btn-type="add"
  :click-function="handleAdd"
  :is-success-callback="true"
  @successCallback="onSuccess"
  @errorCallback="onError"
  @before-click="onBeforeClick"
  @after-click="onAfterClick"
/>`

const code8 = `<!-- disabled 通过 $attrs 透传给 el-button -->
<C7Button btn-type="add" disabled />
<C7Button btn-type="edit" disabled />`
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

  .demo-title {
    font-size: 24px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px;
  }

  .demo-desc {
    color: #606266;
    font-size: 14px;
    margin: 0;
    line-height: 1.6;
  }
}

.btn-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 16px 0;
}

.event-log {
  margin-top: 12px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 12px 16px;
  min-height: 80px;
  max-height: 180px;
  overflow-y: auto;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;

  p { margin: 3px 0; }
  .info  { color: #89b4fa; }
  .success { color: #a6e3a1; }
  .error { color: #f38ba8; }
  .empty { color: #6c7086; font-style: italic; }
}
</style>

<!-- 局部子组件：section 容器 -->
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

export const DemoCode = defineComponent({
  name: 'DemoCode',
  props: { code: String },
  setup(props) {
    const open = ref(false)
    return () => h('div', { class: 'code-toggle' }, [
      h('span', {
        class: 'code-toggle-btn',
        onClick: () => { open.value = !open.value }
      }, open.value ? '▲ 收起代码' : '▶ 查看示例代码'),
      open.value ? h('pre', { class: 'code-block' }, h('code', {}, props.code)) : null
    ])
  }
})
</script>

<style>
.demo-section {
  margin-bottom: 36px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}
.section-body { padding-top: 4px; }
.code-toggle { margin-top: 12px; }
.code-toggle-btn {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  user-select: none;
}
.code-toggle-btn:hover { text-decoration: underline; }
.code-block {
  margin-top: 8px;
  background: #282c34;
  color: #abb2bf;
  border-radius: 6px;
  padding: 14px 16px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  overflow-x: auto;
  line-height: 1.6;
}
</style>
