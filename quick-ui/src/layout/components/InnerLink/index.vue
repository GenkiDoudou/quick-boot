<template>
  <div :style="{ height: rHeight, overflow: 'hidden' }">
    <iframe
      :id="iframeId"
      style="width: 100%; height: 100%"
      :src="src"
      frameborder="no"
    ></iframe>
  </div>
</template>

<script setup>
/** 内嵌 iframe 容器，高度随窗口 resize 自适应（扣除顶栏与 TagsView） */
defineProps({
  iframeId: {
    type: String,
    required: true
  },
  src: {
    type: String,
    required: true
  }
})

const rHeight = ref(document.documentElement.clientHeight - 94.5 + 'px')

window.addEventListener('resize', () => {
  rHeight.value = document.documentElement.clientHeight - 94.5 + 'px'
})
</script>
