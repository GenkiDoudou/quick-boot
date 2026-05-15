<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Descriptions 描述列表组件</h2>
      <p class="demo-desc">封装 el-descriptions，通过 items 配置数组驱动渲染，支持嵌套路径、字典标签、图片预览、链接、复制、自定义格式化与自定义 slot。</p>
    </div>

    <!-- Section 1: 基础用法 -->
    <DemoSection title="基础用法">
      <C7Descriptions :data="userInfo" :items="basicItems" :column="3" border />
      <DemoCode :code="code1" />
    </DemoSection>

    <!-- Section 2: 空值兜底 -->
    <DemoSection title="空值兜底文本（defaultEmptyText）">
      <C7Descriptions :data="partialInfo" :items="basicItems" :column="3" default-empty-text="-" border />
      <DemoCode :code="code2" />
    </DemoSection>

    <!-- Section 3: 嵌套属性路径 -->
    <DemoSection title="嵌套属性路径（prop: 'address.city'）">
      <C7Descriptions :data="nestedInfo" :items="nestedItems" :column="2" border />
      <DemoCode :code="code3" />
    </DemoSection>

    <!-- Section 4: 字典标签 -->
    <DemoSection title="字典标签（columnType: tag）">
      <C7Descriptions :data="userInfo" :items="tagItems" :column="3" border />
      <DemoCode :code="code4" />
    </DemoSection>

    <!-- Section 5: 图片预览 -->
    <DemoSection title="图片预览（columnType: image）">
      <C7Descriptions :data="userInfo" :items="imageItems" :column="2" border />
      <DemoCode :code="code5" />
    </DemoSection>

    <!-- Section 6: 超链接 -->
    <DemoSection title="超链接（columnType: link）">
      <C7Descriptions :data="userInfo" :items="linkItems" :column="2" border />
      <DemoCode :code="code6" />
    </DemoSection>

    <!-- Section 7: 复制 -->
    <DemoSection title="复制（copyable / columnType: copy）">
      <C7Descriptions :data="userInfo" :items="copyItems" :column="2" border />
      <DemoCode :code="code7" />
    </DemoSection>

    <!-- Section 8: 格式化 -->
    <DemoSection title="自定义格式化（formatter）">
      <C7Descriptions :data="orderInfo" :items="formatterItems" :column="2" border />
      <DemoCode :code="code8" />
    </DemoSection>

    <!-- Section 9: 自定义 slot -->
    <DemoSection title="自定义 Slot（slotName）">
      <C7Descriptions :data="userInfo" :items="slotItems" :column="2" border>
        <template #statusSlot="{ value, data }">
          <el-tag :type="value === '1' ? 'success' : 'danger'" size="small">
            {{ value === '1' ? '启用' : '禁用' }}
          </el-tag>
          <el-button size="small" link type="primary" style="margin-left:8px" @click="handleToggle(data)">
            切换
          </el-button>
        </template>
        <template #actionSlot="{ data }">
          <el-button size="small" type="primary" @click="handleEdit(data)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(data)">删除</el-button>
        </template>
      </C7Descriptions>
      <div v-if="opLogs.length" class="event-log">
        <p v-for="(log, i) in opLogs" :key="i" class="log-item">{{ log }}</p>
      </div>
      <DemoCode :code="code9" />
    </DemoSection>

    <!-- Section 10: 方向与尺寸 -->
    <DemoSection title="方向与尺寸（direction / size）">
      <div class="ctrl-row">
        <span class="ctrl-label">方向：</span>
        <el-radio-group v-model="direction" size="small">
          <el-radio-button value="horizontal">horizontal</el-radio-button>
          <el-radio-button value="vertical">vertical</el-radio-button>
        </el-radio-group>
        <span class="ctrl-label" style="margin-left:16px">尺寸：</span>
        <el-radio-group v-model="descSize" size="small">
          <el-radio-button value="large">large</el-radio-button>
          <el-radio-button value="default">default</el-radio-button>
          <el-radio-button value="small">small</el-radio-button>
        </el-radio-group>
      </div>
      <C7Descriptions :data="userInfo" :items="basicItems" :column="3" :direction="direction" :size="descSize" border />
      <DemoCode :code="code10" />
    </DemoSection>
  </div>
</template>

<script setup>
import { ref, defineComponent, h } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'C7DescriptionsDemo' })

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

// ── 演示数据 ──
const userInfo = ref({
  name: '张三',
  age: 28,
  gender: '1',
  status: '1',
  phone: '138****8888',
  email: 'zhangsan@example.com',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea0722523def44a45b9e0e23f0jpeg.jpeg',
  homepage: 'https://element-plus.org',
  userId: 1001,
  token: 'eyJhbGciOiJIUzI1NiJ9.demo-token',
  createTime: '2024-01-15 10:30:00',
})

const partialInfo = ref({ name: '李四', age: null, email: '' })

const nestedInfo = ref({
  name: '王五',
  address: { province: '广东省', city: '深圳市', street: '南山区科技园路' },
})

const orderInfo = ref({
  orderId: 'ORD-2024-001',
  amount: 1299.9,
  createTime: '2024-06-20 14:25:38',
  payTime: null,
})

// ── 字典数据 ──
const statusDict = [
  { label: '启用', value: '1', elTagType: 'success' },
  { label: '禁用', value: '0', elTagType: 'danger' },
]
const genderDict = [
  { label: '男', value: '1', elTagType: '' },
  { label: '女', value: '0', elTagType: 'warning' },
]

// ── 交互状态 ──
const direction = ref('horizontal')
const descSize = ref('default')
const opLogs = ref([])

function pushLog(msg) {
  const time = new Date().toLocaleTimeString()
  opLogs.value.unshift(`[${time}] ${msg}`)
  if (opLogs.value.length > 5) opLogs.value.pop()
}
function handleToggle(data) {
  data.status = data.status === '1' ? '0' : '1'
  pushLog(`切换状态 → ${data.status === '1' ? '启用' : '禁用'}`)
}
function handleEdit(data) {
  ElMessage.success(`编辑用户：${data.name}`)
  pushLog(`编辑用户：${data.name}`)
}
function handleDelete(data) {
  ElMessage.warning(`删除用户：${data.name}`)
  pushLog(`删除用户：${data.name}`)
}

// ── items 配置 ──
const basicItems = [
  { prop: 'name',       label: '姓名' },
  { prop: 'age',        label: '年龄' },
  { prop: 'email',      label: '邮箱' },
  { prop: 'phone',      label: '手机号' },
  { prop: 'createTime', label: '创建时间' },
]

const nestedItems = [
  { prop: 'name',             label: '姓名' },
  { prop: 'address.province', label: '省份' },
  { prop: 'address.city',     label: '城市' },
  { prop: 'address.street',   label: '街道', span: 2 },
]

const tagItems = [
  { prop: 'name',   label: '姓名' },
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
  { prop: 'gender', label: '性别', columnType: 'tag', dictList: genderDict },
]

const imageItems = [
  { prop: 'name',   label: '姓名' },
  { prop: 'avatar', label: '头像', columnType: 'image', imageWidth: '64px', imageHeight: '64px' },
]

const linkItems = [
  { prop: 'name', label: '姓名' },
  { prop: 'homepage', label: '官网主页', columnType: 'link', linkText: '访问主页', linkTarget: '_blank' },
  {
    prop: 'userId',
    label: '用户详情',
    columnType: 'link',
    linkHref: (val) => `https://example.com/user/${val}`,
    linkText: (val) => `查看用户 #${val}`,
  },
]

const copyItems = [
  { prop: 'name',  label: '姓名' },
  { prop: 'phone', label: '手机号', copyable: true },
  { prop: 'token', label: 'Token', columnType: 'copy' },
  {
    prop: 'phone',
    label: '手机号（自定义复制）',
    copyable: true,
    copyText: (val) => val.replace(/\*/g, '8'),
  },
]

const formatterItems = [
  { prop: 'orderId', label: '订单号' },
  { prop: 'amount', label: '金额', formatter: (val) => val != null ? `¥${Number(val).toFixed(2)}` : '' },
  { prop: 'createTime', label: '创建时间', formatter: (val) => val ? val.slice(0, 10) : '' },
  { prop: 'payTime', label: '支付时间', emptyText: '未支付', formatter: (val) => val ? val.slice(0, 10) : '' },
]

const slotItems = [
  { prop: 'name',   label: '姓名' },
  { prop: 'status', label: '状态', slotName: 'statusSlot' },
  { label: '操作',  slotName: 'actionSlot' },
]

// ── 示例代码字符串 ──
const code1 = `const items = [
  { prop: 'name',  label: '姓名' },
  { prop: 'age',   label: '年龄' },
  { prop: 'email', label: '邮箱' },
]
<C7Descriptions :data="userInfo" :items="items" :column="3" border />`

const code2 = `<!-- 全局空值显示「-」，单个 item 可用 emptyText 覆盖 -->
<C7Descriptions :data="userInfo" :items="items" default-empty-text="-" border />`

const code3 = `const items = [
  { prop: 'address.province', label: '省份' },
  { prop: 'address.city',     label: '城市' },
  { prop: 'address.street',   label: '街道', span: 2 },
]`

const code4 = `const statusDict = [
  { label: '启用', value: '1', elTagType: 'success' },
  { label: '禁用', value: '0', elTagType: 'danger' },
]
const items = [
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
]`

const code5 = `const items = [
  { prop: 'avatar', label: '头像', columnType: 'image', imageWidth: '64px', imageHeight: '64px' },
]`

const code6 = `const items = [
  { prop: 'homepage', label: '主页', columnType: 'link', linkText: '访问主页', linkTarget: '_blank' },
  {
    prop: 'userId',
    label: '详情',
    columnType: 'link',
    linkHref: (val) => '/user/' + val,
    linkText: (val) => '查看用户 #' + val,
  },
]`

const code7 = `const items = [
  { prop: 'phone', label: '手机号', copyable: true },
  { prop: 'token', label: 'Token', columnType: 'copy' },
  {
    prop: 'phone',
    label: '手机号',
    copyable: true,
    copyText: (val) => val.replace(/*/g, '8'),  // 自定义复制内容
  },
]`

const code8 = `const items = [
  { prop: 'amount', label: '金额', formatter: (val) => '¥' + Number(val).toFixed(2) },
  { prop: 'createTime', label: '创建时间', formatter: (val) => val.slice(0, 10) },
  { prop: 'payTime', label: '支付时间', emptyText: '未支付', formatter: (val) => val?.slice(0, 10) },
]`

const code9 = `const items = [
  { prop: 'status', label: '状态', slotName: 'statusSlot' },
  { label: '操作',  slotName: 'actionSlot' },
]

<C7Descriptions :data="row" :items="items" border>
  <template #statusSlot="{ value, data }">
    <el-tag :type="value === '1' ? 'success' : 'danger'">启用/禁用</el-tag>
    <el-button @click="handleToggle(data)">切换</el-button>
  </template>
  <template #actionSlot="{ data }">
    <el-button @click="handleEdit(data)">编辑</el-button>
  </template>
</C7Descriptions>`

const code10 = `<C7Descriptions
  :data="userInfo"
  :items="items"
  :column="3"
  direction="vertical"
  size="small"
  border
/>`
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 1000px;
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

.ctrl-row {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 6px;
}

.ctrl-label {
  font-size: 13px;
  color: #606266;
}

.event-log {
  margin-top: 12px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;

  .log-item {
    margin: 2px 0;
    color: #a6e3a1;
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