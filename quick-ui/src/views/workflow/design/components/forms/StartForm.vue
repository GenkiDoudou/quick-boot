<template>
  <div class="start-form">
    <div class="start-form__header">
      <div class="start-form__title-wrap">
        <span class="start-form__title">输入</span>
        <el-tooltip content="定义工作流运行时可传入的变量；引用路径为 节点ID.变量名" placement="top">
          <el-icon class="start-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-button link class="start-form__add-btn" title="添加变量" @click.stop="addRow">
        <el-icon :size="16"><Plus /></el-icon>
      </el-button>
    </div>

    <div v-if="localInputs.length" class="start-form__table">
      <div class="start-form__thead">
        <span class="start-form__col start-form__col--name">变量名</span>
        <span class="start-form__col start-form__col--type">变量类型</span>
        <span class="start-form__col start-form__col--required">必填</span>
        <span class="start-form__col start-form__col--actions" />
      </div>

      <div
        v-for="(field, idx) in localInputs"
        :key="field._id"
        class="start-form__block"
        :class="{ 'start-form__block--expanded': isExpanded(field._id), 'start-form__block--error': errors[`inputs.${idx}.key`] }"
      >
        <div class="start-form__row">
          <div class="start-form__col start-form__col--name">
            <el-input
              v-model="field.key"
              size="small"
              placeholder="输入变量名"
              @change="() => onKeyChange(field, idx)"
            />
          </div>
          <div class="start-form__col start-form__col--type">
            <el-select
              v-model="field.fieldType"
              size="small"
              class="start-form__type-select"
              @change="(val) => onFieldTypeChange(field, val)"
            >
              <el-option
                v-for="item in START_FIELD_TYPES"
                :key="item.fieldType"
                :label="item.label"
                :value="item.fieldType"
              >
                <div class="start-form__type-option">
                  <span class="start-form__type-option-en">{{ item.label }}</span>
                  <span class="start-form__type-option-zh">{{ item.labelZh }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
          <div class="start-form__col start-form__col--required">
            <el-checkbox v-model="field.required" @change="sync" />
          </div>
          <div class="start-form__col start-form__col--actions">
            <el-button link class="start-form__action-btn" title="展开/收起" @click="toggleExpand(field._id)">
              <el-icon :size="14"><FullScreen /></el-icon>
            </el-button>
            <el-button link type="danger" class="start-form__action-btn" title="删除" @click="removeField(idx)">
              <el-icon :size="14"><Minus /></el-icon>
            </el-button>
          </div>
        </div>

        <div v-if="isExpanded(field._id)" class="start-form__expand">
          <div class="start-form__expand-item">
            <div class="start-form__expand-label">默认值</div>
            <el-input-number
              v-if="field.fieldType === 'integer'"
              v-model="field.defaultValue"
              size="small"
              :precision="0"
              :step="1"
              controls-position="right"
              class="start-form__expand-control"
              @change="sync"
            />
            <el-input-number
              v-else-if="field.fieldType === 'number'"
              v-model="field.defaultValue"
              size="small"
              controls-position="right"
              class="start-form__expand-control"
              @change="sync"
            />
            <el-switch
              v-else-if="field.fieldType === 'boolean'"
              v-model="field.defaultValue"
              @change="sync"
            />
            <el-date-picker
              v-else-if="field.fieldType === 'time'"
              v-model="field.defaultValue"
              type="datetime"
              size="small"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择默认时间"
              class="start-form__expand-control"
              @change="sync"
            />
            <el-input
              v-else-if="isJsonInputField(field)"
              v-model="field.defaultValue"
              type="textarea"
              :rows="2"
              size="small"
              :placeholder="field.fieldType === 'array' ? '默认 JSON 数组，如 []' : '默认 JSON 对象，如 {}'"
              class="start-form__expand-control"
              @change="sync"
            />
            <el-input
              v-else-if="isFileInputField(field)"
              disabled
              size="small"
              placeholder="文件类型暂不支持默认值"
              class="start-form__expand-control"
            />
            <el-input
              v-else
              v-model="field.defaultValue"
              size="small"
              placeholder="参数默认值，在没有传入该参数时，将使用默认值"
              class="start-form__expand-control"
              @change="sync"
            />
          </div>
          <div class="start-form__expand-item">
            <div class="start-form__expand-label">描述</div>
            <el-input
              v-model="field.description"
              type="textarea"
              :rows="2"
              size="small"
              placeholder="帮助大模型准确了解参数的作用"
              class="start-form__expand-control"
              @change="sync"
            />
          </div>
        </div>
      </div>
    </div>

    <div v-else class="start-form__empty">暂无输入变量，点击右上角 + 添加</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { FullScreen, InfoFilled, Minus, Plus } from '@element-plus/icons-vue'
import {
  START_FIELD_TYPES,
  START_FIELD_TYPE_MAP,
  createDefaultInputField,
  inferFieldTypeFromLegacy,
  isFileInputField,
  isJsonInputField,
  migrateFieldType
} from './startFieldTypes'

defineOptions({ name: 'StartForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  nodeId: { type: String, default: '' },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const localInputs = ref([])
const expandedIds = ref(new Set())
let rowSeq = 0

watch(
  () => props.modelValue?.inputs,
  (val) => {
    localInputs.value = normalizeInputs(val)
  },
  { immediate: true, deep: true }
)

/**
 * 补全历史字段的 fieldType、description 等 UI 属性。
 * @param {Array|undefined} inputs
 * @returns {Array}
 */
function normalizeInputs(inputs) {
  return JSON.parse(JSON.stringify(inputs || [])).map((field) => {
    const fieldType = inferFieldTypeFromLegacy(field)
    const meta = START_FIELD_TYPE_MAP[fieldType]
    return {
      ...createDefaultInputField(fieldType),
      ...field,
      fieldType,
      type: meta?.backendType || field.type || 'string',
      description: field.description ?? '',
      _id: field._id || `row_${++rowSeq}`
    }
  })
}

function onFieldTypeChange(field, fieldType) {
  const ft = migrateFieldType(fieldType)
  const meta = START_FIELD_TYPE_MAP[ft]
  if (!meta) return
  field.fieldType = ft
  field.type = meta.backendType
  const defaults = createDefaultInputField(ft)
  field.defaultValue = defaults.defaultValue
  sync()
}

function isExpanded(id) {
  return expandedIds.value.has(id)
}

function toggleExpand(id) {
  const next = new Set(expandedIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedIds.value = next
}

function onKeyChange(field, idx) {
  const key = field.key?.trim()
  if (!key) {
    sync()
    return
  }
  if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(key)) {
    ElMessage.warning('变量名仅支持字母、数字、下划线，且不能包含 . 或节点 ID')
    field.key = ''
    sync()
    return
  }
  if (key.includes('.')) {
    ElMessage.warning('变量名不要带点号；引用路径会自动生成为 节点ID.变量名')
    field.key = ''
    sync()
    return
  }
  const duplicate = localInputs.value.some((f, i) => i !== idx && f.key === key)
  if (duplicate) {
    ElMessage.warning('变量名已存在')
    field.key = ''
    sync()
    return
  }
  if (!field.label?.trim()) {
    field.label = key
  }
  field.key = key
  sync()
}

function sync() {
  const inputs = localInputs.value.map(({ _id, ...rest }) => ({
    ...rest,
    key: rest.key?.trim() || '',
    label: rest.label?.trim() || rest.key?.trim() || '',
    description: rest.description?.trim() || ''
  }))
  emit('update:modelValue', { ...props.modelValue, inputs })
}

function addRow() {
  const row = { ...createDefaultInputField('string'), _id: `row_${++rowSeq}` }
  localInputs.value.push(row)
  expandedIds.value = new Set([...expandedIds.value, row._id])
  sync()
}

function removeField(idx) {
  const id = localInputs.value[idx]?._id
  localInputs.value.splice(idx, 1)
  if (id) {
    const next = new Set(expandedIds.value)
    next.delete(id)
    expandedIds.value = next
  }
  sync()
}
</script>

<style scoped lang="scss">
.start-form {
  padding: 0 2px;
}

.start-form__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.start-form__title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.start-form__title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.start-form__info {
  font-size: 14px;
  color: #c0c4cc;
  cursor: help;
}

.start-form__add-btn {
  color: #606266;
  padding: 4px;

  &:hover {
    color: #409eff;
  }
}

.start-form__table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.start-form__thead {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.start-form__row {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  gap: 0;
}

.start-form__col {
  flex-shrink: 0;

  &--name {
    flex: 1;
    min-width: 0;
    padding-right: 8px;
  }

  &--type {
    width: 118px;
    padding-right: 8px;
  }

  &--required {
    width: 44px;
    display: flex;
    justify-content: center;
  }

  &--actions {
    width: 56px;
    display: flex;
    justify-content: flex-end;
    gap: 2px;
  }
}

.start-form__type-select {
  width: 100%;
}

.start-form__type-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 12px;
}

.start-form__type-option-en {
  font-size: 13px;
  color: #303133;
}

.start-form__type-option-zh {
  font-size: 12px;
  color: #909399;
}

.start-form__block {
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }

  &--expanded {
    background: #f0f7ff;
  }

  &--error .start-form__row :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #f56c6c inset;
  }
}

.start-form__action-btn {
  padding: 4px;
  min-height: auto;
}

.start-form__expand {
  padding: 0 10px 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.start-form__expand-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.start-form__expand-label {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.start-form__expand-control {
  width: 100%;
}

.start-form__empty {
  padding: 24px 12px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
}
</style>
