<template>
  <div class="loop-set-var-form">
    <el-form label-position="top" size="small">
      <el-form-item label="中间变量">
        <el-select v-model="local.target" placeholder="选择循环节点声明的变量" filterable @change="sync">
          <el-option v-for="k in intermediateKeys" :key="k" :label="k" :value="k" />
        </el-select>
        <div v-if="!intermediateKeys.length" class="loop-set-var-form__hint">
          请先在循环节点配置中间变量
        </div>
      </el-form-item>
      <el-form-item label="设置值">
        <ConditionValueField
          v-model="local.value"
          :variable-tree="variableTree"
          placeholder="输入或引用变量"
          @update:model-value="sync"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import ConditionValueField from './ConditionValueField.vue'
import { getLoopIntermediateKeys } from '../../utils/loopUtils'

defineOptions({ name: 'LoopSetVariableForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  nodeId: { type: String, default: '' },
  canvasNodes: { type: Array, default: () => [] },
  variableTree: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue'])

const local = reactive({ target: '', value: '' })

const intermediateKeys = computed(() => {
  const node = (props.canvasNodes || []).find((n) => n.id === props.nodeId)
  const bodyId = node?.parentNode || node?.data?.parentId
  if (!bodyId) return []
  return getLoopIntermediateKeys(bodyId, props.canvasNodes)
})

watch(
  () => props.modelValue,
  (val) => {
    local.target = val?.target || ''
    local.value = val?.value || ''
  },
  { immediate: true, deep: true }
)

function sync() {
  emit('update:modelValue', { ...props.modelValue, ...local })
}
</script>

<style scoped lang="scss">
.loop-set-var-form__hint {
  margin-top: 6px;
  font-size: 12px;
  color: #e6a23c;
}
</style>
