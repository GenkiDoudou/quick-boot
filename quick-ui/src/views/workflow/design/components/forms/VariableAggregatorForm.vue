<template>
  <div class="var-agg-form">
    <p class="var-agg-form__desc">
      将多路分支的输出变量按分组聚合为 Group1、Group2…；每组取<strong>第一个非空</strong>值，避免未执行分支导致下游报错。
    </p>

    <div class="var-agg-form__section-head">
      <span class="var-agg-form__section-title">聚合分组</span>
      <el-button link type="primary" title="添加分组" @click="addGroup">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <div class="var-agg-form__groups">
      <div
        v-for="(group, gIdx) in groups"
        :key="group._id"
        class="var-agg-form__group"
        :class="{
          'var-agg-form__group--dragging': dragGroupIdx === gIdx,
          'var-agg-form__group--drag-over': dragOverIdx === gIdx && dragGroupIdx !== gIdx
        }"
        draggable="true"
        @dragstart="onGroupDragStart(gIdx, $event)"
        @dragend="onGroupDragEnd"
        @dragover.prevent="dragOverIdx = gIdx"
        @dragleave="onGroupDragLeave(gIdx)"
        @drop.prevent="onGroupDrop(gIdx)"
      >
        <div class="var-agg-form__group-head">
          <el-icon class="var-agg-form__drag-handle" title="拖拽调整分组顺序"><Rank /></el-icon>
          <span class="var-agg-form__group-label">分组</span>
          <el-input
            v-model="group.name"
            size="small"
            class="var-agg-form__group-name"
            placeholder="Group1"
            @change="onGroupNameChange(gIdx)"
          />
          <el-tag size="small" type="info" class="var-agg-form__type-tag">
            {{ groupTypeLabel(group) }}
          </el-tag>
          <span class="var-agg-form__strategy">策略：第一个非空值</span>
          <el-button
            v-if="groups.length > 1"
            link
            type="danger"
            title="删除分组"
            @click.stop="removeGroup(gIdx)"
          >
            <el-icon><Minus /></el-icon>
          </el-button>
        </div>

        <p v-if="groupTypeMismatch(group)" class="var-agg-form__warn">
          组内变量类型须一致（当前以首个变量类型为准：{{ groupTypeLabel(group) }}）
        </p>

        <div class="var-agg-form__vars">
          <div
            v-for="(row, vIdx) in group.variables"
            :key="row._id"
            class="var-agg-form__var-row"
            :class="{
              'var-agg-form__var-row--dragging': dragVarKey === varDragKey(gIdx, vIdx),
              'var-agg-form__var-row--drag-over':
                dragVarOverKey === varDragKey(gIdx, vIdx) && dragVarKey !== varDragKey(gIdx, vIdx)
            }"
            draggable="true"
            @dragstart="onVarDragStart(gIdx, vIdx, $event)"
            @dragend="onVarDragEnd"
            @dragover.prevent="dragVarOverKey = varDragKey(gIdx, vIdx)"
            @dragleave="onVarDragLeave(gIdx, vIdx)"
            @drop.prevent="onVarDrop(gIdx, vIdx)"
          >
            <el-icon class="var-agg-form__var-drag" title="拖拽调整优先级"><Rank /></el-icon>
            <span class="var-agg-form__var-order">{{ vIdx + 1 }}</span>
            <ConditionValueField
              v-model="row.value"
              :variable-tree="variableTree"
              placeholder="输入或引用变量"
              class="var-agg-form__var-select"
              @update:model-value="sync"
            />
            <el-button
              link
              type="danger"
              title="删除"
              @click.stop="removeVariable(gIdx, vIdx)"
            >
              <el-icon :size="14"><Minus /></el-icon>
            </el-button>
          </div>
          <el-button link type="primary" size="small" @click="addVariable(gIdx)">
            <el-icon><Plus /></el-icon>
            添加变量
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Minus, Plus, Rank } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import {
  aggregatorTypeLabel,
  inferGroupType,
  normalizeAggregatorGroups,
  resolveAggregatorVariableType,
  serializeAggregatorGroups
} from '../../utils/variableAggregatorUtils'

defineOptions({ name: 'VariableAggregatorForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const groups = ref([])
const dragGroupIdx = ref(null)
const dragOverIdx = ref(null)
const dragVarKey = ref('')
const dragVarOverKey = ref('')
let rowSeq = 0
let syncing = false

function nextId(prefix = 'row') {
  return `${prefix}_${++rowSeq}`
}

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    groups.value = normalizeAggregatorGroups(val, nextId, groups.value)
  },
  { immediate: true, deep: true }
)

function sync() {
  syncing = true
  groups.value.forEach((group, idx) => {
    if (!(group.name || '').trim()) {
      group.name = `Group${idx + 1}`
    }
  })
  emit('update:modelValue', {
    ...props.modelValue,
    groups: serializeAggregatorGroups(groups.value)
  })
  queueMicrotask(() => {
    syncing = false
  })
}

function groupTypeLabel(group) {
  const vars = (group.variables || []).map((r) => r.value).filter(Boolean)
  return aggregatorTypeLabel(inferGroupType(vars, props.variableTree))
}

function groupTypeMismatch(group) {
  const base = inferGroupType(
    (group.variables || []).map((r) => r.value).filter(Boolean),
    props.variableTree
  )
  return (group.variables || []).some((row) => {
    const v = (row.value || '').trim()
    if (!v) return false
    const t = resolveAggregatorVariableType(v, props.variableTree)
    return String(t).toLowerCase() !== String(base).toLowerCase()
  })
}

function onGroupNameChange() {
  sync()
}

function createGroup(index) {
  return {
    _id: nextId('agg_group'),
    id: `group_${index + 1}_${Date.now()}`,
    name: `Group${index + 1}`,
    strategy: 'first_non_empty',
    variables: [{ _id: nextId('agg_var'), value: '' }]
  }
}

function addGroup() {
  groups.value.push(createGroup(groups.value.length))
  sync()
}

function removeGroup(gIdx) {
  groups.value.splice(gIdx, 1)
  if (!groups.value.length) {
    groups.value.push(createGroup(0))
  }
  sync()
}

function addVariable(gIdx) {
  groups.value[gIdx].variables.push({ _id: nextId('agg_var'), value: '' })
  sync()
}

function removeVariable(gIdx, vIdx) {
  const list = groups.value[gIdx].variables
  list.splice(vIdx, 1)
  if (!list.length) {
    list.push({ _id: nextId('agg_var'), value: '' })
  }
  sync()
}

function varDragKey(gIdx, vIdx) {
  return `${gIdx}_${vIdx}`
}

function onGroupDragStart(idx, event) {
  dragGroupIdx.value = idx
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(idx))
  }
}

function onGroupDragEnd() {
  dragGroupIdx.value = null
  dragOverIdx.value = null
}

function onGroupDragLeave(idx) {
  if (dragOverIdx.value === idx) dragOverIdx.value = null
}

function onGroupDrop(targetIdx) {
  const from = dragGroupIdx.value
  if (from == null || from === targetIdx) {
    onGroupDragEnd()
    return
  }
  const list = [...groups.value]
  const [moved] = list.splice(from, 1)
  list.splice(targetIdx, 0, moved)
  groups.value = list
  onGroupDragEnd()
  sync()
}

function onVarDragStart(gIdx, vIdx, event) {
  dragVarKey.value = varDragKey(gIdx, vIdx)
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', varDragKey(gIdx, vIdx))
  }
}

function onVarDragEnd() {
  dragVarKey.value = ''
  dragVarOverKey.value = ''
}

function onVarDragLeave(gIdx, vIdx) {
  if (dragVarOverKey.value === varDragKey(gIdx, vIdx)) {
    dragVarOverKey.value = ''
  }
}

function onVarDrop(targetGIdx, targetVIdx) {
  const fromKey = dragVarKey.value
  if (!fromKey) {
    onVarDragEnd()
    return
  }
  const [fromGIdx, fromVIdx] = fromKey.split('_').map(Number)
  if (fromGIdx === targetGIdx && fromVIdx === targetVIdx) {
    onVarDragEnd()
    return
  }
  const sourceList = groups.value[fromGIdx]?.variables
  const targetList = groups.value[targetGIdx]?.variables
  if (!sourceList || !targetList) {
    onVarDragEnd()
    return
  }
  const [moved] = sourceList.splice(fromVIdx, 1)
  if (!moved) {
    onVarDragEnd()
    return
  }
  let insertIdx = targetVIdx
  if (fromGIdx === targetGIdx && fromVIdx < targetVIdx) {
    insertIdx -= 1
  }
  targetList.splice(insertIdx, 0, moved)
  if (!sourceList.length) {
    sourceList.push({ _id: nextId('agg_var'), value: '' })
  }
  onVarDragEnd()
  sync()
}
</script>

<style scoped lang="scss">
.var-agg-form__desc {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.55;

  strong {
    color: #606266;
    font-weight: 600;
  }
}

.var-agg-form__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.var-agg-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.var-agg-form__groups {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.var-agg-form__group {
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

.var-agg-form__group-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  border-radius: 8px 8px 0 0;
}

.var-agg-form__drag-handle,
.var-agg-form__var-drag {
  cursor: grab;
  color: #909399;
  flex-shrink: 0;

  &:active {
    cursor: grabbing;
  }
}

.var-agg-form__group-label {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.var-agg-form__group-name {
  width: 108px;
  flex-shrink: 0;
}

.var-agg-form__type-tag {
  flex-shrink: 0;
}

.var-agg-form__strategy {
  margin-left: auto;
  font-size: 11px;
  color: #909399;
}

.var-agg-form__warn {
  margin: 0;
  padding: 6px 10px;
  font-size: 11px;
  color: #e6a23c;
  background: #fdf6ec;
  border-bottom: 1px solid #faecd8;
}

.var-agg-form__vars {
  padding: 8px 10px 10px;
}

.var-agg-form__var-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f2f5;

  &--dragging {
    opacity: 0.55;
  }

  &--drag-over {
    background: #f0f7ff;
    border-radius: 4px;
  }

  &:last-of-type {
    border-bottom: none;
  }
}

.var-agg-form__var-order {
  flex-shrink: 0;
  width: 18px;
  font-size: 11px;
  color: #909399;
  text-align: center;
}

.var-agg-form__var-select {
  flex: 1;
  min-width: 0;
}
</style>
