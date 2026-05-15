<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7TimePicker 时间选择器</h2>
      <p class="demo-desc">基于 el-time-picker 封装，默认 format/valueFormat 为 HH:mm:ss，支持时间范围合并/拆分（rangeMerge），通过 $attrs 透传其他属性。</p>
    </div>

    <!-- Section 1: 基础用法 -->
    <demo-section title="基础用法">
      <C7TimePicker v-model="val1" placeholder="请选择时间" />
      <val-display :value="val1" />
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 自定义格式 -->
    <demo-section title="自定义格式（HH:mm）">
      <C7TimePicker v-model="val2" format="HH:mm" value-format="HH:mm" placeholder="请选择时间" />
      <val-display :value="val2" />
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 时间范围（数组） -->
    <demo-section title="时间范围 — 输出数组（默认）">
      <C7TimePicker
        v-model="val3"
        is-range
        start-placeholder="开始时间"
        end-placeholder="结束时间"
      />
      <val-display :value="val3" />
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 时间范围（合并字符串） -->
    <demo-section title="时间范围 — 合并为字符串（rangeMerge）">
      <C7TimePicker
        v-model="val4"
        is-range
        range-merge
        start-placeholder="开始时间"
        end-placeholder="结束时间"
      />
      <val-display :value="val4" />
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 禁用状态 -->
    <demo-section title="禁用状态">
      <C7TimePicker v-model="val5" disabled placeholder="已禁用" />
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 事件监听 -->
    <demo-section title="事件监听（change / blur / focus）">
      <C7TimePicker
        v-model="val6"
        placeholder="选择时间触发事件"
        @change="onTimeChange"
        @blur="onTimeBlur"
        @focus="onTimeFocus"
      />
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">操作时间选择器后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code6" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'C7TimePickerDemo' })

const val1 = ref('')
const val2 = ref('')
const val3 = ref(null)
const val4 = ref('')
const val5 = ref('12:00:00')
const val6 = ref('')

const eventLogs = ref([])
function onTimeChange(val) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] change 触发，值 = ${JSON.stringify(val)}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onTimeBlur() {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] blur 失去焦点`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onTimeFocus() {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] focus 获得焦点`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}

const code1 = `<C7TimePicker v-model="form.startTime" placeholder="请选择时间" />
<!-- 输出示例: '08:30:00' -->`

const code2 = `<C7TimePicker
  v-model="form.time"
  format="HH:mm"
  value-format="HH:mm"
  placeholder="请选择时间"
/>
<!-- 输出示例: '08:30' -->`

const code3 = `<!-- is-range 通过 $attrs 透传，默认输出数组 -->
<C7TimePicker
  v-model="form.timeRange"
  is-range
  start-placeholder="开始时间"
  end-placeholder="结束时间"
/>
<!-- 输出示例: ['08:00:00', '18:00:00'] -->`

const code4 = `<!-- range-merge 输出逗号分隔字符串 -->
<C7TimePicker
  v-model="form.timeRange"
  is-range
  range-merge
  start-placeholder="开始时间"
  end-placeholder="结束时间"
/>
<!-- 输出示例: '08:00:00,18:00:00' -->`

const code5 = `<C7TimePicker v-model="form.time" disabled placeholder="已禁用" />`

const code6 = `<C7TimePicker
  v-model="form.time"
  placeholder="选择时间触发事件"
  @change="(val) => console.log('change', val)"
  @blur="() => console.log('blur')"
  @focus="() => console.log('focus')"
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
