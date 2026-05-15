<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Dialog 弹窗/抽屉组件</h2>
      <p class="demo-desc">统一封装 el-dialog 与 el-drawer，通过 mode 切换，内置默认 footer，支持异步确认自动管理 loading，onUnmounted 自动关闭防白屏。</p>
    </div>

    <!-- Section 1: 基础 dialog -->
    <DemoSection title="基础用法（Dialog）">
      <div class="row">
        <el-button type="primary" @click="basic.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog v-model="basic.visible" title="基础弹窗">
        <p class="demo-content-text">这是弹窗内容区域，可以放置任意内容。</p>
      </C7Dialog>
      <DemoCode :code="code1" />
    </DemoSection>

    <!-- Section 2: 抽屉模式 -->
    <DemoSection title="抽屉模式（mode=drawer）">
      <div class="row">
        <el-button type="primary" @click="drawer.visible = true">打开抽屉</el-button>
        <el-button @click="drawerSize = '30%'; drawer.visible = true">30% 宽度</el-button>
        <el-button @click="drawerSize = '600px'; drawer.visible = true">600px 宽度</el-button>
      </div>
      <C7Dialog v-model="drawer.visible" mode="drawer" title="抽屉详情" :size="drawerSize">
        <p class="demo-content-text">这是抽屉内容区域，从右侧滑入。</p>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">张三</el-descriptions-item>
          <el-descriptions-item label="手机号">138****8888</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="success">启用</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </C7Dialog>
      <DemoCode :code="code2" />
    </DemoSection>

    <!-- Section 3: 异步确认 onConfirm -->
    <DemoSection title="异步确认函数（onConfirm）">
      <div class="row">
        <el-button type="primary" @click="asyncConfirm.visible = true">打开（成功场景）</el-button>
        <el-button type="warning" @click="asyncConfirm.shouldFail = true; asyncConfirm.visible = true">打开（失败场景）</el-button>
      </div>
      <C7Dialog
        v-model="asyncConfirm.visible"
        title="异步确认"
        :on-confirm="handleAsyncConfirm"
      >
        <p class="demo-content-text">
          当前场景：<el-tag :type="asyncConfirm.shouldFail ? 'danger' : 'success'" size="small">
            {{ asyncConfirm.shouldFail ? '失败（不关闭）' : '成功（自动关闭）' }}
          </el-tag>
        </p>
        <p class="demo-content-text" style="color:#909399;font-size:13px">
          点击「确定」将等待 1.5 秒模拟异步请求，成功后自动关闭；失败则保持弹窗打开。
        </p>
      </C7Dialog>
      <div v-if="asyncConfirm.log" class="event-log">
        <p class="log-item">{{ asyncConfirm.log }}</p>
      </div>
      <DemoCode :code="code3" />
    </DemoSection>

    <!-- Section 4: 外部控制 confirmLoading -->
    <DemoSection title="外部控制 loading（confirmLoading + @confirm）">
      <div class="row">
        <el-button type="primary" @click="extLoading.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog
        v-model="extLoading.visible"
        title="外部控制 Loading"
        :confirm-loading="extLoading.loading"
        @confirm="handleExtConfirm"
        @cancel="extLoading.log = '点击了取消'"
      >
        <p class="demo-content-text">点击确定后父组件手动管理 loading 和关闭时机。</p>
      </C7Dialog>
      <div v-if="extLoading.log" class="event-log">
        <p class="log-item">{{ extLoading.log }}</p>
      </div>
      <DemoCode :code="code4" />
    </DemoSection>

    <!-- Section 5: 自定义 footer -->
    <DemoSection title="自定义 Footer（#footer slot）">
      <div class="row">
        <el-button type="primary" @click="customFooter.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog v-model="customFooter.visible" title="自定义底部">
        <p class="demo-content-text">底部完全由 #footer slot 接管。</p>
        <template #footer>
          <el-button type="danger" plain @click="customFooter.visible = false">删 除</el-button>
          <el-button type="primary" @click="customFooter.visible = false">保 存</el-button>
          <el-button @click="customFooter.visible = false">关 闭</el-button>
        </template>
      </C7Dialog>
      <DemoCode :code="code5" />
    </DemoSection>

    <!-- Section 6: footer extra 左侧区域 -->
    <DemoSection title="Footer 左侧 extra 区域（#extra slot）">
      <div class="row">
        <el-button type="primary" @click="extraSlot.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog
        v-model="extraSlot.visible"
        title="带 extra 的弹窗"
        :on-confirm="() => (extraSlot.visible = false)"
      >
        <p class="demo-content-text">footer 左侧可通过 #extra slot 放置额外操作。</p>
        <template #extra>
          <el-checkbox v-model="extraSlot.agree">我已阅读并同意用户协议</el-checkbox>
        </template>
      </C7Dialog>
      <DemoCode :code="code6" />
    </DemoSection>

    <!-- Section 7: 隐藏 footer -->
    <DemoSection title="隐藏默认 Footer（:footer=false）">
      <div class="row">
        <el-button type="primary" @click="noFooter.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog v-model="noFooter.visible" title="纯展示弹窗" :footer="false">
        <p class="demo-content-text">此弹窗无操作按钮，仅用于展示信息。</p>
        <el-button style="margin-top:12px" @click="noFooter.visible = false">关 闭</el-button>
      </C7Dialog>
      <DemoCode :code="code7" />
    </DemoSection>

    <!-- Section 8: 自定义按钮文字 + 宽度 -->
    <DemoSection title="自定义按钮文字 / 宽度（confirmText / cancelText / width）">
      <div class="row">
        <el-button type="primary" @click="customText.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog
        v-model="customText.visible"
        title="自定义文字"
        confirm-text="提 交"
        cancel-text="放 弃"
        width="600px"
        :on-confirm="() => (customText.visible = false)"
      >
        <p class="demo-content-text">宽度 600px，确认按钮文字「提 交」，取消按钮文字「放 弃」。</p>
      </C7Dialog>
      <DemoCode :code="code8" />
    </DemoSection>

    <!-- Section 9: 事件监听 -->
    <DemoSection title="事件监听（open / close / confirm / cancel）">
      <div class="row">
        <el-button type="primary" @click="eventDemo.visible = true">打开弹窗</el-button>
      </div>
      <C7Dialog
        v-model="eventDemo.visible"
        title="事件演示"
        @open="pushEventLog('open 触发')"
        @close="pushEventLog('close 触发')"
        @confirm="pushEventLog('confirm 触发'); eventDemo.visible = false"
        @cancel="pushEventLog('cancel 触发')"
      >
        <p class="demo-content-text">操作后查看下方事件日志。</p>
      </C7Dialog>
      <div class="event-log" v-if="eventLogs.length">
        <p v-for="(log, i) in eventLogs" :key="i" class="log-item">{{ log }}</p>
      </div>
      <DemoCode :code="code9" />
    </DemoSection>
  </div>
</template>

<script setup>
import { ref, reactive, defineComponent, h } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'C7DialogDemo' })

// ── 局部子组件 ──
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

// ── 各 Section 状态 ──
const basic = reactive({ visible: false })

const drawer = reactive({ visible: false })
const drawerSize = ref('500px')

const asyncConfirm = reactive({ visible: false, shouldFail: false, log: '' })
async function handleAsyncConfirm() {
  await new Promise((resolve, reject) =>
    setTimeout(() => asyncConfirm.shouldFail ? reject(new Error('模拟失败')) : resolve(), 1500)
  )
  asyncConfirm.log = `[${new Date().toLocaleTimeString()}] 确认成功，弹窗已自动关闭`
  asyncConfirm.shouldFail = false
  ElMessage.success('提交成功')
}

const extLoading = reactive({ visible: false, loading: false, log: '' })
async function handleExtConfirm() {
  extLoading.loading = true
  extLoading.log = `[${new Date().toLocaleTimeString()}] 确认中...`
  await new Promise(resolve => setTimeout(resolve, 1500))
  extLoading.loading = false
  extLoading.visible = false
  extLoading.log = `[${new Date().toLocaleTimeString()}] 确认完成，父组件手动关闭`
  ElMessage.success('操作成功')
}

const customFooter = reactive({ visible: false })
const extraSlot = reactive({ visible: false, agree: false })
const noFooter = reactive({ visible: false })
const customText = reactive({ visible: false })

const eventDemo = reactive({ visible: false })
const eventLogs = ref([])
function pushEventLog(msg) {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] ${msg}`)
  if (eventLogs.value.length > 6) eventLogs.value.pop()
}

// ── 示例代码 ──
const code1 = `<el-button @click="visible = true">打开弹窗</el-button>
<C7Dialog v-model="visible" title="基础弹窗">
  <p>弹窗内容</p>
</C7Dialog>`

const code2 = `<C7Dialog v-model="visible" mode="drawer" title="抽屉详情" size="500px">
  <p>抽屉内容</p>
</C7Dialog>`

const code3 = `<!-- onConfirm 成功后自动关闭，失败则保持打开 -->
<C7Dialog
  v-model="visible"
  title="保存确认"
  :on-confirm="handleSave"
/>

async function handleSave() {
  await api.saveUser(form)  // 抛出异常则不关闭
}`

const code4 = `<C7Dialog
  v-model="visible"
  title="外部控制 Loading"
  :confirm-loading="submitting"
  @confirm="handleConfirm"
/>

async function handleConfirm() {
  submitting.value = true
  await api.submit()
  submitting.value = false
  visible.value = false
}`

const code5 = `<C7Dialog v-model="visible" title="自定义底部">
  <p>内容</p>
  <template #footer>
    <el-button type="danger" plain @click="handleDelete">删 除</el-button>
    <el-button type="primary" @click="handleSave">保 存</el-button>
    <el-button @click="visible = false">关 闭</el-button>
  </template>
</C7Dialog>`

const code6 = `<C7Dialog v-model="visible" title="带 extra">
  <template #extra>
    <el-checkbox v-model="agree">我已阅读并同意</el-checkbox>
  </template>
</C7Dialog>`

const code7 = `<!-- :footer="false" 隐藏默认确定/取消按钮 -->
<C7Dialog v-model="visible" title="纯展示" :footer="false">
  <p>只读内容</p>
</C7Dialog>`

const code8 = `<C7Dialog
  v-model="visible"
  title="自定义文字"
  confirm-text="提 交"
  cancel-text="放 弃"
  width="600px"
/>`

const code9 = `<C7Dialog
  v-model="visible"
  title="事件演示"
  @open="onOpen"
  @close="onClose"
  @confirm="onConfirm"
  @cancel="onCancel"
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

.row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.demo-content-text {
  margin: 0 0 12px;
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
}

.event-log {
  margin-top: 12px;
  background: #1e1e2e;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;

  .log-item {
    margin: 2px 0;
    color: #a6e3a1;
  }
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
  gap: 12px;
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
