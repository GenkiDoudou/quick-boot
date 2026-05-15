<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7JsonTableColumn 表格列渲染</h2>
      <p class="demo-desc">JSON 配置驱动的表格列渲染组件，支持 text / tag / image / link / slot 五种列类型，内置列可见性过滤与 order 排序，并支持自定义列标题 slot。</p>
    </div>

    <!-- Section 1: text 类型 -->
    <demo-section title="text 类型（默认，支持 formatter / emptyText）">
      <el-table :data="tableData1" border stripe>
        <C7JsonTableColumn :columns="textColumns" />
      </el-table>
      <val-display :value="tableData1" />
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: tag 类型 -->
    <demo-section title="tag 类型（字典标签）">
      <el-table :data="tableData2" border stripe>
        <C7JsonTableColumn :columns="tagColumns" />
      </el-table>
      <val-display :value="tableData2" />
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: image + link 类型 -->
    <demo-section title="image 类型（图片预览）+ link 类型（超链接）">
      <el-table :data="tableData3" border stripe>
        <C7JsonTableColumn :columns="imageAndLinkColumns" />
      </el-table>
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: slot 类型 + 自定义列标题 -->
    <demo-section title="slot 类型（自定义内容）+ 自定义列标题 header-[prop]">
      <el-table :data="tableData4" border stripe>
        <C7JsonTableColumn :columns="slotColumns">
          <!-- 自定义列标题 -->
          <template #header-name>
            姓名 <el-tag size="small" style="margin-left:4px">必填</el-tag>
          </template>
          <!-- 自定义操作列内容 -->
          <template #operate="{ row }">
            <el-button type="primary" size="small" plain @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </C7JsonTableColumn>
      </el-table>
      <div v-if="operateLog" class="operate-log">{{ operateLog }}</div>
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: visible 过滤 + order 排序 -->
    <demo-section title="visible 过滤 + order 排序控制">
      <div style="margin-bottom:12px;display:flex;gap:8px;flex-wrap:wrap">
        <el-tag
          v-for="col in orderableColumns"
          :key="col.prop"
          :type="col.visible === false ? 'info' : 'success'"
          style="cursor:pointer"
          @click="toggleVisible(col)"
        >
          {{ col.label }}（order:{{ col.order }}）{{ col.visible === false ? ' 隐藏' : ' 显示' }}
        </el-tag>
        <span style="font-size:12px;color:#909399;line-height:32px">点击标签切换列显示/隐藏</span>
      </div>
      <el-table :data="tableData5" border stripe>
        <C7JsonTableColumn :columns="orderableColumns" />
      </el-table>
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 综合示例 -->
    <demo-section title="综合示例（所有列类型组合）">
      <el-table :data="tableDataFull" border stripe>
        <el-table-column type="index" label="#" width="55" />
        <C7JsonTableColumn :columns="fullColumns">
          <template #operate="{ row }">
            <el-button type="primary" size="small" plain>编辑</el-button>
            <el-button type="danger" size="small" plain>删除</el-button>
          </template>
        </C7JsonTableColumn>
      </el-table>
      <demo-code :code="code6" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'DemoC7JsonTableColumn' })

// ── 字典数据 ──
const statusDict = [
  { label: '启用', value: 1, elTagType: 'success' },
  { label: '禁用', value: 0, elTagType: 'danger' },
]
const levelDict = [
  { label: '普通', value: 1, elTagType: '' },
  { label: 'VIP', value: 2, elTagType: 'warning' },
  { label: '超级VIP', value: 3, elTagType: 'danger' },
]

// ── Section 1: text 类型 ──
const tableData1 = ref([
  { id: 1, name: '张三', amount: 1234.5, score: 98, deletedAt: null },
  { id: 2, name: '李四', amount: 567.89, score: 0, deletedAt: '2024-03-01' },
  { id: 3, name: '王五', amount: null, score: 75, deletedAt: null },
])
const textColumns = [
  { prop: 'name', label: '姓名', columnType: 'text' },
  {
    prop: 'amount',
    label: '金额',
    columnType: 'text',
    formatter: (row, col, val) => val != null ? `¥${Number(val).toFixed(2)}` : '',
    emptyText: '暂无',
  },
  {
    prop: 'score',
    label: '评分',
    columnType: 'text',
    formatter: (row, col, val) => `${val} 分`,
  },
  { prop: 'deletedAt', label: '删除时间', columnType: 'text', emptyText: '未删除' },
]

// ── Section 2: tag 类型 ──
const tableData2 = ref([
  { id: 1, name: '张三', status: 1, level: 3 },
  { id: 2, name: '李四', status: 0, level: 2 },
  { id: 3, name: '王五', status: 1, level: 1 },
])
const tagColumns = [
  { prop: 'name', label: '姓名', columnType: 'text' },
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
  { prop: 'level', label: '会员等级', columnType: 'tag', dictList: levelDict },
]

// ── Section 3: image + link 类型 ──
const tableData3 = ref([
  {
    id: 1,
    name: '商品A',
    cover: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    detailUrl: 'https://element-plus.org',
  },
  {
    id: 2,
    name: '商品B',
    cover: 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
    detailUrl: 'https://vuejs.org',
  },
  { id: 3, name: '商品C', cover: '', detailUrl: 'https://vitepress.dev' },
])
const imageAndLinkColumns = [
  { prop: 'name', label: '商品名称', columnType: 'text' },
  { prop: 'cover', label: '封面图', columnType: 'image', imageWidth: 60, imageHeight: 60 },
  {
    prop: 'detailUrl',
    label: '链接',
    columnType: 'link',
    linkText: (row) => `查看 ${row.name}`,
    linkHref: (row) => row.detailUrl,
    linkTarget: '_blank',
  },
]

// ── Section 4: slot 类型 + 自定义标题 ──
const tableData4 = ref([
  { id: 1, name: '张三', dept: '研发部', status: 1 },
  { id: 2, name: '李四', dept: '产品部', status: 0 },
  { id: 3, name: '王五', dept: '运营部', status: 1 },
])
const slotColumns = [
  { prop: 'name', label: '姓名', columnType: 'text' },
  { prop: 'dept', label: '部门', columnType: 'text' },
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
  { prop: 'operate', label: '操作', columnType: 'slot', slotName: 'operate', width: 160, fixed: 'right' },
]
const operateLog = ref('')
function handleEdit(row) {
  operateLog.value = `[编辑] ${JSON.stringify(row)}`
  ElMessage.success(`编辑：${row.name}`)
}
function handleDelete(row) {
  operateLog.value = `[删除] ${JSON.stringify(row)}`
  ElMessage.warning(`删除：${row.name}`)
}

// ── Section 5: visible 过滤 + order 排序 ──
const tableData5 = ref([
  { id: 1, name: '张三', age: 28, email: 'zhangsan@example.com', phone: '138****0001', secret: '隐藏数据' },
  { id: 2, name: '李四', age: 32, email: 'lisi@example.com', phone: '139****0002', secret: '隐藏数据' },
])
const orderableColumns = reactive([
  { prop: 'name',   label: '姓名', columnType: 'text', order: 1, visible: true },
  { prop: 'age',    label: '年龄', columnType: 'text', order: 3, visible: true },
  { prop: 'email',  label: '邮箱', columnType: 'text', order: 2, visible: true },
  { prop: 'phone',  label: '电话', columnType: 'text', order: 4, visible: true },
  { prop: 'secret', label: '隐藏列', columnType: 'text', order: 5, visible: false },
])
function toggleVisible(col) {
  col.visible = col.visible === false ? true : false
}

// ── Section 6: 综合示例 ──
const tableDataFull = ref([
  {
    id: 1, name: '张三', amount: 1234.5, status: 1, level: 3,
    avatar: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
    detailUrl: 'https://element-plus.org',
  },
  {
    id: 2, name: '李四', amount: 567.89, status: 0, level: 2,
    avatar: 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
    detailUrl: 'https://vuejs.org',
  },
  { id: 3, name: '王五', amount: null, status: 1, level: 1, avatar: '', detailUrl: '' },
])
const fullColumns = [
  { prop: 'name',      label: '姓名',   columnType: 'text',  order: 1 },
  { prop: 'amount',    label: '金额',   columnType: 'text',  order: 2, formatter: (r, c, v) => v != null ? `¥${Number(v).toFixed(2)}` : '', emptyText: '暂无' },
  { prop: 'status',   label: '状态',   columnType: 'tag',   order: 3, dictList: statusDict },
  { prop: 'level',    label: '等级',   columnType: 'tag',   order: 4, dictList: levelDict },
  { prop: 'avatar',   label: '头像',   columnType: 'image', order: 5, imageWidth: 50, imageHeight: 50 },
  {
    prop: 'detailUrl', label: '详情', columnType: 'link', order: 6,
    linkText: (row) => row.detailUrl ? '查看' : '无',
    linkHref: (row) => row.detailUrl || '#',
    linkTarget: '_blank',
  },
  { prop: 'operate',  label: '操作',   columnType: 'slot',  order: 7, fixed: 'right', width: 140 },
]

// ── 示例代码 ──
const code1 = `const columns = [
  { prop: 'name',      label: '姓名', columnType: 'text' },
  {
    prop: 'amount', label: '金额', columnType: 'text',
    formatter: (row, col, val) => \`¥\${Number(val).toFixed(2)}\`,
    emptyText: '暂无',
  },
  { prop: 'deletedAt', label: '删除时间', columnType: 'text', emptyText: '未删除' },
]
<el-table :data="tableData">
  <C7JsonTableColumn :columns="columns" />
</el-table>`

const code2 = `const statusDict = [
  { label: '启用', value: 1, elTagType: 'success' },
  { label: '禁用', value: 0, elTagType: 'danger' },
]
const columns = [
  { prop: 'name',   label: '姓名', columnType: 'text' },
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
]`

const code3 = `const columns = [
  { prop: 'cover', label: '封面图', columnType: 'image', imageWidth: 60, imageHeight: 60 },
  {
    prop: 'detailUrl', label: '链接', columnType: 'link',
    linkText: (row) => \`查看 \${row.name}\`,
    linkHref: (row) => row.detailUrl,
    linkTarget: '_blank',
  },
]`

const code4 = `const columns = [
  { prop: 'name',    label: '姓名', columnType: 'text' },
  { prop: 'operate', label: '操作', columnType: 'slot', slotName: 'operate', width: 160 },
]
<C7JsonTableColumn :columns="columns">
  <!-- 自定义列标题 -->
  <template #header-name>
    姓名 <el-tag size="small">必填</el-tag>
  </template>
  <!-- 自定义操作列 -->
  <template #operate="{ row }">
    <el-button type="primary" size="small">编辑</el-button>
    <el-button type="danger" size="small">删除</el-button>
  </template>
</C7JsonTableColumn>`

const code5 = `// visible: false 的列不会渲染；order 决定渲染顺序
const columns = [
  { prop: 'name',   label: '姓名', order: 1, visible: true },
  { prop: 'email',  label: '邮箱', order: 2, visible: true },
  { prop: 'age',    label: '年龄', order: 3, visible: true },
  { prop: 'secret', label: '隐藏列', order: 5, visible: false },  // 不渲染
]`

const code6 = `// 综合使用所有列类型
const columns = [
  { prop: 'name',      label: '姓名', columnType: 'text',  order: 1 },
  { prop: 'amount',    label: '金额', columnType: 'text',  order: 2, formatter: ... },
  { prop: 'status',    label: '状态', columnType: 'tag',   order: 3, dictList: statusDict },
  { prop: 'avatar',    label: '头像', columnType: 'image', order: 4, imageWidth: 50 },
  { prop: 'detailUrl', label: '详情', columnType: 'link',  order: 5, linkText: () => '查看' },
  { prop: 'operate',   label: '操作', columnType: 'slot',  order: 6, fixed: 'right' },
]
<C7JsonTableColumn :columns="columns">
  <template #operate="{ row }">
    <el-button type="primary" size="small">编辑</el-button>
  </template>
</C7JsonTableColumn>`
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 1100px;
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
.operate-log {
  margin-top: 10px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 8px 14px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  color: #a6e3a1;
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