<template>
  <div class="kb-detail app-container">
    <aside class="kb-detail__sidebar">
      <button
        v-for="item in navItems"
        :key="item.key"
        type="button"
        class="kb-detail__nav"
        :class="{ 'is-active': activePanel === item.key }"
        @click="activePanel = item.key"
      >
        <el-icon class="kb-detail__nav-icon"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </button>
    </aside>

    <section class="kb-detail__main">
      <header class="kb-detail__header">
        <div class="kb-detail__header-left">
          <el-button link class="kb-detail__back" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h2 class="kb-detail__title">{{ kbDetail?.name || '知识库详情' }}</h2>
        </div>
        <el-button link @click="goBack">
          <el-icon><Close /></el-icon>
        </el-button>
      </header>

      <!-- 文档面板 -->
      <div v-show="activePanel === 'docs'" class="kb-detail__panel">
        <div class="kb-detail__toolbar">
          <el-input
            v-model="searchTitle"
            placeholder="请输入文档名称，回车搜索"
            clearable
            class="kb-detail__search"
            @keyup.enter="loadDocuments"
            @clear="loadDocuments"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div v-loading="docLoading" class="kb-detail__grid">
          <!-- 新建文档卡片 -->
          <div class="kb-doc-card kb-doc-card--create">
            <div class="kb-doc-card__create-title">
              <span>新建文档</span>
              <el-tooltip content="支持四种来源入库" placement="top">
                <el-icon class="kb-doc-card__hint"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
            <ul class="kb-doc-card__create-list">
              <li v-hasPermi="['knowledge:doc:upload']" @click="openWizard('manual')">
                <el-icon><EditPen /></el-icon>
                <span>手动录入</span>
              </li>
              <li v-hasPermi="['knowledge:doc:upload']" @click="openWizard('file')">
                <el-icon><Upload /></el-icon>
                <span>文件上传</span>
              </li>
              <li v-hasPermi="['knowledge:doc:upload']" @click="openWizard('web')">
                <el-icon><Link /></el-icon>
                <span>网页录入</span>
              </li>
              <li v-hasPermi="['knowledge:doc:upload']" @click="openWizard('library')">
                <el-icon><FolderOpened /></el-icon>
                <span>文档库上传</span>
              </li>
            </ul>
          </div>

          <!-- 文档卡片 -->
          <div
            v-for="doc in docList"
            :key="doc.docId"
            class="kb-doc-card"
            @click="onDocCardClick(doc)"
          >
            <div class="kb-doc-card__head">
              <div class="kb-doc-card__icon" :class="docIconClass(doc)">
                <el-icon><Document /></el-icon>
              </div>
              <span class="kb-doc-card__name" :title="doc.title">{{ doc.title }}</span>
              <el-dropdown trigger="click" @click.stop>
                <el-button link class="kb-doc-card__more" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :disabled="doc.docStatus === 'PARSING'"
                      @click="handleReindex(doc)"
                    >
                      重建索引
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleRemove(doc)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <p class="kb-doc-card__preview">{{ docPreview(doc) }}</p>
            <div class="kb-doc-card__tags">
              <el-tag size="small" type="info">{{ sourceTypeLabel(doc.sourceType) }}</el-tag>
              <el-tag v-if="doc.chunkCount != null && doc.docStatus === 'INDEXED'" size="small">{{ doc.chunkCount }} 段</el-tag>
            </div>
            <div class="kb-doc-card__footer">
              <span class="kb-doc-card__status-label">状态：</span>
              <template v-if="isProcessing(doc.docStatus)">
                <el-icon class="is-loading kb-doc-card__status-icon"><Loading /></el-icon>
                <span>构建中</span>
              </template>
              <template v-else-if="doc.docStatus === 'INDEXED'">
                <el-icon class="kb-doc-card__status-icon kb-doc-card__status-icon--done"><CircleCheck /></el-icon>
                <span>已完成</span>
              </template>
              <template v-else-if="doc.docStatus === 'FAILED'">
                <el-icon class="kb-doc-card__status-icon kb-doc-card__status-icon--fail"><CircleClose /></el-icon>
                <span>失败</span>
              </template>
              <template v-else>
                <span>待处理</span>
              </template>
            </div>
          </div>
        </div>

        <div class="kb-detail__pagination">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next, sizes"
            background
            @current-change="loadDocuments"
            @size-change="loadDocuments"
          />
        </div>
      </div>

      <!-- 命中测试面板 -->
      <div v-show="activePanel === 'hitTest'" class="kb-detail__panel kb-detail__panel--hit">
        <div class="kb-hit-test">
          <div class="kb-hit-test__intro">
            <h3 class="kb-hit-test__title">命中测试</h3>
            <p class="kb-hit-test__desc">针对用户提问测试段落匹配情况，保障回答效果。</p>
          </div>

          <div v-loading="hitLoading" class="kb-hit-test__body">
            <el-empty
              v-if="!hitLoading && !hitSearched"
              description="在下方输入问题并检索，查看命中的文档段落"
              :image-size="80"
            />
            <el-empty v-else-if="!hitLoading && hitSearched && !hitResults.length" description="未找到相关片段" :image-size="80" />
            <div v-else-if="hitResults.length" class="kb-hit-test__results">
              <div
                v-for="(item, index) in hitResults"
                :key="item.chunkId || index"
                class="kb-hit-test__item"
              >
                <div class="kb-hit-test__item-head">
                  <span class="kb-hit-test__rank">#{{ index + 1 }}</span>
                  <span class="kb-hit-test__file">{{ item.fileName || '未知文件' }}</span>
                  <el-tag size="small" type="success">Score {{ formatScore(item.score) }}</el-tag>
                  <el-tag v-if="item.vectorScore != null" size="small" type="info">向量 {{ formatScore(item.vectorScore) }}</el-tag>
                  <el-tag v-if="item.keywordScore != null && item.keywordScore > 0" size="small">关键词 {{ formatScore(item.keywordScore) }}</el-tag>
                  <el-button link type="primary" size="small" @click="copyHitContent(item.content)">复制</el-button>
                </div>
                <p class="kb-hit-test__content" @dblclick="copyHitContent(item.content)">{{ item.content }}</p>
              </div>
            </div>
          </div>

          <div class="kb-hit-test__dock">
            <div v-if="retrievalHistory.length" class="kb-hit-test__history">
              <span class="kb-hit-test__params-label">最近检索</span>
              <div class="kb-hit-test__history-tags">
                <el-tag
                  v-for="item in retrievalHistory"
                  :key="item.logId"
                  class="kb-hit-test__history-tag"
                  effect="plain"
                  @click="applyHistoryQuery(item)"
                >
                  {{ truncateQuery(item.query) }}
                  <span class="kb-hit-test__history-meta">({{ item.hitCount }} 条)</span>
                </el-tag>
              </div>
            </div>
            <div class="kb-hit-test__params">
              <span class="kb-hit-test__params-label">参数配置</span>
              <div class="kb-hit-test__param">
                <span class="kb-hit-test__param-name">模式</span>
                <el-select v-model="hitSearchMode" size="small" style="width: 110px">
                  <el-option label="Hybrid" value="HYBRID" />
                  <el-option label="向量" value="VECTOR" />
                </el-select>
              </div>
              <div class="kb-hit-test__param">
                <span class="kb-hit-test__param-name">条数</span>
                <el-input-number v-model="hitTopK" :min="1" :max="50" :step="1" size="small" controls-position="right" />
              </div>
              <div class="kb-hit-test__param">
                <span class="kb-hit-test__param-name">Score阈值</span>
                <el-input-number
                  v-model="hitScoreThreshold"
                  :min="0"
                  :max="1"
                  :step="0.05"
                  :precision="2"
                  size="small"
                  controls-position="right"
                />
              </div>
            </div>
            <div class="kb-hit-test__input-row">
              <el-input
                v-model="hitQuery"
                placeholder="请输入"
                clearable
                maxlength="2000"
                @keyup.enter="handleHitSearch"
              />
              <el-button
                class="kb-hit-test__search-btn"
                :loading="hitLoading"
                v-hasPermi="['knowledge:search']"
                @click="handleHitSearch"
              >
                <el-icon><Filter /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 设置面板（Dify 知识库设置） -->
      <div v-show="activePanel === 'settings'" class="kb-detail__panel">
        <KnowledgeKbSettings v-if="kbId" :kb-id="kbId" @saved="onKbSettingsSaved" />
      </div>

      <!-- 对话测试 -->
      <div v-show="activePanel === 'chatTest'" class="kb-detail__panel kb-detail__panel--chat">
        <KnowledgeChatPanel v-if="kbId" :kb-id="kbId" />
      </div>
    </section>

    <KnowledgeDocSegmentDrawer v-model="segmentDrawerVisible" :doc-id="activeDocId" />

    <KnowledgeDocWizard
      v-if="kbId"
      v-model="wizardVisible"
      :kb-id="kbId"
      :initial-source-tab="wizardSourceTab"
      @success="loadDocuments"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ChatDotRound,
  CircleCheck,
  CircleClose,
  Close,
  Document,
  EditPen,
  Filter,
  FolderOpened,
  Link,
  Loading,
  MoreFilled,
  Operation,
  QuestionFilled,
  Search,
  Setting,
  Upload
} from '@element-plus/icons-vue'
import { getKnowledgeBase } from '@/api/knowledge/base'
import { listDocument, reindexDocument, removeDocument } from '@/api/knowledge/doc'
import { searchKnowledge, listRetrievalHistory } from '@/api/knowledge/search'
import KnowledgeDocWizard from '@/components/knowledge/KnowledgeDocWizard.vue'
import KnowledgeDocSegmentDrawer from '@/components/knowledge/KnowledgeDocSegmentDrawer.vue'
import KnowledgeKbSettings from '@/components/knowledge/KnowledgeKbSettings.vue'
import KnowledgeChatPanel from '@/components/knowledge/KnowledgeChatPanel.vue'

defineOptions({ name: 'KnowledgeDocument' })

const route = useRoute()
const router = useRouter()

/** 路由中的知识库 ID 必须保持字符串，避免 Snowflake 精度丢失 */
const kbId = computed(() => {
  const id = route.query.kbId
  if (id == null || id === '') return null
  return String(id)
})

const activePanel = ref('docs')

const navItems = [
  { key: 'docs', label: '文档', icon: Document },
  { key: 'hitTest', label: '命中测试', icon: Operation },
  { key: 'chatTest', label: '对话测试', icon: ChatDotRound },
  { key: 'settings', label: '设置', icon: Setting }
]

const SOURCE_TYPE_MAP = {
  FILE: '文件',
  MANUAL: '手动',
  WEB: '网页',
  LIBRARY: '文档库'
}
const kbDetail = ref(null)
const docList = ref([])
const docLoading = ref(false)
const searchTitle = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pollTimer = ref(null)

const wizardVisible = ref(false)
const wizardSourceTab = ref('file')

const segmentDrawerVisible = ref(false)
const activeDocId = ref(null)

const hitQuery = ref('')
const hitTopK = ref(5)
const hitScoreThreshold = ref(0.5)
const hitSearchMode = ref('HYBRID')
const hitLoading = ref(false)
const hitSearched = ref(false)
const hitResults = ref([])
const retrievalHistory = ref([])

const POLLING_STATUSES = ['PENDING', 'PARSING']

function sourceTypeLabel(type) {
  return SOURCE_TYPE_MAP[type] || type || '—'
}

function isProcessing(status) {
  return POLLING_STATUSES.includes(status)
}

function docIconClass(doc) {
  const title = String(doc?.title || '').toLowerCase()
  if (title.endsWith('.pdf')) return 'kb-doc-card__icon--pdf'
  return 'kb-doc-card__icon--doc'
}

function docPreview(doc) {
  if (doc.docStatus === 'FAILED' && doc.errorMsg) {
    return String(doc.errorMsg).slice(0, 120)
  }
  if (doc.sourceUrl) return doc.sourceUrl
  if (doc.docStatus === 'INDEXED' && doc.chunkCount != null) {
    return `已成功索引，共 ${doc.chunkCount} 个分块`
  }
  return '暂无内容预览'
}

function formatScore(score) {
  if (score == null || Number.isNaN(Number(score))) return '—'
  return Number(score).toFixed(4)
}

function goBack() {
  router.push({ path: '/knowledge/base' })
}

function loadKbDetail() {
  if (!kbId.value) return Promise.resolve()
  return getKnowledgeBase(kbId.value).then((res) => {
    kbDetail.value = res?.data || null
  })
}

function loadDocuments() {
  if (!kbId.value) return Promise.resolve()
  docLoading.value = true
  return listDocument({
    kbId: kbId.value,
    title: searchTitle.value || undefined,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  })
    .then((res) => {
      docList.value = res?.data?.records || []
      total.value = res?.data?.total || 0
      schedulePolling(docList.value)
    })
    .finally(() => {
      docLoading.value = false
    })
}

function schedulePolling(records) {
  stopPolling()
  if (Array.isArray(records) && records.some((r) => POLLING_STATUSES.includes(r.docStatus))) {
    pollTimer.value = setInterval(loadDocuments, 4000)
  }
}

function stopPolling() {
  if (pollTimer.value) {
    clearInterval(pollTimer.value)
    pollTimer.value = null
  }
}

function openWizard(sourceTab) {
  if (!kbId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }
  wizardSourceTab.value = sourceTab
  wizardVisible.value = true
}

function onDocCardClick(doc) {
  if (doc.docStatus === 'FAILED' && doc.errorMsg) {
    ElMessageBox.alert(doc.errorMsg, `文档「${doc.title}」入库失败`, { type: 'warning' })
    return
  }
  if (doc.docStatus === 'PARSING' || doc.docStatus === 'PENDING') {
    ElMessage.info('文档构建中，请稍后再查看分段')
    return
  }
  activeDocId.value = doc.docId
  segmentDrawerVisible.value = true
}

function onKbSettingsSaved(payload) {
  kbDetail.value = { ...(kbDetail.value || {}), ...payload }
}

function handleReindex(doc) {
  return reindexDocument(doc.docId).then(() => {
    ElMessage.success('已提交重建索引')
    return loadDocuments()
  })
}

function handleRemove(doc) {
  return ElMessageBox.confirm(`确认删除文档「${doc.title}」吗？`, '提示', { type: 'warning' })
    .then(() => removeDocument([doc.docId]))
    .then(() => {
      ElMessage.success('删除成功')
      return loadDocuments()
    })
    .catch(() => {})
}

function handleHitSearch() {
  if (!kbId.value) return
  const query = String(hitQuery.value || '').trim()
  if (!query) {
    ElMessage.warning('请输入检索词')
    return
  }
  hitLoading.value = true
  hitSearched.value = true
  searchKnowledge({
    kbId: kbId.value,
    query,
    topK: hitTopK.value,
    similarityThreshold: hitScoreThreshold.value,
    searchMode: hitSearchMode.value
  })
    .then((res) => {
      hitResults.value = Array.isArray(res?.data) ? res.data : []
      loadRetrievalHistory()
    })
    .catch(() => {
      hitResults.value = []
    })
    .finally(() => {
      hitLoading.value = false
    })
}

function loadRetrievalHistory() {
  if (!kbId.value) return Promise.resolve()
  return listRetrievalHistory({ kbId: kbId.value, pageNum: 1, pageSize: 8 }).then((res) => {
    retrievalHistory.value = res?.data?.records || []
  })
}

function applyHistoryQuery(item) {
  hitQuery.value = item.query || ''
  hitTopK.value = item.topK ?? hitTopK.value
  hitScoreThreshold.value = item.similarityThreshold ?? hitScoreThreshold.value
  hitSearchMode.value = item.searchMode || 'HYBRID'
  handleHitSearch()
}

function truncateQuery(text) {
  const s = String(text || '')
  return s.length > 24 ? `${s.slice(0, 24)}…` : s
}

async function copyHitContent(text) {
  const content = String(text || '').trim()
  if (!content) return
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

watch(kbId, (id) => {
  if (id) {
    loadKbDetail()
    pageNum.value = 1
    loadDocuments()
  } else {
    ElMessage.warning('请从知识库列表进入')
    goBack()
  }
})

onMounted(() => {
  if (!kbId.value) {
    goBack()
    return
  }
  if (route.query.panel === 'settings') {
    activePanel.value = 'settings'
  } else if (route.query.panel === 'hitTest') {
    activePanel.value = 'hitTest'
  } else if (route.query.panel === 'chatTest') {
    activePanel.value = 'chatTest'
  }
  loadKbDetail()
  loadDocuments()
  loadRetrievalHistory()
})

onBeforeUnmount(stopPolling)
</script>

<style scoped lang="scss">
.kb-detail {
  --kb-accent: #67c23a;
  --kb-accent-bg: #f0f9eb;
  --kb-card-border: #ebeef5;
  display: flex;
  gap: 0;
  padding: 0;
  min-height: calc(100vh - 120px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.kb-detail__sidebar {
  width: 128px;
  flex-shrink: 0;
  padding: 12px 0;
  border-right: 1px solid var(--kb-card-border);
  background: #fafafa;
}

.kb-detail__nav {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 100%;
  padding: 14px 8px;
  border: none;
  background: transparent;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;

  &:hover {
    color: var(--el-text-color-primary);
    background: var(--el-fill-color-light);
  }

  &.is-active {
    color: var(--kb-accent);
    background: var(--kb-accent-bg);
    font-weight: 500;
    box-shadow: inset 3px 0 0 var(--kb-accent);
  }
}

.kb-detail__nav-icon {
  font-size: 18px;
}

.kb-detail__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 20px 20px;
}

.kb-detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--kb-card-border);
}

.kb-detail__header-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.kb-detail__back {
  font-size: 18px;
  padding: 4px;
}

.kb-detail__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kb-detail__panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.kb-detail__toolbar {
  margin-bottom: 16px;
}

.kb-detail__search {
  max-width: 360px;
}

.kb-detail__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  flex: 1;
  align-content: start;
  min-height: 200px;
}

.kb-detail__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 12px;
}

.kb-doc-card {
  border: 1px solid var(--kb-card-border);
  border-radius: 8px;
  padding: 14px 14px 12px;
  background: #fff;
  min-height: 148px;
  display: flex;
  flex-direction: column;
  cursor: default;
  transition: box-shadow 0.2s, border-color 0.2s;

  &:hover:not(.kb-doc-card--create) {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }

  &--create {
    cursor: default;
    background: #fafcff;
  }
}

.kb-doc-card__create-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.kb-doc-card__hint {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  cursor: help;
}

.kb-doc-card__create-list {
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 4px;
    font-size: 14px;
    color: var(--el-text-color-regular);
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.15s, color 0.15s;

    &:hover {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }

    .el-icon {
      font-size: 16px;
    }
  }
}

.kb-doc-card__head {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.kb-doc-card__icon {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;

  &--pdf {
    background: #fef0f0;
    color: #f56c6c;
  }

  &--doc {
    background: #ecf5ff;
    color: #409eff;
  }
}

.kb-doc-card__name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 28px;
}

.kb-doc-card__more {
  flex-shrink: 0;
  padding: 2px;
}

.kb-doc-card__preview {
  flex: 1;
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
}

.kb-doc-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.kb-doc-card__footer {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding-top: 8px;
  border-top: 1px solid var(--el-fill-color-light);
}

.kb-doc-card__status-label {
  color: var(--el-text-color-placeholder);
}

.kb-doc-card__status-icon {
  font-size: 14px;

  &--done {
    color: var(--kb-accent);
  }

  &--fail {
    color: var(--el-color-danger);
  }
}

.kb-detail__panel--hit {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.kb-hit-test {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: calc(100vh - 220px);
}

.kb-hit-test__intro {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.kb-hit-test__title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.kb-hit-test__desc {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.kb-hit-test__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 8px 0 16px;
}

.kb-hit-test__results {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.kb-hit-test__item {
  padding: 14px 16px;
  background: #fafafa;
  border: 1px solid var(--kb-card-border);
  border-radius: 8px;
}

.kb-hit-test__item-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
}

.kb-hit-test__rank {
  font-weight: 600;
  color: var(--kb-accent);
}

.kb-hit-test__file {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.kb-hit-test__content {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  word-break: break-word;
  cursor: pointer;
}

.kb-hit-test__dock {
  flex-shrink: 0;
  border-top: 1px solid var(--kb-card-border);
  padding-top: 16px;
  background: #fff;
}

.kb-hit-test__history {
  margin-bottom: 12px;
}

.kb-hit-test__history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.kb-hit-test__history-tag {
  cursor: pointer;
}

.kb-hit-test__history-meta {
  margin-left: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.kb-detail__panel--chat {
  overflow-y: auto;
}

.kb-hit-test__params {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.kb-hit-test__params-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.kb-hit-test__param {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.kb-hit-test__param-name {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.kb-hit-test__param :deep(.el-input-number) {
  width: 100px;
}

.kb-hit-test__input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.kb-hit-test__input-row .el-input {
  flex: 1;
}

.kb-hit-test__search-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  padding: 0;
  border-radius: 8px;
  background: var(--kb-accent-bg);
  border-color: transparent;
  color: var(--kb-accent);

  &:hover,
  &:focus {
    background: #e1f3d8;
    color: var(--kb-accent);
    border-color: transparent;
  }
}
</style>
