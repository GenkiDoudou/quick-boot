<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7MessageBox 对话框工具</h2>
      <p class="demo-desc">函数式对话框工具集，封装 ElMessageBox，支持异步确认自动 loading、危险操作确认、输入框、全局 Loading 及全局默认配置。</p>
    </div>

    <DemoSection title="c7Confirm — 基础确认对话框">
      <div class="row">
        <el-button type="primary" @click="demo1">基础确认</el-button>
        <el-button @click="demo1Cancel">确认（点取消）</el-button>
      </div>
      <ResultLog :result="results.confirm" />
      <DemoCode :code="code1" />
    </DemoSection>

    <DemoSection title="c7Confirm — 异步确认（onConfirm 自动 loading）">
      <div class="row">
        <el-button type="primary" @click="demo2(false)">异步确认（成功）</el-button>
        <el-button type="warning" @click="demo2(true)">异步确认（失败）</el-button>
      </div>
      <ResultLog :result="results.async" />
      <DemoCode :code="code2" />
    </DemoSection>

    <DemoSection title="c7Alert — 提示对话框">
      <div class="row">
        <el-button type="success" @click="demo3">打开 Alert</el-button>
      </div>
      <ResultLog :result="results.alert" />
      <DemoCode :code="code3" />
    </DemoSection>

    <DemoSection title="c7Prompt — 输入框对话框">
      <div class="row">
        <el-button type="primary" @click="demo4">打开 Prompt</el-button>
        <el-button @click="demo4Validate">带输入校验</el-button>
      </div>
      <ResultLog :result="results.prompt" />
      <DemoCode :code="code4" />
    </DemoSection>

    <DemoSection title="c7DangerConfirm — 危险操作确认">
      <div class="row">
        <el-button type="danger" @click="demo5">危险操作确认</el-button>
        <el-button type="danger" plain @click="demo5Async">危险 + 异步确认</el-button>
      </div>
      <ResultLog :result="results.danger" />
      <DemoCode :code="code5" />
    </DemoSection>

    <DemoSection title="c7Loading — 全局 Loading">
      <div class="row">
        <el-button type="primary" @click="demo6">触发 Loading（2 秒）</el-button>
        <el-button @click="demo6Custom">自定义文字</el-button>
      </div>
      <ResultLog :result="results.loading" />
      <DemoCode :code="code6" />
    </DemoSection>

    <DemoSection title="setMessageBoxDefaults — 全局默认配置">
      <div class="row">
        <el-button @click="demo7">设置默认配置后打开确认框</el-button>
      </div>
      <ResultLog :result="results.defaults" />
      <DemoCode :code="code7" />
    </DemoSection>

    <DemoSection title="c7MessageBox 命名空间用法">
      <div class="row">
        <el-button type="primary" @click="demo8">通过命名空间调用</el-button>
      </div>
      <ResultLog :result="results.namespace" />
      <DemoCode :code="code8" />
    </DemoSection>
  </div>
</template>

<script setup>
import { ref, reactive, defineComponent, h } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import {
  c7Confirm, c7Alert, c7Prompt, c7DangerConfirm,
  c7Loading, c7MessageBox, setMessageBoxDefaults
} from '@/packages'

defineOptions({ name: 'C7MessageBoxDemo' })

// ── 局部子组件 ──
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
          h('span', { class: 'result-label' }, '返回值：'),
          h('code', { class: 'result-code' }, JSON.stringify(props.result))
        ])
      : null
  }
})

const results = reactive({
  confirm: null, async: null, alert: null,
  prompt: null, danger: null, loading: null,
  defaults: null, namespace: null
})

// ── Section 1: 基础确认 ──
async function demo1() {
  results.confirm = await c7Confirm('确定要删除这条记录吗？', '删除确认')
}
async function demo1Cancel() {
  results.confirm = await c7Confirm('点击取消查看返回值', '提示')
}

// ── Section 2: 异步确认 ──
async function demo2(shouldFail) {
  results.async = await c7Confirm(
    shouldFail ? '此次将模拟失败（不关闭对话框）' : '此次将模拟成功（1.5s 后自动关闭）',
    '异步确认演示',
    {
      asyncConfirm: async () => {
        await new Promise((resolve, reject) =>
          setTimeout(() => shouldFail ? reject(new Error('模拟业务失败')) : resolve(), 1500)
        )
      },
      errorNotify: (err) => ElNotification({
        type: 'error',
        title: '操作失败',
        message: err?.message || '未知错误'
      })
    }
  )
}

// ── Section 3: Alert ──
async function demo3() {
  results.alert = await c7Alert('您的操作已成功提交，请等待审核。', '操作成功')
}

// ── Section 4: Prompt ──
async function demo4() {
  results.prompt = await c7Prompt('请输入备注信息', '添加备注')
}
async function demo4Validate() {
  results.prompt = await c7Prompt('名称不能包含空格', '带校验的 Prompt', {
    inputPattern: /^\S+$/,
    inputErrorMessage: '名称不能包含空格'
  })
}

// ── Section 5: DangerConfirm ──
async function demo5() {
  results.danger = await c7DangerConfirm(
    '此操作将永久删除该数据，且不可恢复，请谨慎操作！',
    '⚠️ 危险操作'
  )
}
async function demo5Async() {
  results.danger = await c7DangerConfirm('确认清空所有日志？', '清空日志', {
    asyncConfirm: async () => {
      await new Promise(resolve => setTimeout(resolve, 1500))
      ElMessage.success('清空成功')
    }
  })
}

// ── Section 6: Loading ──
async function demo6() {
  results.loading = null
  const loading = c7Loading()
  await new Promise(resolve => setTimeout(resolve, 2000))
  loading.close()
  results.loading = { action: 'closed', message: 'loading 已关闭' }
}
async function demo6Custom() {
  results.loading = null
  const loading = c7Loading('正在同步数据，请稍候...')
  await new Promise(resolve => setTimeout(resolve, 2000))
  loading.close()
  results.loading = { action: 'closed', message: '自定义文字 loading 已关闭' }
}

// ── Section 7: setDefaults ──
async function demo7() {
  setMessageBoxDefaults({ draggable: true, closeOnClickModal: false })
  results.defaults = await c7Confirm(
    '此对话框已应用全局默认配置：draggable=true, closeOnClickModal=false',
    '全局配置演示'
  )
}

// ── Section 8: 命名空间 ──
async function demo8() {
  results.namespace = await c7MessageBox.confirm('通过 c7MessageBox.confirm 调用', '命名空间')
}

// ── 示例代码 ──
const code1 = `const result = await c7Confirm('确定删除吗？', '删除确认')
if (result.action === 'confirm') {
  await api.delete(id)
}`

const code2 = `await c7Confirm('确定提交吗？', '提交', {
  asyncConfirm: async () => {
    await api.submit(form)  // 抛出异常则不关闭
  },
  errorNotify: (err) => ElNotification({ type: 'error', message: err.message })
})`

const code3 = `await c7Alert('操作已成功提交，请等待审核。', '提示')`

const code4 = `const { action, value } = await c7Prompt('请输入备注', '备注')
if (action === 'confirm') {
  console.log('备注：', value)
}

// 带校验
await c7Prompt('请输入名称', '新建', {
  inputPattern: /^\\S+$/,
  inputErrorMessage: '名称不能为空'
})`

const code5 = `await c7DangerConfirm('此操作不可撤销！', '危险操作')

// 异步确认
await c7DangerConfirm('确认清空所有数据？', '警告', {
  asyncConfirm: async () => { await api.clearAll() }
})`

const code6 = `const loading = c7Loading('正在处理...')
try {
  await longOperation()
} finally {
  loading.close()
}`

const code7 = `// 在 main.js 中设置一次即可
setMessageBoxDefaults({
  draggable: true,
  closeOnClickModal: false,
})`

const code8 = `import { c7MessageBox } from '@/packages'

c7MessageBox.confirm('确认操作？')
c7MessageBox.alert('操作成功')
c7MessageBox.loading('加载中...')
c7MessageBox.setDefaults({ draggable: true })`
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

.row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.result-log {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f4f4f5;
  border-radius: 4px;
  font-size: 13px;

  .result-label {
    color: #909399;
    margin-right: 6px;
  }

  .result-code {
    color: #303133;
    font-family: 'JetBrains Mono', monospace;
  }
}
</style>

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
  margin: 0 0 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}
.section-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.code-toggle { margin-top: 10px; }
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
  white-space: pre;
}
</style>
