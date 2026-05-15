<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Copy 复制组件</h2>
      <p class="demo-desc">一键复制文本内容，支持按钮/图标/文字/插槽四种模式，内置 Clipboard API 降级方案，支持动态内容、自定义通知与前后钩子。</p>
    </div>

    <!-- Section 1: 图标模式（默认） -->
    <demo-section title="图标模式（默认）">
      <div class="row">
        <span class="label-text">复制 ID：{{ sampleId }}</span>
        <C7Copy :text="sampleId" />
      </div>
      <demo-code :code="code1" />
    </demo-section>

    <!-- Section 2: 图标 + 文字 -->
    <demo-section title="图标 + 展示文字（show-text）">
      <div class="row">
        <C7Copy :text="sampleId" show-text />
        <C7Copy :text="sampleId" show-text display-text="复制 ID" />
      </div>
      <demo-code :code="code2" />
    </demo-section>

    <!-- Section 3: 按钮模式 -->
    <demo-section title="按钮模式（mode=button）">
      <div class="row">
        <C7Copy :text="sampleId" mode="button" />
        <C7Copy :text="sampleId" mode="button" button-text="复制 Token" button-type="success" />
        <C7Copy :text="sampleId" mode="button" :button-circle="true" />
        <C7Copy :text="sampleId" mode="button" :button-link="true" button-text="链接式复制" />
        <C7Copy :text="sampleId" mode="button" button-size="small" button-type="warning" button-text="小按钮" />
      </div>
      <demo-code :code="code3" />
    </demo-section>

    <!-- Section 4: 文字模式 -->
    <demo-section title="文字模式（mode=text）">
      <div class="row">
        <C7Copy :text="samplePhone" mode="text" display-text="点击复制手机号" />
        <C7Copy :text="sampleId" mode="text" />
      </div>
      <demo-code :code="code4" />
    </demo-section>

    <!-- Section 5: 插槽模式 -->
    <demo-section title="自定义插槽模式（mode=clickable）">
      <div class="row">
        <C7Copy :text="sampleToken" mode="clickable">
          <el-tag type="info" style="cursor:pointer">{{ sampleToken }}</el-tag>
        </C7Copy>
      </div>
      <demo-code :code="code5" />
    </demo-section>

    <!-- Section 6: getCopyText 动态内容 -->
    <demo-section title="getCopyText — 动态计算复制内容">
      <div class="row">
        <span class="label-text">展示：{{ samplePhone }}（实际复制去除掩码）</span>
        <C7Copy
          :text="samplePhone"
          :get-copy-text="(t) => t.replace(/\*/g, '8')"
          display-text="复制手机号"
        />
      </div>
      <div class="row">
        <span class="label-text">同步添加前缀：</span>
        <C7Copy
          :text="sampleId"
          :get-copy-text="(t) => 'ID:' + t"
          show-text
          display-text="复制带前缀 ID"
        />
      </div>
      <demo-code :code="code6" />
    </demo-section>

    <!-- Section 7: 自定义 notify -->
    <demo-section title="自定义通知（notify prop）">
      <div class="row">
        <C7Copy
          :text="sampleId"
          mode="button"
          button-text="自定义通知复制"
          :notify="customNotify"
        />
        <C7Copy
          :text="sampleId"
          mode="button"
          button-text="关闭提示复制"
          :show-message="false"
        />
      </div>
      <demo-code :code="code7" />
    </demo-section>

    <!-- Section 8: beforeCopy / afterCopy 钩子 -->
    <demo-section title="前后钩子（beforeCopy / afterCopy）">
      <div class="row">
        <el-switch v-model="copyAllowed" active-text="允许复制" inactive-text="禁止复制" />
        <C7Copy
          :text="sampleId"
          mode="button"
          button-text="受控复制"
          :before-copy="beforeCopyGuard"
          :after-copy="afterCopyLog"
        />
      </div>
      <div class="event-log">
        <p v-for="(log, i) in hookLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!hookLogs.length" class="log-empty">操作后这里会显示钩子日志...</p>
      </div>
      <demo-code :code="code8" />
    </demo-section>

    <!-- Section 9: 事件监听 -->
    <demo-section title="事件：copy / success / error">
      <div class="row">
        <C7Copy
          :text="sampleId"
          mode="button"
          button-text="触发事件复制"
          :show-message="false"
          @copy="onCopy"
          @success="onSuccess"
          @error="onError"
        />
      </div>
      <div class="event-log">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
        <p v-if="!eventLogs.length" class="log-empty">操作后这里会显示事件日志...</p>
      </div>
      <demo-code :code="code9" />
    </demo-section>

    <!-- Section 10: 禁用状态 -->
    <demo-section title="禁用状态（disabled）">
      <div class="row">
        <C7Copy :text="sampleId" disabled />
        <C7Copy :text="sampleId" mode="button" button-text="禁用按钮" disabled />
        <C7Copy :text="sampleId" mode="text" display-text="禁用文字" disabled />
      </div>
      <demo-code :code="code10" />
    </demo-section>

    <!-- Section 11: 空值安全 -->
    <demo-section title="空值安全（null / undefined）">
      <div class="row">
        <span class="label-text">text=null：</span>
        <C7Copy :text="null" mode="button" button-text="复制 null" />
        <span class="label-text">text=undefined：</span>
        <C7Copy :text="undefined" mode="button" button-text="复制 undefined" />
      </div>
      <demo-code :code="code11" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

defineOptions({ name: 'C7CopyDemo' })

// ── 演示数据 ──
const sampleId = '1234567890'
const samplePhone = '138****8888'
const sampleToken = 'eyJhbGciOiJIUzI1NiJ9.sample'

// ── Section 8: 钩子控制 ──
const copyAllowed = ref(true)
const hookLogs = ref([])

function pushHookLog(msg) {
  const time = new Date().toLocaleTimeString()
  hookLogs.value.unshift(`[${time}] ${msg}`)
  if (hookLogs.value.length > 6) hookLogs.value.pop()
}

function beforeCopyGuard() {
  if (!copyAllowed.value) {
    ElMessage.warning('当前不允许复制')
    pushHookLog('beforeCopy → 返回 false，复制被阻止')
    return false
  }
  pushHookLog('beforeCopy → 返回 true，允许复制')
  return true
}

function afterCopyLog(text) {
  pushHookLog(`afterCopy → 已复制："${text}"`)
}

// ── Section 9: 事件 ──
const eventLogs = ref([])
function pushEventLog(msg) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] ${msg}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}
function onCopy(text) { pushEventLog(`copy 触发，text = "${text}"`) }
function onSuccess(text) { pushEventLog(`success 触发，text = "${text}"`) }
function onError(err) { pushEventLog(`error 触发，msg = ${err.message}`) }

// ── Section 7: 自定义 notify ──
function customNotify(type, msg) {
  ElNotification({ type, title: type === 'success' ? '操作成功' : '操作失败', message: msg, duration: 2000 })
}

// ── 示例代码 ──
const code1 = `<C7Copy :text="row.id" />`

const code2 = `<!-- 图标 + text 本身 -->
<C7Copy :text="row.id" show-text />

<!-- 图标 + 自定义展示文字 -->
<C7Copy :text="row.id" show-text display-text="复制 ID" />`

const code3 = `<!-- 默认按钮 -->
<C7Copy :text="row.id" mode="button" />

<!-- 自定义文字和类型 -->
<C7Copy :text="row.id" mode="button" button-text="复制 Token" button-type="success" />

<!-- 圆形图标按钮 -->
<C7Copy :text="row.id" mode="button" :button-circle="true" />`

const code4 = `<C7Copy :text="row.phone" mode="text" display-text="点击复制手机号" />`

const code5 = `<C7Copy :text="row.token" mode="clickable">
  <el-tag type="info">{{ row.token }}</el-tag>
</C7Copy>`

const code6 = `<!-- 异步获取真实内容 -->
<C7Copy
  :text="row.phoneMasked"
  :get-copy-text="(t) => fetchRealPhone(row.id)"
  display-text="复制手机号"
/>

<!-- 同步添加前缀 -->
<C7Copy
  :text="row.id"
  :get-copy-text="(t) => 'ID:' + t"
/>`

const code7 = `<!-- 使用 ElNotification 替代 ElMessage -->
<C7Copy
  :text="row.token"
  :notify="(type, msg) => ElNotification({ type, title: msg })"
/>

<!-- 完全关闭提示 -->
<C7Copy :text="row.id" :show-message="false" />`

const code8 = `<C7Copy
  :text="row.id"
  :before-copy="() => {
    if (!hasPermission) {
      ElMessage.warning('无复制权限')
      return false  // 阻止复制
    }
  }"
  :after-copy="(text) => logCopyEvent(text)"
/>`

const code9 = `<C7Copy
  :text="row.id"
  @copy="(text) => console.log('copy', text)"
  @success="(text) => console.log('success', text)"
  @error="(err) => console.error('error', err)"
/>`

const code10 = `<C7Copy :text="row.id" disabled />
<C7Copy :text="row.id" mode="button" disabled />`

const code11 = `<!-- null/undefined 安全处理为空字符串，不会复制 "null" -->
<C7Copy :text="row.optionalField" />`
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
  padding: 4px 0;
}

.label-text {
  font-size: 13px;
  color: #606266;
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