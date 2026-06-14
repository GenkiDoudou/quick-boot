<template>
  <div class="variable-assign-form">
    <WfVariableTableSection
      title="变量赋值"
      tooltip="赋值内容可直接输入文本，或通过 (x) 按钮插入 {{变量}} 引用"
      :columns="columns"
      :has-rows="rows.length > 0"
      empty-text="暂无赋值项，点击右上角 + 添加"
      add-title="添加赋值"
      @add="addRow"
    >
      <div
        v-for="(row, idx) in rows"
        :key="row._id"
        class="wf-vt-section__row"
        :class="{ 'wf-vt-section__row--error': errors[`assignments.${idx}.target`] }"
      >
        <el-input
          v-model="row.target"
          size="small"
          placeholder="目标变量名"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="sync"
        />
        <ConditionValueField
          v-model="row.value"
          :variable-tree="variableTree"
          placeholder="输入或引用变量"
          class="wf-vt-section__col wf-vt-section__col--value"
          @update:model-value="sync"
        />
        <el-button
          link
          type="danger"
          class="wf-vt-section__col wf-vt-section__col--actions"
          title="删除"
          @click.stop="removeRow(idx)"
        >
          <el-icon :size="16"><Minus /></el-icon>
        </el-button>
      </div>
    </WfVariableTableSection>
  </div>
</template>

<script setup>
import { toRef } from 'vue'
import { Minus } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import { useWfFormRows } from './shared/useWfFormRows'

defineOptions({ name: 'VariableAssignForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const columns = [
  { key: 'name', label: '目标变量', class: 'wf-vt-section__col--name' },
  { key: 'value', label: '赋值内容', class: 'wf-vt-section__col--flex' }
]

const { rows, sync, addRow, removeRow } = useWfFormRows({
  getSource: () => props.modelValue?.assignments,
  toRow: (item, idx, prevRows, id) => ({
    target: item?.target || '',
    value: item?.value || '',
    _id: prevRows[idx]?._id || id('assign')
  }),
  fromRows: (list) =>
    list.map(({ _id, ...rest }) => ({
      target: (rest.target || '').trim(),
      value: rest.value || ''
    })),
  emitModel: (assignments) => emit('update:modelValue', { ...props.modelValue, assignments }),
  createRow: (id) => ({ _id: id('assign'), target: '', value: '' }),
  allowEmpty: true
})
</script>
