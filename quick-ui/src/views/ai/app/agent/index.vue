<template>
  <div class="ai-agent-design">
    <header class="ai-agent-design__header">
      <div class="ai-agent-design__title">
        <el-button link class="ai-agent-design__back" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="ai-agent-design__title-text">
          <span class="ai-agent-design__name">{{ app.name || '智能体编排' }}</span>
          <el-tag size="small" round :type="app.status === 'published' ? 'success' : 'info'">
            {{ app.status === 'published' ? '已发布' : '草稿' }}
          </el-tag>
        </div>
      </div>
      <div class="ai-agent-design__actions">
        <el-button :loading="saving" @click="save" v-hasPermi="['aiapp:edit']">保存配置</el-button>
        <el-button type="primary" @click="publishVisible = true" v-hasPermi="['aiapp:publish']">发布</el-button>
      </div>
    </header>

    <div class="ai-agent-design__body">
      <!-- 左：人设 -->
      <section class="ai-agent-design__col">
        <div class="ai-agent-design__panel">
          <div class="ai-agent-design__panel-head">
            <el-icon class="ai-agent-design__panel-icon"><User /></el-icon>
            <div>
              <div class="ai-agent-design__panel-title">人设与回复逻辑</div>
              <div class="ai-agent-design__panel-desc">定义角色、语气与回答规则（System Prompt）</div>
            </div>
          </div>

          <div class="ai-agent-design__field-label">从提示词库载入</div>
          <el-select
            v-model="selectedPromptId"
            filterable
            clearable
            placeholder="选择已有提示词模板（可选）"
            class="ai-agent-design__full-width"
            :loading="promptLoading"
            @change="onPromptSelect"
          >
            <el-option
              v-for="item in promptOptions"
              :key="item.promptId"
              :label="formatPromptLabel(item)"
              :value="item.promptId"
            />
          </el-select>

          <div class="ai-agent-design__field-label ai-agent-design__field-label--mt">提示词正文</div>
          <el-input
            v-model="config.systemPrompt"
            type="textarea"
            :rows="16"
            resize="none"
            placeholder="描述智能体是谁、如何说话、有哪些约束与能力…"
            class="ai-agent-design__prompt-textarea"
            @input="onSystemPromptInput"
          />

          <div class="ai-agent-design__prompt-actions">
            <el-button
              type="primary"
              plain
              :loading="optimizing"
              :disabled="!config.systemPrompt?.trim()"
              @click="handleOptimize"
              v-hasPermi="['ai:prompt:optimize']"
            >
              AI 优化提示词
            </el-button>
          </div>
        </div>
      </section>

      <!-- 中：技能 -->
      <section class="ai-agent-design__col ai-agent-design__col--skills">
        <div class="ai-agent-design__panel ai-agent-design__panel--scroll">
          <div class="ai-agent-design__panel-head">
            <el-icon class="ai-agent-design__panel-icon ai-agent-design__panel-icon--skill"><SetUp /></el-icon>
            <div>
              <div class="ai-agent-design__panel-title">技能与对话体验</div>
              <div class="ai-agent-design__panel-desc">模型、知识库、流程与开场设置</div>
            </div>
          </div>

          <el-form label-position="top" class="ai-agent-design__form">
            <div class="ai-agent-design__skill-block">
              <div class="ai-agent-design__skill-title">基础能力</div>
              <el-form-item label="对话模型">
                <el-select
                  v-model="config.chatModelId"
                  filterable
                  clearable
                  placeholder="请选择 Chat 模型"
                  class="ai-agent-design__full-width"
                >
                  <el-option
                    v-for="m in chatModels"
                    :key="m.modelId"
                    :label="`${m.name} (${m.code})`"
                    :value="m.modelId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="知识库">
                <el-select
                  v-model="config.kbIds"
                  multiple
                  filterable
                  clearable
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="可多选，对话中将作为检索工具"
                  class="ai-agent-design__full-width"
                >
                  <el-option v-for="kb in kbOptions" :key="kb.kbId" :label="kb.name" :value="kb.kbId" />
                </el-select>
              </el-form-item>
            </div>

            <div class="ai-agent-design__skill-block">
              <div class="ai-agent-design__skill-title">
                关联工作流
                <el-tooltip placement="top" :show-after="200">
                  <template #content>
                    <div class="ai-agent-design__tooltip">
                      智能体在对话中会<strong>自动判断</strong>是否调用已关联的流程，无需用户手动点选。<br />
                      「触发说明」帮助模型理解什么场景该用哪个流程；保存时会自动生成内部工具标识。
                    </div>
                  </template>
                  <el-icon class="ai-agent-design__help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>

              <el-alert
                type="info"
                :closable="false"
                show-icon
                class="ai-agent-design__wf-tip"
              >
                绑定<strong>已发布</strong>的工作流后，用户提问时模型会视情况自动执行对应流程（Function Calling），不是每次对话都固定跑流程。
              </el-alert>

              <div v-if="!config.workflowBindings.length" class="ai-agent-design__empty">
                尚未关联工作流
              </div>

              <div
                v-for="(binding, index) in config.workflowBindings"
                :key="index"
                class="ai-agent-design__wf-card"
              >
                <div class="ai-agent-design__wf-card-head">
                  <span class="ai-agent-design__wf-card-index">流程 {{ index + 1 }}</span>
                  <el-button link type="danger" @click="removeWorkflowBinding(index)">移除</el-button>
                </div>

                <el-select
                  v-model="binding.workflowId"
                  filterable
                  clearable
                  placeholder="选择已发布的工作流"
                  class="ai-agent-design__full-width"
                  @change="() => onWorkflowPick(binding)"
                >
                  <el-option
                    v-for="w in workflowOptions"
                    :key="w.workflowId"
                    :label="w.name"
                    :value="w.workflowId"
                  >
                    <div class="ai-agent-design__wf-option">
                      <span class="ai-agent-design__wf-option-name">{{ w.name }}</span>
                      <span v-if="w.description" class="ai-agent-design__wf-option-desc">{{ w.description }}</span>
                    </div>
                  </el-option>
                </el-select>

                <template v-if="binding.workflowId">
                  <div class="ai-agent-design__field-label ai-agent-design__field-label--sm">
                    触发说明
                    <span class="ai-agent-design__field-hint">（模型用此判断何时调用该流程）</span>
                  </div>
                  <el-input
                    v-model="binding.description"
                    type="textarea"
                    :rows="2"
                    :placeholder="workflowTriggerPlaceholder(binding.workflowId)"
                  />
                </template>
              </div>

              <el-button class="ai-agent-design__add-btn" @click="addWorkflowBinding">
                <el-icon><Plus /></el-icon>
                关联工作流
              </el-button>
            </div>

            <div class="ai-agent-design__skill-block">
              <div class="ai-agent-design__skill-title">变量记忆</div>
              <el-table
                v-if="config.memoryVariables.length"
                :data="config.memoryVariables"
                size="small"
                class="ai-agent-design__table"
              >
                <el-table-column label="变量名" width="110">
                  <template #default="{ row }">
                    <el-input v-model="row.key" placeholder="如 user_name" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="说明">
                  <template #default="{ row }">
                    <el-input v-model="row.description" placeholder="用途说明" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="默认值" width="100">
                  <template #default="{ row }">
                    <el-input v-model="row.defaultValue" size="small" />
                  </template>
                </el-table-column>
                <el-table-column width="52" align="center">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="config.memoryVariables.splice($index, 1)">删</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button class="ai-agent-design__add-btn" @click="addVariable">
                <el-icon><Plus /></el-icon>
                添加变量
              </el-button>
            </div>

            <div class="ai-agent-design__skill-block">
              <div class="ai-agent-design__skill-title">对话体验</div>
              <el-form-item label="开场白">
                <el-input v-model="config.openingMessage" type="textarea" :rows="2" placeholder="用户进入对话时展示的欢迎语" />
              </el-form-item>

              <el-form-item label="预设问题">
                <div v-for="(q, i) in config.suggestedQuestions" :key="i" class="ai-agent-design__row-input">
                  <el-input v-model="config.suggestedQuestions[i]" placeholder="点击即可发送的示例问题" />
                  <el-button link type="danger" @click="config.suggestedQuestions.splice(i, 1)">删</el-button>
                </div>
                <el-button size="small" plain @click="config.suggestedQuestions.push('')">添加问题</el-button>
              </el-form-item>

              <el-form-item label="快捷指令">
                <div v-for="(cmd, i) in config.quickCommands" :key="i" class="ai-agent-design__row-input">
                  <el-input v-model="cmd.label" placeholder="按钮文字" style="width: 108px" />
                  <el-input v-model="cmd.prompt" placeholder="点击后发送的内容" />
                  <el-button link type="danger" @click="config.quickCommands.splice(i, 1)">删</el-button>
                </div>
                <el-button size="small" plain @click="config.quickCommands.push({ label: '', prompt: '' })">添加快捷指令</el-button>
              </el-form-item>

              <el-form-item label="历史轮数">
                <el-input-number v-model="config.historyTurns" :min="1" :max="50" />
                <span class="ai-agent-design__field-hint ai-agent-design__field-hint--inline">携带最近 N 轮上下文</span>
              </el-form-item>
            </div>
          </el-form>
        </div>
      </section>

      <!-- 右：预览 -->
      <section class="ai-agent-design__col ai-agent-design__col--preview">
        <div class="ai-agent-design__panel ai-agent-design__panel--preview">
          <div class="ai-agent-design__panel-head ai-agent-design__panel-head--compact">
            <el-icon class="ai-agent-design__panel-icon ai-agent-design__panel-icon--preview"><ChatDotRound /></el-icon>
            <div>
              <div class="ai-agent-design__panel-title">预览调试</div>
              <div class="ai-agent-design__panel-desc">使用当前草稿配置试聊</div>
            </div>
          </div>
          <AiAppChatPanel
            :session-id="sessionId"
            :messages="messages"
            :streaming="streaming"
            :stream-buffer="streamBuffer"
            :tool-status="toolStatus"
            :opening-message="config.openingMessage"
            :suggested-questions="config.suggestedQuestions.filter(Boolean)"
            :quick-commands="config.quickCommands.filter((c) => c.label && c.prompt)"
            :show-web-search="isQwenModel"
            :web-search-model="isQwenModel"
            @send="onSend"
          />
        </div>
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
import { ArrowLeft, ChatDotRound, Plus, QuestionFilled, SetUp, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listModelOptions } from '@/api/ai/model'
import { getPromptInfo, listPromptOptions, optimizePromptContent } from '@/api/ai/prompt'
import { listKnowledgeBase } from '@/api/knowledge/base'
import { listWorkflow } from '@/api/workflow'
import { getAiAppInfo, updateAiApp } from '@/api/ai/app'
import AiAppChatPanel from '../components/AiAppChatPanel.vue'
import PublishDialog from '../components/PublishDialog.vue'
import { useAiAppChat } from '../composables/useAiAppChat'

defineOptions({ name: 'AiAppAgentDesign' })

const route = useRoute()
const router = useRouter()
const appId = computed(() => String(route.params.id || ''))

const app = reactive({ id: null, name: '', status: 'draft', appType: 'agent' })
const config = reactive({
  chatModelId: null,
  systemPrompt: '',
  openingMessage: '',
  suggestedQuestions: [],
  quickCommands: [],
  kbIds: [],
  workflowBindings: [],
  memoryVariables: [],
  historyTurns: 10,
  multiSession: true
})

const saving = ref(false)
const optimizing = ref(false)
const publishVisible = ref(false)
const chatModels = ref([])
const kbOptions = ref([])
const workflowOptions = ref([])
const promptOptions = ref([])
const promptLoading = ref(false)
const selectedPromptId = ref(null)

const {
  sessionId,
  messages,
  streaming,
  streamBuffer,
  toolStatus,
  reuseOrCreateSession,
  sendChat
} = useAiAppChat()

const isQwenModel = computed(() => {
  const m = chatModels.value.find((item) => item.modelId === config.chatModelId)
  if (!m) return false
  const code = (m.code || '').toLowerCase()
  const provider = (m.provider || '').toLowerCase()
  return code.includes('qwen') || provider.includes('dashscope') || provider.includes('aliyun')
})

onMounted(async () => {
  await Promise.all([loadOptions(), loadApp()])
  // 预览会话延后创建，避免阻塞编排页首屏渲染
  reuseOrCreateSession(appId.value, '预览调试')
})

async function loadOptions() {
  promptLoading.value = true
  const [models, kbs, wfs, prompts] = await Promise.all([
    listModelOptions('CHAT'),
    listKnowledgeBase({ pageNum: 1, pageSize: 200 }),
    listWorkflow({ pageNum: 1, pageSize: 200, status: 'PUBLISHED' }),
    listPromptOptions().catch(() => ({ data: [] }))
  ])
  chatModels.value = models.data || []
  kbOptions.value = kbs.data?.records || []
  workflowOptions.value = wfs.data?.records || []
  promptOptions.value = prompts.data || []
  promptLoading.value = false
}

async function loadApp() {
  const res = await getAiAppInfo(appId.value)
  const data = res.data || {}
  Object.assign(app, data)
  if (data.configJson) {
    try {
      const parsed = JSON.parse(data.configJson)
      Object.assign(config, {
        suggestedQuestions: [],
        quickCommands: [],
        kbIds: [],
        workflowBindings: [],
        memoryVariables: [],
        ...parsed
      })
    } catch {
      ElMessage.warning('配置 JSON 解析失败')
    }
  }
}

/**
 * @param {{ name?: string, category?: string }} item
 */
function formatPromptLabel(item) {
  const category = (item.category || '').trim()
  return category ? `${item.name}（${category}）` : item.name
}

function onSystemPromptInput() {
  selectedPromptId.value = null
}

/**
 * 从提示词库载入正文。
 * @param {string|number|null} promptId
 */
async function onPromptSelect(promptId) {
  if (!promptId) return
  promptLoading.value = true
  try {
    const res = await getPromptInfo(promptId)
    config.systemPrompt = res.data?.content ?? ''
    ElMessage.success('已载入提示词')
  } catch {
    ElMessage.error('加载提示词失败')
    selectedPromptId.value = null
  } finally {
    promptLoading.value = false
  }
}

/**
 * 根据工作流名称生成工具标识（后端 Function Calling 用）。
 * @param {string|number} workflowId
 */
function findWorkflow(workflowId) {
  return workflowOptions.value.find((w) => String(w.workflowId) === String(workflowId))
}

/**
 * @param {string} name
 * @param {string|number} workflowId
 */
function buildToolName(name, workflowId) {
  const slug = (name || '')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_|_$/g, '')
  if (slug && /^[a-z][a-z0-9_]*$/.test(slug)) {
    return slug.slice(0, 48)
  }
  return `wf_${workflowId}`
}

/**
 * @param {object|null|undefined} wf
 */
function buildTriggerDescription(wf) {
  if (!wf) return '在需要时执行关联工作流处理用户问题'
  const desc = (wf.description || '').trim()
  if (desc) {
    return `当用户问题涉及「${wf.name}」相关场景时使用：${desc}`
  }
  return `当用户问题适合由「${wf.name}」工作流处理时调用`
}

/**
 * @param {string|number} workflowId
 */
function workflowTriggerPlaceholder(workflowId) {
  return buildTriggerDescription(findWorkflow(workflowId))
}

/**
 * 选中工作流后自动填充触发说明与内部 tool 名。
 * @param {{ workflowId?: string|number, toolName?: string, description?: string }} binding
 */
function onWorkflowPick(binding) {
  if (!binding.workflowId) {
    binding.toolName = ''
    binding.description = ''
    return
  }
  const wf = findWorkflow(binding.workflowId)
  binding.toolName = buildToolName(wf?.name, binding.workflowId)
  if (!binding.description?.trim()) {
    binding.description = buildTriggerDescription(wf)
  }
}

function addWorkflowBinding() {
  config.workflowBindings.push({ workflowId: null, toolName: '', description: '' })
}

function removeWorkflowBinding(index) {
  config.workflowBindings.splice(index, 1)
}

function addVariable() {
  config.memoryVariables.push({ key: '', description: '', defaultValue: '' })
}

function normalizeWorkflowBindings() {
  return config.workflowBindings
    .filter((b) => b.workflowId)
    .map((b) => {
      const wf = findWorkflow(b.workflowId)
      return {
        workflowId: String(b.workflowId),
        toolName: b.toolName?.trim() || buildToolName(wf?.name, b.workflowId),
        description: b.description?.trim() || buildTriggerDescription(wf)
      }
    })
}

function defaultConfig() {
  return {
    chatModelId: config.chatModelId != null ? String(config.chatModelId) : null,
    systemPrompt: config.systemPrompt,
    openingMessage: config.openingMessage,
    suggestedQuestions: config.suggestedQuestions.filter(Boolean),
    quickCommands: config.quickCommands.filter((c) => c.label && c.prompt),
    kbIds: (config.kbIds || []).map((id) => String(id)),
    workflowBindings: normalizeWorkflowBindings(),
    memoryVariables: config.memoryVariables.filter((v) => v.key),
    historyTurns: config.historyTurns,
    multiSession: config.multiSession
  }
}

async function save() {
  saving.value = true
  try {
    await updateAiApp({
      id: appId.value,
      name: app.name,
      description: app.description,
      appType: 'agent',
      configJson: JSON.stringify(defaultConfig())
    })
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}

async function handleOptimize() {
  optimizing.value = true
  try {
    const res = await optimizePromptContent({ content: config.systemPrompt, modelId: config.chatModelId })
    const data = res.data
    if (data?.success && data.optimizedContent) {
      config.systemPrompt = data.optimizedContent
      selectedPromptId.value = null
      ElMessage.success('提示词已优化')
    } else {
      ElMessage.error(data?.errorMsg || '优化失败')
    }
  } finally {
    optimizing.value = false
  }
}

async function onSend({ message, webSearch }) {
  sendChat({
    appId: appId.value,
    message,
    preview: true,
    webSearch,
    previewConfigJson: JSON.stringify(defaultConfig())
  })
}

function goBack() {
  router.push('/ai/app/list')
}
</script>

<style scoped>
.ai-agent-design {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: #f0f2f5;
}

.ai-agent-design__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.ai-agent-design__title {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ai-agent-design__title-text {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-agent-design__name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.ai-agent-design__body {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(280px, 1fr) minmax(360px, 1.25fr) minmax(300px, 1fr);
  gap: 12px;
  padding: 12px;
  min-height: 0;
}

.ai-agent-design__col {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.ai-agent-design__col--skills {
  min-width: 0;
}

.ai-agent-design__col--preview {
  min-width: 0;
}

.ai-agent-design__panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.ai-agent-design__panel--scroll {
  overflow-y: auto;
}

.ai-agent-design__panel--preview {
  padding: 0;
  overflow: hidden;
}

.ai-agent-design__panel-head {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.ai-agent-design__panel-head--compact {
  padding: 14px 16px 0;
  margin-bottom: 8px;
}

.ai-agent-design__panel-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.ai-agent-design__panel-icon--skill {
  color: #67c23a;
}

.ai-agent-design__panel-icon--preview {
  color: #e6a23c;
}

.ai-agent-design__panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.ai-agent-design__panel-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  line-height: 1.4;
}

.ai-agent-design__field-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
  font-weight: 500;
}

.ai-agent-design__field-label--mt {
  margin-top: 14px;
}

.ai-agent-design__field-label--sm {
  margin-top: 10px;
  margin-bottom: 4px;
  font-size: 12px;
}

.ai-agent-design__field-hint {
  color: #909399;
  font-weight: 400;
}

.ai-agent-design__field-hint--inline {
  margin-left: 8px;
  font-size: 12px;
}

.ai-agent-design__full-width {
  width: 100%;
}

.ai-agent-design__prompt-textarea :deep(textarea) {
  font-family: inherit;
  line-height: 1.6;
}

.ai-agent-design__prompt-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.ai-agent-design__form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.ai-agent-design__form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  padding-bottom: 4px;
}

.ai-agent-design__skill-block {
  margin-bottom: 20px;
  padding-bottom: 4px;
  border-bottom: 1px dashed #ebeef5;
}

.ai-agent-design__skill-block:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.ai-agent-design__skill-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.ai-agent-design__help-icon {
  font-size: 15px;
  color: #909399;
  cursor: help;
}

.ai-agent-design__wf-tip {
  margin-bottom: 12px;
}

.ai-agent-design__wf-tip :deep(.el-alert__description) {
  line-height: 1.6;
  font-size: 13px;
}

.ai-agent-design__empty {
  font-size: 13px;
  color: #909399;
  padding: 12px;
  text-align: center;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.ai-agent-design__wf-card {
  padding: 12px;
  margin-bottom: 10px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.ai-agent-design__wf-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ai-agent-design__wf-card-index {
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
}

.ai-agent-design__wf-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 2px 0;
  line-height: 1.4;
}

.ai-agent-design__wf-option-name {
  font-size: 14px;
  color: #303133;
}

.ai-agent-design__wf-option-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 420px;
}

.ai-agent-design__add-btn {
  width: 100%;
  border-style: dashed;
  margin-top: 4px;
}

.ai-agent-design__table {
  margin-bottom: 8px;
  border-radius: 8px;
  overflow: hidden;
}

.ai-agent-design__row-input {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.ai-agent-design__tooltip {
  max-width: 280px;
  line-height: 1.6;
  font-size: 13px;
}

.ai-agent-design__col--preview :deep(.ai-app-chat-panel) {
  flex: 1;
  min-height: 0;
  border-radius: 0 0 10px 10px;
}
</style>
