<template>
  <div class="question-classifier-form">
    <div class="question-classifier-form__section">
      <div class="question-classifier-form__label">待分类文本</div>
      <TemplateField
        v-model="data.query"
        :variable-tree="variableTree"
        :rows="3"
        :class="{ 'question-classifier-form__field--error': errors.query }"
        @update:model-value="emitUpdate"
      />
    </div>

    <WfVariableTableSection
      title="分类列表"
      tooltip="分类 ID 与画布连线 Handle 联动"
      :columns="classColumns"
      :has-rows="rows.length > 0"
      empty-text="暂无分类，点击右上角 + 添加"
      add-title="添加分类"
      @add="addRow"
    >
      <div
        v-for="(row, idx) in rows"
        :key="row._id"
        class="question-classifier-form__block"
        :class="{ 'question-classifier-form__block--expanded': isExpanded(row._id) }"
      >
        <div
          class="wf-vt-section__row"
          :class="{ 'wf-vt-section__row--error': errors[`classes.${idx}.id`] }"
        >
          <el-input
            v-model="row.id"
            size="small"
            placeholder="ID"
            class="wf-vt-section__col wf-vt-section__col--name"
            @change="sync"
          />
          <el-input
            v-model="row.name"
            size="small"
            placeholder="类别名称"
            class="wf-vt-section__col wf-vt-section__col--flex"
            @change="sync"
          />
          <div class="wf-vt-section__col wf-vt-section__col--actions question-classifier-form__actions">
            <el-button link title="展开描述" @click.stop="toggleExpand(row._id)">
              <el-icon :size="14"><FullScreen /></el-icon>
            </el-button>
            <el-button link type="danger" title="删除" @click.stop="removeRow(idx)">
              <el-icon :size="14"><Minus /></el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="isExpanded(row._id)" class="question-classifier-form__expand">
          <div class="question-classifier-form__expand-label">描述</div>
          <el-input
            v-model="row.description"
            size="small"
            type="textarea"
            :rows="2"
            placeholder="分类说明，帮助模型区分"
            @change="sync"
          />
        </div>
      </div>
    </WfVariableTableSection>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { FullScreen, Minus } from '@element-plus/icons-vue'
import TemplateField from './TemplateField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import { useWfFormRows } from './shared/useWfFormRows'

defineOptions({ name: 'QuestionClassifierForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ query: '' })
const expandedIds = ref(new Set())
let classSeq = 0

const classColumns = [
  { key: 'id', label: 'ID', class: 'wf-vt-section__col--name' },
  { key: 'name', label: '名称', class: 'wf-vt-section__col--flex' }
]

watch(
  () => props.modelValue?.query,
  (val) => {
    data.query = val ?? ''
  },
  { immediate: true }
)

const { rows, sync, addRow, removeRow } = useWfFormRows({
  getSource: () => props.modelValue?.classes,
  toRow: (item, idx, prevRows, id) => ({
    id: item?.id || '',
    name: item?.name || '',
    description: item?.description || '',
    _id: prevRows[idx]?._id || id('cls')
  }),
  fromRows: (list) =>
    list.map(({ _id, ...rest }) => ({
      id: (rest.id || '').trim(),
      name: (rest.name || '').trim(),
      description: (rest.description || '').trim()
    })),
  emitModel: (classes) =>
    emit('update:modelValue', { ...props.modelValue, query: data.query, classes }),
  createRow: (id) => {
    classSeq += 1
    return { _id: id('cls'), id: `c${classSeq}`, name: `类别${classSeq}`, description: '' }
  },
  allowEmpty: true
})

function isExpanded(id) {
  return expandedIds.value.has(id)
}

function toggleExpand(id) {
  const next = new Set(expandedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expandedIds.value = next
}

function emitUpdate() {
  sync()
}
</script>

<style scoped lang="scss">
.question-classifier-form__section {
  margin-bottom: 16px;
}

.question-classifier-form__label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.question-classifier-form__field--error :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.question-classifier-form__block {
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }

  &--expanded {
    background: #f0f7ff;
  }
}

.question-classifier-form__actions {
  display: flex;
  width: 52px !important;
  gap: 2px;
  justify-content: flex-end;
}

.question-classifier-form__expand {
  padding: 0 10px 10px;
}

.question-classifier-form__expand-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
</style>
