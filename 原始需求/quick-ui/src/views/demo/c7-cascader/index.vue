<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Cascader 级联选择器</h2>
      <p class="demo-desc">基于 el-cascader 封装，支持静态数据、异步加载、懒加载、多选、值类型控制及多种输出格式。</p>
    </div>

    <!-- Section 1: 静态数据基础用法 -->
    <demo-section title="基础用法（静态数据）">
      <div class="row">
        <C7Cascader v-model="val1" :data-list="regionTree" style="width:320px" />
        <span class="val-display">当前值：{{ JSON.stringify(val1) }}</span>
      </div>
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 多选 + 逗号字符串输出 -->
    <demo-section title="多选 + 逗号字符串输出（resultType=2）">
      <div class="row">
        <C7Cascader v-model="val2" :data-list="regionTree" multiple :result-type="2" style="width:400px" />
        <span class="val-display">当前值：{{ JSON.stringify(val2) }}</span>
      </div>
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 多选 + 数组输出 -->
    <demo-section title="多选 + 数组输出（默认）">
      <div class="row">
        <C7Cascader v-model="val3" :data-list="regionTree" multiple style="width:400px" />
        <span class="val-display">当前值：{{ JSON.stringify(val3) }}</span>
      </div>
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: checkStrictly=false -->
    <demo-section title=":check-strictly=&quot;false&quot; —— 仅可选叶子节点">
      <div class="row">
        <C7Cascader v-model="val4" :data-list="regionTree" :check-strictly="false" style="width:320px" />
        <span class="val-display">当前值：{{ JSON.stringify(val4) }}</span>
      </div>
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: valueType 控制 -->
    <demo-section title="valueType 值类型控制">
      <div class="stack">
        <div class="row">
          <span class="label">valueType="auto"（默认）：</span>
          <C7Cascader v-model="val5a" :data-list="regionTree" value-type="auto" style="width:260px" />
          <span class="val-display">{{ JSON.stringify(val5a) }}</span>
        </div>
        <div class="row">
          <span class="label">valueType="string"：</span>
          <C7Cascader v-model="val5b" :data-list="regionTree" value-type="string" style="width:260px" />
          <span class="val-display">{{ JSON.stringify(val5b) }} ({{ typeof val5b }})</span>
        </div>
        <div class="row">
          <span class="label">valueType="number"：</span>
          <C7Cascader v-model="val5c" :data-list="regionTree" value-type="number" style="width:260px" />
          <span class="val-display">{{ JSON.stringify(val5c) }} ({{ typeof val5c }})</span>
        </div>
      </div>
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 异步加载完整树 -->
    <demo-section title="异步加载完整树（fetchData）">
      <div class="row">
        <C7Cascader v-model="val6" :fetch-data="fetchRegionTree" style="width:320px" />
        <span class="val-display">当前值：{{ JSON.stringify(val6) }}</span>
      </div>
      <demo-code :code="code6" />
    </demo-section>

    <!-- Section 7: 懒加载 -->
    <demo-section title="懒加载（lazy + fetchData）">
      <div class="row">
        <C7Cascader
          v-model="val7"
          :fetch-data="fetchLazyChildren"
          lazy
          :root-parent-id="0"
          style="width:320px"
        />
        <span class="val-display">当前值：{{ JSON.stringify(val7) }}</span>
      </div>
      <demo-code :code="code7" />
    </demo-section>

    <!-- Section 8: 自定义字段名 -->
    <demo-section title="自定义 labelKey / valueKey / resultKey">
      <div class="row">
        <C7Cascader
          v-model="val8"
          :data-list="customTree"
          label-key="name"
          value-key="id"
          result-key="sub"
          style="width:320px"
        />
        <span class="val-display">当前值：{{ JSON.stringify(val8) }}</span>
      </div>
      <demo-code :code="code8" />
    </demo-section>

    <!-- Section 9: change / visible-change 事件 -->
    <demo-section title="事件：change / visible-change">
      <div class="row">
        <C7Cascader
          v-model="val9"
          :data-list="regionTree"
          style="width:320px"
          @change="onChangeEvent"
          @visible-change="onVisibleEvent"
        />
      </div>
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">操作选择器后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code9" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'C7CascaderDemo' })

// ── 静态树形数据（模拟行政区划）──
const regionTree = [
  {
    label: '华东', value: 1,
    children: [
      { label: '上海', value: 11, children: [{ label: '浦东新区', value: 111 }, { label: '黄浦区', value: 112 }] },
      { label: '江苏', value: 12, children: [{ label: '南京', value: 121 }, { label: '苏州', value: 122 }] }
    ]
  },
  {
    label: '华南', value: 2,
    children: [
      { label: '广东', value: 21, children: [{ label: '广州', value: 211 }, { label: '深圳', value: 212 }] },
      { label: '福建', value: 22, children: [{ label: '福州', value: 221 }, { label: '厦门', value: 222 }] }
    ]
  },
  {
    label: '华北', value: 3,
    children: [
      { label: '北京', value: 31, children: [{ label: '朝阳区', value: 311 }, { label: '海淀区', value: 312 }] },
      { label: '天津', value: 32, children: [{ label: '滨海新区', value: 321 }, { label: '河西区', value: 322 }] }
    ]
  }
]

// ── 自定义字段名数据 ──
const customTree = [
  { id: 'a', name: '前端', sub: [{ id: 'a1', name: 'Vue', sub: [] }, { id: 'a2', name: 'React', sub: [] }] },
  { id: 'b', name: '后端', sub: [{ id: 'b1', name: 'Java', sub: [] }, { id: 'b2', name: 'Go', sub: [] }] }
]

// ── 各 Section 绑定值 ──
const val1 = ref(undefined)
const val2 = ref('')
const val3 = ref([])
const val4 = ref(undefined)
const val5a = ref(undefined)
const val5b = ref(undefined)
const val5c = ref(undefined)
const val6 = ref(undefined)
const val7 = ref(undefined)
const val8 = ref(undefined)
const val9 = ref(undefined)

// ── 事件日志 ──
const eventLogs = ref([])
function pushLog(msg) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] ${msg}`)
  if (eventLogs.value.length > 8) eventLogs.value.pop()
}
function onChangeEvent(val) { pushLog(`change 触发，值 = ${JSON.stringify(val)}`) }
function onVisibleEvent(show) { pushLog(`visible-change 触发，show = ${show}`) }

// ── mock 异步加载完整树（模拟 800ms 延迟）──
function fetchRegionTree() {
  return new Promise(resolve => setTimeout(() => resolve(regionTree), 800))
}

// ── mock 懒加载子节点 ──
const lazyData = {
  0:  [{ label: '大区A', value: 'A' }, { label: '大区B', value: 'B' }, { label: '直达叶子', value: 'L', leaf: true }],
  A:  [{ label: 'A-1', value: 'A1' }, { label: 'A-2', value: 'A2', leaf: true }],
  B:  [{ label: 'B-1', value: 'B1', leaf: true }, { label: 'B-2', value: 'B2', leaf: true }],
  A1: [{ label: 'A1-子节点', value: 'A1a', leaf: true }]
}
function fetchLazyChildren(parentId) {
  return new Promise((resolve, reject) =>
    setTimeout(() => {
      const children = lazyData[parentId]
      if (children) resolve(children)
      else reject(new Error('无子节点数据'))
    }, 500)
  )
}

// ── 示例代码 ──
const code1 = `<C7Cascader v-model="form.regionId" :data-list="regionTree" />`

const code2 = `<!-- 多选，输出逗号字符串："111,112" -->
<C7Cascader
  v-model="form.tags"
  :data-list="tagTree"
  multiple
  :result-type="2"
/>`

const code3 = `<!-- 多选，输出数组：[111, 112] -->
<C7Cascader
  v-model="form.tags"
  :data-list="tagTree"
  multiple
/>`

const code4 = `<!-- 关闭 checkStrictly，只能选叶子节点 -->
<C7Cascader
  v-model="form.regionId"
  :data-list="regionTree"
  :check-strictly="false"
/>`

const code5 = `<!-- 强制输出 string 类型 -->
<C7Cascader v-model="form.code" :data-list="tree" value-type="string" />

<!-- 强制输出 number 类型 -->
<C7Cascader v-model="form.id" :data-list="tree" value-type="number" />`

const code6 = `<!-- fetchData 返回 Promise<树形数组> -->
<C7Cascader
  v-model="form.deptId"
  :fetch-data="fetchDeptTree"
/>

async function fetchDeptTree() {
  const res = await getDeptTreeApi()
  return res.data
}`

const code7 = `<!-- 懒加载：fetchData 接收 parentId，返回子节点列表 -->
<C7Cascader
  v-model="form.areaId"
  :fetch-data="fetchAreaChildren"
  lazy
  :root-parent-id="0"
/>

async function fetchAreaChildren(parentId) {
  const res = await getAreaChildrenApi({ parentId })
  return res.data  // [{ label, value, leaf? }]
}`

const code8 = `<!-- 自定义字段名 -->
<C7Cascader
  v-model="form.id"
  :data-list="tree"
  label-key="name"
  value-key="id"
  result-key="sub"
/>`

const code9 = `<C7Cascader
  v-model="form.id"
  :data-list="tree"
  @change="(val) => console.log('change', val)"
  @visible-change="(show) => console.log('visible-change', show)"
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
  gap: 16px;
  flex-wrap: wrap;
  padding: 8px 0;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.label {
  font-size: 13px;
  color: #606266;
  min-width: 160px;
  flex-shrink: 0;
}

.val-display {
  font-size: 12px;
  color: #909399;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  background: #f5f7fa;
  padding: 3px 8px;
  border-radius: 4px;
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
