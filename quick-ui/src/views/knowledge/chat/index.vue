<template>
  <div class="app-container knowledge-chat-redirect">
    <el-result icon="info" title="RAG 问答已并入知识库详情">
      <template #sub-title>
        请在知识库详情页的「对话测试」Tab 中使用；若从菜单进入，请先选择知识库。
      </template>
      <template #extra>
        <el-select v-model="kbId" placeholder="选择知识库" filterable clearable style="width: 320px; margin-right: 8px">
          <el-option v-for="item in kbOptions" :key="item.kbId" :label="item.name" :value="String(item.kbId)" />
        </el-select>
        <el-button type="primary" :disabled="!kbId" @click="goDetail">进入对话测试</el-button>
        <el-button @click="goBase">知识库列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listKnowledgeBase } from '@/api/knowledge/base'

defineOptions({ name: 'KnowledgeChat' })

const route = useRoute()
const router = useRouter()
const kbOptions = ref([])
const kbId = ref(route.query.kbId ? String(route.query.kbId) : '')

function goDetail() {
  if (!kbId.value) return
  router.replace({ path: '/knowledge/document', query: { kbId: kbId.value, panel: 'chatTest' } })
}

function goBase() {
  router.push({ path: '/knowledge/base' })
}

onMounted(() => {
  listKnowledgeBase({ pageNum: 1, pageSize: 500, status: 0 }).then((res) => {
    kbOptions.value = res?.data?.records || []
    if (kbId.value) {
      goDetail()
    }
  })
})
</script>

<style scoped>
.knowledge-chat-redirect {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}
</style>
