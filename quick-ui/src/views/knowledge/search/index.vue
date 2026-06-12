<template>
  <div class="app-container knowledge-search">
    <el-alert
      title="推荐使用知识库详情内的「命中测试」：与 Dify 一致，在单个知识库上下文中调试 topK 与 Score 阈值。"
      type="info"
      show-icon
      :closable="false"
      class="knowledge-search__tip"
    />
    <c7-card label="语义检索" class="knowledge-search__card">
      <el-form :model="form" label-width="90px" class="knowledge-search__form" @submit.prevent>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="知识库" required>
              <el-select v-model="form.kbId" placeholder="请选择知识库" filterable clearable style="width: 100%">
                <el-option v-for="item in kbOptions" :key="item.kbId" :label="item.name" :value="item.kbId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="检索词" required>
              <el-input
                v-model="form.query"
                placeholder="输入自然语言检索词"
                clearable
                maxlength="2000"
                show-word-limit
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="knowledge-search__actions">
          <el-button type="primary" :loading="loading" @click="handleSearch" v-hasPermi="['knowledge:search']">检索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </c7-card>

    <c7-card label="检索结果" class="knowledge-search__card">
      <el-empty v-if="!loading && searched && !results.length" description="未找到相关片段" />
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <div v-else class="knowledge-search__results">
        <div v-for="(item, index) in results" :key="item.chunkId || index" class="knowledge-search__hit">
          <div class="knowledge-search__hit-header">
            <span class="knowledge-search__hit-rank">#{{ index + 1 }}</span>
            <span class="knowledge-search__hit-file">{{ item.fileName || '未知文件' }}</span>
            <el-tag size="small" type="primary">相似度 {{ formatScore(item.score) }}</el-tag>
            <el-tag v-if="item.pageNumber" size="small" type="info">第 {{ item.pageNumber }} 页</el-tag>
          </div>
          <div class="knowledge-search__hit-content">{{ item.content }}</div>
        </div>
      </div>
    </c7-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listKnowledgeBase } from '@/api/knowledge/base'
import { searchKnowledge } from '@/api/knowledge/search'

/**
 * 知识库语义检索页：选择知识库与自然语言 query，展示命中片段及相似度。
 */
defineOptions({ name: 'KnowledgeSearch' })

const kbOptions = ref([])
const loading = ref(false)
const searched = ref(false)
const results = ref([])

const form = ref({
  kbId: '',
  query: ''
})

function formatScore(score) {
  if (score == null || Number.isNaN(Number(score))) return '—'
  return Number(score).toFixed(4)
}

function loadKbOptions() {
  return listKnowledgeBase({ pageNum: 1, pageSize: 500, status: 0 })
    .then((res) => {
      kbOptions.value = Array.isArray(res?.data?.records) ? res.data.records : []
    })
    .catch(() => {
      kbOptions.value = []
    })
}

function handleSearch() {
  if (!form.value.kbId) {
    ElMessage.warning('请选择知识库')
    return
  }
  const query = String(form.value.query || '').trim()
  if (!query) {
    ElMessage.warning('请输入检索词')
    return
  }
  loading.value = true
  searched.value = true
  searchKnowledge({ kbId: form.value.kbId, query })
    .then((res) => {
      results.value = Array.isArray(res?.data) ? res.data : []
    })
    .catch(() => {
      results.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function handleReset() {
  form.value = { kbId: '', query: '' }
  results.value = []
  searched.value = false
}

onMounted(() => {
  loadKbOptions()
})
</script>

<style scoped>
.knowledge-search__tip {
  margin-bottom: 16px;
}

.knowledge-search__card {
  margin-bottom: 16px;
}

.knowledge-search__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 4px;
}

.knowledge-search__results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.knowledge-search__hit {
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.knowledge-search__hit:hover {
  background: var(--el-color-primary-light-9);
}

.knowledge-search__hit-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.knowledge-search__hit-rank {
  font-weight: 600;
  color: var(--el-color-primary);
}

.knowledge-search__hit-file {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.knowledge-search__hit-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
