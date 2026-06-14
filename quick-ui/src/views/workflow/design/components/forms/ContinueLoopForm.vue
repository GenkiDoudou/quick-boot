<template>
  <p class="loop-control-form__hint">
    执行到此节点时<strong>跳过本轮剩余节点</strong>，直接进入下一轮迭代；不会终止整个循环。
  </p>
</template>

<script setup>
defineOptions({ name: 'ContinueLoopForm' })
</script>

<style scoped lang="scss">
.loop-control-form__hint {
  margin: 8px 0;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.55;
  color: #606266;
  background: #fdf6ec;
  border-radius: 6px;
  border: 1px solid #faecd8;
}
</style>
