<template>
  <div v-loading="loading" class="kb-settings">
    <div class="kb-settings__intro">
      <h3 class="kb-settings__title">知识库设置</h3>
      <p class="kb-settings__desc">配置默认分段与预处理策略，新建文档时可选择继承或单次覆盖。</p>
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="kb-settings__form">
      <el-form-item label="知识库名称" prop="name">
        <el-input v-model="form.name" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="分段模式" prop="segmentMode">
        <el-radio-group v-model="form.segmentMode">
          <el-radio label="AUTO">自动分段</el-radio>
          <el-radio label="CUSTOM">自定义分段</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.segmentMode === 'CUSTOM'" label="分隔符">
        <el-select v-model="form.chunkDelimiter" style="width: 240px">
          <el-option label="单换行" value="SINGLE_NEWLINE" />
          <el-option label="双换行" value="DOUBLE_NEWLINE" />
        </el-select>
      </el-form-item>
      <el-form-item label="分块大小" prop="chunkSize">
        <el-input-number v-model="form.chunkSize" :min="128" :max="4096" :step="64" controls-position="right" />
        <span class="kb-settings__unit">tokens</span>
      </el-form-item>
      <el-form-item label="分块重叠" prop="chunkOverlap">
        <el-input-number v-model="form.chunkOverlap" :min="0" :max="512" :step="16" controls-position="right" />
        <span class="kb-settings__unit">tokens</span>
      </el-form-item>
      <el-form-item label="文本预处理">
        <el-checkbox v-model="form.preprocessNormalizeWs">归一化连续空白</el-checkbox>
        <el-checkbox v-model="form.preprocessRemoveUrl">删除 URL</el-checkbox>
        <el-checkbox v-model="form.preprocessRemoveEmail">删除邮箱</el-checkbox>
      </el-form-item>
      <el-form-item label="Chat 模型">
        <el-select v-model="form.chatModelId" filterable clearable placeholder="留空则使用全局默认" style="width: 100%">
          <el-option
            v-for="item in chatModelOptions"
            :key="item.modelId"
            :label="`${item.name} (${item.code})${item.defaultSlot ? ' ★' : ''}`"
            :value="item.modelId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Embedding 模型">
        <el-select v-model="form.embeddingModelId" filterable clearable placeholder="留空则使用全局默认" style="width: 100%" @change="onEmbeddingModelChange">
          <el-option
            v-for="item in embeddingModelOptions"
            :key="item.modelId"
            :label="`${item.name} (${item.code})${item.defaultSlot ? ' ★' : ''}`"
            :value="item.modelId"
          />
        </el-select>
        <p class="kb-settings__field-tip">变更 Embedding 模型后，建议对该知识库文档重建索引。</p>
      </el-form-item>
      <el-form-item label="关联 MCP">
        <el-select
          v-model="form.mcpIds"
          multiple
          filterable
          clearable
          placeholder="选择外部 MCP（仅启用项可在对话中调用工具）"
          style="width: 100%"
        >
          <el-option
            v-for="item in mcpOptions"
            :key="item.mcpId"
            :label="`${item.name} (${item.code})`"
            :value="item.mcpId"
            :disabled="item.status === 1"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="0">正常</el-radio>
          <el-radio :label="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit" v-hasPermi="['knowledge:base:edit']">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getKnowledgeBase, updateKnowledgeBase } from '@/api/knowledge/base'
import { listModelOptions } from '@/api/ai/model'
import { listMcpOptions } from '@/api/ai/mcp'

defineOptions({ name: 'KnowledgeKbSettings' })

const props = defineProps({
  kbId: { type: String, required: true }
})

const emit = defineEmits(['saved'])

const loading = ref(false)
const saving = ref(false)
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
  status: 0,
  chatModelId: null,
  embeddingModelId: null,
  mcpIds: []
})

const mcpOptions = ref([])
const chatModelOptions = ref([])
const embeddingModelOptions = ref([])
const prevEmbeddingModelId = ref(null)

const rules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }],
  chunkSize: [{ required: true, message: '请输入分块大小', trigger: 'blur' }],
  chunkOverlap: [{ required: true, message: '请输入分块重叠', trigger: 'blur' }]
}

function loadDetail() {
  if (!props.kbId) return
  loading.value = true
  getKnowledgeBase(props.kbId)
    .then((res) => {
      const d = res?.data || {}
      form.value = {
        kbId: d.kbId,
        name: d.name || '',
        description: d.description || '',
        segmentMode: d.segmentMode || 'AUTO',
        chunkDelimiter: d.chunkDelimiter || 'DOUBLE_NEWLINE',
        chunkSize: d.chunkSize ?? 800,
        chunkOverlap: d.chunkOverlap ?? 120,
        preprocessNormalizeWs: d.preprocessNormalizeWs === 0 ? false : d.preprocessNormalizeWs === 1 ? true : true,
        preprocessRemoveUrl: d.preprocessRemoveUrl === 1,
        preprocessRemoveEmail: d.preprocessRemoveEmail === 1,
        status: d.status ?? 0,
        chatModelId: d.chatModelId ?? null,
        embeddingModelId: d.embeddingModelId ?? null,
        mcpIds: Array.isArray(d.mcpIds) ? d.mcpIds : []
      }
      prevEmbeddingModelId.value = form.value.embeddingModelId
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * Embedding 模型变更时提示重建索引。
 * @param {number|null} val 新选中的模型 ID
 */
function onEmbeddingModelChange(val) {
  if (prevEmbeddingModelId.value != null && val !== prevEmbeddingModelId.value) {
    ElMessageBox.confirm(
      '变更 Embedding 模型后，向量空间将发生变化，建议对该知识库文档重建索引。是否继续？',
      '提示',
      { type: 'warning', confirmButtonText: '继续', cancelButtonText: '取消' }
    )
      .then(() => {
        prevEmbeddingModelId.value = val
      })
      .catch(() => {
        form.value.embeddingModelId = prevEmbeddingModelId.value
      })
  } else {
    prevEmbeddingModelId.value = val
  }
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('validate'))
      saving.value = true
      updateKnowledgeBase(form.value)
        .then(() => {
          ElMessage.success('设置已保存')
          emit('saved', form.value)
          resolve()
        })
        .catch(reject)
        .finally(() => {
          saving.value = false
        })
    })
  })
}

function loadMcpOptions() {
  listMcpOptions()
    .then((res) => {
      mcpOptions.value = res?.data || []
    })
    .catch(() => {
      mcpOptions.value = []
    })
}

function loadModelOptions() {
  listModelOptions('LANGUAGE')
    .then((res) => {
      chatModelOptions.value = res?.data || []
    })
    .catch(() => {
      chatModelOptions.value = []
    })
  listModelOptions('VECTOR')
    .then((res) => {
      embeddingModelOptions.value = res?.data || []
    })
    .catch(() => {
      embeddingModelOptions.value = []
    })
}

loadMcpOptions()
loadModelOptions()
watch(() => props.kbId, loadDetail, { immediate: true })
</script>

<style scoped>
.kb-settings__intro {
  margin-bottom: 20px;
}

.kb-settings__title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
}

.kb-settings__desc {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.kb-settings__form {
  max-width: 640px;
}

.kb-settings__unit {
  margin-left: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.kb-settings__field-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
