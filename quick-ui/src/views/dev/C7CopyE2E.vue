<template>
  <div class="c7-copy-e2e-wrap">
    <h1 data-testid="c7-copy-title">C7Copy Dev</h1>

    <section data-testid="tc-button">
      <h2>button</h2>
      <c7-copy text="ORDER-001" data-testid="c7-copy-btn"/>
    </section>

    <section data-testid="tc-icon">
      <h2>icon</h2>
      <c7-copy mode="icon" text="https://example.com/deep-link" icon-label="链接"/>
    </section>

    <section data-testid="tc-text">
      <h2>text</h2>
      <c7-copy mode="text" :text="42"/>
    </section>

    <section data-testid="tc-slot">
      <h2>clickable（插槽）</h2>
      <c7-copy mode="clickable" text="slot-secret">
        <el-tag type="info" style="cursor: inherit">点我复制 slot-secret</el-tag>
      </c7-copy>
    </section>

    <section data-testid="tc-none-alias">
      <h2>none 别名（同 clickable）</h2>
      <c7-copy mode="none" text="none-alias-val">
        <span class="fake-link">自定义区域</span>
      </c7-copy>
    </section>

    <section data-testid="tc-async">
      <h2>getCopyText（Promise）</h2>
      <c7-copy
          text="base"
          :get-copy-text="getCopyTextDelayed"
          button-text="复制动态串"
      />
    </section>

    <section data-testid="tc-before-block">
      <h2>beforeCopy 阻止</h2>
      <c7-copy text="blocked" :before-copy="beforeAlwaysFalse" button-text="应不复制"/>
    </section>

    <section data-testid="tc-notify">
      <h2>自定义 notify（不经 ElMessage）</h2>
      <c7-copy text="notify-only" :notify="customNotify" :show-message="true" button-text="notify 复制"/>
      <p data-testid="c7-copy-notify-log">最后通知：{{ notifyLog }}</p>
    </section>

    <section data-testid="tc-disabled">
      <h2>disabled</h2>
      <c7-copy text="x" disabled button-text="禁用"/>
    </section>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const notifyLog = ref('（无）')

/**
 * @param {string} _base
 * @returns {Promise<string>}
 */
function getCopyTextDelayed(_base) {
  return new Promise((resolve) => {
    setTimeout(() => resolve('dynamic-from-promise'), 200)
  })
}

function beforeAlwaysFalse() {
  return false
}

/**
 * @param {'success'|'error'|'info'|'warning'} type
 * @param {string} message
 */
function customNotify(type, message) {
  notifyLog.value = `[${type}] ${message}`
}
</script>

<style scoped lang="scss">
.c7-copy-e2e-wrap {
  padding: 16px 24px 48px;
  max-width: 720px;
}

section {
  margin-bottom: 24px;
}

.fake-link {
  color: var(--el-color-primary);
  text-decoration: underline;
  cursor: pointer;
}
</style>
