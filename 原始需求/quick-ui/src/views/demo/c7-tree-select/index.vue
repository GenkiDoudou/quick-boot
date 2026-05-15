<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7TreeSelect 树形选择器</h2>
      <p class="demo-desc">基于 el-tree-select 封装，支持异步加载/静态数据、多选、值类型转换（valueType）、范围值合并（rangeMerge）、可搜索，通过 $attrs 透传其他属性。</p>
    </div>

    <demo-section title="静态数据 — 单选">
      <C7TreeSelect v-model="val1" :data-list="treeData" placeholder="请选择部门" style="width:300px" />
      <val-display :value="val1" />
      <demo-code :code="code1" />
    </demo-section>

    <demo-section title="静态数据 — 多选">
      <C7TreeSelect v-model="val2" :data-list="treeData" multiple placeholder="请选择部门" style="width:300px" />
      <val-display :value="val2" />
      <demo-code :code="code2" />
    </demo-section>

    <demo-section title="多选 — 合并为字符串（rangeMerge）">
      <C7TreeSelect v-model="val3" :data-list="treeData" multiple range-merge placeholder="请选择部门" style="width:300px" />
      <val-display :value="val3" />
      <demo-code :code="code3" />
    </demo-section>

    <demo-section title="父子不关联（checkStrictly）">
      <C7TreeSelect v-model="val4" :data-list="treeData" multiple check-strictly placeholder="请选择节点" style="width:300px" />
      <val-display :value="val4" />
      <demo-code :code="code4" />
    </demo-section>

    <demo-section title="可搜索（filterable）">
      <C7TreeSelect v-model="val5" :data-list="treeData" filterable placeholder="输入关键词搜索" style="width:300px" />
      <val-display :value="val5" />
      <demo-code :code="code5" />
    </demo-section>

    <demo-section title="值类型转换（valueType=string）">
      <C7TreeSelect v-model="val6" :data-list="treeData" value-type="string" placeholder="值强制为字符串" style="width:300px" />
      <val-display :value="val6" />
      <demo-code :code="code6" />
    </demo-section>

    <demo-section title="异步加载（fetchData）">
      <C7TreeSelect v-model="val7" :fetch-data="fetchDeptTree" placeholder="异步加载树数据" style="width:300px" />
      <val-display :value="val7" />
      <demo-code :code="code7" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'C7TreeSelectDemo' })

// 静态树数据
const treeData = [
  {
    value: 1, label: '总公司',
    children: [
      { value: 11, label: '研发部', children: [
        { value: 111, label: '前端组' },
        { value: 112, label: '后端组' }
      ]},
      { value: 12, label: '产品部' },
      { value: 13, label: '运营部' }
    ]
  }
]

const val1 = ref(null)
const val2 = ref([])
const val3 = ref('')
const val4 = ref([])
const val5 = ref(null)
const val6 = ref('')
const val7 = ref(null)

// 模拟异步加载
async function fetchDeptTree() {
  await new Promise(r => setTimeout(r, 800))
  return [
    {
      value: 1, label: '总公司（异步）',
      children: [
        { value: 11, label: '研发部' },
        { value: 12, label: '市场部' }
      ]
    }
  ]
}

const code1 = `<C7TreeSelect v-model="form.deptId" :data-list="treeData" placeholder="请选择部门" />`
const code2 = `<C7TreeSelect v-model="form.depts" :data-list="treeData" multiple placeholder="请选择部门" />
<!-- 输出示例: [11, 12] -->`
const code3 = `<C7TreeSelect v-model="form.depts" :data-list="treeData" multiple range-merge />
<!-- 输出示例: '11,12' -->`
const code4 = `<!-- 父子不关联，父节点可单独选中 -->
<C7TreeSelect v-model="form.nodes" :data-list="treeData" multiple check-strictly />`
const code5 = `<C7TreeSelect v-model="form.deptId" :data-list="treeData" filterable />`
const code6 = `<!-- 节点 value 为数字，valueType=string 强制转为字符串输出 -->
<C7TreeSelect v-model="form.deptId" :data-list="treeData" value-type="string" />
<!-- 输出示例: '11' (string) -->`
const code7 = `<C7TreeSelect v-model="form.deptId" :fetch-data="fetchDeptTree" placeholder="异步加载" />

async function fetchDeptTree() {
  const res = await getDeptTree()
  return res.data
}`
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
