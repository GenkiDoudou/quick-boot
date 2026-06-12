<template>
  <el-form label-position="top" size="small">
    <el-form-item label="知识库" :error="errors.kbId">
      <el-select
        v-model="kbSelect"
        filterable
        clearable
        placeholder="选择知识库"
        :loading="kbLoading"
        style="width: 100%"
        @change="onKbChange"
      >
        <el-option label="使用 sys.kbId（运行时注入）" value="__sys__" />
        <el-option
          v-for="kb in kbList"
          :key="kb.kbId"
          :label="kb.name"
          :value="String(kb.kbId)"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="检索问题" :error="errors.query">
      <TemplateField
        v-model="data.query"
        :variable-tree="variableTree"
        :rows="3"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <el-form-item label="Top K">
      <el-input-number v-model="data.topK" :min="1" :max="50" @change="emitUpdate" />
    </el-form-item>
    <el-form-item label="相似度阈值">
      <el-input-number v-model="data.similarityThreshold" :min="0" :max="1" :step="0.05" @change="emitUpdate" />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { listKnowledgeBase } from '@/api/knowledge/base'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'KnowledgeForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ kbId: '', query: '', topK: 5, similarityThreshold: 0.65 })
const kbSelect = ref('')
const kbList = ref([])
const kbLoading = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    data.kbId = val?.kbId ?? ''
    data.query = val?.query ?? ''
    data.topK = val?.topK ?? 5
    data.similarityThreshold = val?.similarityThreshold ?? 0.65
    if (data.kbId === '{{sys.kbId}}') {
      kbSelect.value = '__sys__'
    } else {
      kbSelect.value = String(data.kbId).replace(/\{\{|\}\}/g, '') || ''
    }
  },
  { immediate: true, deep: true }
)

function onKbChange(val) {
  data.kbId = val === '__sys__' ? '{{sys.kbId}}' : val
  emitUpdate()
}

function emitUpdate() {
  emit('update:modelValue', {
    ...props.modelValue,
    kbId: data.kbId,
    query: data.query,
    topK: data.topK,
    similarityThreshold: data.similarityThreshold
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
