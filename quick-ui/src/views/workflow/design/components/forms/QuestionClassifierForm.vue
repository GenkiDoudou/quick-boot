<template>
  <el-form label-position="top" size="small">
    <el-form-item label="待分类文本" :error="errors.query">
      <TemplateField
        v-model="data.query"
        :variable-tree="variableTree"
        :rows="3"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <div class="wf-form__section">分类列表（连线 Handle 与 ID 联动）</div>
    <div v-for="(row, idx) in classes" :key="idx" class="wf-form__row">
      <el-form-item label="ID" :error="errors[`classes.${idx}.id`]">
        <el-input v-model="row.id" placeholder="a" @change="sync" />
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="row.name" placeholder="类别名称" @change="sync" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="row.description" type="textarea" :rows="2" @change="sync" />
      </el-form-item>
      <el-button link type="danger" @click="removeRow(idx)">删除</el-button>
    </div>
    <el-button size="small" @click="addRow">+ 添加分类</el-button>
  </el-form>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'QuestionClassifierForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ query: '' })
const classes = ref([])

watch(
  () => props.modelValue,
  (val) => {
    data.query = val?.query ?? ''
    classes.value = JSON.parse(JSON.stringify(val?.classes || []))
    if (!classes.value.length) {
      classes.value = [{ id: 'a', name: '类别A', description: '' }]
    }
  },
  { immediate: true, deep: true }
)

function sync() {
  emit('update:modelValue', { ...props.modelValue, query: data.query, classes: [...classes.value] })
}

function emitUpdate() {
  sync()
}

function addRow() {
  const id = `c${classes.value.length + 1}`
  classes.value.push({ id, name: `类别${classes.value.length + 1}`, description: '' })
  sync()
}

function removeRow(idx) {
  classes.value.splice(idx, 1)
  sync()
}
</script>

<style scoped>
.wf-form__section {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
  margin: 12px 0 8px;
}
.wf-form__row {
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}
</style>
