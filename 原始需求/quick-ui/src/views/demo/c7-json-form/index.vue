<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7JsonForm JSON 动态表单</h2>
      <p class="demo-desc">JSON 配置驱动的动态表单，支持多种字段类型、字段联动、栅格布局、表单校验方法暴露。</p>
    </div>

    <!-- Section 1: 基础用法 -->
    <demo-section title="基础用法（多种字段类型）">
      <C7JsonForm v-model="form1" :columns="basicColumns" label-width="90px" ref="formRef1" />
      <div style="display:flex;gap:8px;margin-top:12px;">
        <el-button type="primary" @click="handleSubmit1">提交校验</el-button>
        <el-button @click="formRef1?.resetFields()">重置</el-button>
      </div>
      <val-display :value="form1" />
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 字段联动 -->
    <demo-section title="字段联动（visibleWhen / disabledWhen / optionsWhen）">
      <C7JsonForm v-model="form2" :columns="linkageColumns" label-width="90px" />
      <val-display :value="form2" />
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 栅格布局 -->
    <demo-section title="栅格布局（span / gutter）">
      <C7JsonForm v-model="form3" :columns="gridColumns" label-width="80px" :gutter="16" />
      <val-display :value="form3" />
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 自定义 label 和 slot 字段 -->
    <demo-section title="自定义 label slot + slot 类型字段">
      <C7JsonForm v-model="form4" :columns="slotColumns" label-width="100px">
        <template #label-username>
          <span>用户名</span>
          <el-tooltip content="用于登录的唯一标识，不可重复" placement="top">
            <el-icon style="margin-left:4px;color:#909399"><QuestionFilled /></el-icon>
          </el-tooltip>
        </template>

<script setup>
import { ref } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'C7JsonFormDemo' })

const formRef1 = ref()
const form1 = ref({})
const basicColumns = [
  { prop: 'name',   label: '姓名',    type: 'input',        span: 12, rules: [{ required: true, message: '请输入姓名' }] },
  { prop: 'age',    label: '年龄',    type: 'input-number', span: 12, min: 0, max: 150 },
  { prop: 'gender', label: '性别',    type: 'radio',        span: 12, dataList: [{ label: '男', value: 1 }, { label: '女', value: 2 }] },
  { prop: 'status', label: '状态',    type: 'select',       span: 12, dataList: [{ label: '启用', value: 1 }, { label: '禁用', value: 0 }] },
  { prop: 'birth',  label: '生日',    type: 'date',         span: 12 },
  { prop: 'active', label: '是否激活', type: 'switch',       span: 12 },
  { prop: 'remark', label: '备注',    type: 'textarea',     span: 24, rows: 3 },
]

async function handleSubmit1() {
  try {
    await formRef1.value.validate()
    ElMessage.success('校验通过！')
  } catch {
    ElMessage.error('请检查表单填写')
  }
}

const form2 = ref({ userType: 'admin' })
const linkageColumns = [
  { prop: 'userType', label: '用户类型', type: 'select', span: 12,
    dataList: [{ label: '管理员', value: 'admin' }, { label: '普通用户', value: 'user' }] },
  { prop: 'adminRole', label: '管理角色', type: 'select', span: 12,
    dataList: [{ label: '超级管理员', value: 1 }, { label: '普通管理员', value: 2 }],
    visibleWhen: (form) => form.userType === 'admin' },
  { prop: 'level', label: '用户等级', type: 'select', span: 12,
    optionsWhen: (form) => form.userType === 'admin'
      ? [{ label: '高级', value: 3 }, { label: '顶级', value: 4 }]
      : [{ label: '初级', value: 1 }, { label: '中级', value: 2 }] },
  { prop: 'username', label: '账号', type: 'input', span: 12,
    disabledWhen: (form) => !!form.adminRole,
    tooltip: '选择管理角色后账号不可修改' },
]

const form3 = ref({})
const gridColumns = [
  { prop: 'firstName', label: '名',   type: 'input',    span: 8 },
  { prop: 'lastName',  label: '姓',   type: 'input',    span: 8 },
  { prop: 'phone',     label: '电话', type: 'input',    span: 8 },
  { prop: 'email',     label: '邮箱', type: 'input',    span: 12 },
  { prop: 'dept',      label: '部门', type: 'select',   span: 12,
    dataList: [{ label: '研发部', value: 1 }, { label: '产品部', value: 2 }, { label: '运营部', value: 3 }] },
  { prop: 'address',   label: '地址', type: 'textarea', span: 24, rows: 2 },
]

const form4 = ref({ customField: 0 })
const slotColumns = [
  { prop: 'username',    label: '用户名', type: 'input', span: 12 },
  { prop: 'customField', label: '得分',   type: 'slot',  span: 12 },
]

const form5 = ref({ name: '张三', age: 28, gender: 1, status: 1, active: true, remark: '这是一条备注' })

const form6 = ref({})
const eventColumns = [
  { prop: 'field1', label: '字段A', type: 'input',  span: 12 },
  { prop: 'field2', label: '字段B', type: 'select', span: 12,
    dataList: [{ label: '选项1', value: 1 }, { label: '选项2', value: 2 }] },
]
const eventLogs = ref([])
function onFieldChange(prop, value) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] field-change: ${prop} = ${JSON.stringify(value)}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}

const code1 = `<C7JsonForm v-model="form" :columns="columns" label-width="90px" ref="formRef" />
// columns 示例
// { prop: 'name', label: '姓名', type: 'input', span: 12, rules: [{required:true}] }`

const code2 = `// 字段联动
{ prop: 'adminRole', visibleWhen: (form) => form.userType === 'admin' }
{ prop: 'level',     optionsWhen: (form) => form.userType === 'admin' ? [...] : [...] }
{ prop: 'username',  disabledWhen: (form) => !!form.adminRole }`

const code3 = `// span 控制栅格宽度（24 列）
{ prop: 'firstName', span: 8 }  // 1/3 宽
{ prop: 'email',     span: 12 } // 1/2 宽
{ prop: 'address',   span: 24 } // 全宽
<C7JsonForm :columns="columns" :gutter="16" />`

const code4 = `// slot 类型 + 自定义 label
<C7JsonForm v-model="form" :columns="columns">
  <template #label-username>用户名 <el-tooltip>...</el-tooltip></template>
  <template #customField="{ formData, onChange }">
    <el-input-number v-model="formData.customField" />
    <el-button @click="onChange(100)">满分</el-button>
  </template>
  <template #actions><el-button type="primary">提交</el-button></template>
</C7JsonForm>`

const code5 = `<C7JsonForm v-model="form" :columns="columns" disabled />`

const code6 = `<C7JsonForm
  v-model="form"
  :columns="columns"
  @field-change="(prop, value, formData) => console.log(prop, value)"
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
        <template #customField="{ formData, onChange }">
          <div style="display:flex;gap:8px;">
            <el-input-number v-model="formData.customField" :min="0" :max="100" style="flex:1" />
            <span style="line-height:32px;color:#909399">分</span>
            <el-button size="small" @click="onChange(100)">满分</el-button>
          </div>
        </template>
        <template #actions>
          <el-button type="primary">提交</el-button>
          <el-button>取消</el-button>
        </template>
      </C7JsonForm>
      <val-display :value="form4" />
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 全局禁用 -->
    <demo-section title="全局禁用（disabled）">
      <C7JsonForm v-model="form5" :columns="basicColumns" label-width="90px" disabled />
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: field-change 事件 -->
    <demo-section title="field-change 事件监听">
      <C7JsonForm v-model="form6" :columns="eventColumns" label-width="90px" @field-change="onFieldChange" />
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">操作表单字段后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code6" />
    </demo-section>
  </div>
</template>
