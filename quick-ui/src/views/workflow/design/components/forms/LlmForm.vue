<template>
  <el-form label-position="top" size="small">
    <el-form-item label="系统提示词">
      <TemplateField
        v-model="data.systemPrompt"
        :variable-tree="variableTree"
        :rows="3"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <el-form-item label="用户提示词" :error="errors.userPrompt">
      <TemplateField
        v-model="data.userPrompt"
        :variable-tree="variableTree"
        :rows="4"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <el-form-item label="温度">
      <div class="wf-llm-temp">
        <el-slider v-model="data.temperature" :min="0" :max="1" :step="0.1" @change="emitUpdate" />
        <el-input-number v-model="data.temperature" :min="0" :max="1" :step="0.1" @change="emitUpdate" />
      </div>
    </el-form-item>
    <el-form-item label="流式输出">
      <el-switch v-model="data.streaming" @change="emitUpdate" />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'LlmForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ systemPrompt: '', userPrompt: '', temperature: 0.3, streaming: true })

watch(
  () => props.modelValue,
  (val) => {
    data.systemPrompt = val?.systemPrompt ?? ''
    data.userPrompt = val?.userPrompt ?? ''
    data.temperature = val?.temperature ?? 0.3
    data.streaming = val?.streaming ?? true
  },
  { immediate: true, deep: true }
)

function emitUpdate() {
  emit('update:modelValue', {
    ...props.modelValue,
    systemPrompt: data.systemPrompt,
    userPrompt: data.userPrompt,
    temperature: data.temperature,
    streaming: data.streaming
  })
}
</script>

<style scoped>
.wf-llm-temp {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.wf-llm-temp .el-slider {
  flex: 1;
}
</style>
