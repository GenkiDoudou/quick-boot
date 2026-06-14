<template>
  <p class="loop-control-form__hint">
    执行到此节点时立即<strong>终止整个循环</strong>，已收集的结果会保留。无限循环模式下须配合条件分支使用。
  </p>
</template>

<script setup>
defineOptions({ name: 'BreakLoopForm' })
</script>

<style scoped lang="scss">
.loop-control-form__hint {
  margin: 8px 0;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.55;
  color: #606266;
  background: #fef0f0;
  border-radius: 6px;
  border: 1px solid #fde2e2;
}
</style>
