<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7DatePicker 日期选择器</h2>
      <p class="demo-desc">基于 el-date-picker 封装，根据 type 自动推断 format/valueFormat，支持范围值合并/拆分（rangeMerge），通过 $attrs 透传其他属性。</p>
    </div>

    <!-- Section 1: 基础日期选择 -->
    <demo-section title="基础日期选择">
      <C7DatePicker v-model="val1" placeholder="请选择日期" />
      <val-display :value="val1" />
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 日期时间选择 -->
    <demo-section title="日期时间选择（type=datetime）">
      <C7DatePicker v-model="val2" type="datetime" placeholder="请选择日期时间" />
      <val-display :value="val2" />
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 日期范围 - 合并字符串 -->
    <demo-section title="日期范围 — 合并为字符串（默认）">
      <C7DatePicker
        v-model="val3"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <val-display :value="val3" />
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 日期范围 - 自定义分隔符 -->
    <demo-section title="日期范围 — 自定义分隔符（range-separator='~'）">
      <C7DatePicker
        v-model="val4"
        type="daterange"
        range-separator="~"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <val-display :value="val4" />
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 日期范围 - 输出数组 -->
    <demo-section title="日期范围 — 输出数组（:range-merge=false）">
      <C7DatePicker
        v-model="val5"
        type="daterange"
        :range-merge="false"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <val-display :value="val5" />
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: 日期时间范围 -->
    <demo-section title="日期时间范围（type=datetimerange）">
      <C7DatePicker
        v-model="val6"
        type="datetimerange"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
      />
      <val-display :value="val6" />
      <demo-code :code="code6" />
    </demo-section>

    <!-- Section 7: 年月选择 -->
    <demo-section title="年份 / 月份选择">
      <div style="display:flex; gap:12px; flex-wrap:wrap;">
        <C7DatePicker v-model="val7Year" type="year" placeholder="请选择年份" />
        <C7DatePicker v-model="val7Month" type="month" placeholder="请选择月份" />
      </div>
      <val-display :value="{ year: val7Year, month: val7Month }" />
      <demo-code :code="code7" />
    </demo-section>

    <!-- Section 8: 月份范围 -->
    <demo-section title="月份范围（type=monthrange）">
      <C7DatePicker
        v-model="val8"
        type="monthrange"
        start-placeholder="开始月份"
        end-placeholder="结束月份"
      />
      <val-display :value="val8" />
      <demo-code :code="code8" />
    </demo-section>

    <!-- Section 9: 事件监听 -->
    <demo-section title="事件监听（change / blur / focus）">
      <C7DatePicker
        v-model="val9"
        placeholder="选择日期触发事件"
        @change="onDateChange"
        @blur="onDateBlur"
        @focus="onDateFocus"
      />
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">操作日期选择器后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code9" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'C7DatePickerDemo' })

// ── 各场景绑定值 ──
const val1 = ref('')
const val2 = ref('')
const val3 = ref('')
const val4 = ref('')
const val5 = ref(null)
const val6 = ref('')
const val7Year = ref('')
const val7Month = ref('')
const val8 = ref('')
const val9 = ref('')

// ── 事件日志 ──
const eventLogs = ref([])
function onDateChange(val) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] change 触发，值 = ${JSON.stringify(val)}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onDateBlur() {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] blur 失去焦点`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onDateFocus() {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] focus 获得焦点`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}

// ── 示例代码 ──
const code1 = `<C7DatePicker v-model="form.birthday" placeholder="请选择日期" />
<!-- 输出示例: '2024-06-01' -->`

const code2 = `<C7DatePicker v-model="form.createTime" type="datetime" placeholder="请选择日期时间" />
<!-- 输出示例: '2024-06-01 12:00:00' -->`

const code3 = `<!-- 默认 rangeMerge=true，输出逗号分隔字符串 -->
<C7DatePicker
  v-model="form.dateRange"
  type="daterange"
  start-placeholder="开始日期"
  end-placeholder="结束日期"
/>
<!-- 输出示例: '2024-01-01,2024-12-31' -->`

const code4 = `<!-- 自定义分隔符为波浪号 -->
<C7DatePicker
  v-model="form.dateRange"
  type="daterange"
  range-separator="~"
  start-placeholder="开始日期"
  end-placeholder="结束日期"
/>
<!-- 输出示例: '2024-01-01~2024-12-31' -->`

const code5 = `<!-- :range-merge="false" 输出数组 -->
<C7DatePicker
  v-model="form.dateRange"
  type="daterange"
  :range-merge="false"
  start-placeholder="开始日期"
  end-placeholder="结束日期"
/>
<!-- 输出示例: ['2024-01-01', '2024-12-31'] -->`

const code6 = `<C7DatePicker
  v-model="form.timeRange"
  type="datetimerange"
  start-placeholder="开始时间"
  end-placeholder="结束时间"
/>
<!-- 输出示例: '2024-01-01 00:00:00,2024-12-31 23:59:59' -->`

const code7 = `<!-- 年份选择 -->
<C7DatePicker v-model="form.year" type="year" placeholder="请选择年份" />
<!-- 输出示例: '2024' -->

<!-- 月份选择 -->
<C7DatePicker v-model="form.month" type="month" placeholder="请选择月份" />
<!-- 输出示例: '2024-06' -->`

const code8 = `<C7DatePicker
  v-model="form.monthRange"
  type="monthrange"
  start-placeholder="开始月份"
  end-placeholder="结束月份"
/>
<!-- 输出示例: '2024-01,2024-12' -->`

const code9 = `<C7DatePicker
  v-model="form.date"
  placeholder="选择日期触发事件"
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

export const ValDisplay = defineComponent({
  name: 'ValDisplay',
  props: { value: { default: undefined } },
  setup(props) {
    return () => h('div', { class: 'val-display' },
      `当前值：${JSON.stringify(props.value)}`
    )
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
  margin: 0 0 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}
.section-body {
  padding-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.val-display {
  font-size: 12px;
  color: #909399;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 4px;
  display: inline-block;
}
.code-toggle { margin-top: 8px; }
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
