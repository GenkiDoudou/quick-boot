<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7JsonTable JSON 动态表格</h2>
      <p class="demo-desc">JSON 配置驱动的完整表格组件，集成搜索表单、操作工具栏、数据表格、分页、列设置，通过注入 listFunction 获取数据。</p>
    </div>

    <!-- Section 1: 基础用法 -->
    <demo-section title="基础用法（listFunction + tableColumns + searchColumns）">
      <C7JsonTable
        :list-function="mockListFn"
        :table-columns="basicTableColumns"
        :search-columns="basicSearchColumns"
        :show-index="true"
      />
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 删除 + 默认搜索参数 + beforeDelete -->
    <demo-section title="删除功能 + defaultSearchParam + beforeDelete 自定义确认">
      <C7JsonTable
        :list-function="mockListFn"
        :table-columns="basicTableColumns"
        :search-columns="basicSearchColumns"
        :delete-function="mockDeleteFn"
        :default-search-param="{ status: 1 }"
        :before-delete="customBeforeDelete"
        @delete-success="onDeleteSuccess"
      />
      <div v-if="deleteLog" class="event-log">
        <span class="log-item">{{ deleteLog }}</span>
      </div>
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 自定义列 slot + header slot -->
    <demo-section title="slot 列（自定义操作）+ 自定义列标题 header-[prop]">
      <C7JsonTable
        :list-function="mockListFn"
        :table-columns="slotTableColumns"
        :search-columns="basicSearchColumns"
      >
        <!-- 自定义列标题 -->
        <template #header-name>
          姓名 <el-tag size="small" style="margin-left:4px">必填</el-tag>
        </template>
        <!-- 操作列 -->
        <template #operate="{ row }">
          <el-button type="primary" size="small" plain @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" plain @click="handleView(row)">查看</el-button>
        </template>
      </C7JsonTable>
      <div v-if="slotLog" class="event-log">
        <span class="log-item">{{ slotLog }}</span>
      </div>
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 列设置持久化 + toolbar slot -->
    <demo-section title="列设置持久化（columnSettingKey）+ toolbar-left slot">
      <C7JsonTable
        :list-function="mockListFn"
        :table-columns="settingTableColumns"
        :search-columns="basicSearchColumns"
        column-setting-key="demo-c7-json-table-cols"
      >
        <template #toolbar-left="{ selection }">
          <el-button type="warning" plain :disabled="!selection.length" @click="handleBatchOp(selection)">
            批量操作（{{ selection.length }}）
          </el-button>
        </template>
      </C7JsonTable>
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 自定义 rowsKey/totalKey + after-fetch 事件 -->
    <demo-section title="自定义 rowsKey / totalKey + after-fetch 事件">
      <C7JsonTable
        :list-function="mockNestedListFn"
        :table-columns="basicTableColumns"
        rows-key="data.records"
        total-key="data.total"
        @after-fetch="onAfterFetch"
        @before-fetch="onBeforeFetch"
      />
      <div v-if="fetchLog" class="event-log">
        <span class="log-item">{{ fetchLog }}</span>
      </div>
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 通过 ref 暴露方法调用 -->
    <demo-section title="通过 ref 暴露方法（refreshData / getDataList / selectedRows）">
      <div style="margin-bottom:12px;display:flex;gap:8px;">
        <el-button type="primary" plain @click="tableRef6.getDataList()">重置第1页加载</el-button>
        <el-button plain @click="tableRef6.refreshData()">刷新（保留当前页）</el-button>
        <el-button plain @click="showSelected">查看选中行</el-button>
      </div>
      <C7JsonTable
        ref="tableRef6"
        :list-function="mockListFn"
        :table-columns="basicTableColumns"
        :auto-load="true"
      />
      <div v-if="refLog" class="event-log">
        <span class="log-item">{{ refLog }}</span>
      </div>
      <demo-code :code="code6" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

defineOptions({ name: 'DemoC7JsonTable' })

// ── 字典 ──
const statusDict = [
  { label: '启用', value: 1, elTagType: 'success' },
  { label: '禁用', value: 0, elTagType: 'danger' },
]
const levelDict = [
  { label: '普通', value: 1, elTagType: '' },
  { label: 'VIP', value: 2, elTagType: 'warning' },
  { label: '超级VIP', value: 3, elTagType: 'danger' },
]

// ── Mock 数据 ──
const mockRows = [
  { id: 1, name: '张三', dept: '研发部', status: 1, level: 3, amount: 9800, createTime: '2024-01-15' },
  { id: 2, name: '李四', dept: '产品部', status: 0, level: 2, amount: 6500, createTime: '2024-02-20' },
  { id: 3, name: '王五', dept: '运营部', status: 1, level: 1, amount: 4200, createTime: '2024-03-10' },
  { id: 4, name: '赵六', dept: '研发部', status: 1, level: 2, amount: 7700, createTime: '2024-04-05' },
  { id: 5, name: '孙七', dept: '市场部', status: 0, level: 1, amount: 3100, createTime: '2024-05-18' },
]

/** 模拟分页接口 */
async function mockListFn(params) {
  await new Promise(r => setTimeout(r, 300))
  const { pageNum = 1, pageSize = 10, name, status } = params
  let rows = [...mockRows]
  if (name) rows = rows.filter(r => r.name.includes(name))
  if (status !== undefined && status !== '' && status !== null) {
    rows = rows.filter(r => r.status === Number(status))
  }
  const start = (pageNum - 1) * pageSize
  return { data: rows.slice(start, start + pageSize), total: rows.length }
}

/** 模拟嵌套路径接口：{ data: { records, total } } */
async function mockNestedListFn(params) {
  await new Promise(r => setTimeout(r, 300))
  return { code: 0, data: { records: mockRows.slice(0, 3), total: 3 } }
}

/** 模拟删除接口 */
async function mockDeleteFn(ids) {
  await new Promise(r => setTimeout(r, 200))
  return true
}

// ── 基础列配置 ──
const basicTableColumns = [
  { prop: 'name',       label: '姓名',   columnType: 'text' },
  { prop: 'dept',       label: '部门',   columnType: 'text' },
  { prop: 'status',     label: '状态',   columnType: 'tag', dictList: statusDict },
  { prop: 'level',      label: '等级',   columnType: 'tag', dictList: levelDict },
  { prop: 'amount',     label: '薪资',   columnType: 'text', formatter: (r, c, v) => `¥${Number(v).toLocaleString()}` },
  { prop: 'createTime', label: '入职日期', columnType: 'text' },
]

const basicSearchColumns = [
  { prop: 'name',   label: '姓名', type: 'input' },
  { prop: 'status', label: '状态', type: 'select', dataList: statusDict },
]

// ── slot 列配置 ──
const slotTableColumns = [
  { prop: 'name',   label: '姓名', columnType: 'text' },
  { prop: 'dept',   label: '部门', columnType: 'text' },
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
  { prop: 'amount', label: '薪资', columnType: 'text', formatter: (r, c, v) => `¥${Number(v).toLocaleString()}` },
  { prop: 'operate', label: '操作', columnType: 'slot', slotName: 'operate', width: 160, fixed: 'right' },
]

// ── 列设置配置（带不可见列） ──
const settingTableColumns = [
  { prop: 'name',       label: '姓名',   columnType: 'text', order: 1 },
  { prop: 'dept',       label: '部门',   columnType: 'text', order: 2 },
  { prop: 'status',     label: '状态',   columnType: 'tag',  dictList: statusDict, order: 3 },
  { prop: 'amount',     label: '薪资',   columnType: 'text', formatter: (r, c, v) => `¥${Number(v).toLocaleString()}`, order: 4 },
  { prop: 'createTime', label: '入职日期', columnType: 'text', order: 5 },
]

// ── 事件日志 ──
const deleteLog = ref('')
const slotLog = ref('')
const fetchLog = ref('')
const refLog = ref('')
const tableRef6 = ref()

/** 自定义删除前确认 */
async function customBeforeDelete(ids) {
  try {
    await ElMessageBox.confirm(
      `自定义确认：将删除 ${ids.length} 条数据，ID：${ids.join(', ')}`,
      '自定义删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '我再想想' }
    )
    return true
  } catch {
    return false
  }
}

function onDeleteSuccess(ids) {
  deleteLog.value = `[delete-success] 已删除 ID：${ids.join(', ')} — ${new Date().toLocaleTimeString()}`
}

function handleEdit(row) {
  slotLog.value = `[编辑] ${JSON.stringify(row)} — ${new Date().toLocaleTimeString()}`
  ElMessage.success(`编辑：${row.name}`)
}

function handleView(row) {
  slotLog.value = `[查看] ${JSON.stringify(row)} — ${new Date().toLocaleTimeString()}`
  ElMessage.info(`查看：${row.name}`)
}

function handleBatchOp(rows) {
  ElMessage.warning(`批量操作 ${rows.length} 条`)
}

function onBeforeFetch(params) {
  fetchLog.value = `[before-fetch] params: ${JSON.stringify(params)}`
}

function onAfterFetch(rows, total) {
  fetchLog.value = `[after-fetch] rows: ${rows.length} 条，total: ${total} — ${new Date().toLocaleTimeString()}`
}

function showSelected() {
  const rows = tableRef6.value?.selectedRows ?? []
  refLog.value = rows.length
    ? `[selectedRows] ${rows.map(r => r.name).join(', ')}`
    : '[selectedRows] 未选中任何行'
}

// ── 示例代码 ──
const code1 = `<C7JsonTable
  :list-function="getUserList"
  :table-columns="tableColumns"
  :search-columns="searchColumns"
  :show-index="true"
/>
// tableColumns
[
  { prop: 'name',   label: '姓名', columnType: 'text' },
  { prop: 'status', label: '状态', columnType: 'tag', dictList: statusDict },
]
// searchColumns
[
  { prop: 'name',   label: '姓名', type: 'input' },
  { prop: 'status', label: '状态', type: 'select', dataList: statusDict },
]`

const code2 = `<C7JsonTable
  :list-function="getUserList"
  :delete-function="deleteUser"
  :default-search-param="{ status: 1 }"  // 默认只查启用
  :before-delete="async (ids) => { ... return true/false }"
  @delete-success="onDeleteSuccess"
/>`

const code3 = `<C7JsonTable :list-function="getList" :table-columns="columns">
  <!-- 自定义列标题 -->
  <template #header-name>
    姓名 <el-tag size="small">必填</el-tag>
  </template>
  <!-- slot 类型列 -->
  <template #operate="{ row }">
    <el-button type="primary" size="small" @click="edit(row)">编辑</el-button>
  </template>
</C7JsonTable>`

const code4 = `<!-- 列设置状态持久化到 localStorage['demo-c7-json-table-cols'] -->
<C7JsonTable
  :list-function="getList"
  column-setting-key="demo-c7-json-table-cols"
>
  <template #toolbar-left="{ selection }">
    <el-button :disabled="!selection.length">批量操作</el-button>
  </template>
</C7JsonTable>`

const code5 = `<!-- 响应格式：{ data: { records: [], total: 100 } } -->
<C7JsonTable
  :list-function="getList"
  rows-key="data.records"
  total-key="data.total"
  @before-fetch="(params) => console.log(params)"
  @after-fetch="(rows, total) => console.log(rows, total)"
/>`

const code6 = `<C7JsonTable ref="tableRef" :list-function="getList" />

// 调用暴露方法
tableRef.value.refreshData()      // 刷新（保留当前页）
tableRef.value.getDataList()      // 重置第 1 页
console.log(tableRef.value.selectedRows)  // 获取选中行`
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 1200px;
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
  margin-top: 10px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 8px 14px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  .log-item { color: #a6e3a1; }
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
.code-toggle { margin-top: 8px; }
.code-toggle-btn { font-size: 12px; color: #409eff; cursor: pointer; user-select: none; }
.code-toggle-btn:hover { text-decoration: underline; }
.code-block { margin-top: 8px; background: #282c34; color: #abb2bf; border-radius: 6px; padding: 14px 16px; font-size: 12px; font-family: 'JetBrains Mono', 'Fira Code', monospace; overflow-x: auto; line-height: 1.6; white-space: pre; }
</style>