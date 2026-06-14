<template>
  <div class="if-else-form">
    <p class="if-else-form__desc">
      按优先级依次判断条件分支；命中则执行对应分支，均不满足时执行「否则」分支。
    </p>

    <div class="if-else-form__section-head">
      <span class="if-else-form__section-title">条件分支</span>
      <el-button link type="primary" title="添加条件分支" @click="addBranch">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <div class="if-else-form__branches">
      <div
        v-for="(branch, bIdx) in branches"
        :key="branch._id"
        class="if-else-form__branch"
        :class="{
          'if-else-form__branch--dragging': dragBranchIdx === bIdx,
          'if-else-form__branch--drag-over': dragOverIdx === bIdx && dragBranchIdx !== bIdx
        }"
        draggable="true"
        @dragstart="onBranchDragStart(bIdx, $event)"
        @dragend="onBranchDragEnd"
        @dragover.prevent="dragOverIdx = bIdx"
        @dragleave="onBranchDragLeave(bIdx)"
        @drop.prevent="onBranchDrop(bIdx)"
      >
        <div class="if-else-form__branch-head">
          <el-icon class="if-else-form__drag-handle" title="拖拽调整优先级"><Rank /></el-icon>
          <span class="if-else-form__priority">优先级 {{ bIdx + 1 }}</span>
          <span class="if-else-form__branch-type">{{ bIdx === 0 ? '如果' : '否则如果' }}</span>
          <div class="if-else-form__logic">
            <span class="if-else-form__logic-label">条件关系</span>
            <el-radio-group v-model="branch.logic" size="small" @change="sync">
              <el-radio-button value="AND">且</el-radio-button>
              <el-radio-button value="OR">或</el-radio-button>
            </el-radio-group>
          </div>
          <el-button
            v-if="branches.length > 1"
            link
            type="danger"
            title="删除此分支"
            @click.stop="removeBranch(bIdx)"
          >
            <el-icon><Minus /></el-icon>
          </el-button>
        </div>

        <div class="if-else-form__conditions">
          <div class="if-else-form__cond-thead">
            <span class="if-else-form__cond-col if-else-form__cond-col--value">对比前的值</span>
            <span class="if-else-form__cond-col if-else-form__cond-col--op">对比条件</span>
            <span class="if-else-form__cond-col if-else-form__cond-col--value">对比后的值</span>
            <span class="if-else-form__cond-col if-else-form__cond-col--actions" />
          </div>
          <div
            v-for="(row, cIdx) in branch.conditions"
            :key="row._id"
            class="if-else-form__cond-row-wrap"
          >
            <div class="if-else-form__cond-row">
              <ConditionValueField
                v-model="row.left"
                :variable-tree="variableTree"
                placeholder="输入或引用变量"
                class="if-else-form__cond-col if-else-form__cond-col--value"
                @update:model-value="onLeftChange(row)"
              />
              <el-select
                v-model="row.operator"
                size="small"
                class="if-else-form__cond-col if-else-form__cond-col--op"
                @change="sync"
              >
                <el-option
                  v-for="op in operatorsForRow(row)"
                  :key="op.value"
                  :label="op.label"
                  :value="op.value"
                />
              </el-select>
              <ConditionValueField
                v-if="needsRight(row.operator)"
                v-model="row.right"
                :variable-tree="variableTree"
                :placeholder="rightPlaceholder(row)"
                class="if-else-form__cond-col if-else-form__cond-col--value"
                @update:model-value="sync"
              />
              <span
                v-else
                class="if-else-form__cond-col if-else-form__cond-col--value if-else-form__cond-na"
              >
                —
              </span>
              <el-button
                link
                type="danger"
                class="if-else-form__cond-col if-else-form__cond-col--actions"
                title="删除条件"
                @click.stop="removeCondition(bIdx, cIdx)"
              >
                <el-icon :size="14"><Minus /></el-icon>
              </el-button>
            </div>
          </div>

          <el-button link type="primary" size="small" class="if-else-form__add-cond" @click="addCondition(bIdx)">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </div>
    </div>

    <div class="if-else-form__else">
      <div class="if-else-form__else-title">否则</div>
      <p class="if-else-form__else-desc">当以上条件分支均不满足时，执行此分支。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Minus, Plus, Rank } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import {
  createEmptyIfElseCondition,
  createIfElseBranch,
  normalizeIfElseBranches,
  serializeIfElseBranches
} from '../../utils/ifElseBranchUtils'
import {
  ensureIfElseOperator,
  getOperatorsForVariableType,
  ifElseOperatorNeedsRight,
  ifElseRightValuePlaceholder,
  resolveConditionOperandType
} from '../../utils/ifElseOperators'

defineOptions({ name: 'IfElseForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const branches = ref([])
const dragBranchIdx = ref(null)
const dragOverIdx = ref(null)
let rowSeq = 0
let syncing = false

function nextId(prefix = 'row') {
  return `${prefix}_${++rowSeq}`
}

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    branches.value = normalizeIfElseBranches(val, nextId, branches.value)
  },
  { immediate: true, deep: true }
)

function sync() {
  syncing = true
  branches.value.forEach((branch, idx) => {
    branch.name = idx === 0 ? '如果' : '否则如果'
  })
  const serialized = serializeIfElseBranches(branches.value)
  emit('update:modelValue', {
    ...props.modelValue,
    branches: serialized
  })
  queueMicrotask(() => {
    syncing = false
  })
}

function needsRight(op) {
  return ifElseOperatorNeedsRight(op)
}

function operatorsForRow(row) {
  const type = resolveConditionOperandType(row.left, props.variableTree)
  return getOperatorsForVariableType(type)
}

function rightPlaceholder(row) {
  const type = resolveConditionOperandType(row.left, props.variableTree)
  return ifElseRightValuePlaceholder(row.operator, type)
}

function onLeftChange(row) {
  const type = resolveConditionOperandType(row.left, props.variableTree)
  row.operator = ensureIfElseOperator(row.operator, type)
  sync()
}

function addBranch() {
  branches.value.push(createIfElseBranch(branches.value.length, nextId))
  sync()
}

function removeBranch(bIdx) {
  branches.value.splice(bIdx, 1)
  if (!branches.value.length) {
    branches.value.push(createIfElseBranch(0, nextId))
  }
  sync()
}

function addCondition(bIdx) {
  branches.value[bIdx].conditions.push(createEmptyIfElseCondition(nextId))
  sync()
}

function removeCondition(bIdx, cIdx) {
  const list = branches.value[bIdx].conditions
  list.splice(cIdx, 1)
  if (!list.length) {
    list.push(createEmptyIfElseCondition(nextId))
  }
  sync()
}

function onBranchDragStart(idx, event) {
  dragBranchIdx.value = idx
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(idx))
  }
}

function onBranchDragEnd() {
  dragBranchIdx.value = null
  dragOverIdx.value = null
}

function onBranchDragLeave(idx) {
  if (dragOverIdx.value === idx) {
    dragOverIdx.value = null
  }
}

function onBranchDrop(targetIdx) {
  const from = dragBranchIdx.value
  if (from == null || from === targetIdx) {
    onBranchDragEnd()
    return
  }
  const list = [...branches.value]
  const [moved] = list.splice(from, 1)
  list.splice(targetIdx, 0, moved)
  branches.value = list
  onBranchDragEnd()
  sync()
}
</script>

<style scoped lang="scss">
.if-else-form__desc {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.if-else-form__branches {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.if-else-form__branch {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  transition: border-color 0.15s, box-shadow 0.15s;

  &--dragging {
    opacity: 0.55;
  }

  &--drag-over {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
  }
}

.if-else-form__branch-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 10px 10px 6px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  border-radius: 8px 8px 0 0;
}

.if-else-form__drag-handle {
  cursor: grab;
  color: #909399;
  flex-shrink: 0;

  &:active {
    cursor: grabbing;
  }
}

.if-else-form__priority {
  font-size: 11px;
  font-weight: 600;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}

.if-else-form__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.if-else-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.if-else-form__branch-type {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.if-else-form__cond-row-wrap {
  padding: 4px 0;
  border-bottom: 1px solid #f0f2f5;

  &:last-of-type {
    border-bottom: none;
  }
}

.if-else-form__cond-thead {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 0 6px;
  font-size: 12px;
  color: #909399;
}

.if-else-form__cond-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
}

.if-else-form__cond-col {
  &--value {
    flex: 1;
    min-width: 0;
  }

  &--op {
    width: 112px;
    flex-shrink: 0;
  }

  &--actions {
    display: flex;
    justify-content: center;
    width: 28px;
    flex-shrink: 0;
  }
}

.if-else-form__cond-na {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  color: #c0c4cc;
  font-size: 13px;
}

.if-else-form__logic {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
}

.if-else-form__logic-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.if-else-form__branch-handle {
  padding: 4px 10px 0;
  font-size: 11px;
  color: #909399;

  code {
    font-size: 11px;
    color: #606266;
    background: #f5f7fa;
    padding: 1px 4px;
    border-radius: 3px;
  }
}

.if-else-form__conditions {
  padding: 8px 10px 10px;
}

.if-else-form__add-cond {
  margin-top: 4px;
}

.if-else-form__add-branch {
  width: 100%;
  margin-bottom: 12px;
}

.if-else-form__else {
  padding: 10px 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fdf6ec;
}

.if-else-form__else-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;

  code {
    font-size: 11px;
    color: #606266;
    background: #fff;
    padding: 1px 6px;
    border-radius: 3px;
    border: 1px solid #ebeef5;
  }
}

.if-else-form__else-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.45;
}
</style>
