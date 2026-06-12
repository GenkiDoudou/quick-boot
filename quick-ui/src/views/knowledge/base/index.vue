<template>
  <div class="app-container kb-base-page">
    <div class="kb-base-page__header">
      <div>
        <h2 class="kb-base-page__title">知识库</h2>
        <p class="kb-base-page__subtitle">创建知识库并管理文档、命中测试与检索配置（对标 Dify 知识库工作流）</p>
      </div>
    </div>

    <div v-loading="loading" class="kb-base-page__grid">
      <!-- 创建卡片 -->
      <div class="kb-base-card kb-base-card--create" @click="openAdd" v-hasPermi="['knowledge:base:add']">
        <el-icon class="kb-base-card__create-icon"><Plus /></el-icon>
        <span class="kb-base-card__create-text">创建知识库</span>
      </div>

      <div
        v-for="kb in kbList"
        :key="kb.kbId"
        class="kb-base-card"
        @click="enterKb(kb)"
      >
        <div class="kb-base-card__top">
          <div class="kb-base-card__icon">
            <el-icon><Collection /></el-icon>
          </div>
          <el-dropdown trigger="click" @click.stop>
            <el-button link class="kb-base-card__more" @click.stop>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="enterKbSettings(kb)">进入设置</el-dropdown-item>
                <el-dropdown-item divided @click="openEdit(kb)" v-hasPermi="['knowledge:base:edit']">编辑信息</el-dropdown-item>
                <el-dropdown-item @click="removeRow(kb)" v-hasPermi="['knowledge:base:remove']">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <h3 class="kb-base-card__name" :title="kb.name">{{ kb.name }}</h3>
        <p class="kb-base-card__desc">{{ kb.description || '暂无描述' }}</p>
        <div class="kb-base-card__meta">
          <el-tag size="small" :type="kb.status === 0 ? 'success' : 'info'">
            {{ kb.status === 0 ? '可用' : '停用' }}
          </el-tag>
          <span>{{ kb.segmentMode || 'AUTO' }}</span>
        </div>
      </div>

      <el-empty v-if="!loading && !kbList.length" description="暂无知识库，点击左侧卡片创建" class="kb-base-page__empty" />
    </div>

    <div v-if="total > pageSize" class="kb-base-page__pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, prev, pager, next, sizes"
        background
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>

    <c7-dialog v-model="visible" :title="form.kbId ? '编辑知识库' : '创建知识库'" :on-confirm="submit" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入知识库名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item v-if="!form.kbId" label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <p v-if="!form.kbId" class="kb-base-page__form-tip">分段与预处理等高级设置可在知识库详情 → 设置 中配置。</p>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, MoreFilled, Plus } from '@element-plus/icons-vue'
import {
  addKnowledgeBase,
  getKnowledgeBase,
  listKnowledgeBase,
  removeKnowledgeBase,
  updateKnowledgeBase
} from '@/api/knowledge/base'

defineOptions({ name: 'KnowledgeBase' })

const router = useRouter()
const loading = ref(false)
const kbList = ref([])
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

const visible = ref(false)
const formRef = ref(null)
const form = ref({
  kbId: null,
  name: '',
  description: '',
  segmentMode: 'AUTO',
  chunkDelimiter: 'DOUBLE_NEWLINE',
  chunkSize: 800,
  chunkOverlap: 120,
  preprocessNormalizeWs: true,
  preprocessRemoveUrl: false,
  preprocessRemoveEmail: false,
  status: 0
})

const rules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }]
}

function loadList() {
  loading.value = true
  return listKnowledgeBase({ pageNum: pageNum.value, pageSize: pageSize.value })
    .then((res) => {
      kbList.value = res?.data?.records || []
      total.value = res?.data?.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function enterKb(kb) {
  router.push({ path: '/knowledge/document', query: { kbId: String(kb.kbId) } })
}

function enterKbSettings(kb) {
  router.push({ path: '/knowledge/document', query: { kbId: String(kb.kbId), panel: 'settings' } })
}

function openAdd() {
  form.value = {
    kbId: null,
    name: '',
    description: '',
    segmentMode: 'AUTO',
    chunkDelimiter: 'DOUBLE_NEWLINE',
    chunkSize: 800,
    chunkOverlap: 120,
    preprocessNormalizeWs: true,
    preprocessRemoveUrl: false,
    preprocessRemoveEmail: false,
    status: 0
  }
  visible.value = true
}

function openEdit(kb) {
  if (!kb) return
  getKnowledgeBase(kb.kbId).then((res) => {
    const d = res.data || kb
    form.value = {
      kbId: d.kbId,
      name: d.name || '',
      description: d.description || '',
      segmentMode: d.segmentMode || 'AUTO',
      chunkDelimiter: d.chunkDelimiter || 'DOUBLE_NEWLINE',
      chunkSize: d.chunkSize ?? 800,
      chunkOverlap: d.chunkOverlap ?? 120,
      preprocessNormalizeWs: true,
      preprocessRemoveUrl: false,
      preprocessRemoveEmail: false,
      status: d.status ?? 0
    }
    visible.value = true
  })
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const req = form.value.kbId ? updateKnowledgeBase(form.value) : addKnowledgeBase(form.value)
      req
        .then(() => {
          ElMessage.success('操作成功')
          visible.value = false
          loadList()
          resolve()
        })
        .catch(reject)
    })
  })
}

function removeRow(kb) {
  return ElMessageBox.confirm(`确认删除知识库「${kb.name}」吗？`, '提示', { type: 'warning' })
    .then(() => removeKnowledgeBase([kb.kbId]))
    .then(() => {
      ElMessage.success('删除成功')
      return loadList()
    })
    .catch(() => {})
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.kb-base-page__header {
  margin-bottom: 20px;
}

.kb-base-page__title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
}

.kb-base-page__subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.kb-base-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.kb-base-page__empty {
  grid-column: 1 / -1;
}

.kb-base-page__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.kb-base-page__form-tip {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.kb-base-card {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.15s;
  min-height: 148px;

  &:hover:not(.kb-base-card--create) {
    border-color: #c2e7b0;
    box-shadow: 0 8px 20px rgba(103, 194, 58, 0.12);
    transform: translateY(-2px);
  }

  &--create {
    border-style: dashed;
    border-color: #dcdfe6;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: var(--el-text-color-secondary);
    background: #fafafa;

    &:hover {
      border-color: #67c23a;
      color: #67c23a;
      background: #f0f9eb;
    }
  }
}

.kb-base-card__create-icon {
  font-size: 28px;
}

.kb-base-card__create-text {
  font-size: 14px;
  font-weight: 500;
}

.kb-base-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.kb-base-card__icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f0f9eb;
  color: #67c23a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.kb-base-card__more {
  padding: 2px;
}

.kb-base-card__name {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kb-base-card__desc {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 40px;
}

.kb-base-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
</style>
