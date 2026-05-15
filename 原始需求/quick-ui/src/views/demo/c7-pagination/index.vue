<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Pagination 分页组件</h2>
      <p class="demo-desc">封装 el-pagination，内置合理默认值，新增 autoReset 自动重置和统一 change 事件，支持 $attrs 透传所有原生属性。</p>
    </div>

    <DemoSection title="基础用法">
      <C7Pagination
        v-model:current-page="demo1.page"
        v-model:page-size="demo1.size"
        :total="demo1.total"
        @change="demo1.onChange"
      />
      <ResultLog :result="demo1.log" />
      <DemoCode :code="code1" />
    </DemoSection>

    <DemoSection title="切换条数自动重置页码（autoReset=true，默认）">
      <div class="state-row">
        <span>当前页：<strong>{{ demo2.page }}</strong></span>
        <span>每页：<strong>{{ demo2.size }}</strong></span>
      </div>
      <C7Pagination
        v-model:current-page="demo2.page"
        v-model:page-size="demo2.size"
        :total="demo2.total"
        @change="demo2.onChange"
      />
      <ResultLog :result="demo2.log" />
      <DemoCode :code="code2" />
    </DemoSection>

    <DemoSection title="禁用自动重置（autoReset=false）">
      <div class="state-row">
        <span>当前页：<strong>{{ demo3.page }}</strong></span>
        <span>每页：<strong>{{ demo3.size }}</strong></span>
      </div>
      <C7Pagination
        v-model:current-page="demo3.page"
        v-model:page-size="demo3.size"
        :total="demo3.total"
        :auto-reset="false"
        @change="demo3.onChange"
      />
      <ResultLog :result="demo3.log" />
      <DemoCode :code="code3" />
    </DemoSection>

    <DemoSection title="小型分页（small）">
      <C7Pagination
        v-model:current-page="demo4.page"
        v-model:page-size="demo4.size"
        :total="demo4.total"
        small
        @change="demo4.onChange"
      />
      <ResultLog :result="demo4.log" />
      <DemoCode :code="code4" />
    </DemoSection>

    <DemoSection title="自定义布局（layout）">
      <p class="sub-label">prev, pager, next</p>
      <C7Pagination
        v-model:current-page="demo5.page"
        v-model:page-size="demo5.size"
        :total="demo5.total"
        layout="prev, pager, next"
      />
      <p class="sub-label" style="margin-top:12px">total, prev, pager, next</p>
      <C7Pagination
        v-model:current-page="demo5.page"
        v-model:page-size="demo5.size"
        :total="demo5.total"
        layout="total, prev, pager, next"
      />
      <DemoCode :code="code5" />
    </DemoSection>

    <DemoSection title="自定义每页条数选项（pageSizes）">
      <C7Pagination
        v-model:current-page="demo6.page"
        v-model:page-size="demo6.size"
        :total="demo6.total"
        :page-sizes="[5, 10, 20, 50]"
        @change="demo6.onChange"
      />
      <ResultLog :result="demo6.log" />
      <DemoCode :code="code6" />
    </DemoSection>

    <DemoSection title="禁用状态（disabled）">
      <C7Pagination
        v-model:current-page="demo7.page"
        v-model:page-size="demo7.size"
        :total="demo7.total"
        disabled
      />
      <DemoCode :code="code7" />
    </DemoSection>

    <DemoSection title="事件监听（@change / @size-change / @current-change）">
      <C7Pagination
        v-model:current-page="demo8.page"
        v-model:page-size="demo8.size"
        :total="demo8.total"
        @change="demo8.onChange"
        @size-change="demo8.onSizeChange"
        @current-change="demo8.onCurrentChange"
      />
      <div class="event-log" v-if="demo8.logs.length">
        <p v-for="(l, i) in demo8.logs" :key="i" class="log-item">{{ l }}</p>
      </div>
      <DemoCode :code="code8" />
    </DemoSection>
  </div>
</template>

<script setup>
import { ref, reactive, defineComponent, h } from 'vue'

defineOptions({ name: 'C7PaginationDemo' })

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

const ResultLog = defineComponent({
  name: 'ResultLog',
  props: { result: Object },
  setup(props) {
    return () => props.result
      ? h('div', { class: 'result-log' }, [
          h('span', { class: 'result-label' }, 'change 事件：'),
          h('code', { class: 'result-code' }, JSON.stringify(props.result))
        ])
      : null
  }
})

function makeDemo(total = 500, initPage = 1, initSize = 10) {
  const state = reactive({
    page: initPage,
    size: initSize,
    total,
    log: null,
    logs: [],
    onChange(page, size) {
      state.log = { page, size }
    },
    onSizeChange(size) {
      state.logs.unshift(`[${new Date().toLocaleTimeString()}] size-change: ${size}`)
      if (state.logs.length > 5) state.logs.pop()
    },
    onCurrentChange(page) {
      state.logs.unshift(`[${new Date().toLocaleTimeString()}] current-change: ${page}`)
      if (state.logs.length > 5) state.logs.pop()
    }
  })
  return state
}

const demo1 = makeDemo()
const demo2 = makeDemo(500, 5, 10)
const demo3 = makeDemo(500, 5, 10)
const demo4 = makeDemo(200)
const demo5 = makeDemo(300)
const demo6 = makeDemo(100)
const demo7 = makeDemo(200)
const demo8 = makeDemo(500)

const code1 = `<C7Pagination
  v-model:current-page="currentPage"
  v-model:page-size="pageSize"
  :total="total"
  @change="fetchData"
/>

function fetchData(page, size) {
  // page 和 size 已是最新值
}`

const code2 = `<!-- 切换条数时自动重置到第一页（默认行为）-->
<C7Pagination
  v-model:current-page="page"
  v-model:page-size="size"
  :total="total"
  @change="fetchData"
/>`

const code3 = `<!-- 切换条数时不重置页码 -->
<C7Pagination
  v-model:current-page="page"
  v-model:page-size="size"
  :total="total"
  :auto-reset="false"
  @change="fetchData"
/>`

const code4 = `<C7Pagination
  v-model:current-page="page"
  v-model:page-size="size"
  :total="total"
  small
/>`

const code5 = `<!-- 自定义布局 -->
<C7Pagination layout="prev, pager, next" ... />
<C7Pagination layout="total, prev, pager, next" ... />`

const code6 = `<C7Pagination
  v-model:current-page="page"
  v-model:page-size="size"
  :total="total"
  :page-sizes="[5, 10, 20, 50]"
/>`

const code7 = `<C7Pagination
  v-model:current-page="page"
  v-model:page-size="size"
  :total="total"
  disabled
/>`

const code8 = `<!-- 推荐监听 @change，统一处理翻页和切换条数 -->
<C7Pagination
  v-model:current-page="page"
  v-model:page-size="size"
  :total="total"
  @change="(page, size) => fetchData(page, size)"
  @size-change="(size) => console.log('size-change:', size)"
  @current-change="(page) => console.log('current-change:', page)"
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
.state-row {
  display: flex; gap: 20px; font-size: 13px; color: #606266; margin-bottom: 4px;
  strong { color: #303133; }
}
.sub-label { font-size: 12px; color: #909399; margin: 0 0 4px; }
.result-log {
  margin-top: 6px; padding: 6px 12px; background: #f4f4f5; border-radius: 4px; font-size: 13px;
  .result-label { color: #909399; margin-right: 6px; }
  .result-code { color: #303133; font-family: monospace; }
}
.event-log {
  margin-top: 10px; background: #1e1e2e; border-radius: 6px; padding: 8px 14px;
  .log-item { margin: 2px 0; color: #a6e3a1; font-size: 12px; font-family: monospace; }
}
</style>

<style>
.demo-section { margin-bottom: 36px; background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 20px 24px; box-shadow: 0 1px 4px rgba(0,0,0,.04); }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 14px; padding-bottom: 10px; border-bottom: 1px dashed #ebeef5; }
.section-body { display: flex; flex-direction: column; gap: 12px; }
.code-toggle { margin-top: 10px; }
.code-toggle-btn { font-size: 12px; color: #409eff; cursor: pointer; user-select: none; }
.code-toggle-btn:hover { text-decoration: underline; }
.code-block { margin-top: 8px; background: #282c34; color: #abb2bf; border-radius: 6px; padding: 14px 16px; font-size: 12px; font-family: 'JetBrains Mono', monospace; overflow-x: auto; line-height: 1.6; white-space: pre; }
</style>
