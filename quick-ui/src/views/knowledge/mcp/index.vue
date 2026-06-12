<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({ name: 'KnowledgeMcpRedirect' })

const router = useRouter()

onMounted(() => {
  router.replace('/ai/mcp')
})
</script>

<template>
  <div />
</template>
