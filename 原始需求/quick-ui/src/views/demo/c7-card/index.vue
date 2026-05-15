<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Card 卡片组件</h2>
      <p class="demo-desc">基于 el-card 封装，支持折叠/展开、色块装饰、标题样式控制、外部状态绑定及自定义 header 操作区。</p>
    </div>

    <!-- Section 1: 基础用法 -->
    <demo-section title="基础用法">
      <C7Card label="基本信息">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">张三</el-descriptions-item>
          <el-descriptions-item label="年龄">28</el-descriptions-item>
          <el-descriptions-item label="部门">技术部</el-descriptions-item>
          <el-descriptions-item label="职位">高级工程师</el-descriptions-item>
        </el-descriptions>
      </C7Card>
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 初始收起 + v-model:expanded -->
    <demo-section title="初始收起 + v-model:expanded 双向绑定">
      <div class="ctrl-bar">
        <el-button size="small" type="primary" plain @click="showAdvanced = !showAdvanced">
          当前状态：{{ showAdvanced ? '已展开' : '已收起' }}，点击切换
        </el-button>
      </div>
      <C7Card
        label="高级配置"
        v-model:expanded="showAdvanced"
        :default-expanded="false"
        @change="onExpandChange"
      >
        <el-alert type="info" :closable="false" title="此处是高级配置内容，通过 v-model:expanded 双向绑定，按钮与卡片状态同步。" />
      </C7Card>
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">折叠/展开卡片后这里会显示 change 事件...</p>
      </div>
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 色块装饰 + 标题样式 -->
    <demo-section title="色块装饰 + 标题样式 textSize / isBold">
      <div class="card-stack">
        <C7Card label="h1 加粗 + 蓝色色块" text-size="h1" show-color-block color-block-color="#409eff">
          <p class="placeholder-text">h1 标题，默认蓝色色块</p>
        </C7Card>
        <C7Card label="h3 普通 + 绿色色块" text-size="h3" :is-bold="false" show-color-block color-block-color="#67C23A">
          <p class="placeholder-text">h3 标题，绿色色块，非加粗</p>
        </C7Card>
        <C7Card label="h5 加粗 + 橙色色块" text-size="h5" show-color-block color-block-color="#E6A23C">
          <p class="placeholder-text">h5 标题，橙色色块</p>
        </C7Card>
      </div>
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: extra slot 操作区 -->
    <demo-section title="#extra slot —— header 右侧操作区">
      <C7Card label="用户列表" show-color-block>
        <template #extra>
          <C7Button btn-type="add" size="small" :click-function="mockAdd" :is-success-callback="true" success-message="新增成功" />
          <C7Button btn-type="refresh" size="small" :click-function="mockRefresh" />
        </template>
        <el-empty description="暂无数据，点击新增按钮添加" :image-size="60" />
      </C7Card>
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 自定义 toggle slot -->
    <demo-section title="#toggle slot —— 完全自定义折叠触发器">
      <C7Card label="自定义触发器">
        <template #toggle="{ expanded, toggle }">
          <el-tag
            :type="expanded ? 'success' : 'info'"
            style="cursor:pointer"
            @click="toggle"
          >
            {{ expanded ? '▲ 收起' : '▼ 展开' }}
          </el-tag>
        </template>
        <el-alert type="success" :closable="false" title="使用 #toggle 插槽完全自定义折叠触发器，作用域暴露 { expanded, toggle }" />
      </C7Card>
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 不可折叠 -->
    <demo-section title=":collapsible=&quot;false&quot; 禁用折叠">
      <C7Card label="固定面板" :collapsible="false" show-color-block color-block-color="#909399">
        <p class="placeholder-text">该卡片禁用了折叠能力，header 右侧不显示折叠按钮。</p>
      </C7Card>
      <demo-code :code="code6" />
    </demo-section>

    <!-- Section 7: 完全自定义 header -->
    <demo-section title="#header slot —— 完全自定义 header">
      <C7Card>
        <template #header>
          <div class="custom-header">
            <el-tag type="danger">自定义</el-tag>
            <span style="font-weight:600;margin-left:8px">完全由 #header slot 控制 header 区域</span>
            <el-button size="small" style="margin-left:auto" type="primary" plain>操作</el-button>
          </div>
        </template>
        <p class="placeholder-text">使用 #header slot 时，内置 header 全部由外部接管，组件不渲染默认 header 结构。</p>
      </C7Card>
      <demo-code :code="code7" />
    </demo-section>

    <!-- Section 8: 暴露方法 ref 调用 -->
    <demo-section title="暴露方法 —— ref 调用 expand / collapse / toggle">
      <div class="ctrl-bar">
        <el-button size="small" @click="cardRef?.expand()">expand()</el-button>
        <el-button size="small" @click="cardRef?.collapse()">collapse()</el-button>
        <el-button size="small" type="primary" plain @click="cardRef?.toggle()">toggle()</el-button>
      </div>
      <C7Card ref="cardRef" label="通过 ref 控制" show-color-block>
        <p class="placeholder-text">通过 defineExpose 暴露的 expand / collapse / toggle 方法，可被父组件直接调用。</p>
      </C7Card>
      <demo-code :code="code8" />
    </demo-section>

    <!-- Section 9: shadow 透传 -->
    <demo-section title="shadow 透传 el-card">
      <div class="card-stack">
        <C7Card label="shadow: always" shadow="always">
          <p class="placeholder-text">始终显示阴影</p>
        </C7Card>
        <C7Card label="shadow: hover" shadow="hover">
          <p class="placeholder-text">悬停时显示阴影</p>
        </C7Card>
        <C7Card label="shadow: never" shadow="never">
          <p class="placeholder-text">从不显示阴影（默认）</p>
        </C7Card>
      </div>
      <demo-code :code="code9" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref, defineComponent, h } from 'vue'

defineOptions({ name: 'C7CardDemo' })

// ── ref ──
const cardRef = ref(null)
const showAdvanced = ref(false)
const eventLogs = ref([])

function onExpandChange(val) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] change 事件触发，expanded = ${val}`)
  if (eventLogs.value.length > 5) eventLogs.value.pop()
}

// ── mock 函数 ──
function mockAdd() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 200 }), 600))
}
function mockRefresh() {
  return new Promise(resolve => setTimeout(() => resolve(), 400))
}

// ── 示例代码 ──
const code1 = `<C7Card label="基本信息">
  <!-- 内容 -->
</C7Card>`

const code2 = `<!-- 初始收起，外部 v-model 双向绑定 -->
<el-button @click="showAdvanced = !showAdvanced">切换</el-button>

<C7Card
  label="高级配置"
  v-model:expanded="showAdvanced"
  :default-expanded="false"
  @change="onExpandChange"
>
  <!-- 内容 -->
</C7Card>`

const code3 = `<C7Card
  label="用户列表"
  text-size="h1"
  show-color-block
  color-block-color="#409eff"
>
  <!-- 内容 -->
</C7Card>

<C7Card
  label="配置项"
  text-size="h3"
  :is-bold="false"
  show-color-block
  color-block-color="#67C23A"
>
  <!-- 内容 -->
</C7Card>`

const code4 = `<C7Card label="用户列表" show-color-block>
  <template #extra>
    <C7Button btn-type="add" size="small" :click-function="handleAdd" />
    <C7Button btn-type="refresh" size="small" :click-function="handleRefresh" />
  </template>
  <!-- 内容 -->
</C7Card>`

const code5 = `<C7Card label="自定义触发器">
  <template #toggle="{ expanded, toggle }">
    <el-tag :type="expanded ? 'success' : 'info'" @click="toggle">
      {{ expanded ? '▲ 收起' : '▼ 展开' }}
    </el-tag>
  </template>
  <!-- 内容 -->
</C7Card>`

const code6 = `<!-- 禁用折叠能力 -->
<C7Card label="固定面板" :collapsible="false" />`

const code7 = `<!-- 完全自定义 header -->
<C7Card>
  <template #header>
    <div class="my-header">
      <el-tag type="danger">自定义</el-tag>
      <span>标题</span>
      <el-button size="small">操作</el-button>
    </div>
  </template>
  <!-- 内容 -->
</C7Card>`

const code8 = `<C7Card ref="cardRef" label="通过 ref 控制">
  <!-- 内容 -->
</C7Card>

<!-- 脚本 -->
const cardRef = ref(null)
cardRef.value?.expand()
cardRef.value?.collapse()
cardRef.value?.toggle()`

const code9 = `<C7Card label="始终阴影" shadow="always" />
<C7Card label="悬浮阴影" shadow="hover" />
<C7Card label="无阴影" shadow="never" />`
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 900px;
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

.ctrl-bar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.card-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.placeholder-text {
  margin: 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
  padding: 4px 0;
}

.custom-header {
  display: flex;
  align-items: center;
  width: 100%;
}

.event-log {
  margin-top: 12px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 10px 14px;
  min-height: 48px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;

  .log-item {
    margin: 2px 0;
    color: #a6e3a1;
  }

  .log-empty {
    margin: 0;
    color: #6c7086;
    font-style: italic;
  }
}
</style>

<!-- 局部子组件 -->
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
  white-space: pre;
}
</style>
