<template>
  <div class="ai-workflow-design">
    <div class="ai-workflow-design__header">
      <div class="ai-workflow-design__title">
        <el-button link @click="goBack"><el-icon><ArrowLeft /></el-icon></el-button>
        <span>{{ app.name || '高级编排' }}</span>
        <el-tag size="small" :type="app.status === 'published' ? 'success' : 'info'">
          {{ app.status === 'published' ? '已发布' : '草稿' }}
        </el-tag>
      </div>
      <div>
        <el-button :loading="saving" @click="save" v-hasPermi="['aiapp:edit']">保存</el-button>
        <el-button type="success" @click="publishVisible = true" v-hasPermi="['aiapp:publish']">发布</el-button>
      </div>
    </div>

    <div class="ai-workflow-design__body">
      <section class="ai-workflow-design__config">
        <div class="ai-workflow-design__section-title">编排配置</div>
        <el-form label-width="100px" label-position="top">
          <el-form-item label="绑定工作流（已发布）">
            <el-select v-model="config.workflowId" filterable placeholder="请选择工作流" style="width: 100%">
              <el-option v-for="w in workflowOptions" :key="w.workflowId" :label="w.name" :value="w.workflowId" />
            </el-select>
          </el-form-item>
          <el-form-item label="开场白">
            <el-input v-model="config.openingMessage" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="预设问题">
            <div v-for="(q, i) in config.suggestedQuestions" :key="i" class="ai-workflow-design__row">
              <el-input v-model="config.suggestedQuestions[i]" />
              <el-button link type="danger" @click="config.suggestedQuestions.splice(i, 1)">删</el-button>
            </div>
            <el-button size="small" @click="config.suggestedQuestions.push('')">添加</el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="ai-workflow-design__preview">
        <div class="ai-workflow-design__section-title">预览调试</div>
        <AiAppChatPanel
          :session-id="sessionId"
          :messages="messages"
          :streaming="streaming"
          :stream-buffer="streamBuffer"
          :tool-status="toolStatus"
          :opening-message="config.openingMessage"
          :suggested-questions="config.suggestedQuestions.filter(Boolean)"
          @send="onSend"
        />
      </section>
    </div>

    <PublishDialog
      v-model="publishVisible"
      :app-id="appId"
      :app-name="app.name"
      :status="app.status"
      @published="loadApp"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listWorkflow } from '@/api/workflow'
import { getAiAppInfo, updateAiApp } from '@/api/ai/app'
import AiAppChatPanel from '../components/AiAppChatPanel.vue'
import PublishDialog from '../components/PublishDialog.vue'
import { useAiAppChat } from '../composables/useAiAppChat'

defineOptions({ name: 'AiAppWorkflowDesign' })

const route = useRoute()
const router = useRouter()
const appId = computed(() => String(route.params.id || ''))

const app = reactive({ name: '', status: 'draft' })
const config = reactive({
  workflowId: null,
  openingMessage: '',
  suggestedQuestions: [],
  multiSession: true
})

const saving = ref(false)
const publishVisible = ref(false)
const workflowOptions = ref([])
const {
  sessionId,
  messages,
  streaming,
  streamBuffer,
  toolStatus,
  reuseOrCreateSession,
  sendChat
} = useAiAppChat()

onMounted(async () => {
  const res = await listWorkflow({ pageNum: 1, pageSize: 200, status: 'PUBLISHED' })
  workflowOptions.value = res.data?.records || []
  await loadApp()
  await reuseOrCreateSession(appId.value, '预览调试')
})

async function loadApp() {
  const res = await getAiAppInfo(appId.value)
  Object.assign(app, res.data || {})
  if (res.data?.configJson) {
    try {
      Object.assign(config, { suggestedQuestions: [], ...JSON.parse(res.data.configJson) })
    } catch {
      ElMessage.warning('配置解析失败')
    }
  }
}

async function save() {
  saving.value = true
  try {
    await updateAiApp({
      id: appId.value,
      name: app.name,
      description: app.description,
      appType: 'workflow',
      configJson: JSON.stringify({
        workflowId: config.workflowId,
        openingMessage: config.openingMessage,
        suggestedQuestions: config.suggestedQuestions.filter(Boolean),
        multiSession: config.multiSession
      })
    })
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}

async function onSend({ message }) {
  await save()
  sendChat({ appId: appId.value, message, preview: true })
}

function goBack() {
  router.push('/ai/app/list')
}
</script>

<style scoped>
.ai-workflow-design {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
}

.ai-workflow-design__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.ai-workflow-design__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.ai-workflow-design__body {
  flex: 1;
  display: grid;
  grid-template-columns: 400px 1fr;
  min-height: 0;
}

.ai-workflow-design__config {
  padding: 16px;
  border-right: 1px solid var(--el-border-color-lighter);
  overflow-y: auto;
}

.ai-workflow-design__preview {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.ai-workflow-design__section-title {
  font-weight: 600;
  margin-bottom: 12px;
}

.ai-workflow-design__row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
