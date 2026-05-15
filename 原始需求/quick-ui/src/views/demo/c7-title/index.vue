<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Title 标题组件</h2>
      <p class="demo-desc">通用标题组件，支持 h1~h6 预设尺寸或自定义单位，底部装饰线、前置图标、右侧操作区、多 slot 等功能，颜色跟随 Element Plus 主题。</p>
    </div>

    <demo-section title="基础用法">
      <C7Title label="用户管理" />
      <demo-code :code="code1" />
    </demo-section>

    <demo-section title="预设尺寸（h1 ~ h6）">
      <C7Title label="h1 标题" label-size="h1" />
      <C7Title label="h2 标题（默认）" label-size="h2" />
      <C7Title label="h3 标题" label-size="h3" />
      <C7Title label="h4 标题" label-size="h4" />
      <C7Title label="h5 标题" label-size="h5" />
      <C7Title label="h6 标题" label-size="h6" />
      <demo-code :code="code2" />
    </demo-section>

    <demo-section title="自定义尺寸（px / rem / em）">
      <C7Title label="自定义 28px" label-size="28px" />
      <C7Title label="自定义 1.8rem" label-size="1.8rem" />
      <demo-code :code="code3" />
    </demo-section>

    <demo-section title="底部装饰线（decorationColor）">
      <C7Title label="蓝色装饰线" decoration-color="#409EFF" />
      <C7Title label="绿色装饰线" decoration-color="#67C23A" />
      <C7Title label="橙色装饰线" decoration-color="#E6A23C" />
      <demo-code :code="code4" />
    </demo-section>

    <demo-section title="前置图标（icon）">
      <C7Title label="系统设置" icon="Setting" />
      <C7Title label="用户管理" icon="User" decoration-color="#409EFF" />
      <C7Title label="数据分析" icon="DataAnalysis" decoration-color="#67C23A" />
      <demo-code :code="code5" />
    </demo-section>

    <demo-section title="右侧操作区（默认 slot）">
      <C7Title label="用户列表">
        <el-button type="primary" size="small">新增</el-button>
        <el-button size="small">导出</el-button>
      </C7Title>
      <demo-code :code="code6" />
    </demo-section>

    <demo-section title="自定义标题内容（#title slot）">
      <C7Title>
        <template #title>
          <span>自定义 <em style="color:#409EFF">标题</em> 内容</span>
        </template>
        <el-button size="small">操作</el-button>
      </C7Title>
      <demo-code :code="code7" />
    </demo-section>

    <demo-section title="无边框（:show-border=false）">
      <C7Title label="无边框标题" :show-border="false" />
      <demo-code :code="code8" />
    </demo-section>

    <demo-section title="语义化标签（tag）">
      <C7Title label="这是 h2 语义标签" tag="h2" decoration-color="#909399" />
      <demo-code :code="code9" />
    </demo-section>
  </div>
</template>

<script setup>
defineOptions({ name: 'C7TitleDemo' })

const code1 = `<C7Title label="用户管理" />`
const code2 = `<C7Title label="h1 标题" label-size="h1" />
<C7Title label="h2 标题（默认）" label-size="h2" />
<C7Title label="h3 标题" label-size="h3" />`
const code3 = `<C7Title label="自定义 28px" label-size="28px" />
<C7Title label="自定义 1.8rem" label-size="1.8rem" />`
const code4 = `<C7Title label="蓝色装饰线" decoration-color="#409EFF" />
<C7Title label="绿色装饰线" decoration-color="#67C23A" />`
const code5 = `<C7Title label="系统设置" icon="Setting" />
<C7Title label="用户管理" icon="User" decoration-color="#409EFF" />`
const code6 = `<C7Title label="用户列表">
  <el-button type="primary" size="small">新增</el-button>
  <el-button size="small">导出</el-button>
</C7Title>`
const code7 = `<C7Title>
  <template #title>
    <span>自定义 <em>标题</em> 内容</span>
  </template>
  <el-button size="small">操作</el-button>
</C7Title>`
const code8 = `<C7Title label="无边框标题" :show-border="false" />`
const code9 = `<C7Title label="这是 h2 语义标签" tag="h2" decoration-color="#909399" />`
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
