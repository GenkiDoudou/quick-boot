<template>
  <div class="app-container oauth-authorize">
    <el-card>
      <template #header>
        <span>授权确认</span>
      </template>
      <p v-if="clientId">应用 <strong>{{ clientId }}</strong> 请求以下权限：</p>
      <ul>
        <li v-for="s in scopeList" :key="s">{{ scopeLabel(s) }}</li>
      </ul>
      <div class="actions">
        <el-button type="primary" @click="confirm(true)">同意</el-button>
        <el-button @click="confirm(false)">拒绝</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineOptions({ name: 'OauthAuthorize' })

const route = useRoute()

const clientId = computed(() => route.query.client_id || '')
const scopeList = computed(() => {
  const raw = route.query.scope || 'openid'
  return String(raw).split(/[\s,]+/).filter(Boolean)
})

function scopeLabel(s) {
  if (s === 'openid') return '基础标识 (openid)'
  if (s === 'profile') return '只读资料 (profile)'
  return s
}

/** 跳转 Sa-Token doConfirm（由后端 /oauth2/doConfirm 处理） */
function confirm(accept) {
  const q = new URLSearchParams(route.query)
  q.set('build_redirect_uri', accept ? 'true' : 'false')
  window.location.href = `${import.meta.env.VITE_APP_BASE_API || ''}/oauth2/doConfirm?${q.toString()}`
}
</script>

<style scoped>
.oauth-authorize {
  max-width: 480px;
  margin: 40px auto;
}
.actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>
