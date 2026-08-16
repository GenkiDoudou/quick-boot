<template>
  <div class="error-page">
    <div class="error-content">
      <h1>401</h1>
      <p>无权限访问</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
/** HTTP 401 错误页：无权限访问时展示，提供返回首页入口 */
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  color: white;
}

.error-content h1 {
  font-size: 120px;
  margin: 0;
  font-weight: bold;
}

.error-content p {
  font-size: 24px;
  margin: 20px 0;
}
</style>
