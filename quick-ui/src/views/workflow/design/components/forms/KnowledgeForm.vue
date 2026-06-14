<template>
  <div class="knowledge-form">
    <!-- 输入：固定 Query，样式对齐 LLM 输入参数 / 输出变量 -->
    <WfVariableTableSection
      title="输入"
      tooltip="Query 为检索关键信息，需引用上游节点输出；格式为 String，可引用任意类型数据（将转为文本检索）"
      :columns="inputColumns"
      :has-rows="true"
      :show-add="false"
      :show-actions="false"
    >
      <div
        class="wf-vt-section__row"
        :class="{ 'knowledge-form__row--error': errors.query }"
      >
        <el-input
          model-value="Query"
          size="small"
          disabled
          class="wf-vt-section__col wf-vt-section__col--name knowledge-form__fixed-key"
        />
        <ConditionValueField
          v-model="data.query"
          :variable-tree="variableTree"
          placeholder="输入或引用变量"
          class="wf-vt-section__col wf-vt-section__col--value"
          @update:model-value="emitUpdate"
        />
      </div>
      <p v-if="errors.query" class="knowledge-form__error">{{ errors.query }}</p>
    </WfVariableTableSection>

    <!-- 知识库 -->
    <div class="knowledge-form__section">
      <div class="knowledge-form__section-header">
        <span class="knowledge-form__section-title">知识库</span>
        <el-tooltip content="选择本项目已创建的知识库，检索时将使用该知识库" placement="top">
          <el-icon class="knowledge-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-form-item label="知识库" :error="errors.kbId" class="knowledge-form__field">
        <el-select
          v-model="kbSelect"
          filterable
          clearable
          placeholder="选择知识库"
          :loading="kbLoading"
          style="width: 100%"
          @change="onKbChange"
        >
          <el-option
            v-for="kb in kbList"
            :key="kb.kbId"
            :label="kb.name"
            :value="String(kb.kbId)"
          />
        </el-select>
      </el-form-item>
    </div>

    <!-- 召回策略 -->
    <div class="knowledge-form__section">
      <div class="knowledge-form__section-header">
        <span class="knowledge-form__section-title">召回策略</span>
        <el-tooltip
          content="混合检索结合向量语义与关键词匹配；语义检索侧重理解句意与跨语言相似表达"
          placement="top"
        >
          <el-icon class="knowledge-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-form-item label="搜索策略" class="knowledge-form__field">
        <el-radio-group v-model="data.searchMode" @change="emitUpdate">
          <el-radio value="VECTOR">语义</el-radio>
          <el-radio value="HYBRID">混合</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="最大召回数量" class="knowledge-form__field">
        <el-input-number v-model="data.topK" :min="1" :max="50" controls-position="right" @change="emitUpdate" />
      </el-form-item>
      <el-form-item label="最小匹配度" class="knowledge-form__field">
        <div class="knowledge-form__threshold">
          <el-slider
            v-model="data.similarityThreshold"
            :min="0"
            :max="1"
            :step="0.05"
            :show-tooltip="true"
            @change="emitUpdate"
          />
          <span class="knowledge-form__threshold-val">{{ data.similarityThreshold?.toFixed(2) }}</span>
        </div>
      </el-form-item>
      <el-form-item label="记录检索历史" class="knowledge-form__field">
        <el-switch v-model="data.saveHistory" @change="emitUpdate" />
        <span class="knowledge-form__hint-inline">开启后写入知识库命中测试历史（工作流批量运行建议关闭）</span>
      </el-form-item>
    </div>

    <!-- 输出说明 -->
    <div class="knowledge-form__section knowledge-form__section--output">
      <div class="knowledge-form__section-header">
        <span class="knowledge-form__section-title">输出</span>
      </div>
      <div class="knowledge-form__output-desc">
        <p><code>outputList</code>：召回结果数组（按相关度排序），每项包含：</p>
        <ul>
          <li><code>output</code>（String）— 片段正文</li>
          <li><code>documentId</code>（String）— 文档 ID</li>
          <li><code>chunkId</code>、<code>score</code>、<code>fileName</code></li>
        </ul>
        <p>兼容字段：<code>contextText</code>（拼接上下文）、<code>chunks</code>、<code>citations</code></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { listKnowledgeBase } from '@/api/knowledge/base'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'

defineOptions({ name: 'KnowledgeForm' })

const inputColumns = [
  { key: 'name', label: '参数名', class: 'wf-vt-section__col--name' },
  { key: 'value', label: '参数值', class: 'wf-vt-section__col--value' }
]

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({
  kbId: '',
  query: '',
  searchMode: 'VECTOR',
  topK: 8,
  similarityThreshold: 0.5,
  saveHistory: false
})
const kbSelect = ref('')
const kbList = ref([])
const kbLoading = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    data.kbId = val?.kbId ?? ''
    data.query = val?.query ?? ''
    data.searchMode = val?.searchMode === 'HYBRID' ? 'HYBRID' : 'VECTOR'
    data.topK = val?.topK ?? 8
    data.similarityThreshold = val?.similarityThreshold ?? 0.5
    data.saveHistory = val?.saveHistory === true
    kbSelect.value = String(data.kbId).replace(/\{\{|\}\}/g, '') || ''
  },
  { immediate: true }
)

function onKbChange(val) {
  data.kbId = val || ''
  emitUpdate()
}

function emitUpdate() {
  emit('update:modelValue', {
    ...props.modelValue,
    kbId: data.kbId,
    query: data.query,
    searchMode: data.searchMode,
    topK: data.topK,
    similarityThreshold: data.similarityThreshold,
    saveHistory: data.saveHistory
  })
}

onMounted(() => {
  kbLoading.value = true
  listKnowledgeBase({ pageNum: 1, pageSize: 500, status: 0 })
    .then((res) => {
      kbList.value = res.data?.records || []
    })
    .finally(() => {
      kbLoading.value = false
    })
})
</script>

<style scoped lang="scss">
.knowledge-form__fixed-key {
  :deep(.el-input__wrapper) {
    background: #f5f7fa;
  }
}

.knowledge-form__row--error {
  :deep(.wf-cond-value-field .el-input__wrapper) {
    box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  }
}

.knowledge-form__error {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--el-color-danger);
}

.knowledge-form__section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;

  &--output {
    border-bottom: none;
  }
}

.knowledge-form__section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.knowledge-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
}

.knowledge-form__info {
  color: #909399;
  cursor: help;
}

.knowledge-form__field {
  margin-bottom: 8px;

  :deep(.el-form-item__label) {
    font-size: 12px;
    color: #606266;
  }
}

.knowledge-form__threshold {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;

  .el-slider {
    flex: 1;
  }
}

.knowledge-form__threshold-val {
  min-width: 36px;
  font-size: 12px;
  color: #606266;
  font-variant-numeric: tabular-nums;
}

.knowledge-form__hint-inline {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.knowledge-form__output-desc {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;

  p {
    margin: 0 0 6px;
  }

  ul {
    margin: 0 0 8px;
    padding-left: 18px;
  }

  code {
    padding: 1px 4px;
    background: #f5f7fa;
    border-radius: 3px;
    font-family: Consolas, Monaco, monospace;
    font-size: 11px;
  }
}
</style>
