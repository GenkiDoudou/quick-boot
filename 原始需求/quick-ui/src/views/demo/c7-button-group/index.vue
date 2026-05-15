<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7ButtonGroup 按钮组组件</h2>
      <p class="demo-desc">将多个 C7Button 收纳为按钮组，超出 maxVisible 数量后自动折叠进「更多」下拉菜单。支持数据驱动和 slot 两种模式。</p>
    </div>

    <!-- Section 1: 数据驱动基础用法 -->
    <div class="demo-section">
      <h3 class="section-title">数据驱动模式（推荐）</h3>
      <div class="section-body">
        <C7ButtonGroup :buttons="basicButtons" :max-visible="2" />
        <demo-code :code="code1" />
      </div>
    </div>

    <!-- Section 2: slot 模式 -->
    <div class="demo-section">
      <h3 class="section-title">Slot 模式（向后兼容）</h3>
      <div class="section-body">
        <C7ButtonGroup :max-visible="2">
          <C7Button btn-type="add" :click-function="mockFn" />
          <C7Button btn-type="edit" :click-function="mockFn" />
          <C7Button btn-type="delete" :click-function="mockFn" :confirm="true" confirm-message="确认删除？" />
        </C7ButtonGroup>
        <demo-code :code="code2" />
      </div>
    </div>

    <!-- Section 3: mode 模式切换 -->
    <div class="demo-section">
      <h3 class="section-title">mode 模式切换</h3>
      <div class="section-body">
        <div class="btn-row">
          <span class="label">auto（默认）:</span>
          <C7ButtonGroup :buttons="basicButtons" :max-visible="2" mode="auto" />
        </div>
        <div class="btn-row" style="margin-top:12px">
          <span class="label">inline（全部显示）:</span>
          <C7ButtonGroup :buttons="basicButtons" mode="inline" />
        </div>
        <div class="btn-row" style="margin-top:12px">
          <span class="label">dropdown（全部折叠）:</span>
          <C7ButtonGroup :buttons="basicButtons" mode="dropdown" />
        </div>
        <demo-code :code="code3" />
      </div>
    </div>

    <!-- Section 4: spacing 间距 -->
    <div class="demo-section">
      <h3 class="section-title">spacing 间距</h3>
      <div class="section-body">
        <div class="btn-row">
          <span class="label">tight (4px):</span>
          <C7ButtonGroup :buttons="basicButtons" mode="inline" spacing="tight" />
        </div>
        <div class="btn-row" style="margin-top:12px">
          <span class="label">normal (8px):</span>
          <C7ButtonGroup :buttons="basicButtons" mode="inline" spacing="normal" />
        </div>
        <div class="btn-row" style="margin-top:12px">
          <span class="label">loose (12px):</span>
          <C7ButtonGroup :buttons="basicButtons" mode="inline" spacing="loose" />
        </div>
        <div class="btn-row" style="margin-top:12px">
          <span class="label">自定义 20px:</span>
          <C7ButtonGroup :buttons="basicButtons" mode="inline" :spacing="20" />
        </div>
        <demo-code :code="code4" />
      </div>
    </div>

    <!-- Section 5: 自定义更多按钮 -->
    <div class="demo-section">
      <h3 class="section-title">自定义更多按钮</h3>
      <div class="section-body">
        <div class="btn-row">
          <C7ButtonGroup
            :buttons="basicButtons"
            :max-visible="1"
            more-text="操作菜单"
            more-button-type="primary"
            :more-button-plain="false"
          />
        </div>
        <div class="btn-row" style="margin-top:12px">
          <C7ButtonGroup :buttons="basicButtons" :max-visible="1">
            <template #dropdown-trigger="{ count }">
              <el-button type="warning">自定义触发 ({{ count }})</el-button>
            </template>
          </C7ButtonGroup>
        </div>
        <demo-code :code="code5" />
      </div>
    </div>

    <!-- Section 6: 带确认框的按钮 -->
    <div class="demo-section">
      <h3 class="section-title">数据驱动 + 确认框</h3>
      <div class="section-body">
        <C7ButtonGroup :buttons="confirmButtons" :max-visible="2" />
        <demo-code :code="code6" />
      </div>
    </div>

    <!-- Section 7: 禁用与隐藏 -->
    <div class="demo-section">
      <h3 class="section-title">禁用与隐藏</h3>
      <div class="section-body">
        <C7ButtonGroup :buttons="disabledButtons" :max-visible="3" mode="inline" />
        <demo-code :code="code7" />
      </div>
    </div>

    <!-- Section 8: 事件回调 -->
    <div class="demo-section">
      <h3 class="section-title">事件回调 before-command / after-command</h3>
      <div class="section-body">
        <C7ButtonGroup
          :buttons="basicButtons"
          :max-visible="1"
          @before-command="onBeforeCommand"
          @after-command="onAfterCommand"
        />
        <div class="event-log">
          <p v-for="(log, i) in eventLogs" :key="i" :class="log.type">{{ log.msg }}</p>
          <p v-if="!eventLogs.length" class="empty">点击下拉菜单中的按钮查看事件日志...</p>
        </div>
        <demo-code :code="code8" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineComponent, h } from 'vue'

defineOptions({ name: 'C7ButtonGroupDemo' })

function mockFn() {
  return new Promise(resolve => setTimeout(() => resolve({ code: 200 }), 600))
}

const basicButtons = [
  { key: 'add',    btnType: 'add',    clickFunction: mockFn },
  { key: 'edit',   btnType: 'edit',   clickFunction: mockFn },
  { key: 'delete', btnType: 'delete', clickFunction: mockFn },
]

const confirmButtons = [
  { key: 'add',    btnType: 'add',    clickFunction: mockFn, isSuccessCallback: true, successMessage: '新增成功' },
  { key: 'edit',   btnType: 'edit',   clickFunction: mockFn, isSuccessCallback: true, successMessage: '修改成功' },
  { key: 'delete', btnType: 'delete', clickFunction: mockFn, confirm: true, confirmMessage: '确认删除该记录吗？', isSuccessCallback: true, successMessage: '删除成功' },
]

const disabledButtons = [
  { key: 'add',      btnType: 'add',      clickFunction: mockFn },
  { key: 'edit',     btnType: 'edit',     clickFunction: mockFn, disabled: true },
  { key: 'delete',   btnType: 'delete',   clickFunction: mockFn, disabled: true },
  { key: 'download', btnType: 'download', clickFunction: mockFn, hidden: true },
]

const eventLogs = ref([])
function pushLog(msg, type = 'info') {
  eventLogs.value.unshift({ msg: `[${new Date().toLocaleTimeString()}] ${msg}`, type })
  if (eventLogs.value.length > 8) eventLogs.value.pop()
}
function onBeforeCommand(item) { pushLog(`before-command: ${item.label || item.btnType || item.key}`, 'info') }
function onAfterCommand(item)  { pushLog(`after-command:  ${item.label || item.btnType || item.key}`, 'success') }

const code1 = `<C7ButtonGroup
  :buttons="[
    { key: 'add',    btnType: 'add',    clickFunction: handleAdd },
    { key: 'edit',   btnType: 'edit',   clickFunction: handleEdit },
    { key: 'delete', btnType: 'delete', clickFunction: handleDelete },
  ]"
  :max-visible="2"
/>`

const code2 = `<C7ButtonGroup :max-visible="2">
  <C7Button btn-type="add"    :click-function="handleAdd" />
  <C7Button btn-type="edit"   :click-function="handleEdit" />
  <C7Button btn-type="delete" :click-function="handleDelete" />
</C7ButtonGroup>`

const code3 = `<!-- auto: 超出 maxVisible 折叠 -->
<C7ButtonGroup :buttons="buttons" :max-visible="2" mode="auto" />
<!-- inline: 全部展示 -->
<C7ButtonGroup :buttons="buttons" mode="inline" />
<!-- dropdown: 全部折叠进下拉 -->
<C7ButtonGroup :buttons="buttons" mode="dropdown" />`

const code4 = `<C7ButtonGroup spacing="tight"  mode="inline" />
<C7ButtonGroup spacing="normal" mode="inline" />
<C7ButtonGroup spacing="loose"  mode="inline" />
<C7ButtonGroup :spacing="20"   mode="inline" />`

const code5 = `<!-- 自定义文字和样式 -->
<C7ButtonGroup :max-visible="1" more-text="操作菜单" more-button-type="primary" />

<!-- 自定义触发 slot -->
<C7ButtonGroup :max-visible="1">
  <template #dropdown-trigger="{ count }">
    <el-button type="warning">自定义触发 ({{ count }})</el-button>
  </template>
</C7ButtonGroup>`

const code6 = `<C7ButtonGroup
  :buttons="[
    { key: 'add',    btnType: 'add',    clickFunction: handleAdd },
    { key: 'delete', btnType: 'delete', clickFunction: handleDelete,
      confirm: true, confirmMessage: '确认删除该记录吗？' },
  ]"
  :max-visible="2"
/>`

const code7 = `<C7ButtonGroup
  :buttons="[
    { key: 'add',    btnType: 'add' },
    { key: 'edit',   btnType: 'edit',   disabled: true },
    { key: 'secret', btnType: 'delete', hidden: true },
  ]"
  mode="inline"
/>`

const code8 = `<C7ButtonGroup
  :buttons="buttons"
  :max-visible="1"
  @before-command="onBeforeCommand"
  @after-command="onAfterCommand"
/>`

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
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}
.demo-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
  .demo-title { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0 0 8px; }
  .demo-desc  { color: #606266; font-size: 14px; margin: 0; line-height: 1.6; }
}
.demo-section {
  margin-bottom: 32px;
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
.section-body { padding-top: 4px; }
.btn-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.label {
  font-size: 13px;
  color: #909399;
  min-width: 130px;
}
.event-log {
  margin-top: 12px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 12px 16px;
  min-height: 80px;
  max-height: 180px;
  overflow-y: auto;
  font-size: 12px;
  font-family: 'JetBrains Mono', monospace;
  p { margin: 3px 0; }
  .info    { color: #89b4fa; }
  .success { color: #a6e3a1; }
  .error   { color: #f38ba8; }
  .empty   { color: #6c7086; font-style: italic; }
}
</style>

<style>
.code-toggle { margin-top: 12px; }
.code-toggle-btn { font-size: 12px; color: #409eff; cursor: pointer; user-select: none; }
.code-toggle-btn:hover { text-decoration: underline; }
.code-block {
  margin-top: 8px;
  background: #282c34;
  color: #abb2bf;
  border-radius: 6px;
  padding: 14px 16px;
  font-size: 12px;
  font-family: 'JetBrains Mono', monospace;
  overflow-x: auto;
  line-height: 1.6;
  white-space: pre;
}
</style>
