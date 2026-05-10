<template>
  <div class="c7-pagination-e2e-wrap">
    <h1 data-testid="c7-pagination-title">C7Pagination Dev</h1>

    <section data-testid="tc-autoreset">
      <h2>autoReset（默认）：切换条数回第 1 页</h2>
      <p class="hint">当前页故意置为 3；切换每页条数后应为第 1 页，且下方 <code>change</code> 记录每次仅一条最终态。</p>
      <c7-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          background
          data-testid="c7-pg-autoreset"
          @change="onChange"
      />
      <pre data-testid="c7-pg-change-log">{{ changeLogText }}</pre>
      <p>currentPage={{ page }}, pageSize={{ size }}</p>
    </section>

    <section data-testid="tc-no-autoreset">
      <h2>autoReset=false</h2>
      <p class="hint">切换条数不强制回第 1 页（页码由 Element Plus 钳制）。</p>
      <c7-pagination
          v-model:current-page="page2"
          v-model:page-size="size2"
          :total="total2"
          :auto-reset="false"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          data-testid="c7-pg-no-reset"
      />
      <p>currentPage={{ page2 }}, pageSize={{ size2 }}</p>
    </section>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'

const total = 233
const page = ref(3)
const size = ref(10)

const total2 = 100
const page2 = ref(4)
const size2 = ref(10)

const changeLines = ref([])

function onChange(p, s) {
  changeLines.value.unshift(`change(${p}, ${s}) @ ${new Date().toISOString()}`)
  if (changeLines.value.length > 12) {
    changeLines.value.length = 12
  }
}

const changeLogText = computed(() => changeLines.value.join('\n') || '（尚无 change）')
</script>

<style scoped>
.c7-pagination-e2e-wrap {
  padding: 16px 24px 48px;
  max-width: 960px;
}

section {
  margin-bottom: 40px;
}

.hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 8px 0 12px;
}

pre {
  margin-top: 12px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
