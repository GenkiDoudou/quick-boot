<template>
  <div class="c7-dialog-e2e-wrap">
    <h1 data-testid="c7-dialog-title">C7Dialog Dev</h1>

    <section>
      <h2>Dialog · onConfirm 成功关闭</h2>
      <el-button @click="openDialog = true">打开 Dialog</el-button>
      <c7-dialog
          v-model="openDialog"
          title="Dialog 标题"
          :on-confirm="onDialogConfirmOk"
      >
        <p>内容区：resolve 后自动关闭。</p>
      </c7-dialog>
    </section>

    <section>
      <h2>Dialog · onConfirm reject 不关</h2>
      <el-button @click="openDialogFail = true">打开（会失败）</el-button>
      <c7-dialog
          v-model="openDialogFail"
          title="失败不关"
          :on-confirm="onDialogConfirmFail"
      >
        <p>点击确定后 reject，弹层应保持打开。</p>
      </c7-dialog>
    </section>

    <section>
      <h2>Drawer · 取消关闭</h2>
      <el-button @click="openDrawer = true">打开 Drawer</el-button>
      <c7-dialog
          v-model="openDrawer"
          mode="drawer"
          title="抽屉"
          cancel-text="取消"
      >
        <p>点取消应关闭并触发 cancel。</p>
      </c7-dialog>
    </section>

    <section>
      <h2>confirmLoading 显式 false 覆盖内部</h2>
      <el-button @click="openOverride = true">打开</el-button>
      <c7-dialog
          v-model="openOverride"
          title="外部 loading=false"
          :confirm-loading="false"
          :on-confirm="onOverrideSlowConfirm"
      >
        <p>内部 2s 后 resolve；确定钮应不因内部 pending 出现 loading。</p>
      </c7-dialog>
    </section>

    <section>
      <h2>自定义 footer 插槽</h2>
      <el-button @click="openCustomFooter = true">打开</el-button>
      <c7-dialog v-model="openCustomFooter" title="自定义底部" :footer="false">
        <p>仅插槽 footer。</p>
        <template #footer>
          <el-button type="primary" @click="openCustomFooter = false">插槽内关闭</el-button>
        </template>
      </c7-dialog>
    </section>

    <section>
      <h2>无 onConfirm · confirm + submit</h2>
      <el-button @click="openEmitOnly = true">打开</el-button>
      <c7-dialog
          v-model="openEmitOnly"
          title="仅事件"
          @confirm="emitConfirmCount += 1"
          @submit="emitSubmitCount += 1"
      >
        <p>点确定 emit 后不自动关，请点取消或遮罩。</p>
      </c7-dialog>
      <p data-testid="c7-dialog-emit-log">confirm={{ emitConfirmCount }} submit={{ emitSubmitCount }}</p>
    </section>
  </div>
</template>

<script setup>
/** C7Dialog 组件 E2E 联调页：覆盖 dialog/drawer 模式、异步确定与双 v-model。 */
import {ref} from 'vue'

const openDialog = ref(false)
const openDialogFail = ref(false)
const openDrawer = ref(false)
const openOverride = ref(false)
const openCustomFooter = ref(false)
const openEmitOnly = ref(false)
const emitConfirmCount = ref(0)
const emitSubmitCount = ref(0)

function onDialogConfirmOk() {
  return Promise.resolve()
}

function onDialogConfirmFail() {
  return Promise.reject(new Error('biz fail'))
}

function onOverrideSlowConfirm() {
  return new Promise((resolve) => {
    setTimeout(resolve, 2000)
  })
}
</script>

<style scoped>
.c7-dialog-e2e-wrap {
  padding: 24px;
  max-width: 960px;
}

section {
  margin-bottom: 28px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

h2 {
  font-size: 16px;
  margin: 0 0 12px;
}
</style>
