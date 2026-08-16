<template>
  <div></div>
</template>

<script setup>
/**
 * 路由重定向中转页：TagsView 刷新等场景通过 /redirect/:path 无闪烁替换目标路由。
 */
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const { params, query } = route
const { path } = params
router.replace({ path: '/' + path, query })
</script>
