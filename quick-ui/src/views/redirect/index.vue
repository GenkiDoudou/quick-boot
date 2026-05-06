<template>
  <div></div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 重定向逻辑
const { params, query } = route
const { path } = params
router.replace({ path: '/' + path, query })
</script>
