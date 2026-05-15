<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Checkbox 复选框</h2>
      <p class="demo-desc">基于 el-checkbox-group 封装，支持静态/异步数据、全选控制、数组/字符串双模式绑定、三种样式及禁用控制。</p>
    </div>

    <!-- Section 1: 基础用法 -->
    <demo-section title="基础用法（静态数据，输出逗号字符串）">
      <C7Checkbox v-model="val1" :data-list="tagList" />
      <val-display :value="val1" />
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 输出数组 -->
    <demo-section title=":join-value=&quot;false&quot; — 输出数组">
      <C7Checkbox v-model="val2" :data-list="tagList" :join-value="false" />
      <val-display :value="val2" />
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 全选控制 -->
    <demo-section title="全选控制（indeterminate）">
      <C7Checkbox v-model="val3" :data-list="tagList" indeterminate :join-value="false" />
      <val-display :value="val3" />
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 按钮样式 -->
    <demo-section title="按钮样式（checkbox-style=&quot;button&quot;）">
      <C7Checkbox v-model="val4" :data-list="tagList" checkbox-style="button" />
      <val-display :value="val4" />
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 边框样式 -->
    <demo-section title="边框样式（checkbox-style=&quot;border&quot;）">
      <C7Checkbox v-model="val5" :data-list="tagList" checkbox-style="border" />
      <val-display :value="val5" />
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 全选 + 按钮样式 -->
    <demo-section title="全选 + 按钮样式">
      <C7Checkbox v-model="val6" :data-list="tagList" indeterminate checkbox-style="button" />
      <val-display :value="val6" />
      <demo-code :code="code6" />
    </demo-section>

    <!-- Section 7: 整体禁用 -->
    <demo-section title="整体禁用（disabled）">
      <C7Checkbox v-model="val7" :data-list="tagList" disabled />
      <val-display :value="val7" />
      <demo-code :code="code7" />
    </demo-section>

    <!-- Section 8: 单项禁用 -->
    <demo-section title="单项禁用（Option.disabled）">
      <C7Checkbox v-model="val8" :data-list="tagListWithDisabled" />
      <val-display :value="val8" />
      <demo-code :code="code8" />
    </demo-section>

    <!-- Section 9: 限制选择数量 -->
    <demo-section title="限制选择数量（min=1 max=3）">
      <C7Checkbox v-model="val9" :data-list="tagList" :min="1" :max="3" :join-value="false" />
      <val-display :value="val9" />
      <demo-code :code="code9" />
    </demo-section>

    <!-- Section 10: 异步加载 -->
    <demo-section title="异步加载（fetchData）">
      <C7Checkbox v-model="val10" :fetch-data="fetchTags" :join-value="false" />
      <val-display :value="val10" />
      <demo-code :code="code10" />
    </demo-section>

    <!-- Section 11: 自定义字段名 -->
    <demo-section title="自定义字段名（labelKey / valueKey）">
      <C7Checkbox v-model="val11" :data-list="customList" label-key="name" value-key="id" :join-value="false" />
      <val-display :value="val11" />
      <demo-code :code="code11" />
    </demo-section>

    <!-- Section 12: change 事件 -->
    <demo-section title="事件：change">
      <C7Checkbox v-model="val12" :data-list="tagList" :join-value="false" @change="onChangeEvent" />
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">操作复选框后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code12" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'C7CheckboxDemo' })

// ── 静态数据 ──
const tagList = [
  { label: '前端', value: 'fe' },
  { label: '后端', value: 'be' },
  { label: '运维', value: 'ops' },
  { label: '测试', value: 'qa' },
  { label: '产品', value: 'pm' },
]

// ── 含单项禁用的数据 ──
const tagListWithDisabled = [
  { label: '前端', value: 'fe' },
  { label: '后端', value: 'be', disabled: true },
  { label: '运维', value: 'ops' },
  { label: '测试', value: 'qa', disabled: true },
  { label: '产品', value: 'pm' },
]

// ── 自定义字段名数据 ──
const customList = [
  { id: 1, name: 'Vue' },
  { id: 2, name: 'React' },
  { id: 3, name: 'Angular' },
]

// ── 各 Section 绑定值 ──
const val1 = ref('')
const val2 = ref([])
const val3 = ref([])
const val4 = ref('')
const val5 = ref('')
const val6 = ref([])
const val7 = ref('fe,ops')
const val8 = ref('')
const val9 = ref([])
const val10 = ref([])
const val11 = ref([])
const val12 = ref([])

// ── 事件日志 ──
const eventLogs = ref([])
function onChangeEvent(val) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] change 触发，值 = ${JSON.stringify(val)}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}

// ── mock 异步加载（模拟 600ms 延迟）──
function fetchTags() {
  return new Promise((resolve) =>
    setTimeout(() => resolve(tagList), 600)
  )
}

// ── 示例代码 ──
const code1 = `<C7Checkbox v-model="form.tags" :data-list="tagList" />
<!-- 输出示例："fe,ops" -->`

const code2 = `<!-- join-value=false: 输出数组 -->
<C7Checkbox v-model="form.tags" :data-list="tagList" :join-value="false" />
<!-- 输出示例：['fe', 'ops'] -->`

const code3 = `<!-- indeterminate: 显示全选控件 -->
<C7Checkbox
  v-model="form.tags"
  :data-list="tagList"
  indeterminate
  :join-value="false"
/>`

const code4 = `<C7Checkbox
  v-model="form.tags"
  :data-list="tagList"
  checkbox-style="button"
/>`

const code5 = `<C7Checkbox
  v-model="form.tags"
  :data-list="tagList"
  checkbox-style="border"
/>`

const code6 = `<C7Checkbox
  v-model="form.tags"
  :data-list="tagList"
  indeterminate
  checkbox-style="button"
/>`

const code7 = `<C7Checkbox v-model="form.tags" :data-list="tagList" disabled />`

const code8 = `const tagList = [
  { label: '前端', value: 'fe' },
  { label: '后端', value: 'be', disabled: true },
  { label: '运维', value: 'ops' },
]
<C7Checkbox v-model="form.tags" :data-list="tagList" />`

const code9 = `<!-- 最少选 1 个，最多选 3 个 -->
<C7Checkbox
  v-model="form.tags"
  :data-list="tagList"
  :min="1"
  :max="3"
  :join-value="false"
/>`

const code10 = `<C7Checkbox
  v-model="form.roles"
  :fetch-data="fetchRoles"
  :join-value="false"
/>

async function fetchRoles() {
  const res = await getRoleListApi()
  return res.data  // [{ label, value }]
}`

const code11 = `<C7Checkbox
  v-model="form.ids"
  :data-list="list"
  label-key="name"
  value-key="id"
  :join-value="false"
/>`

const code12 = `<C7Checkbox
  v-model="form.tags"
  :data-list="tagList"
  :join-value="false"
  @change="(val) => console.log('change', val)"
/>
<!-- change 事件参数始终为数组 -->`
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

export const ValDisplay = defineComponent({
  name: 'ValDisplay',
  props: { value: { default: undefined } },
  setup(props) {
    return () => h('div', { class: 'val-display' },
      `当前值：${JSON.stringify(props.value)}`
    )
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
  margin: 0 0 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}
.section-body {
  padding-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.val-display {
  font-size: 12px;
  color: #909399;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 4px;
  display: inline-block;
}
.code-toggle { margin-top: 8px; }
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
