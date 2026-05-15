<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7DictTag 字典标签组件</h2>
      <p class="demo-desc">将字典值转换为对应标签显示，支持单值、数组、逗号分隔字符串，内置超出折叠、未匹配值显示等能力。</p>
    </div>

    <DemoSection title="基础用法">
      <div class="row">
        <span class="label">状态 1：</span><C7DictTag :options="statusDict" model-value="1" />
        <span class="label">状态 0：</span><C7DictTag :options="statusDict" model-value="0" />
        <span class="label">数字值：</span><C7DictTag :options="statusDict" :model-value="1" />
      </div>
      <DemoCode :code="code1" />
    </DemoSection>

    <DemoSection title="多值（数组 / 逗号字符串）">
      <div class="row">
        <span class="label">数组：</span><C7DictTag :options="roleDict" :model-value="['1','2','3']" />
      </div>
      <div class="row">
        <span class="label">逗号串：</span><C7DictTag :options="roleDict" model-value="1,2,3" />
      </div>
      <DemoCode :code="code2" />
    </DemoSection>

    <DemoSection title="限制显示数量（max）">
      <div class="row">
        <span class="label">max=3：</span><C7DictTag :options="roleDict" model-value="1,2,3,4,5" :max="3" />
      </div>
      <div class="row">
        <span class="label">max=2：</span><C7DictTag :options="roleDict" model-value="1,2,3,4,5" :max="2" />
      </div>
      <DemoCode :code="code3" />
    </DemoSection>

    <DemoSection title="超出折叠 Tooltip（max + collapse）">
      <div class="row">
        <span class="label">max=3 + collapse：</span>
        <C7DictTag :options="roleDict" model-value="1,2,3,4,5" :max="3" collapse />
      </div>
      <DemoCode :code="code4" />
    </DemoSection>

    <DemoSection title="标签效果（effect）">
      <div class="row">
        <span class="label">light（默认）：</span><C7DictTag :options="levelDict" model-value="1,2,3" effect="light" />
      </div>
      <div class="row">
        <span class="label">dark：</span><C7DictTag :options="levelDict" model-value="1,2,3" effect="dark" />
      </div>
      <div class="row">
        <span class="label">plain：</span><C7DictTag :options="levelDict" model-value="1,2,3" effect="plain" />
      </div>
      <DemoCode :code="code5" />
    </DemoSection>

    <DemoSection title="圆角标签（round）">
      <div class="row">
        <C7DictTag :options="levelDict" model-value="1,2,3" round />
      </div>
      <DemoCode :code="code6" />
    </DemoSection>

    <DemoSection title="纯文本模式（elTagType: 'text'）">
      <div class="row">
        <C7DictTag :options="textDict" model-value="1,0" />
      </div>
      <DemoCode :code="code7" />
    </DemoSection>

    <DemoSection title="尺寸（size）">
      <div class="row">
        <span class="label">large：</span><C7DictTag :options="statusDict" model-value="1" size="large" />
        <span class="label">default：</span><C7DictTag :options="statusDict" model-value="1" size="default" />
        <span class="label">small：</span><C7DictTag :options="statusDict" model-value="1" size="small" />
      </div>
      <DemoCode :code="code8" />
    </DemoSection>

    <DemoSection title="未匹配原始值（showValue）">
      <div class="row">
        <span class="label">showValue=true（默认）：</span><C7DictTag :options="statusDict" model-value="9" />
        <span class="label">showValue=false：</span><C7DictTag :options="statusDict" model-value="9" :show-value="false" />
      </div>
      <DemoCode :code="code9" />
    </DemoSection>

    <DemoSection title="空值兜底">
      <div class="row">
        <span class="label">undefined：</span><C7DictTag :options="statusDict" :model-value="undefined" />
        <span class="label">空字符串：</span><C7DictTag :options="statusDict" model-value="" />
        <span class="label">null：</span><C7DictTag :options="statusDict" :model-value="null" />
      </div>
      <DemoCode :code="code10" />
    </DemoSection>
  </div>
</template>

<script setup>
import { ref, defineComponent, h } from 'vue'

defineOptions({ name: 'C7DictTagDemo' })

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

const statusDict = [
  { label: '启用', value: '1', elTagType: 'success' },
  { label: '禁用', value: '0', elTagType: 'danger' },
]

const roleDict = [
  { label: '管理员', value: '1', elTagType: 'primary' },
  { label: '编辑', value: '2', elTagType: 'warning' },
  { label: '审核员', value: '3', elTagType: 'success' },
  { label: '访客', value: '4', elTagType: 'info' },
  { label: '运营', value: '5', elTagType: 'danger' },
]

const levelDict = [
  { label: '低', value: '1', elTagType: 'info' },
  { label: '中', value: '2', elTagType: 'warning' },
  { label: '高', value: '3', elTagType: 'danger' },
]

const textDict = [
  { label: '男', value: '1', elTagType: 'text' },
  { label: '女', value: '0', elTagType: 'text' },
]

const code1 = `const statusDict = [
  { label: '启用', value: '1', elTagType: 'success' },
  { label: '禁用', value: '0', elTagType: 'danger' },
]
<C7DictTag :options="statusDict" :model-value="row.status" />`

const code2 = `<!-- 数组 -->
<C7DictTag :options="roleDict" :model-value="['1','2','3']" />
<!-- 逗号字符串 -->
<C7DictTag :options="roleDict" model-value="1,2,3" />`

const code3 = `<!-- 最多显示 3 个，超出显示 +N -->
<C7DictTag :options="roleDict" model-value="1,2,3,4,5" :max="3" />`

const code4 = `<!-- 超出 3 个时，hover +N 显示 tooltip -->
<C7DictTag :options="roleDict" model-value="1,2,3,4,5" :max="3" collapse />`

const code5 = `<C7DictTag :options="dict" model-value="1" effect="dark" />
<C7DictTag :options="dict" model-value="1" effect="plain" />`

const code6 = `<C7DictTag :options="dict" model-value="1,2,3" round />`

const code7 = `const textDict = [
  { label: '男', value: '1', elTagType: 'text' },
  { label: '女', value: '0', elTagType: 'text' },
]
<C7DictTag :options="textDict" model-value="1" />`

const code8 = `<C7DictTag :options="dict" model-value="1" size="large" />
<C7DictTag :options="dict" model-value="1" size="default" />
<C7DictTag :options="dict" model-value="1" size="small" />`

const code9 = `<!-- showValue=true（默认）：显示原始值 -->
<C7DictTag :options="statusDict" model-value="9" />
<!-- showValue=false：不显示未匹配值 -->
<C7DictTag :options="statusDict" model-value="9" :show-value="false" />`

const code10 = `<!-- 值为空时显示「-」兜底 -->
<C7DictTag :options="dict" :model-value="undefined" />`
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
  gap: 12px;
  flex-wrap: wrap;
  padding: 2px 0;
}

.label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
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
  gap: 10px;
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
